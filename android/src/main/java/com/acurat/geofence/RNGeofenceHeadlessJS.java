package com.acurat.geofence;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

public class RNGeofenceHeadlessJS extends HeadlessJsTaskService {

    @Override
    protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            GeofenceDataObject dataObject = (GeofenceDataObject) extras.getSerializable(GeofenceConstants.LOCAL_GEOFENCE_DATA);
            return new HeadlessJsTaskConfig(
                    "RNGeofence",
                    GeofenceHelper.convertDataObjectToRNMap(dataObject),
                    10000
            );
        }
        return null;
    }
}