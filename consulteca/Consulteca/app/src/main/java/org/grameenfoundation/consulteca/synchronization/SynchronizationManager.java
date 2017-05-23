package org.grameenfoundation.consulteca.synchronization;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import com.google.gson.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.grameenfoundation.consulteca.ApplicationRegistry;
import org.grameenfoundation.consulteca.R;
import org.grameenfoundation.consulteca.location.GpsManager;
import org.grameenfoundation.consulteca.model.Farmer;
import org.grameenfoundation.consulteca.model.SearchLog;
import org.grameenfoundation.consulteca.model.SearchMenu;
import org.grameenfoundation.consulteca.model.SearchMenuItem;
import org.grameenfoundation.consulteca.services.MenuItemService;
import org.grameenfoundation.consulteca.settings.SettingsConstants;
import org.grameenfoundation.consulteca.settings.SettingsManager;
import org.grameenfoundation.consulteca.storage.DatabaseHelperConstants;
import org.grameenfoundation.consulteca.utils.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * A Facade that handles synchronization of search menus and menu items.
 * It abstracts the underlying synchronization protocol from the callers
 * and provides methods to initiate the synchronization.
 */
public class SynchronizationManager {
    private final static String XML_NAME_SPACE = "http://schemas.applab.org/2010/07/search";
    private final static String REQUEST_ELEMENT_NAME = "GetKeywordsRequest";
    private final static String VERSION_ELEMENT_NAME = "localKeywordsVersion";
    private final static String IMAGES_VERSION_ELEMENT_NAME = "localImagesVersion";
    private final static String CURRENT_MENU_IDS = "menuIds";
    private final static String DEFAULT_KEYWORDS_VERSION = "2010-04-04 00:00:00";
    private final static String DEFAULT_IMAGES_VERSION = "2010-04-04 00:00:00";
    private final static String DEFAULT_FARMERS_VERSION = "2014-11-03 00:00:00";
    private MenuItemService menuItemService = new MenuItemService();

    private boolean synchronizing = false;
    private static final SynchronizationManager INSTANCE = new SynchronizationManager();
    private Context applicationContext;
    private Map<String, SynchronizationListener> synchronizationListenerList =
            new HashMap<String, SynchronizationListener>();

    private static final int DEFAULT_NETWORK_TIMEOUT = 3 * 60 * 1000;

    private SynchronizationManager() {
        applicationContext = ApplicationRegistry.getApplicationContext();

    }

    public static SynchronizationManager getInstance() {
        return INSTANCE;
    }

    /**
     * called to initialize the synchronization manager.
     */
    public synchronized void initialize() {
    }

    /**
     * called to start the synchronization process in a new
     * thread only if it's not running. The synchronization manager gives feedback through the
     * synchronization listener events.
     * <p/>
     * This method is non-blocking and therefore returns immediately.
     *
     * @see #registerListener(SynchronizationListener)
     */
    public synchronized void start() {
        if (this.isSynchronizing())
            return;

        /*
        starts a new thread to begin the synchronization. The synchronization manager
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    notifySynchronizationListeners("synchronizationStart");
                    synchronizing = true;

                    int maxSynchronizationSteps = 5;
                    notifySynchronizationListeners("synchronizationUpdate", 1, maxSynchronizationSteps,
                            ApplicationRegistry.getApplicationContext().
                                    getResources().getString(R.string.country_code_download_msg), false);
                    downloadCountryCode();

                    notifySynchronizationListeners("synchronizationUpdate", 2, maxSynchronizationSteps,
                            ApplicationRegistry.getApplicationContext().
                                    getResources().getString(R.string.upload_search_logs_download_msg), false);
                    uploadBulkSearchLogs();

                    notifySynchronizationListeners("synchronizationUpdate", 3, maxSynchronizationSteps,
                            ApplicationRegistry.getApplicationContext().
                                    getResources().getString(R.string.keyword_download_msg), false);
                    downloadSearchMenus();

                    notifySynchronizationListeners("synchronizationUpdate", 4, maxSynchronizationSteps,
                            ApplicationRegistry.getApplicationContext().
                                    getResources().getString(R.string.farmer_download_msg), false);
                    downloadFarmers();

                    notifySynchronizationListeners("synchronizationUpdate", maxSynchronizationSteps,
                            maxSynchronizationSteps,
                            ApplicationRegistry.getApplicationContext().
                                    getResources().getString(R.string.synchronization_complete_msg), true);

                    notifySynchronizationListeners("synchronizationComplete");
                } catch (Exception e) {
                    Log.e(SynchronizationManager.class.getName(), "IOException", e);
                    notifySynchronizationListeners("onSynchronizationError",
                            new Throwable(applicationContext.getString(R.string.error_connecting_to_server)));
                } finally {
                    synchronizing = false;
                }
            }
        }).start();
    }

    /**
     * uploads multiple search logs to the server in one request.
     */
    protected void uploadBulkSearchLogs() throws Exception {

        List<SearchLog> searchLogs = menuItemService.getAllSearchLogs();
        if(searchLogs.size() == 0){
            return;
        }
        GpsManager.getInstance().update();
        for(SearchLog log : searchLogs) {
            log.setSubmissionLocation(GpsManager.getInstance().getLocationAsString());
        }

        SearchLogRequest request = new SearchLogRequest();
        request.setRequest(SettingsConstants.REQUEST_UPLOAD_SEARCHLOGS);
        request.setImei(DeviceMetadata.getDeviceImei(ApplicationRegistry.getApplicationContext()));
        request.setSearchLogs(searchLogs);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
        String jsonRequest = gson.toJson(request);

        String url = SettingsManager.getInstance().getValue(SettingsConstants.KEY_SERVER);
        url = url.substring(0, url.lastIndexOf("/") + 1) + SettingsConstants.REQUEST_SUBMIT_SEARCHLOGS_PAGE;
        int networkTimeout = 10 * 60 * 1000;

        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair(SettingsConstants.REQUEST_METHODNAME, SettingsConstants.REQUEST_UPLOAD_SEARCHLOGS));
        params.add(new BasicNameValuePair(SettingsConstants.REQUEST_DATA, jsonRequest));

        InputStream inputStream = HttpHelpers.postFormRequestAndGetStream(url, new UrlEncodedFormEntity(params),
                networkTimeout);
        StringBuilder stringBuilder = HttpHelpers.getUncompressedResponseString(new BufferedReader(
                new InputStreamReader(inputStream)));
        try {
            String responseJson = stringBuilder.toString();
            SearchLogResponse response = new Gson().fromJson(responseJson, SearchLogResponse.class);
            if (response != null && response.getResultCode().equals("0")) {
                for (SearchLog searchLog : searchLogs) {
                    menuItemService.deleteSearchLog(searchLog);
                    notifySynchronizationListeners("synchronizationUpdate",
                            ApplicationRegistry.getApplicationContext().
                                    getResources().getString(R.string.uploading_search_logs), true);
                }
            }
            else {
                notifySynchronizationListeners("onSynchronizationError",
                        new Throwable(applicationContext.getString(R.string.error_uploading_searchlogs),
                                new Exception(responseJson)));
            }
        }catch (Exception ex){
            Log.e(SynchronizationManager.class.getName(), "Error uploading search logs", ex);
            notifySynchronizationListeners("onSynchronizationError",
                    new Throwable(applicationContext.getString(R.string.error_uploading_searchlogs)));

        }
    }

