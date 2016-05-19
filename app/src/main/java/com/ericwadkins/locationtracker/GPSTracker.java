package com.ericwadkins.locationtracker;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
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
import com.ericwadkins.request.Request;
import com.ericwadkins.request.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by ericwadkins on 5/18/16.
 */
public class GPSTracker extends Service implements LocationListener {

    private static Location location;

    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;

    private LocationManager locationManager;

    public GPSTracker() {
    }

    private void initialize() {
        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            // getting GPS and network status
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkEnabled =
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            boolean hasPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            Log.e("debug", "GPS: " + (gpsEnabled ? "enabled" : "disabled"));
            Log.e("debug", "Network: " + (networkEnabled ? "enabled" : "disabled"));
            Log.e("debug", "Permission: " + (hasPermission ? "enabled" : "disabled"));

            if (gpsEnabled && networkEnabled && hasPermission) {
                // First get location from Network Provider
                if (networkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                }
                // if GPS Enabled get lat/long using GPS Services
                if (gpsEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopSelf();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.e("Location changed", location.getLatitude() + ", " + location.getLongitude());
        Toast.makeText(this, location.getLatitude() + ", " + location.getLongitude(),
                Toast.LENGTH_SHORT).show();

        final Location loc = location;
        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String urlString = "http://18.111.88.64:5200";
                Request request = new Request(urlString);
                JSONObject obj = new JSONObject();
                try {
                    obj.put("latitude", loc.getLatitude());
                    obj.put("longitude", loc.getLongitude());
                } catch (JSONException e) {}
                request.addJsonData(obj);
                try {
                    Response response = request.POST();
                    Log.e("response", response.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        //networkThread.start();
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

    public static Location getLastLocation() {
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
