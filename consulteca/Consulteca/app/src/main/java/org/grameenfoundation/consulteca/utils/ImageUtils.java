/**
 * Copyright (C) 2010 Grameen Foundation
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy of
 the License at http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 License for the specific language governing permissions and limitations under
 the License.
 */
package org.grameenfoundation.consulteca.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Environment;
import android.util.Log;
import org.grameenfoundation.consulteca.R;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Contains methods for managing image files on the file system.
 */
public class ImageUtils {
    public static final String IMAGE_ROOT = Environment.getExternalStorageDirectory() + "/gfsearch/";
    private static final String LOG_TAG = "ImageFilesUtility";
    private static String[] SUPPORTED_FORMATS = {".jpg", ".jpeg"};

    public static boolean storageReady() {
        String cardstatus = Environment.getExternalStorageState();
        if (cardstatus.equals(Environment.MEDIA_REMOVED)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardstatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean createRootFolder() {
        if (storageReady()) {
            File dir = new File(IMAGE_ROOT);
            if (!dir.exists()) {
                return dir.mkdirs();
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteFile(File file) {
        if (storageReady()) {
            return file.delete();
        } else {
            return false;
        }
    }

    public static void writeFile(String fileName, InputStream inputStream) throws IOException {
        if (storageReady() && createRootFolder()) {
            // replace spaces with underscores
            fileName = fileName.replace(" ", "_");
            // change to lowercase
            fileName = fileName.toLowerCase();
            FileOutputStream fileOutputStream = new FileOutputStream(new File(IMAGE_ROOT, fileName));
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.close();
        }
    }

    public static Drawable getImageAsDrawable(Context context, String fileName) {
        if (!storageReady()) {
            return null;
        }
        fileName = getFullPath(fileName);
        Bitmap bitmap = BitmapFactory.decodeFile(fileName);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public static Drawable getImageAsDrawable(Context context, String fileName, boolean isPartialName) {
        if (!storageReady()) {
            return null;
        }
        fileName = getFullPath(fileName, true);
        Bitmap bitmap = BitmapFactory.decodeFile(fileName);
        return new BitmapDrawable(context.getResources(), bitmap);
    }

    public static ArrayList<String> getFilesAsArrayList() {
        ArrayList<String> fileList = new ArrayList<String>();
        File rootDirectory = new File(IMAGE_ROOT);
        if (!storageReady()) {
            return null;
        }
        // If directory does not exist, create it.
        if (!rootDirectory.exists()) {
            if (!createRootFolder()) {
                return null;
            }
        }
        File[] children = rootDirectory.listFiles();
        for (File child : children) {
            fileList.add(child.getAbsolutePath());
        }

        return fileList;
    }

    public static boolean imageExists(String fileName) {
        if (!storageReady()) {
            return false;
        }

        return getFullPath(fileName) == null ? false : true;
    }

    /**
     * Overload to allow getting iamge by full name
     *
     * @param fileName
     * @param isPartialName
     * @return
     */
    public static boolean imageExists(String fileName, boolean isPartialName) {
        if (!storageReady()) {
            return false;
        } else if (isPartialName) {
            return getFullPath(fileName, true) == null ? false : true;
        } else {
            return imageExists(fileName);
        }
    }

    public static String getFullPath(String fileName) {
        for (String format : SUPPORTED_FORMATS) {
            String path = IMAGE_ROOT + fileName + format;
            File file = new File(path);

            if (file.exists()) {
                return path;
            }
        }
        return null;
    }

    public static String getFullPath(String fileName, boolean isPartialName) {
        if (!isPartialName) {
            return getFullPath(fileName);
        }
        File dir = new File(IMAGE_ROOT);
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                Log.d("FILES", file.getName());
                if (fileName != null && file.getName().toLowerCase().contains(fileName.toLowerCase())) {
                    return file.getAbsolutePath();
                }
            }
        }

        return null;

    }

    public static String getSHA1Hash(File file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] messageDigest = md.digest(getFileAsBytes(file));
            BigInteger number = new BigInteger(1, messageDigest);
            String sha1 = number.toString(16);
            while (sha1.length() < 32)
                sha1 = "0" + sha1;
            return sha1;
        } catch (NoSuchAlgorithmException e) {
            Log.e("SHA1", e.getMessage());
            return null;
        }
    }

    public static byte[] getFileAsBytes(File file) {
        byte[] bytes = null;
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            // Get the size of the file
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                Log.e("", "File " + file.getName() + "is too large");
                return null;
            }
            // Create the byte array to hold the data
            bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int read = 0;
            try {
                while (offset < bytes.length && read >= 0) {
                    read = is.read(bytes, offset, bytes.length - offset);
                    offset += read;
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Cannot read " + file.getName());
                e.printStackTrace();
                return null;
            }
            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                Log.e(LOG_TAG, "Could not completely read file " + file.getName());
                return null;
            }
            return bytes;
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Cannot find " + file.getName());
            e.printStackTrace();
            return null;
        } finally {
            // Close the input stream
            try {
                is.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Cannot close input stream for " + file.getName());
                e.printStackTrace();
                return null;
            }
        }
    }