    /**
     * uploads the given search log to the server.
     *
     * @param searchLog
     */
    private void uploadSearchLog(SearchLog searchLog) throws Exception {
        StringBuilder requestParameters = new StringBuilder();
        requestParameters.append("?submissionTime=" +
                URLEncoder.encode(DatabaseHelperConstants.DEFAULT_DATE_FORMAT.format(searchLog.getDateCreated()),
                        "UTF-8"));

        requestParameters.append("&intervieweeId=" +
                URLEncoder.encode(searchLog.getClientId() == null ? "" : searchLog.getClientId(), "UTF-8"));

        requestParameters.append("&keyword=" + URLEncoder.encode(searchLog.getMenuItemId(), "UTF-8"));
        requestParameters.append("&location=" + URLEncoder.encode(searchLog.getGpsLocation(), "UTF-8"));

        if (!searchLog.isTestLog()) {
            requestParameters.append("&log=true");
        }

        String url = SettingsManager.getInstance().getValue(SettingsConstants.KEY_SERVER) +
                ApplicationRegistry.getApplicationContext().getString(R.string.search_log_url_path);

        String result = HttpHelpers.fetchContent(url + requestParameters.toString());

        //delete the log record.
        if (result != null) {
            menuItemService.deleteSearchLog(searchLog);
        }
    }

    protected void downloadSearchMenus() throws IOException {
        try {
            String url = SettingsManager.getInstance().getValue(SettingsConstants.KEY_SERVER);

            String keywordVersion =
                    SettingsManager.getInstance().getValue(SettingsConstants.KEY_KEYWORDS_VERSION,
                            DEFAULT_KEYWORDS_VERSION);
            String imagesVersion =
                    SettingsManager.getInstance().getValue(SettingsConstants.KEY_IMAGES_VERSION,
                            DEFAULT_IMAGES_VERSION);

            KeywordsRequestWrapper request = new KeywordsRequestWrapper();
            request.setRequest(SettingsConstants.REQUEST_DOWNLOAD_KEYWORDS);
            request.setImei(DeviceMetadata.getDeviceImei(ApplicationRegistry.getApplicationContext()));
            request.setKeywordsVersion(keywordVersion);
            request.setImagesLastUpdatedDate(imagesVersion);

            List<SearchMenu> menus = menuItemService.getAllSearchMenus();
            ArrayList<String> menuArr = new ArrayList();
            for(int i=0; i<menus.size();i++){
                menuArr.add(menus.get(i).getId());
            }
            request.setMenuIds(menuArr);

            Gson gson = new Gson();
            String jsonRequest = gson.toJson(request);

            int networkTimeout = 10 * 60 * 1000;
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair(SettingsConstants.REQUEST_METHODNAME,
                    SettingsConstants.REQUEST_DOWNLOAD_KEYWORDS));
            params.add(new BasicNameValuePair(SettingsConstants.REQUEST_DATA, jsonRequest));

            InputStream inputStream = HttpHelpers.postFormRequestAndGetStream(url, new UrlEncodedFormEntity(params),
                    networkTimeout);

            String searchCacheFile = ApplicationRegistry.getApplicationContext().getCacheDir() + "/keywords.cache";
            File cacheFile = new File(searchCacheFile);
            if (cacheFile.exists()) {
                boolean deleted = cacheFile.delete();
                if (deleted) {
                    Log.i(SynchronizationManager.class.getName(), "Cache File Deleted.");
                }
            }

            boolean downloadComplete = writeStreamToTempFile(inputStream, searchCacheFile,
                    ApplicationRegistry.getApplicationContext().getResources().getString(R.string.keyword_download_msg));
            FileInputStream fileInputStream = new FileInputStream(cacheFile);
            try {
                if (downloadComplete && fileInputStream != null) {
                    processKeywords(fileInputStream);
                }
            } finally {
                fileInputStream.close();
                inputStream.close();
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception ex) {
            Log.e(SynchronizationManager.class.getName(), "Error downloading keywords", ex);
            notifySynchronizationListeners("onSynchronizationError",
                    new Throwable(applicationContext.getString(R.string.error_downloading_keywords)));
        }
    }

