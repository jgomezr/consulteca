package org.grameenfoundation.consulteca.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.grameenfoundation.consulteca.ApplicationRegistry;
import org.grameenfoundation.consulteca.R;

/**
 * handles settings related operations i.e. storage and retrieval of settings values.
 */
public final class SettingsManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ACTION_SETTINGS_CHANGED = "org.grameenfoundation.consulteca.settings.SETTINGS_CHANGED";
    public static final String INTENT_DATA_CHANGED_SETTING_KEY = "iTts1itr8j80FhcEMe0l";
    private static final SettingsManager INSTANCE = new SettingsManager();
    private SharedPreferences sharedPreferences;

    /**
     * private constructor to avoid instantiation of this class
     */
    private SettingsManager() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationRegistry.getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public static SettingsManager getInstance() {
        return INSTANCE;
    }

    /**
     * gets the settings value for the given settings key.
     *
     * @param settingKey the key for which the value is required.
     * @return the value of the settings key or null if the value cannot be found.
     */
    public String getValue(String settingKey) {
        return sharedPreferences.getString(settingKey, null);
    }

    /**
     * gets the settings value for the given settings key
     *
     * @param settingKey
     * @param defaultValue
     * @return
     */
    public String getValue(String settingKey, String defaultValue) {
        return sharedPreferences.getString(settingKey, defaultValue);
    }

    public boolean getBooleanValue(String settingKey, boolean defaultValue) {
        return sharedPreferences.getBoolean(settingKey, defaultValue);
    }

    /**
     * stores a value in the settings/preferences store with
     * the given key and value
     *
     * @param settingKey
     * @param value
     */
    public void setValue(String settingKey, String value) {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString(settingKey, value);
        editor.commit();
    }

    /**
     * sets the default application settings.
     *
     * @param readAgain
     */
    public void setDefaultSettings(boolean readAgain) {
        PreferenceManager.setDefaultValues(ApplicationRegistry.getApplicationContext(),
                R.xml.connection_preferences, readAgain);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Intent intent = new Intent(SettingsManager.ACTION_SETTINGS_CHANGED);
        intent.putExtra(SettingsManager.INTENT_DATA_CHANGED_SETTING_KEY, key);
        ApplicationRegistry.getApplicationContext().sendBroadcast(intent);
    }
}

