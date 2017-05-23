package org.grameenfoundation.consulteca.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

/**
 * Used to determine the metadata of the device. i.e. IMEI etc.
 */
public final class DeviceMetadata {

    /**
     * gets the device IMEI
     *
     * @param context
     * @return
     */
    public static String getDeviceImei(Context context) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
