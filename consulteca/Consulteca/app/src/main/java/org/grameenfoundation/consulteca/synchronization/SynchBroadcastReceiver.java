package org.grameenfoundation.consulteca.synchronization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.grameenfoundation.consulteca.ApplicationRegistry;
import org.grameenfoundation.consulteca.GlobalConstants;
import org.grameenfoundation.consulteca.R;
import org.grameenfoundation.consulteca.utils.DeviceMetadata;

/**
 * Handler the broadcast message to start background synchronization.
 */
public class SynchBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ApplicationRegistry.getApplicationContext() == null) {
            ApplicationRegistry.setApplicationContext(context);
        }

        //caching the device imie in the application registry
        ApplicationRegistry.register(GlobalConstants.KEY_CACHED_DEVICE_IMEI,
                DeviceMetadata.getDeviceImei(context));

        //register application version in registry
        ApplicationRegistry.register(GlobalConstants.KEY_CACHED_APPLICATION_VERSION,
                context.getResources().getString(R.string.app_name) + "/" + R.string.app_version);

        Intent backgroundServiceIntent = new Intent(context, BackgroundSynchronizationService.class);
        context.startService(backgroundServiceIntent);
    }
}
