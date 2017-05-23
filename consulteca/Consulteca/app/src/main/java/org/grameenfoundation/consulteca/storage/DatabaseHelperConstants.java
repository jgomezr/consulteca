package org.grameenfoundation.consulteca.storage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Utility class that contains the data storage contacts.
 *
 * @author Charles Tumwebaze
 */
public final class DatabaseHelperConstants {
    private DatabaseHelperConstants() {
    }

    /**
     * default date format;
     */
    public static final DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /* Menu Table Columns */
    public static final String MENU_ROWID_COLUMN = "id";
    public static final String MENU_LABEL_COLUMN = "label";

    /* Menu Item Table Columns */
    public static final String MENU_ITEM_ROWID_COLUMN = "id";
    public static final String MENU_ITEM_LABEL_COLUMN = "label";
    public static final String MENU_ITEM_POSITION_COLUMN = "position";
    public static final String MENU_ITEM_CONTENT_COLUMN = "content";
    public static final String MENU_ITEM_MENUID_COLUMN = "menu_id";
    public static final String MENU_ITEM_PARENTID_COLUMN = "parent_id";
    public static final String MENU_ITEM_ATTACHMENTID_COLUMN = "attachment_id";

    /* Available Farmer Ids Table Columns */
    public static final String AVAILABLE_FARMER_ID_ROWID_COLUMN = "id";
    public static final String AVAILABLE_FARMER_ID_FARMER_ID = "farmer_id";
    public static final String AVAILABLE_FARMER_ID_STATUS = "status";

    /* Farmer Local Cache Table Columns */
    public static final String FARMER_LOCAL_CACHE_ROWID_COLUMN = "id";
    public static final String FARMER_LOCAL_CACHE_FARMER_ID = "farmer_id";
    public static final String FARMER_LOCAL_CACHE_FIRST_NAME = "first_name";
    public static final String FARMER_LOCAL_CACHE_MIDDLE_NAME = "middle_name";
    public static final String FARMER_LOCAL_CACHE_LAST_NAME = "last_name";
    public static final String FARMER_LOCAL_CACHE_AGE = "age";
    public static final String FARMER_LOCAL_CACHE_FATHER_NAME = "father_name";

    /* Full farmer list - Local table */
    public static final String FARMERS_ROWID_COLUMN = "id";
    public static final String FARMERS_FARMER_ID = "farmer_id";
    public static final String FARMERS_FIRST_NAME = "first_name";
    public static final String FARMERS_LAST_NAME = "last_name";
    public static final String FARMERS_CREATION_DATE = "creation_date";
    public static final String FARMERS_SUBCOUNTY = "subcounty";
    public static final String FARMERS_VILLAGE = "village";

    /**
     * search log table columns
     */
    public static final String SEARCH_LOG_ROW_ID_COLUMN = "id";
    public static final String SEARCH_LOG_MENU_ITEM_ID_COLUMN = "menu_item_id";
    public static final String SEARCH_LOG_DATE_CREATED_COLUMN = "date_created";
    public static final String SEARCH_LOG_CLIENT_ID_COLUMN = "client_id";
    public static final String SEARCH_LOG_GPS_LOCATION_COLUMN = "gps_location";
    public static final String SEARCH_LOG_CONTENT_COLUMN = "content";
    public static final String SEARCH_LOG_CONTENT_CATEGORY_COLUMN = "category";
    public static final String SEARCH_LOG_TEST_LOG = "test_log";

    /**
     * favourite record table columns
     */
    public static final String FAVOURITE_RECORD_ROW_ID_COLUMN = "id";
    public static final String FAVOURITE_RECORD_NAME_COLUMN = "name";
    public static final String FAVOURITE_RECORD_CATEGORY_COLUMN = "category";
    public static final String FAVOURITE_RECORD_DATE_CREATED_COLUMN = "date_created";
    public static final String FAVOURITE_RECORD_MENU_ITEM_ID_COLUMN = "menu_item_id";

    /**
     * table names
     */
    public static final String MENU_TABLE_NAME = "menu";
    public static final String MENU_ITEM_TABLE_NAME = "menu_item";
    public static final String AVAILABLE_FARMER_ID_TABLE_NAME = "available_farmer_id";
    public static final String FARMER_LOCAL_CACHE_TABLE_NAME = "farmer_local_cache";
    public static final String FARMER_LOCAL_DATABASE_TABLE_NAME = "farmer_local_database";
    public static final String SEARCH_LOG_TABLE_NAME = "search_log";
    public static final String FAVOURITE_RECORD_TABLE_NAME = "favourite_record";

    public static final String DATABASE_NAME = "gfsearch";
    public static final int DATABASE_VERSION = 4;
}
