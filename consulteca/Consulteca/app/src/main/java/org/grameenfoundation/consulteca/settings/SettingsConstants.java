package org.grameenfoundation.consulteca.settings;

/**
 * Contains constants that represents settings of the application.
 */
public final class SettingsConstants {
    private SettingsConstants() {
    }

    public static final String KEY_SERVER = "server_url";
    public static final String KEY_COUNTRY_CODE = "countryCode";
    public static final String KEY_KEYWORDS_VERSION = "keywordsVersion";
    public static final String KEY_FARMERS_VERSION = "farmersVersion";
    public static final String KEY_IMAGES_VERSION = "imagesVersion";

    public  static final String REQUEST_SUBMIT_SEARCHLOGS_PAGE = "SearchSubmitLogs";
    public  static final String REQUEST_DOWNLOAD_FARMERS = "farmers";
    public  static final String REQUEST_UPLOAD_SEARCHLOGS = "searchLogs";
    public  static final String REQUEST_GET_COUNTRY_CODE = "countryCode";
    public  static final String REQUEST_DOWNLOAD_IMAGES = "images";
    public  static final String REQUEST_DOWNLOAD_KEYWORDS = "keywords";
    public  static final String REQUEST_METHODNAME = "method";
    public  static final String REQUEST_DATA = "data";

    public static final String KEY_BACKGROUND_SYNC_INTERVAL = "background_sync_interval";
    public static final String KEY_BACKGROUND_SYNC_INTERVAL_UNITS = "background_sync_interval_units";
    public static final String KEY_BACKGROUND_SYNC_ENABLED = "background_sync_enabled";

    public static final String KEY_CLIENT_IDENTIFIER_PROMPTING_ENABLED = "prompt_for_clientid_enabled";
    public static final String KEY_TEST_SEARCHING_ENABLED = "test_searching_enabled";

    public static final int INTERVAL_UNITS_HOURS = 0;
    public static final int INTERVAL_UNITS_MINUTES = 1;
    public static final int INTERVAL_UNITS_SECONDS = 2;

}