    /**
     * Downloads farmer details for caching on local device
     * @throws IOException
     */
    protected void downloadFarmers() throws IOException {
        try {
            String url = SettingsManager.getInstance().getValue(SettingsConstants.KEY_SERVER);

            String farmersVersion =
                    SettingsManager.getInstance().getValue(SettingsConstants.KEY_FARMERS_VERSION,
                            DEFAULT_FARMERS_VERSION);

            FarmersRequestWrapper request = new FarmersRequestWrapper();
            request.setRequest(SettingsConstants.REQUEST_UPLOAD_SEARCHLOGS);
            request.setImei(DeviceMetadata.getDeviceImei(ApplicationRegistry.getApplicationContext()));
            request.setFarmersVersion(farmersVersion);

            Gson gson = new Gson();
            String jsonRequest = gson.toJson(request);

            int networkTimeout = 10 * 60 * 1000;
            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
            params.add(new BasicNameValuePair(SettingsConstants.REQUEST_METHODNAME,
                    SettingsConstants.REQUEST_DOWNLOAD_FARMERS));
            params.add(new BasicNameValuePair(SettingsConstants.REQUEST_DATA, jsonRequest));
            InputStream inputStream = HttpHelpers.postJsonRequestAndGetStream(url, networkTimeout, params);

            String searchFarmerCacheFile = ApplicationRegistry.getApplicationContext().getCacheDir() + "/farmers.cache";
            File cacheFile = new File(searchFarmerCacheFile);
            if (cacheFile.exists()) {
                boolean deleted = cacheFile.delete();
                if (deleted) {
                    Log.i(SynchronizationManager.class.getName(), "Farmers Cache File Deleted.");
                }
            }

            boolean downloadComplete = writeStreamToTempFile(inputStream, searchFarmerCacheFile,
                    ApplicationRegistry.getApplicationContext().getResources().getString(R.string.farmer_download_msg));
            FileInputStream fileInputStream = new FileInputStream(cacheFile);
            try {
                if (downloadComplete && fileInputStream != null) {
                    processFarmers(fileInputStream);
                }
            } finally {
                fileInputStream.close();
                inputStream.close();
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception ex) {
            Log.e(SynchronizationManager.class.getName(), "Error downloading farmers", ex);
            notifySynchronizationListeners("onSynchronizationError",
                    new Throwable(applicationContext.getString(R.string.error_downloading_farmers)));
        }
    }

    public Boolean writeStreamToTempFile(InputStream inputStream, String filePath, String message) throws IOException {
        File tempFile = new File(filePath);
        FileOutputStream stream = new FileOutputStream(tempFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        int read = 0;
        byte[] bytes = new byte[2048];
        while ((read = inputStream.read(bytes)) != -1) {
            stream.write(bytes, 0, read);
            notifySynchronizationListeners("synchronizationUpdate", message, true);
        }

        stream.flush();
        stream.close();
        reader.close();

        return true;
    }

    private void processKeywords(InputStream inputStream) throws IOException, ParseException {
        final List<SearchMenu> searchMenus = new ArrayList<SearchMenu>();
        List<SearchMenu> oldSearchMenus = menuItemService.getAllSearchMenus();
        final List<SearchMenuItem> searchMenuItems = new ArrayList<SearchMenuItem>();
        final List<SearchMenuItem> deletedSearchMenuItems = new ArrayList<SearchMenuItem>();
        final List<String> imageIdz = new ArrayList<String>();
        final List<String> deleteImageIz = new ArrayList<String>();
        final String[] keywordVersion = new String[1];
        final String[] imagesVersion = new String[1];
        final int[] keywordCount = new int[1];

        try {
            new JSONParser().parse(new InputStreamReader(inputStream), new JsonSimpleBaseParser() {
                private Object keywordObject = null;
                private String keywordType = "";
                private int keywordCounter = 0;

                @Override
                public boolean primitive(Object value) throws ParseException {
                    if (null != key && value != null) {
                        if (key.equals("resultCode")) {
                            if(!value.toString().equals("0")){
                                return false;//request wasn't successfull
                            }
                        } else if (key.equals("resultMessage")) {
                            Log.i(SynchronizationManager.class.getName(), value.toString());
                        } else if (key.equals("total")) {
                            keywordCount[0] = Integer.parseInt(value.toString());
                            notifySynchronizationListeners("synchronizationUpdate", keywordCounter++, keywordCount[0],
                                    ApplicationRegistry.getApplicationContext().
                                            getResources().getString(R.string.processing_keywords_msg), true);
                        } else if (key.equals("version")) {
                            keywordVersion[0] = value.toString();
                            imagesVersion[0] = value.toString();
                        }  else {
                            if (keywordObject instanceof SearchMenu) {
                                populateSearchMenu((SearchMenu) keywordObject, key, value.toString());
                            } else if (keywordObject instanceof SearchMenuItem) {
                                populateSearchMenuItem((SearchMenuItem) keywordObject, key, value.toString());
                            } else if ("id".equalsIgnoreCase(key) && keywordObject instanceof String
                                    && keywordType.equalsIgnoreCase("images")) {
                                keywordObject = value;
                            } else if ("id".equalsIgnoreCase(key) && keywordObject instanceof String
                                    && keywordType.equalsIgnoreCase("deletedImages")) {
                                keywordObject = value;
                            }
                            else{
                                Log.i(SynchronizationManager.class.getName(), "no implementation to process " + key);
                            }
                        }
                    }

                    key = null;
                    return true;
                }

                @Override
                public boolean startArray() throws ParseException {
                    keywordType = key;
                    return true;
                }

                @Override
                public boolean startObject() throws ParseException {
                    if ("menus".equalsIgnoreCase(keywordType)) {
                        keywordObject = new SearchMenu();
                    } else if ("menuItems".equalsIgnoreCase(keywordType)
                            || "deletedMenuItems".equalsIgnoreCase(keywordType)) {
                        keywordObject = new SearchMenuItem();
                    } else if ("images".equalsIgnoreCase(keywordType)) {
                        keywordObject = new String();
                    } else if ("deletedImages".equalsIgnoreCase(keywordType)) {
                        keywordObject = new String();
                    }

                    return true;
                }

                @Override
                public boolean endObject() throws ParseException {
                    if (keywordObject != null) {
                        if (keywordObject instanceof SearchMenu) {
                            searchMenus.add((SearchMenu) keywordObject);

                            menuItemService.save((SearchMenu) keywordObject);
                        } else if (keywordObject instanceof SearchMenuItem &&
                                keywordType.equalsIgnoreCase("menuItems")) {
                            //searchMenuItems.add((SearchMenuItem) keywordObject);
                            menuItemService.save((SearchMenuItem) keywordObject);

                            notifySynchronizationListeners("synchronizationUpdate", keywordCounter++, keywordCount[0],
                                    ApplicationRegistry.getApplicationContext().
                                            getResources().getString(R.string.processing_keywords_msg), true);

                        } else if (keywordObject instanceof SearchMenuItem &&
                                keywordType.equalsIgnoreCase("deletedMenuItems")) {
                            //deletedSearchMenuItems.add((SearchMenuItem) keywordObject);
                            notifySynchronizationListeners("synchronizationUpdate", 1, 1,
                                    ApplicationRegistry.getApplicationContext().
                                            getResources().getString(R.string.removing_keywords_msg), true);

                            menuItemService.deleteSearchMenuItems((SearchMenuItem) keywordObject);
                        } else if (keywordObject instanceof String &&
                                keywordType.equalsIgnoreCase("images")) {
                            imageIdz.add((String) keywordObject);
                        } else if (keywordObject instanceof String &&
                                keywordType.equalsIgnoreCase("deletedImages")) {
                            deleteImageIz.add((String) keywordObject);
                        }
                    }

                    keywordObject = null;
                    return true;
                }
            });

            deleteOldMenus(oldSearchMenus, searchMenus);
            SettingsManager.getInstance().setValue(SettingsConstants.KEY_KEYWORDS_VERSION, keywordVersion[0]);

            downloadImages(imageIdz, imagesVersion[0]);
            deleteUnusedImages(deleteImageIz);
        } catch (ParseException ex) {
            Log.e(SynchronizationManager.class.getName(), "Parsing Error", ex);
            notifySynchronizationListeners("onSynchronizationError",
                    new Throwable(applicationContext.getString(R.string.error_processing_keywords)));
        } catch (IOException ex) {
            Log.e(SynchronizationManager.class.getName(), "IOException Error", ex);
            notifySynchronizationListeners("onSynchronizationError",
                    new Throwable(applicationContext.getString(R.string.error_connecting_to_server)));
        } catch (Exception ex) {
            Log.e(SynchronizationManager.class.getName(), "Exception", ex);
        }
    }

    private void deleteUnusedImages(List<String> deleteImageIz) {
        if (deleteImageIz != null) {
            for (String imageId : deleteImageIz) {
                if (imageId != null && imageId.trim().length() > 0) {
                    File file = new File(ImageUtils.IMAGE_ROOT, imageId + ".jpg");
                    ImageUtils.deleteFile(file);
                }
            }
        }
    }

    private void downloadImages(List<String> imageIds, String imagesVersion) throws IOException, ParseException {
        if (imageIds != null) {
            int count = imageIds.size(), counter = 0;
            boolean complete = true;

            if (ImageUtils.storageReady() && ImageUtils.createRootFolder()) {
                for (final String imageId : imageIds) {
                    notifySynchronizationListeners("synchronizationUpdate", counter++, count,
                            ApplicationRegistry.getApplicationContext().
                                    getResources().getString(R.string.downloading_images_msg), true);

                    if (imageId != null || imageId.trim().length() > 0) {
                        // Only download image if image does not already exist!
                        if (!ImageUtils.imageExists(imageId.toLowerCase(), false)) {
                            Log.d("Image Download", "Getting " + imageId);

                            String url = SettingsManager.getInstance().getValue(SettingsConstants.KEY_SERVER);

                            ImagesRequestWrapper request = new ImagesRequestWrapper();
                            request.setRequest(SettingsConstants.REQUEST_DOWNLOAD_IMAGES);
                            request.setImei(DeviceMetadata.getDeviceImei(ApplicationRegistry.getApplicationContext()));
                            List<String> ids = new ArrayList<String>();
                            ids.add(imageId.split("-")[1]);
                            request.setImageIds(ids);

                            Gson gson = new Gson();
                            String jsonRequest = gson.toJson(request);

                            int networkTimeout = 10 * 60 * 1000;
                            List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                            params.add(new BasicNameValuePair(SettingsConstants.REQUEST_METHODNAME,
                                    SettingsConstants.REQUEST_DOWNLOAD_IMAGES));
                            params.add(new BasicNameValuePair(SettingsConstants.REQUEST_DATA, jsonRequest));
                            InputStream inputStream = HttpHelpers.postJsonRequestAndGetStream(url, networkTimeout, params);

                            String jsonResponse = new java.util.Scanner(inputStream).useDelimiter("\\A").next();
                            ImagesResponseWrapper res = gson.fromJson(jsonResponse, ImagesResponseWrapper.class);
                            FileOutputStream out = null;
                            try {
                                if (res != null && res.getResultCode().equals("0")) {
                                    for (ImageData image : res.getImageResults()) {
                                        byte[] arr = Base64.decode((image.getImageData()), Base64.DEFAULT);
                                        Bitmap png = BitmapFactory.decodeByteArray(arr, 0, arr.length);
                                        out = new FileOutputStream(new File(ImageUtils.IMAGE_ROOT, imageId + ".jpg"));
                                        png.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                    }
                                }
                            } catch (Exception ex) {
                                complete = false;
                                Log.e(SynchronizationManager.class.getName(), "Image parsing Error", ex);
                                notifySynchronizationListeners("onSynchronizationError",
                                        new Throwable(applicationContext.getString(R.string.error_downloading_images)));
                            } finally {
                                out.close();
                                inputStream.close();
                            }
                        }
                    }
                }
                if(complete){
                    SettingsManager.getInstance().setValue(SettingsConstants.KEY_IMAGES_VERSION, imagesVersion);
                }
            }
        }
    }

    private void processImages(InputStream inputStream, final String imageId) throws IOException, ParseException {
        final int[] imagesCount = new int[1];

        try {
            new JSONParser().parse(new InputStreamReader(inputStream), new JsonSimpleBaseParser() {
                private Object imageObject = null;
                private String imageType = "";
                private int imageCounter = 0;

                @Override
                public boolean primitive(Object value) throws ParseException, IOException {
                    if (null != key && value != null) {
                        if (key.equals("resultCode")) {
                            if(!value.toString().equals("0")){
                                return false;//request wasn't successfull
                            }
                        } else if (key.equals("resultMessage")) {
                            Log.i(SynchronizationManager.class.getName(), value.toString());
                        } else if (key.equals("total")) {
                            imagesCount[0] = Integer.parseInt(value.toString());
                            notifySynchronizationListeners("synchronizationUpdate", imageCounter++, imagesCount[0],
                                    ApplicationRegistry.getApplicationContext().
                                            getResources().getString(R.string.processing_images_msg), true);
                        } else {
                            if (imageObject instanceof ImageData) {
                                populateImageData((ImageData) imageObject, key, value.toString());
                            } else if ("imageId".equalsIgnoreCase(key) && imageObject instanceof String
                                    && imageType.equalsIgnoreCase("imageResults")) {
                                imageObject = value;
                            }  else if ("imageData".equalsIgnoreCase(key) && imageObject instanceof String
                                    && imageType.equalsIgnoreCase("imageResults")) {
                                imageObject = value;
                            }
                            else{
                                Log.i(SynchronizationManager.class.getName(), "no implementation to process " + key);
                            }
                        }
                    }

                    key = null;
                    return true;
                }

                @Override
                public boolean startArray() throws ParseException {
                    imageType = key;
                    return true;
                }

                @Override
                public boolean startObject() throws ParseException {
                    if ("imageResults".equalsIgnoreCase(imageType)) {
                        imageObject = new ImageData();
                    }

                    return true;
                }

                @Override
                public boolean endObject() throws ParseException, IOException {
                    if (imageObject != null) {
                        if (imageObject instanceof ImageData &&
                                imageType.equalsIgnoreCase("imageResults")) {

                            try {
                                //saveImage((ImageData) imageObject, imageId);
                                //ImageUtils.writeFile(imageId + ".jpg", new ByteArrayInputStream(Base64.decode(((ImageData) imageObject).getImageData())));

                                notifySynchronizationListeners("synchronizationUpdate", imageCounter++, imagesCount[0],
                                        ApplicationRegistry.getApplicationContext().
                                                getResources().getString(R.string.processing_images_msg), true);

                            }catch (Exception ex){
                                new Throwable(applicationContext.getString(R.string.error_connecting_to_server), ex);
                            }
                        }
                    }

                    imageObject = null;
                    return true;
                }
            });
        }
        catch (ParseException ex) {
            Log.e(SynchronizationManager.class.getName(), "Parsing Error", ex);
            notifySynchronizationListeners("onSynchronizationError",
                    new Throwable(applicationContext.getString(R.string.error_downloading_images)));
        } catch (IOException ex) {
            Log.e(SynchronizationManager.class.getName(), "IOException Error", ex);
            notifySynchronizationListeners("onSynchronizationError",
                    new Throwable(applicationContext.getString(R.string.error_connecting_to_server)));
        } catch (Exception ex) {
            Log.e(SynchronizationManager.class.getName(), "Exception", ex);
        }
    }

    private void populateImageData(ImageData imageData, String property, String value) {
        if ("imageId".equalsIgnoreCase(property)) {
            imageData.setImageId(value);
        } else if ("imageData".equalsIgnoreCase(property)) {
            imageData.setImageData(value);
        }
    }

    private void deleteOldMenus(List<SearchMenu> oldSearchMenus, List<SearchMenu> searchMenus) {
        for (SearchMenu searchMenu : oldSearchMenus) {
            boolean exists = false;
            for (SearchMenu newSearchMenu : searchMenus) {
                if (newSearchMenu.getId().equalsIgnoreCase(searchMenu.getId())) {
                    exists = true;
                }
            }

            if (!exists) {
                menuItemService.deleteSearchMenus(searchMenu);
                menuItemService.deleteSearchMenuItems(searchMenu);
            }
        }
    }

    private void populateSearchMenuItem(SearchMenuItem searchMenuItem, String property, String value) {
        if ("id".equalsIgnoreCase(property)) {
            searchMenuItem.setId(value);
        } else if ("position".equalsIgnoreCase(property)) {
            searchMenuItem.setPosition(Integer.parseInt(value));
        } else if ("parent_id".equalsIgnoreCase(property)) {
            searchMenuItem.setParentId(value);
        } else if ("menu_id".equalsIgnoreCase(property)) {
            searchMenuItem.setMenuId(value);
        } else if ("label".equalsIgnoreCase(property)) {
            searchMenuItem.setLabel(value);
        } else if ("content".equalsIgnoreCase(property)) {
            searchMenuItem.setContent(value);
        }
    }

    private void populateSearchMenu(SearchMenu searchMenu, String property, String value) {
        if ("id".equalsIgnoreCase(property)) {
            searchMenu.setId(value);
        } else if ("label".equalsIgnoreCase(property)) {
            searchMenu.setLabel(value);
        }
    }

    private void processFarmers(InputStream inputStream) throws IOException, ParseException {
        final String[] farmersVersion = new String[]{ "" };
        final int[] farmersCount = new int[]{ 0 };

        final List<Farmer> farmers = new ArrayList<Farmer>();

        try {
            new JSONParser().parse(new InputStreamReader(inputStream), new JsonSimpleBaseParser() {
                private Object farmerObject = null;
                private String keywordType = "";
                private int farmersCounter = 0;

                @Override
                public boolean primitive(Object value) throws ParseException {
                    if (null != key && value != null) {
                        if (key.equals("resultCode")) {
                            if(!value.toString().equals("0")){
                                return false;//request wasn't successfull
                            }
                        } else if (key.equals("resultMessage")) {
                            Log.i(SynchronizationManager.class.getName(), value.toString());
                        } else if (key.equals("farmerVersion")) {
                            farmersVersion[0] = value.toString();
                        } else if (key.equals("farmerCount")) {
                            farmersCount[0] = Integer.parseInt(value.toString());

                            notifySynchronizationListeners("synchronizationUpdate", farmersCounter++, farmersCount[0],
                                    ApplicationRegistry.getApplicationContext().
                                            getResources().getString(R.string.processing_farmers_msg), true);
                        } else {
                            if (farmerObject instanceof Farmer) {
                                populateFarmer((Farmer) farmerObject, key, value.toString());
                            }
                        }
                    }
                    key = null;
                    return true;
                }

                @Override
                public boolean startArray() throws ParseException {
                    keywordType = key;
                    return true;
                }

                @Override
                public boolean startObject() throws ParseException {
                    if ("afarmerResults".equalsIgnoreCase(keywordType)) {
                        farmerObject = new Farmer();
                    }
                    return true;
                }

                @Override
                public boolean endObject() throws ParseException {
                    if (farmerObject != null) {
                        if (farmerObject instanceof Farmer) {
                            farmers.add((Farmer) farmerObject);
                            menuItemService.save((Farmer) farmerObject);
                            notifySynchronizationListeners("synchronizationUpdate", farmersCounter++, farmersCount[0],
                                    ApplicationRegistry.getApplicationContext().
                                            getResources().getString(R.string.processing_farmers_msg), true);
                        }
                    }

                    farmerObject = null;
                    return true;
                }
            });

            SettingsManager.getInstance().setValue(SettingsConstants.KEY_FARMERS_VERSION, farmersVersion[0]);
        } catch (ParseException ex) {
            Log.e(SynchronizationManager.class.getName(), "Parsing Error", ex);
            notifySynchronizationListeners("onSynchronizationError",
                    new Throwable(applicationContext.getString(R.string.error_processing_farmers)));
        } catch (IOException ex) {
            Log.e(SynchronizationManager.class.getName(), "IOException Error", ex);
            notifySynchronizationListeners("onSynchronizationError",
                    new Throwable(applicationContext.getString(R.string.error_connecting_to_server)));
        } catch (Exception ex) {
            Log.e(SynchronizationManager.class.getName(), "Exception", ex);
        }
    }

    private void populateFarmer(Farmer farmer, String property, String value) {
        if ("farmerId".equalsIgnoreCase(property)) {
            farmer.setId(value);
        } else if ("firstName".equalsIgnoreCase(property)) {
            farmer.setFirstName (value);
        } else if ("lastName".equalsIgnoreCase(property)) {
            farmer.setLastName (value);
        } else if ("creationDate".equalsIgnoreCase(property)) {
            farmer.setCreationDate (value);
        } else if ("subcounty".equalsIgnoreCase(property)) {
            farmer.setSubcounty (value);
        } else if ("village".equalsIgnoreCase(property)) {
            farmer.setVillage (value);
        }
    }

    /**
     * Sets the version in the update request entity Passes the keywords version, images version and current MenuIds
     *
     * @return XML request entity
     * @throws UnsupportedEncodingException
     */
    static AbstractHttpEntity getRequestEntity(Context context) throws UnsupportedEncodingException {
        String keywordsVersion = SettingsManager.getInstance().getValue(SettingsConstants.KEY_KEYWORDS_VERSION,
                DEFAULT_KEYWORDS_VERSION);

        String imagesVersion = SettingsManager.getInstance().getValue(SettingsConstants.KEY_IMAGES_VERSION,
                DEFAULT_IMAGES_VERSION);

        XmlEntityBuilder xmlRequest = new XmlEntityBuilder();
        xmlRequest.writeStartElement(REQUEST_ELEMENT_NAME, XML_NAME_SPACE);
        xmlRequest.writeStartElement(VERSION_ELEMENT_NAME);
        xmlRequest.writeText(keywordsVersion);
        xmlRequest.writeEndElement();
        xmlRequest.writeStartElement(IMAGES_VERSION_ELEMENT_NAME);
        xmlRequest.writeText(imagesVersion);
        xmlRequest.writeEndElement();
        xmlRequest.writeStartElement(CURRENT_MENU_IDS);
        xmlRequest.writeText(getMenuIds());
        xmlRequest.writeEndElement();
        xmlRequest.writeEndElement();
        return xmlRequest.getEntity();
    }

    private static String getMenuIds() {
        MenuItemService menuItemService = new MenuItemService();
        List<SearchMenu> searchMenuList = menuItemService.getAllSearchMenus();

        boolean first = true;
        StringBuilder stringBuilder = new StringBuilder("");

        for (SearchMenu searchMenu : searchMenuList) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(",");
            }

            stringBuilder.append(searchMenu.getId());
        }

        return stringBuilder.toString();
    }

