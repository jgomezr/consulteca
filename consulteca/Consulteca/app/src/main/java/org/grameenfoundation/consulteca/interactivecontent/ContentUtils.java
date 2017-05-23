package org.grameenfoundation.consulteca.interactivecontent;

import android.os.Environment;

import java.io.File;

/**
 *
 */
public class ContentUtils {
    private static final String CONTENT_ROOT = Environment.getExternalStorageDirectory() + "/gfinteractive";

    /**
     * default constructor made private to avoid instantiating
     * this class
     */
    private ContentUtils() {

    }

    private static boolean storageReady() {
        String cardStatus = Environment.getExternalStorageState();
        if (cardStatus.equals(Environment.MEDIA_REMOVED)
                || cardStatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardStatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean createContentRootIfNotExists() {
        if (storageReady()) {
            File dir = new File(CONTENT_ROOT);
            if (!dir.exists()) {
                return dir.mkdirs();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * get the interactive content folder names
     *
     * @return
     */
    public static String[] getContentListing() {
        String[] items = new String[]{};

        File dir = new File(CONTENT_ROOT);
        if (dir.exists()) {
            items = dir.list();
            if (items == null)
                return new String[]{};
        } else {
            createContentRootIfNotExists();
        }

        return items;
    }

    /**
     * gets the content folder for the given content item.
     *
     * @param contentItem
     * @return
     */
    public static String getContentFolder(String contentItem) {
        return CONTENT_ROOT + "/" + contentItem;
    }
}
