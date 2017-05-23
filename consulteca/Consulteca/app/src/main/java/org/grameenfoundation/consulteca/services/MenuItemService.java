package org.grameenfoundation.consulteca.services;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import org.grameenfoundation.consulteca.model.*;
import org.grameenfoundation.consulteca.storage.DatabaseHelperConstants;
import org.grameenfoundation.consulteca.storage.StorageManager;
import org.grameenfoundation.consulteca.storage.search.Filter;
import org.grameenfoundation.consulteca.storage.search.Search;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class to handler menu related tasks
 *
 * @author Charles Tumwebaze
 */
public class MenuItemService {

    /**
     * gets all the search menus in the system.
     *
     * @return
     */
    public List<SearchMenu> getAllSearchMenus() {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_TABLE_NAME);
        search.addSortAsc(DatabaseHelperConstants.MENU_LABEL_COLUMN);
        return buildSearchMenus(StorageManager.getInstance().getRecords(search));
    }

    private List<SearchMenu> buildSearchMenus(Cursor cursor) {
        List<SearchMenu> searchMenus = new ArrayList<SearchMenu>();
        while (cursor.moveToNext()) {
            SearchMenu searchMenu = new SearchMenu();
            searchMenu.setId(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.MENU_ROWID_COLUMN)));
            searchMenu.setLabel(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.MENU_LABEL_COLUMN)));

            searchMenus.add(searchMenu);
        }
        return searchMenus;
    }

    /**
     * gets the search menus starting at the given offset and ending at the given limit.
     *
     * @param offset
     * @param limit
     * @return
     */
    public List<SearchMenu> getSearchMenus(int offset, int limit) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_TABLE_NAME);
        search.setFirstResult(offset);
        search.setMaxResults(limit);

        Cursor cursor = StorageManager.getInstance().getRecords(search);
        return buildSearchMenus(cursor);
    }

    /**
     * gets the total number of search menu items.
     *
     * @return
     */
    public int countSearchMenus() {
        return StorageManager.getInstance().recordCount(DatabaseHelperConstants.MENU_TABLE_NAME);
    }

    /**
     * saves the given search menus into the data store
     *
     * @param searchMenus
     */
    public void save(SearchMenu... searchMenus) {
        List<ContentValues> values = getContentValues(searchMenus);
        StorageManager.getInstance().replace(DatabaseHelperConstants.MENU_TABLE_NAME,
                values.toArray(new ContentValues[]{}));
    }

    private List<ContentValues> getContentValues(SearchMenu[] searchMenus) {
        List<ContentValues> values = new ArrayList<ContentValues>();
        for (SearchMenu item : searchMenus) {
            ContentValues contentValue = new ContentValues();
            contentValue.put(DatabaseHelperConstants.MENU_ROWID_COLUMN, item.getId());
            contentValue.put(DatabaseHelperConstants.MENU_LABEL_COLUMN, item.getLabel());

            values.add(contentValue);
        }
        return values;
    }

    /**
     * gets all the search menu items in the system.
     *
     * @return
     */
    public List<SearchMenuItem> getAllSearchMenuItems() {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME);
        Cursor cursor = StorageManager.getInstance().getRecords(search);
        return buildSearchMenuItems(cursor);
    }

    /**
     * builds a list of search menu items from the given cursor.
     *
     * @param cursor
     * @return
     */
    private List<SearchMenuItem> buildSearchMenuItems(Cursor cursor) {
        List<SearchMenuItem> searchMenuItems = new ArrayList<SearchMenuItem>();
        while (cursor.moveToNext()) {
            SearchMenuItem searchMenuItem = new SearchMenuItem();
            searchMenuItem.setId(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.MENU_ITEM_ROWID_COLUMN)));
            searchMenuItem.setLabel(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.MENU_ITEM_LABEL_COLUMN)));
            searchMenuItem.setPosition(cursor.getInt(cursor.
                    getColumnIndex(DatabaseHelperConstants.MENU_ITEM_POSITION_COLUMN)));

            searchMenuItem.setContent(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.MENU_ITEM_CONTENT_COLUMN)));

            searchMenuItem.setParentId(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.MENU_ITEM_PARENTID_COLUMN)));

            searchMenuItem.setMenuId(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.MENU_ITEM_MENUID_COLUMN)));

            searchMenuItem.setAttachmentId(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.MENU_ITEM_ATTACHMENTID_COLUMN)));

            searchMenuItems.add(searchMenuItem);
        }
        return searchMenuItems;
    }

    /**
     * gets the search menus starting at the given offset and ending at the given limit.
     *
     * @param offset
     * @param limit
     * @return
     */
    public List<SearchMenuItem> getSearchMenuItems(int offset, int limit) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME);
        search.setFirstResult(offset);
        search.setMaxResults(limit);

        Cursor cursor = StorageManager.getInstance().getRecords(search);
        return buildSearchMenuItems(cursor);
    }

    /**
     * gets the total number of search menu items in the system.
     *
     * @return
     */
    public int countSearchMenuItems() {
        return StorageManager.getInstance().recordCount(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME);
    }

    public List<SearchMenuItem> getSearchMenuItems(String parentMenuItemId, int offset, int limit) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME);
        search.addFilterEqual(DatabaseHelperConstants.MENU_ITEM_PARENTID_COLUMN, parentMenuItemId);
        search.addSortAsc(DatabaseHelperConstants.MENU_ITEM_POSITION_COLUMN);
        search.setFirstResult(offset);
        search.setMaxResults(limit);

        Cursor cursor = StorageManager.getInstance().getRecords(search);
        return buildSearchMenuItems(cursor);
    }

    /**
     * gets the total number of search menu items for the given parent menu identifier.
     *
     * @param parentMenuItemId
     * @return
     */
    public int countSearchMenuItems(String parentMenuItemId) {
        String sql = String.format("SELECT COUNT(*) as total FROM %1$s WHERE %2$s = %3$s",
                DatabaseHelperConstants.MENU_TABLE_NAME, DatabaseHelperConstants.MENU_ITEM_TABLE_NAME,
                parentMenuItemId);
        Cursor cursor = StorageManager.getInstance().sqlSearch(sql);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        return count;
    }

    /**
     * gets the total number of search menu items for the given search menu.
     *
     * @param searchMenu
     * @return
     */
    public int countTopLevelSearchMenuItems(SearchMenu searchMenu) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME);
        search.addFilterEqual(DatabaseHelperConstants.MENU_ITEM_MENUID_COLUMN, searchMenu.getId());
        search.addFilterOr(Filter.isEmpty(DatabaseHelperConstants.MENU_ITEM_PARENTID_COLUMN));

        return StorageManager.getInstance().recordCount(search);
    }

    /**
     * gets the total number of search menu items whose parent is the
     * given search menu item.
     *
     * @param searchMenuItem
     * @return
     */
    public int countSearchMenuItems(SearchMenuItem searchMenuItem) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME);
        search.addFilterEqual(DatabaseHelperConstants.MENU_ITEM_PARENTID_COLUMN, searchMenuItem.getId());
        search.addSortAsc(DatabaseHelperConstants.MENU_ITEM_POSITION_COLUMN);

        return StorageManager.getInstance().recordCount(search);
    }


    /**
     * saves the given search menu items into the data store
     *
     * @param searchMenuItems
     */
    public void save(SearchMenuItem... searchMenuItems) {
        List<ContentValues> values = getContentValues(searchMenuItems);

        StorageManager.getInstance().replace(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME,
                values.toArray(new ContentValues[]{}));
    }

    private List<ContentValues> getContentValues(SearchMenuItem[] searchMenuItems) {
        List<ContentValues> values = new ArrayList<ContentValues>();
        for (SearchMenuItem item : searchMenuItems) {
            ContentValues contentValue = new ContentValues();
            contentValue.put(DatabaseHelperConstants.MENU_ITEM_ROWID_COLUMN, item.getId());
            contentValue.put(DatabaseHelperConstants.MENU_ITEM_LABEL_COLUMN, item.getLabel());
            contentValue.put(DatabaseHelperConstants.MENU_ITEM_POSITION_COLUMN, item.getPosition());
            contentValue.put(DatabaseHelperConstants.MENU_ITEM_CONTENT_COLUMN, item.getContent());
            contentValue.put(DatabaseHelperConstants.MENU_ITEM_MENUID_COLUMN, item.getMenuId());
            contentValue.put(DatabaseHelperConstants.MENU_ITEM_PARENTID_COLUMN, item.getParentId());
            contentValue.put(DatabaseHelperConstants.MENU_ITEM_ATTACHMENTID_COLUMN, item.getAttachmentId());

            values.add(contentValue);
        }
        return values;
    }

    public void deleteSearchMenuItems(SearchMenuItem... searchMenuItems) {
        for (SearchMenuItem searchMenuItem : searchMenuItems) {
            StorageManager.getInstance().execSql("DELETE FROM " + DatabaseHelperConstants.MENU_ITEM_TABLE_NAME +
                    " WHERE " + DatabaseHelperConstants.MENU_ITEM_ROWID_COLUMN + " ='" + searchMenuItem.getId() + "'");
        }
    }

    public void deleteSearchMenus(SearchMenu... searchMenus) {
        for (SearchMenu searchMenu : searchMenus) {
            StorageManager.getInstance().execSql("DELETE FROM " + DatabaseHelperConstants.MENU_TABLE_NAME +
                    " WHERE " + DatabaseHelperConstants.MENU_ROWID_COLUMN + " ='" + searchMenu.getId() + "'");
        }
    }


    /**
     * delete search menu items for the given search menu.
     *
     * @param searchMenu
     */
    public void deleteSearchMenuItems(SearchMenu searchMenu) {
        StorageManager.getInstance().execSql("DELETE FROM " + DatabaseHelperConstants.MENU_ITEM_TABLE_NAME +
                " WHERE " + DatabaseHelperConstants.MENU_ITEM_MENUID_COLUMN + " ='" + searchMenu.getId() + "'");
    }

    public List<SearchMenuItem> getTopLevelSearchMenuItems(SearchMenu searchMenu) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME);
        search.addFilterEqual(DatabaseHelperConstants.MENU_ITEM_MENUID_COLUMN, searchMenu.getId());
        search.addFilterOr(Filter.isEmpty(DatabaseHelperConstants.MENU_ITEM_PARENTID_COLUMN));

        search.addSortAsc(DatabaseHelperConstants.MENU_ITEM_LABEL_COLUMN);
        return buildSearchMenuItems(StorageManager.getInstance().getRecords(search));
    }

    public List<SearchMenuItem> getSearchMenuItems(SearchMenuItem searchMenuItem) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME);
        search.addFilterEqual(DatabaseHelperConstants.MENU_ITEM_PARENTID_COLUMN, searchMenuItem.getId());
        search.addSortAsc(DatabaseHelperConstants.MENU_ITEM_LABEL_COLUMN);

        return buildSearchMenuItems(StorageManager.getInstance().getRecords(search));
    }

    /**
     * checks whether the given list object has children.
     *
     * @param listObject
     * @return
     */
    public boolean hasChildren(ListObject listObject) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME);

        if (listObject instanceof SearchMenu) {
            search.addFilterEqual(DatabaseHelperConstants.MENU_ITEM_MENUID_COLUMN, listObject.getId());
            search.addFilterOr(Filter.isEmpty(DatabaseHelperConstants.MENU_ITEM_PARENTID_COLUMN));
        } else if (listObject instanceof SearchMenuItem) {
            search.addFilterEqual(DatabaseHelperConstants.MENU_ITEM_PARENTID_COLUMN, listObject.getId());
        }

        int count = StorageManager.getInstance().recordCount(search);
        return count > 0 ? true : false;
    }

    public void save(SearchLog searchLog) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelperConstants.SEARCH_LOG_CLIENT_ID_COLUMN, searchLog.getClientId());
        contentValue.put(DatabaseHelperConstants.SEARCH_LOG_CONTENT_COLUMN, searchLog.getContent());
        contentValue.put(DatabaseHelperConstants.SEARCH_LOG_CONTENT_CATEGORY_COLUMN, searchLog.getCategory());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        contentValue.put(DatabaseHelperConstants.SEARCH_LOG_DATE_CREATED_COLUMN,
                dateFormat.format(searchLog.getDateCreated()));

        contentValue.put(DatabaseHelperConstants.SEARCH_LOG_GPS_LOCATION_COLUMN, searchLog.getGpsLocation());
        contentValue.put(DatabaseHelperConstants.SEARCH_LOG_MENU_ITEM_ID_COLUMN, searchLog.getMenuItemId());

        StorageManager.getInstance().update(DatabaseHelperConstants.SEARCH_LOG_TABLE_NAME, contentValue);
    }

    /**
     * saves the given favourite record to the datastore.
     *
     * @param record
     */
    public void save(FavouriteRecord record) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(DatabaseHelperConstants.FAVOURITE_RECORD_NAME_COLUMN, record.getName());
        contentValue.put(DatabaseHelperConstants.FAVOURITE_RECORD_CATEGORY_COLUMN, record.getCategory());
        contentValue.put(DatabaseHelperConstants.FAVOURITE_RECORD_MENU_ITEM_ID_COLUMN, record.getMenuItemId());

        contentValue.put(DatabaseHelperConstants.FAVOURITE_RECORD_DATE_CREATED_COLUMN,
                DatabaseHelperConstants.DEFAULT_DATE_FORMAT.format(record.getDateCreated()));

        StorageManager.getInstance().update(DatabaseHelperConstants.FAVOURITE_RECORD_TABLE_NAME, contentValue);
    }

    /**
     * gets the favourite record for the given menu item identifier.
     *
     * @param menuItemId identifier of the menu item whose favourite record is required.
     * @return FavouriteRecord
     */
    public FavouriteRecord getFavouriteRecord(String menuItemId) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.FAVOURITE_RECORD_TABLE_NAME);
        search.addFilterEqual(DatabaseHelperConstants.FAVOURITE_RECORD_MENU_ITEM_ID_COLUMN, menuItemId);

        FavouriteRecord favouriteRecord = null;
        Cursor cursor = StorageManager.getInstance().getRecords(search);
        List<FavouriteRecord> favouriteRecords = buildFavouriteRecords(cursor);
        if (favouriteRecords != null && favouriteRecords.size() > 0) {
            favouriteRecord = favouriteRecords.get(0);
        }

        return favouriteRecord;
    }

    private List<FavouriteRecord> buildFavouriteRecords(Cursor cursor) {
        List<FavouriteRecord> favouriteRecords = new ArrayList<FavouriteRecord>();

        while (cursor.moveToNext()) {
            FavouriteRecord favouriteRecord = new FavouriteRecord();
            favouriteRecord.setId(cursor.getInt(
                    cursor.getColumnIndex(DatabaseHelperConstants.FAVOURITE_RECORD_ROW_ID_COLUMN)));
            favouriteRecord.setName(cursor.getString(
                    cursor.getColumnIndex(DatabaseHelperConstants.FAVOURITE_RECORD_NAME_COLUMN)));
            favouriteRecord.setCategory(cursor.getString(
                    cursor.getColumnIndex(DatabaseHelperConstants.FAVOURITE_RECORD_CATEGORY_COLUMN)));
            favouriteRecord.setMenuItemId(cursor.getString(
                    cursor.getColumnIndex(DatabaseHelperConstants.FAVOURITE_RECORD_MENU_ITEM_ID_COLUMN)));
            try {
                favouriteRecord.setDateCreated(DatabaseHelperConstants.DEFAULT_DATE_FORMAT.parse(cursor.getString(
                        cursor.getColumnIndex(DatabaseHelperConstants.FAVOURITE_RECORD_DATE_CREATED_COLUMN))));
            } catch (ParseException e) {
                Log.e(MenuItemService.class.getName(), "ParseException", e);
            }


            favouriteRecords.add(favouriteRecord);
        }
        return favouriteRecords;
    }

    /**
     * deletes the given favourite record from the data store.
     *
     * @param record the favourite record to delete
     */
    public void delete(FavouriteRecord record) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.FAVOURITE_RECORD_TABLE_NAME);
        search.addFilterEqual(DatabaseHelperConstants.FAVOURITE_RECORD_ROW_ID_COLUMN, record.getId());
        StorageManager.getInstance().delete(search);
    }

    /**
     * gets the total number of favourite records in the
     * data store.
     *
     * @return the number of favourite records.
     */
    public int countFavouriteRecords() {
        return StorageManager.getInstance()
                .recordCount(DatabaseHelperConstants.FAVOURITE_RECORD_TABLE_NAME);
    }

    /**
     * gets a list of all the favourite records
     *
     * @return list of favourite records found in the data store.
     */
    public List<FavouriteRecord> getAllFavouriteRecords() {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.FAVOURITE_RECORD_TABLE_NAME);
        search.addSort(DatabaseHelperConstants.FAVOURITE_RECORD_DATE_CREATED_COLUMN, true);

        return buildFavouriteRecords(StorageManager.getInstance().getRecords(search));
    }

    /**
     * gets a search menu item with the given identifier.
     *
     * @param id identifier of the search menu item required.
     * @return SearchMenuItem
     */
    public SearchMenuItem getSearchMenuItem(String id) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.MENU_ITEM_TABLE_NAME);
        search.addFilterEqual(DatabaseHelperConstants.MENU_ITEM_ROWID_COLUMN, id);

        List<SearchMenuItem> items =
                buildSearchMenuItems(StorageManager.getInstance().getRecords(search));

        if (items != null && items.size() > 0)
            return items.get(0);

        return null;
    }

    /**
     * gets all the search logs in the data store.
     *
     * @return list of search logs
     */
    public List<SearchLog> getAllSearchLogs() {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.SEARCH_LOG_TABLE_NAME);
        search.addSort(DatabaseHelperConstants.SEARCH_LOG_DATE_CREATED_COLUMN, true);

        return buildSearchLogs(StorageManager.getInstance().getRecords(search));
    }

    private List<SearchLog> buildSearchLogs(Cursor cursor) {
        List<SearchLog> searchLogs = new ArrayList<SearchLog>();
        while (cursor.moveToNext()) {
            SearchLog searchLog = new SearchLog();
            searchLog.setId(cursor.getInt(cursor.
                    getColumnIndex(DatabaseHelperConstants.SEARCH_LOG_ROW_ID_COLUMN)));
            searchLog.setMenuItemId(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.SEARCH_LOG_MENU_ITEM_ID_COLUMN)));

            try {
                searchLog.setDateCreated(DatabaseHelperConstants.DEFAULT_DATE_FORMAT.parse(cursor.getString(
                        cursor.getColumnIndex(DatabaseHelperConstants.SEARCH_LOG_DATE_CREATED_COLUMN))));
            } catch (ParseException e) {
                Log.e(MenuItemService.class.getName(), "ParseException", e);
            }


            searchLog.setContent(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.MENU_ITEM_CONTENT_COLUMN)));

            searchLog.setCategory(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.SEARCH_LOG_CONTENT_CATEGORY_COLUMN)));

            searchLog.setClientId(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.SEARCH_LOG_CLIENT_ID_COLUMN)));

            searchLog.setGpsLocation(cursor.getString(cursor.
                    getColumnIndex(DatabaseHelperConstants.SEARCH_LOG_GPS_LOCATION_COLUMN)));

            Integer isTestLog = cursor.getInt(cursor.getColumnIndex(DatabaseHelperConstants.SEARCH_LOG_TEST_LOG));
            if (isTestLog <= 0)
                searchLog.setTestLog(false);
            else {
                searchLog.setTestLog(true);
            }

            searchLogs.add(searchLog);
        }

        return searchLogs;
    }

    /**
     * gets the total number of search logs in the data store
     *
     * @return number of search logs in the data store
     */
    public int countSearchLogs() {
        return StorageManager.getInstance().recordCount(DatabaseHelperConstants.SEARCH_LOG_TABLE_NAME);
    }

    /**
     * deletes the given list of search logs from the data store.
     *
     * @param selectedItems search log items to delete.
     */
    public void deleteSearchLogs(List<SearchLog> selectedItems) {
        Integer[] idz = new Integer[selectedItems.size()];

        for (int index = 0; index < selectedItems.size(); index++) {
            idz[index] = selectedItems.get(index).getId();
        }

        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.SEARCH_LOG_TABLE_NAME);
        search.addFilterIn(DatabaseHelperConstants.SEARCH_LOG_ROW_ID_COLUMN, idz);

        StorageManager.getInstance().delete(search);
    }

    /**
     * deletes the given search log from the data store.
     *
     * @param searchLog search log to delete
     */
    public void deleteSearchLog(SearchLog searchLog) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.SEARCH_LOG_TABLE_NAME);
        search.addFilterEqual(DatabaseHelperConstants.SEARCH_LOG_ROW_ID_COLUMN, searchLog.getId());

        StorageManager.getInstance().delete(search);
    }

    /**
     * gets all the farmer records in the system.
     *
     * @return
     */
    public List<Farmer> getAllFarmers() {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.FARMER_LOCAL_DATABASE_TABLE_NAME);
        search.addSortAsc(DatabaseHelperConstants.FARMERS_CREATION_DATE);
        return buildFarmers(StorageManager.getInstance().getRecords(search));
    }

    private List<Farmer> buildFarmers(Cursor cursor) {
        List<Farmer> farmers = new ArrayList<Farmer>();
        while (cursor.moveToNext()) {
            Farmer farmer = new Farmer();
            farmer.setId(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_ROWID_COLUMN)));
            //farmer.setFarmerId(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_FARMER_ID)));
            farmer.setFirstName(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_FIRST_NAME)));
            farmer.setLastName(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_LAST_NAME)));
            farmer.setCreationDate(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_CREATION_DATE)));
            farmer.setSubcounty(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_SUBCOUNTY)));
            farmer.setVillage(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_VILLAGE)));

            farmers.add(farmer);
        }
        return farmers;
    }

    /**
     * gets only the farmer details required for searching.
     *
     * @return stringArray of farmers' farmerId, fname, lname, subcounty and village delimited by |
     */
    public List<String> getAllFarmersSummary() {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.FARMER_LOCAL_DATABASE_TABLE_NAME);
        search.addSortAsc(DatabaseHelperConstants.FARMERS_FIRST_NAME);
        return buildFarmersSummaryDetails(StorageManager.getInstance().getRecords(search));
    }

    private List<String> buildFarmersSummaryDetails(Cursor cursor) {
        List<String> farmers = new ArrayList<String>();
        while (cursor.moveToNext()) {
            StringBuilder farmer = new StringBuilder();
            farmer.append(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_FARMER_ID)));
            farmer.append('|');
            farmer.append(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_FIRST_NAME)));
            farmer.append('|');
            farmer.append(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_LAST_NAME)));
            farmer.append('|');
            farmer.append(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_SUBCOUNTY)));
            farmer.append('|');
            farmer.append(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_VILLAGE)));

            farmers.add(farmer.toString());
        }
        return farmers;
    }

    /**
     * gets farmer records starting at the given offset and ending at the given limit.
     *
     * @param offset
     * @param limit
     * @return
     */
    public List<Farmer> getFarmers(int offset, int limit) {
        Search search = new Search();
        search.setTableName(DatabaseHelperConstants.FARMER_LOCAL_DATABASE_TABLE_NAME);
        search.setFirstResult(offset);
        search.setMaxResults(limit);

        Cursor cursor = StorageManager.getInstance().getRecords(search);
        return buildFarmers(cursor);
    }

    /**
     * gets the total number of farmer records.
     *
     * @return
     */
    public int countFarmers() {
        return StorageManager.getInstance().recordCount(DatabaseHelperConstants.FARMER_LOCAL_DATABASE_TABLE_NAME);
    }

    /**
     * saves the given farmer record into the data store
     *
     * @param farmers   farmer record to save
     */
    public void save(Farmer... farmers) {
        List<ContentValues> values = getContentValues(farmers);
        StorageManager.getInstance().replace(DatabaseHelperConstants.FARMER_LOCAL_DATABASE_TABLE_NAME,
                values.toArray(new ContentValues[]{}));
    }

    private List<ContentValues> getContentValues(Farmer[] farmers) {
        List<ContentValues> values = new ArrayList<ContentValues>();
        for (Farmer item : farmers) {
            ContentValues contentValue = new ContentValues();
            contentValue.put(DatabaseHelperConstants.FARMERS_ROWID_COLUMN, item.getId());
            //contentValue.put(DatabaseHelperConstants.FARMERS_FARMER_ID, item.getFarmerId());
            contentValue.put(DatabaseHelperConstants.FARMERS_FIRST_NAME, item.getFirstName());
            contentValue.put(DatabaseHelperConstants.FARMERS_LAST_NAME, item.getLastName());
            contentValue.put(DatabaseHelperConstants.FARMERS_CREATION_DATE, item.getCreationDate());
            contentValue.put(DatabaseHelperConstants.FARMERS_SUBCOUNTY, item.getSubcounty());
            contentValue.put(DatabaseHelperConstants.FARMERS_VILLAGE, item.getVillage());

            values.add(contentValue);
        }
        return values;
    }

    public List<Farmer> getFarmersByName(String name) {
        //the search platform cannot search concatenated two columns for a value
        /*Search search = new Search();
        search.setTableName(DatabaseHelperConstants.FARMER_LOCAL_DATABASE_TABLE_NAME);
        search.addFilterILike(DatabaseHelperConstants.FARMERS_FIRST_NAME, name);
        search.addSortAsc(DatabaseHelperConstants.FARMERS_FIRST_NAME);
        return buildFarmersSearchResults(StorageManager.getInstance().getRecords(search));*/
        return buildFarmersSearchResults(StorageManager.getInstance().sqlSearch(
                "SELECT * FROM farmer_local_database WHERE (lower(first_name) || ' ' || lower(last_name) like lower('"
                        + name + "%')) order by first_name asc"));
    }

    private List<Farmer> buildFarmersSearchResults(Cursor cursor) {
        List<Farmer> farmers = new ArrayList<Farmer>();
        while (cursor.moveToNext()) {
            Farmer farmer = new Farmer();
            farmer.setId(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_ROWID_COLUMN)));
            //farmer.setFarmerId(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_FARMER_ID)));
            farmer.setFirstName(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_FIRST_NAME)));
            farmer.setLastName(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_LAST_NAME)));
            farmer.setCreationDate(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_CREATION_DATE)));
            farmer.setSubcounty(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_SUBCOUNTY)));
            farmer.setVillage(cursor.getString(cursor.getColumnIndex(DatabaseHelperConstants.FARMERS_VILLAGE)));

            farmers.add(farmer);
        }
        return farmers;
    }
}
