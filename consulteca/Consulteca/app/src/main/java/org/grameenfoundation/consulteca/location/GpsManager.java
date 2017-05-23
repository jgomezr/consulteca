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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import org.grameenfoundation.consulteca.ApplicationRegistry;

import java.util.Date;

/**
 * Manages getting and caching of GPS coordinates using the available location providers. You will need to call the
 * update() method before you can get location information with the getLocation() method.
 */
public class GpsManager {

    /**
     * Flag to help us remember to turn off GPS if we turned it on ourselves instead of the user.
     */
    private boolean gpsWasAutomaticallyTurnedOn;

    /**
     * The maximum allowed location accuracy, in meters. Greater than this is a bit too inaccurate for us to stop trying
     * to get more GPS updates, if we have not yet timed out.
     */
    private static double MAXIMUM_LOCATION_ACCURACY = 5;

    /**
     * The number of minutes at which we time out when searching for gps coordinates.
     */
    private static int GPS_SEARCH_TIMEOUT = 5;

    /**
     * The number of minutes after which we get new GPS coordinates.
     */
    private static int GPS_MAX_AGE = 15;

    /**
     * The current application context.
     */
    private Context context;

    /**
     * The android api location manager.
     */
    private LocationManager locationManager;

    /**
     * Listener for location updates from the gps provider.
     */
    private GpsLocationListener gpsLocationListener;

    /**
     * Listener for location updates form the network provider.
     */
    private GpsLocationListener networkLocationListener;

    /**
     * Caches the best location we so far have as long as the application is running. When the application is restarted,
     * we initialize it with LocationManager.getLastKnownLocation().
     */
    private Location cachedLocation;

    /**
     * Holds the time when we start the location search.
     */
    private long searchStartTime;

    /**
     * Constructs a new GPS manager object.
     */
    private GpsManager() {
        this.context = ApplicationRegistry.getApplicationContext();
        this.locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);

