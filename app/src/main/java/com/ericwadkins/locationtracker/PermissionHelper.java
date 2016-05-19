package com.ericwadkins.locationtracker;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ericwadkins on 5/18/16.
 */
public class PermissionHelper {

    Activity activity;
    PermissionCallback granted = null;
    PermissionCallback denied = null;
    boolean debug = false;

    public PermissionHelper(final Activity activity, boolean debug) {
        this.activity = activity;
        this.debug = debug;
    }

    public PermissionHelper(final Activity activity) {
        this.activity = activity;
    }

    public boolean hasPermissions(String[] permissions) {
        boolean hasPermissions = true;
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("debug", permissions[i] + " permission denied!");
                hasPermissions = false;
            }
        }
        return hasPermissions;
    }

    public void requestPermissions(final String[] permissions,
                                  final int requestCode, String explanation) {
        if (granted == null || denied == null) {
            throw new RuntimeException("Must set permission callbacks before any requests!" +
                    " Use setPermissionGrantedCallback() and setPermissionDeniedCallback()");
        }
        final List<String> requiredPermissions = new ArrayList<>();
        final List<String> notRequiredPermissions = new ArrayList<>();
        boolean requestRequired = false;
        boolean explanationRequired = false;
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("debug", permissions[i] + " permission denied!");
                requiredPermissions.add(permissions[i]);
                requestRequired = true;
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                    explanationRequired = true;
                }
            }
            else {
                notRequiredPermissions.add(permissions[i]);
            }
        }
        if (requestRequired) {
            if (explanationRequired) {
                new MaterialDialog.Builder(activity)
                        .title("Permissions required")
                        .content(explanation)
                        .positiveText("Continue")
                        .negativeText("Cancel")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                ActivityCompat.requestPermissions(activity,
                                        permissions, requestCode);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                callback(requestCode,
                                        notRequiredPermissions.toArray(new String[0]),
                                        requiredPermissions.toArray(new String[0]));
                            }
                        })
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(activity,
                        requiredPermissions.toArray(new String[0]), requestCode);
            }
        }
        else {
            callback(requestCode, permissions, new String[0]);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length == 0) {
            callback(requestCode, new String[0], permissions);
        }
        else {
            boolean allGranted = true;
            List<String> grantedPermissions = new ArrayList<>();
            List<String> deniedPermissions = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permissions[i]);
                }
                else {
                    deniedPermissions.add(permissions[i]);
                    allGranted = false;
                }
            }
            if (allGranted) {
                callback(requestCode, permissions, new String[0]);
            }
            else {
                callback(requestCode, grantedPermissions.toArray(new String[0]),
                        deniedPermissions.toArray(new String[0]));
            }
        }
    }

    public PermissionHelper setPermissionGrantedCallback(PermissionCallback granted) {
        this.granted = granted;
        return this;
    }

    public PermissionHelper setPermissionDeniedCallback(PermissionCallback denied) {
        this.denied = denied;
        return this;
    }

    public void callback(int requestCode, String[] granted, String[] denied) {
        if (debug) {
            printPermissionResults(requestCode, granted, denied);
        }
        if (denied.length == 0) {
            this.granted.run(requestCode, granted, denied);
        }
        else {
            this.denied.run(requestCode, granted, denied);
        }
    }

    private static void printPermissionResults(int requestCode, String[] granted, String[] denied) {
        //Log.e("PermissionHelper", "Got result of permissions, requestCode = " + requestCode);
        if (granted.length > 0) {
            Log.e("PermissionHelper", granted.length + "/" + (granted.length + denied.length)
                    + " permissions granted:");
            for (int i = 0; i < granted.length; i++) {
                Log.e("PermissionHelper", "\t" + granted[i]);
            }
        }
        if (denied.length > 0) {
            Log.e("PermissionHelper", denied.length + "/" + (granted.length + denied.length)
                    + " permissions denied:");
            for (int i = 0; i < denied.length; i++) {
                Log.e("PermissionHelper", "\t" + denied[i]);
            }
        }
    }

    public interface PermissionCallback {
        void run(int requestCode, String[] granted, String[] denied);
    }

    /*
    public void onPermissionGranted(int requestCode) {
        switch (requestCode) {
            case TRACKER_REQUEST_CODE: startTracker(); break;
        }
    }

    public void onPermissionDenied(int requestCode) {
        switch (requestCode) {
            case TRACKER_REQUEST_CODE: Log.e("debug", "Permission is required!"); break;
        }
    }
    */
}
