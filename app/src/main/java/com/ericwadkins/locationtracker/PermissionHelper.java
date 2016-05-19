package com.ericwadkins.locationtracker;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ericwadkins on 5/18/16.
 */
public class PermissionHelper {

    public static boolean debug = false;

    private static final Map<Integer, PermissionCallback> callbackMap = new HashMap<>();

    public static boolean hasPermissions(Activity activity, String[] permissions) {
        boolean hasPermissions = true;
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                hasPermissions = false;
            }
        }
        return hasPermissions;
    }

    public static void requestPermissions(final Activity activity, final String[] permissions,
                                          String explanation, final PermissionCallback callback) {
        final List<String> requiredPermissions = new ArrayList<>();
        final List<String> notRequiredPermissions = new ArrayList<>();
        boolean explanationRequired = false;
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(activity, permissions[i])
                    != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(permissions[i]);
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i])) {
                    explanationRequired = true;
                }
            }
            else {
                notRequiredPermissions.add(permissions[i]);
            }
        }
        if (requiredPermissions.size() > 0) {
            final int requestCode = saveCallback(callback);
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
                                callback(notRequiredPermissions.toArray(new String[0]),
                                        requiredPermissions.toArray(new String[0]), callback);
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
            callback(permissions, new String[0], callback);
        }
    }

    private static int saveCallback(PermissionCallback callback) {
        int requestCode;
        do {
            requestCode = (int) (Math.random() * 65535);
        }
        while (callbackMap.containsKey(requestCode));
        callbackMap.put(requestCode, callback);
        return requestCode;
    }

    public static void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        PermissionCallback callback = callbackMap.get(requestCode);
        if (grantResults.length == 0) {
            callback(new String[0], permissions, callback);
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
                callback(permissions, new String[0], callback);
            }
            else {
                callback(grantedPermissions.toArray(new String[0]),
                        deniedPermissions.toArray(new String[0]), callback);
            }
        }
    }

    public static void callback(String[] granted, String[] denied,
                                PermissionCallback callback) {
        if (debug) {
            printPermissionResults(granted, denied);
        }
        if (denied.length == 0) {
            callback.run(true, granted, denied);
        }
        else {
            callback.run(false, granted, denied);
        }
    }

    private static void printPermissionResults(String[] granted, String[] denied) {
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
        void run(boolean successful, String[] granted, String[] denied);
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