        this.gpsLocationListener = new GpsLocationListener(this);
        this.networkLocationListener = new GpsLocationListener(this);
    }

    /**
     * Gets the global gps manager instance.
     *
     * @return a {@link GpsManager}
     */
    public static GpsManager getInstance() {
        return GpsManagerHolder.INSTANCE;
    }

    /**
     * Searches and caches GPS coordinates for the current location.
     */
    public synchronized void update() {

        this.cachedLocation = getLocation();

        // If we already have location values that are not older than GPS_MAX_AGE,
        // Just use them instead of searching for new ones, all in the name of trying
        // to preserve the device resources.
        if (this.cachedLocation != null) {
            long duration = getDateDiffInMinutes(new Date().getTime(), cachedLocation.getTime());
            if (duration <= GPS_MAX_AGE) {
                return;
            }
        }

        this.cachedLocation = null;

        // We need to register the network provider too just in case the GPS provider
        // takes longer than we can bear.
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this.gpsLocationListener);
        this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this.networkLocationListener);

        // Record the time when we have started, such that we can time out when appropriate.
        this.searchStartTime = new Date().getTime();
    }

    /**
     * Should be called some moments after a call to update() in order to get the current most accurate location fix.
     *
     * @return an object having the location information.
     */
    public Location getLocation() {

        // If we have not yet got a location fix since the call to update(), then let us try to get the
        // last known location fix. Note that this location could be out-of-date,
        // for example if the device was turned off and moved to another location.
        if (this.cachedLocation == null) {
            Location gpsLocation = this.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = this.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            // The GPS location provider takes priority over the network location provider
            // because it is more accurate.
            if (gpsLocation != null) {
                this.cachedLocation = gpsLocation;
            } else if (networkLocation != null) {
                this.cachedLocation = networkLocation;
            }
        }

        return this.cachedLocation;
    }

    /**
     * Should be called some moments after a call to update() in order to get the current most accurate location fix.
     *
     * @return location information formatted as a string of: latitude longitude altitude accuracy time.
     */
    public String getLocationAsString() {
        Location location = getLocation();
        if (location == null) {
            return "";
        }

        // TODO Is this the format the server expects the string formatted location?
        return location.getLatitude() + " " +
                location.getLongitude() + " " +
                location.getAltitude() + " " +
                location.getAccuracy() + " " + location.getTime();
    }

    /**
     * Should be called in Activity:onStart() to ensure that GPS settings are turned on.
     *
     * @param context is the current activity context.
     * @return true if the settings were found disabled and hence showed the settings screen.
     */
    public boolean onStart(Context context) {
        // The context is reset because the initial value of ApplabActivity.getGlobalContext()
        // may have an non Activity context, if it was set in a service, and yet the AlertDialog
        // which we may show, requires an Activity context, else you get the WindowManager$BadTokenException
        this.context = context;

        return showSettingsIfDisabled();
    }

    /**
     * Called whenever we get a location update from either the network or gps provider.
     *
     * @param location is the object with the new location information.
     */
    public void onLocationChanged(Location location) {

        // Check if we have no location info saved yet or if we have just got better accuracy than
        // what we had before.
        if (this.cachedLocation == null || this.cachedLocation.getAccuracy() > location.getAccuracy()) {
            this.cachedLocation = location;
        }

        // Get the period, in minutes, for which we have so far tried to get location.
        long duration = getDateDiffInMinutes(new Date().getTime(), this.searchStartTime);

        // If we have tried getting location for the time out period, or the
        // current location accuracy is less than or equal to the maximum allowed
        // accuracy, then stop the location manager from running (which will turn off the GPS radio)
        // to preserve battery.
        if (duration >= GPS_SEARCH_TIMEOUT || this.cachedLocation.getAccuracy() <= MAXIMUM_LOCATION_ACCURACY) {
            stopGettingUpdates();
        }
    }

    /**
     * Stops any running search for location updates.
     */
    private synchronized void stopGettingUpdates() {

        // A call to LocationManager.removeUpdates() will turn off the GPS radio and hence preserve battery.
        if (this.gpsLocationListener != null) {
            this.locationManager.removeUpdates(this.gpsLocationListener);
        }

        if (this.networkLocationListener != null) {
            this.locationManager.removeUpdates(this.networkLocationListener);
        }

        // If we automatically turned on gps, then turn it off now.
        if (this.gpsWasAutomaticallyTurnedOn) {
            toggleGpsUsingSecurityFlaw();
            this.gpsWasAutomaticallyTurnedOn = false;
        }

        // TODO I think this may not belong here. Could be when a server connection has been closed.
        // DataConnectionManager.getInstance().turnOff();
    }

    /**
     * Checks if GPS settings are enabled and if not, shows the GPS settings screen.
     *
     * @return true if the settings were found disabled and hence showed the settings screen.
     */
    private boolean showSettingsIfDisabled() {

        if (!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            toggleGpsUsingSecurityFlaw();

            // Set this flag such that we can remember to turn off gps.
            this.gpsWasAutomaticallyTurnedOn = true;

            // TODO Check if android fixed the security bug and if so,
            // show user settings screen to turn on GPS
            // This check below will not work because the above security exploit sends a broadcast
            // which may not have been received yet, by the time we do the check..
            // if(!this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            // showGpsAlertDialog("\"Use GPS satellites\"");

            return true;
        }

        if (!this.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // The above security exploit does not deal with location network provider settings
            // and therefore we have no other option other than show the settings screen.
            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            showGpsAlertDialog("\"Enable wireless network access for GPS\"", intent);
            return true;
        }

        return false;
    }

    /**
     * Shows an alert message to the user about the disabled GPS settings.
     *
     * @param name   is the name of the GPS settings that are disabled.
     * @param intent is the intent to start the settings screen activity.
     */
    private void showGpsAlertDialog(String name, final Intent intent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage(name + " is disabled! Please tick to enable it on the screen which is going to be shown next.")
                .setCancelable(false)
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                context.startActivity(intent);
                            }
                        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Turns ON GPS if it is OFF, or turns it OFF if is ON.
     */
    private void toggleGpsUsingSecurityFlaw() {
        Intent poke = new Intent();
        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
        poke.setData(Uri.parse("3"));
        context.sendBroadcast(poke);
    }

    /**
     * Gets the difference between two dates in minutes.
     *
     * @param newerDate the later date in milliseconds.
     * @param olderDate the earlier date in milliseconds.
     * @return the number of minutes between the two dates.
     */
    private long getDateDiffInMinutes(long newerDate, long olderDate) {
        // 1000 converts from milliseconds to seconds.
        // while 60 converts from seconds to minutes.
        return (newerDate - olderDate) / (1000 * 60);
    }

    /**
     * This class is only used to help us implement a thread safe {@link GpsManager} singleton.
     */
    private static class GpsManagerHolder {
        private static final GpsManager INSTANCE = new GpsManager();
    }
}