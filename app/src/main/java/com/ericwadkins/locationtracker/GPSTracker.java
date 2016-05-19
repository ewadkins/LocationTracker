package com.ericwadkins.locationtracker;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by ericwadkins on 5/18/16.
 */
public class GPSTracker extends IntentService implements LocationListener {

    private Context context = null;

    private Location location;

    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1;

    private LocationManager locationManager;

    public GPSTracker() {
        super("GPSTracker");
    }

    public GPSTracker(Activity activity) {
        super("GPSTracker");
        this.context = activity;
        initialize();
    }

    private void initialize() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            // getting GPS and network status
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean hasPermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            Log.e("debug", "GPS: " + (gpsEnabled ? "enabled" : "disabled"));
            Log.e("debug", "Network: " + (networkEnabled ? "enabled" : "disabled"));
            Log.e("debug", "Permission: " + (hasPermission ? "enabled" : "disabled"));

            if (gpsEnabled && networkEnabled && hasPermission) {
                // First get location from Network Provider
                if (networkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (gpsEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.e("Location changed", location.getLatitude() + ", " + location.getLongitude());
        Toast.makeText(context, location.getLatitude() + ", " + location.getLongitude(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("intent", "onHandleIntent");
        Toast.makeText(context, location.getLatitude() + ", " + location.getLongitude(),
                Toast.LENGTH_SHORT).show();
    }

    public Location getLocation() {
        return location;
    }

    public static boolean isGPSEnabled(Context context) {
        LocationManager locationManager =
                (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void requestEnableGPS(final Activity activity) {
        new MaterialDialog.Builder(activity)
                .title("Enable GPS")
                .content("Your GPS must be enabled for this app to work properly. "
                        + " Would you like to enable it?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {
                        Intent enableGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivity(enableGPS);
                    }
                })
                .show();
    }
}