    protected void downloadCountryCode() {
        String countryCode = SettingsManager.getInstance().getValue(SettingsConstants.KEY_COUNTRY_CODE, "NONE");
        if ("NONE".equalsIgnoreCase(countryCode)) {
            String url = SettingsManager.getInstance().getValue(SettingsConstants.KEY_SERVER);

            try {
                GeneralRequestWrapper request = new GeneralRequestWrapper();
                request.setRequest(SettingsConstants.REQUEST_GET_COUNTRY_CODE);
                request.setImei(DeviceMetadata.getDeviceImei(ApplicationRegistry.getApplicationContext()));

                Gson gson = new Gson();
                String jsonRequest = gson.toJson(request);

                int networkTimeout = 10 * 60 * 1000;
                List<NameValuePair> params = new ArrayList<NameValuePair>(2);
                params.add(new BasicNameValuePair(SettingsConstants.REQUEST_METHODNAME,
                        SettingsConstants.REQUEST_GET_COUNTRY_CODE));
                params.add(new BasicNameValuePair(SettingsConstants.REQUEST_DATA, jsonRequest));

                InputStream inputStream = HttpHelpers.postFormRequestAndGetStream(url, new UrlEncodedFormEntity(params),
                        networkTimeout);

                if (inputStream != null) {
                    countryCode = parseCountryCode(inputStream);
                    SettingsManager.getInstance().setValue(SettingsConstants.KEY_COUNTRY_CODE, countryCode);

                    inputStream.close();
                }
            } catch (Exception ex) {
                Log.e(SynchronizationManager.class.getName(), "Error downloading country code", ex);
                notifySynchronizationListeners("onSynchronizationError", new Throwable(ex));
            }
        }
    }

