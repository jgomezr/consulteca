/**
 Copyright (C) 2010 Grameen Foundation
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy of
 the License at http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 License for the specific language governing permissions and limitations under
 the License.
 */
package org.grameenfoundation.consulteca.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Listens to location updates from the GPS provider.
 */
public class GpsLocationListener implements LocationListener {

    private static final String TAG = "GpsLocationListener";

    private GpsManager gpsManager;

    public GpsLocationListener(GpsManager gpsManager) {
        this.gpsManager = gpsManager;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.gpsManager.onLocationChanged(location);
    }

    //We do not use the three overrides below and we have them here only because
    //we are implementing the LocationListener interface which has no adapters.
    @Override
    public void onProviderDisabled(String arg0) {
        Log.d(TAG, "GPS_PROVIDER onProviderDisabled");
    }

    @Override
    public void onProviderEnabled(String arg0) {
        Log.d(TAG, "GPS_PROVIDER onProviderEnabled");
    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        Log.d(TAG, "GPS_PROVIDER onStatusChanged");
    }
}