    public static Drawable drawSelectedImage(Context context, int width, int height) {
        Bitmap canvasBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);

        ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        drawable.setBounds(0, 0, width, height);
        drawable.getPaint().setColor(0xff5E5C5C);

        Canvas canvas = new Canvas(canvasBitmap);
        drawable.draw(canvas);

        Drawable resourceDrawable = context.getResources().getDrawable(R.drawable.ic_action_accept);
        resourceDrawable.draw(canvas);


        return new BitmapDrawable(context.getResources(), canvasBitmap);
    }

    public static Drawable drawRandomColorImageWithText(Context context, String substring, int width, int height) {
        int[] colors = new int[]{0xff67BF74, 0xffE4C62E, 0xff2093CD, 0xff59A2BE, 0xffF9A43A};

        int randomIndex = new Random().nextInt(colors.length);
        Bitmap canvasBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);

        ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        drawable.setBounds(0, 0, width, height);
        drawable.getPaint().setColor(colors[randomIndex]);

        Canvas canvas = new Canvas(canvasBitmap);
        drawable.draw(canvas);

        // Set up the paint for use with our Canvas
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(35f);
        textPaint.setAntiAlias(true);
        //textPaint.setStyle(Paint.Style.FILL);
        Typeface myTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto/Roboto-Light.ttf");
        textPaint.setTypeface(myTypeface);
        //textPaint.setStrokeWidth(0.01f);
        textPaint.setColor(0xffFFFFFF);

        canvas.drawText(substring, width / 2, (height / 1.4f), textPaint);
        return new BitmapDrawable(context.getResources(), canvasBitmap);
    }

    public static File createImageCacheFolderIfNotExists(Context context) {
        String imageCachePath = context.getCacheDir() + "/gfimages/";
        File folder = new File(imageCachePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public static Drawable scaleAndCacheImage(Context context, String sourceImageFile, String destinationImageFile,
                                              int scaleWidth, int scaleHeight) {
        try {
            int inWidth = 0;
            int inHeight = 0;

            InputStream in = new FileInputStream(sourceImageFile);

            // decode image size (decode metadata only, not the whole image)
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            in.close();
            in = null;

            // save width and height
            inWidth = options.outWidth;
            inHeight = options.outHeight;

            // decode full image pre-resized
            in = new FileInputStream(sourceImageFile);
            options = new BitmapFactory.Options();
            // calc rought re-size (this is no exact resize)
            options.inSampleSize = Math.max(inWidth / scaleWidth, inHeight / scaleHeight);
            // decode full image
            Bitmap roughBitmap = BitmapFactory.decodeStream(in, null, options);

            // calc exact destination size
            Matrix m = new Matrix();
            RectF inRect = new RectF(0, 0, roughBitmap.getWidth(), roughBitmap.getHeight());
            RectF outRect = new RectF(0, 0, scaleWidth, scaleHeight);
            m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
            float[] values = new float[9];
            m.getValues(values);

            // resize bitmap
            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(roughBitmap,
                            (int) (roughBitmap.getWidth() * values[0]),
                            (int) (roughBitmap.getHeight() * values[4]), true);

            // save image
            try {
                FileOutputStream out = new FileOutputStream(destinationImageFile);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

                return new BitmapDrawable(context.getResources(), resizedBitmap);
            } catch (Exception e) {
                Log.e("Image", e.getMessage(), e);
            }
        } catch (IOException e) {
            Log.e("Image", e.getMessage(), e);
        }

        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public static Drawable loadBitmapDrawableIfExists(Context context, String cacheImageFile) {
        if (new File(cacheImageFile).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(cacheImageFile);
            return new BitmapDrawable(context.getResources(), bitmap);
        } else {
            return null;
        }
    }
}