    /**
     * parses the given json input stream and returns the country code.
     *
     * @param inputStream
     * @return country code
     * @throws Exception
     */
    private String parseCountryCode(InputStream inputStream) throws Exception {
        final String[] countryCodeHolder = new String[1];

        new JSONParser().parse(new InputStreamReader(inputStream), new JsonSimpleBaseParser() {

            @Override
            public boolean primitive(Object value) throws ParseException, IOException {
                if (null != key) {
                    if ("countryCode".equals(key)) {
                        if (value != null) {
                            Log.d(SynchronizationManager.class.getName(),
                                    "The country code is: " + String.valueOf(value));

                            countryCodeHolder[0] = String.valueOf(value);
                        }
                    }
                }
                return true;
            }
        });

        return countryCodeHolder[0];
    }

    private AbstractHttpEntity buildCountryCodeRequestEntity() throws UnsupportedEncodingException {
        XmlEntityBuilder xmlRequest = new XmlEntityBuilder();
        xmlRequest.writeStartElement(REQUEST_ELEMENT_NAME, XML_NAME_SPACE);
        xmlRequest.writeEndElement();
        return xmlRequest.getEntity();
    }

    protected void notifySynchronizationListeners(String methodName, Object... args) {
        //synchronized (synchronizationListenerList) {
            for (SynchronizationListener listener : synchronizationListenerList.values()) {
                try {
                    Class[] argTypes = null;
                    if (args != null) {
                        argTypes = new Class[args.length];
                        for (int index = 0; index < args.length; index++) {
                            argTypes[index] = args[index].getClass();
                        }
                    }

                    SynchronizationListener.class.
                            getMethod(methodName, argTypes).invoke(listener, args);
                } catch (Exception ex) {
                    Log.e(SynchronizationManager.class.getName(),
                            "Error executing listener method", ex);
                }
            }
        //}
    }

