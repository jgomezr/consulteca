package org.grameenfoundation.consulteca.synchronization;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.grameenfoundation.consulteca.settings.SettingsConstants;
import org.grameenfoundation.consulteca.settings.SettingsManager;

import java.util.Calendar;

/**
 * Responsible for configuring the background synchronization process. It interacts with the
 * AlarmManager to determine how often the background synchronization process will run.
 * <p/>
 * This broadcast configurer is a broadcast listener of
 * BOOT_COMPLETED, TIME_CHANGED, TIME_ZONE_CHANGED, PACKAGE_INSTALL and
 * BACKGROUND_SYNC_CONFIGURATION events.
 */
public class BackgroundSynchronizationConfigurer extends BroadcastReceiver {
    public static final String ACTION_BACKGROUND_SYNC = "org.grameenfoundation.consulteca.synchronization.BACKGROUND_SYNC";
    public static final String ACTION_BACKGROUND_SYNC_CONFIGURATION =
            "org.grameenfoundation.consulteca.synchronization.BACKGROUND_SYNC_CONFIGURATION";

    private AlarmManager alarmManager = null;
    private final static Integer DEFAULT_SYNCHRONIZATION_INTERVAL_MILLISECONDS = 30 * 60 * 1000; //30 minutes
    private final static Integer DEFAULT_SYNCHRONIZATION_INTERVAL_MINUTES = 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (intent.getAction().equals(SettingsManager.ACTION_SETTINGS_CHANGED)) {
            String setting = intent.getStringExtra(SettingsManager.INTENT_DATA_CHANGED_SETTING_KEY);
            if (setting != null) {
                if (setting.equals(SettingsConstants.KEY_BACKGROUND_SYNC_ENABLED)
                        || setting.equals(SettingsConstants.KEY_BACKGROUND_SYNC_INTERVAL)
                        || setting.equals(SettingsConstants.KEY_BACKGROUND_SYNC_INTERVAL_UNITS)) {
                    applyBackgroundSynchronizationSettings(context);
                }
            }

        } else {
            if (!isAlarmSet(context)) {
                applyBackgroundSynchronizationSettings(context);
            }
        }
    }

    private void applyBackgroundSynchronizationSettings(Context context) {
        boolean syncEnabled = SettingsManager
                .getInstance().getBooleanValue(SettingsConstants.KEY_BACKGROUND_SYNC_ENABLED, true);

        int interval = Integer.parseInt(SettingsManager
                .getInstance().getValue(SettingsConstants.KEY_BACKGROUND_SYNC_INTERVAL,
                        String.valueOf(DEFAULT_SYNCHRONIZATION_INTERVAL_MINUTES)));

        int units = Integer.parseInt(SettingsManager
                .getInstance().getValue(SettingsConstants.KEY_BACKGROUND_SYNC_INTERVAL_UNITS,
                        String.valueOf(SettingsConstants.INTERVAL_UNITS_MINUTES)));

        if (syncEnabled) {
            setRepeatingAlarm(context, interval, units);
        } else {
            removeAlarm(context);
        }
    }

    private void removeAlarm(Context context) {
        Intent intent = new Intent(ACTION_BACKGROUND_SYNC);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * sets up a repeating alarm and configures it
     *
     * @param context
     */
    private void setRepeatingAlarm(Context context, int interval, int units) {
        int syncInterval = 0;
        if (units == SettingsConstants.INTERVAL_UNITS_HOURS) {
            syncInterval = interval * (3600 * 1000);
        } else if (units == SettingsConstants.INTERVAL_UNITS_MINUTES) {
            syncInterval = interval * (60 * 1000);
        } else if (units == SettingsConstants.INTERVAL_UNITS_SECONDS) {
            syncInterval = interval * 1000;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());


        Intent intent = new Intent(ACTION_BACKGROUND_SYNC);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                syncInterval, pendingIntent);
    }

    /**
     * checks whether the alarm is already set.
     *
     * @param context
     * @return
     */
    public boolean isAlarmSet(Context context) {
        Intent intent = new Intent(ACTION_BACKGROUND_SYNC);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }
}
