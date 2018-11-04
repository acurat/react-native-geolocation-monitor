package com.acurat.geofence;

import android.app.ActivityManager;
import android.content.Context;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.location.Geofence;

import java.util.List;

final class RNGeofenceHelper {

    private RNGeofenceHelper() {
    }

    public static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses =
                activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static WritableMap convertDataObjectToRNMap(RNGeofenceDataObject data) {

        WritableMap map = Arguments.createMap();
        WritableArray ids = Arguments.fromList(data.getRequestIds());
        map.putArray("ids", ids);
        map.putString("transitionType", data.getTransitionType());
        return map;

    }

    public static String getTransitionCode(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return RNGeofenceConstants.ENTER;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return RNGeofenceConstants.EXIT;
            default:
                return "Unknown Transition";
        }
    }

}
