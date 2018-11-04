package com.acurat.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import static com.acurat.geofence.RNGeofenceConstants.MODULE_NAME;

public class RNGeofenceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String errorMessage = RNGeofenceErrorMessages.getErrorString(context,
                    geofencingEvent.getErrorCode());
            Log.e(MODULE_NAME, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            GeofenceDataObject geofenceDataObject = new GeofenceDataObject();
            List<String> requestIds = new ArrayList<>();
            for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                requestIds.add(geofence.getRequestId());
            }

            geofenceDataObject.setRequestIds(requestIds);
            geofenceDataObject.setTransitionType(RNGeofenceHelper.getTransitionCode(geofenceTransition));
            Log.i(MODULE_NAME, "Sending events " + geofenceDataObject);

            if (RNGeofenceHelper.isAppOnForeground(context)) {
                Log.i(MODULE_NAME, "App in the foreground");
                Intent transitionIntent = new Intent(GeofenceConstants.LOCAL_GEOFENCE_EVENT);
                transitionIntent.putExtra(GeofenceConstants.LOCAL_GEOFENCE_DATA, geofenceDataObject);
                LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(transitionIntent);
            } else {
                // TODO background event
                Log.i(MODULE_NAME, "App not in the foreground");
            }
        } else {
            // Log the error.
            Log.e(MODULE_NAME, "Geofence transition error: invalid transition type");
        }

    }


}