    /**
     * called to stop an on going synchronization process.
     */
    public synchronized void stop() {
        //TODO stop the synchronization process here.
    }

    /**
     * registers the given synchronization listener and if the listener already exists, it
     * will be replaced.
     *
     * @param listener
     */
    public synchronized void registerListener(SynchronizationListener listener) {
        synchronizationListenerList.put(listener.getClass().getName(), listener);
    }

    /**
     * un registers the given synchronization listener
     *
     * @param listener
     */
    public synchronized void unRegisterListener(SynchronizationListener listener) {
        //synchronized (synchronizationListenerList) {
            synchronizationListenerList.remove(listener.getClass().getName());
        //}
    }

    /**
     * get a value to determine whether the synchronization manager is in the process of
     * performing a synchronization manager
     *
     * @return
     */
    public boolean isSynchronizing() {
        return synchronizing;
    }

    public class KeywordsRequestWrapper {
        private String keywordsVersion;
        private List<String> menuIds;
        private String ImagesLastUpdatedDate;
        private String request;
        private String imei;

        public void setRequest(String request) {
            this.request = request;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public void setKeywordsVersion(String keywordsVersion) {
            this.keywordsVersion = keywordsVersion;
        }

        public void setMenuIds(List<String> menuIds) {
            this.menuIds = menuIds;
        }

        public void setImagesLastUpdatedDate(String imagesLastUpdatedDate) {
            ImagesLastUpdatedDate = imagesLastUpdatedDate;
        }

        public String getKeywordsVersion() {
            return keywordsVersion;
        }

        public List<String> getMenuIds() {
            return menuIds;
        }

        public String getImagesLastUpdatedDate() {
            return ImagesLastUpdatedDate;
        }

        public String getRequest() {
            return request;
        }

        public String getImei() {
            return imei;
        }
    }

