package org.grameenfoundation.consulteca;

import android.app.Activity;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class that shares data across the entire application
 */
public final class ApplicationRegistry {
    private ApplicationRegistry() {
    }

    private static Context applicationContext;
    private static Activity mainActivity;
    private static Map<String, Object> registry = new HashMap<String, Object>();

    /**
     * gets the application context
     *
     * @return
     */
    public static Context getApplicationContext() {
        return applicationContext;
    }

    /**
     * sets the application context
     *
     * @param applicationContext
     */
    public static void setApplicationContext(Context applicationContext) {
        ApplicationRegistry.applicationContext = applicationContext;
    }

    /**
     * registers a record in the application registry with the given
     * key and value.
     *
     * @param key   the key used to retrieve an element from the registry
     * @param value value of the element in the registry
     */
    public static void register(String key, Object value) {
        registry.put(key, value);
    }

    /**
     * un registers/ removes a record with the given key from the
     * registry
     *
     * @param key
     */
    public static void unRegister(String key) {
        registry.remove(key);
    }

    /**
     * gets the record associated with the given key
     * from the registry
     *
     * @param key
     * @return
     */
    public static Object retrieve(String key) {
        return registry.get(key);
    }

    public static Activity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(Activity mainActivity) {
        ApplicationRegistry.mainActivity = mainActivity;
    }
}