    public class SearchLogRequest {
        private String request;
        private String imei;
        private List<SearchLog> searchLogs;

        public void setRequest(String request) {
            this.request = request;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public void setSearchLogs(List<SearchLog> searchLogs) {
            this.searchLogs = searchLogs;
        }
    }

    public class FarmersRequestWrapper {
        private String farmersVersion;
        private String request;
        private String imei;

        public void setRequest(String request) {
            this.request = request;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public void setFarmersVersion(String farmersVersion) {
            this.farmersVersion = farmersVersion;
        }
    }

    public class GeneralRequestWrapper {
        private String request;
        private String imei;

        public void setRequest(String request) {
            this.request = request;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }
    }

    public class SearchLogResponse {
        private String resultMessage;
        private String resultCode;

        public String getResultMessage() {
            return resultMessage;
        }

        public void setResultMessage(String resultMessage) {
            this.resultMessage = resultMessage;
        }

        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }
    }

    public class ImagesRequestWrapper {
        private String request;
        private String imei;
        private List<String> imageIds;

        public String getRequest() {
            return request;
        }

        public void setRequest(String request) {
            this.request = request;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public List<String> getImageIds() {
            return imageIds;
        }

        public void setImageIds(List<String> imageIds) {
            this.imageIds = imageIds;
        }
    }

    public class ImagesResponseWrapper {
        private String resultCode;
        private String resultMassage;
        private List<ImageData> imageResults;

        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getResultMassage() {
            return resultMassage;
        }

        public void setResultMassage(String resultMassage) {
            this.resultMassage = resultMassage;
        }

        public List<ImageData> getImageResults() {
            return imageResults;
        }

        public void setImageResults(List<ImageData> imageResults) {
            this.imageResults = imageResults;
        }
    }

    public class ImageData {
        private String imageId;
        private String imageData;

        public String getImageId() {
            return imageId;
        }

        public void setImageId(String imageId) {
            this.imageId = imageId;
        }

        public String getImageData() {
            return imageData;
        }

        public void setImageData(String imageData) {
            this.imageData = imageData;
        }
    }
}
