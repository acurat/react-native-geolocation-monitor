package com.acurat.geofence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.acurat.geofence.RNGeofenceConstants.MODULE_NAME;
import static com.acurat.geofence.RNGeofenceConstants.REQUEST_PERMISSIONS_REQUEST_CODE;
import static com.acurat.geofence.RNGeofenceConstants.UNKNOWN_ERROR;

public class RNGeofenceModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private final ConnectivityBroadcastReceiver mMessageReceiver;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;

    public RNGeofenceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mGeofencingClient = LocationServices.getGeofencingClient(reactContext);
        mMessageReceiver = new ConnectivityBroadcastReceiver();
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        final Map<String, String> types = new HashMap<>();
        types.put(GeofenceConstants.ENTER, GeofenceConstants.ENTER);
        types.put(GeofenceConstants.EXIT, GeofenceConstants.EXIT);
        constants.put("TRANSITION_TYPES", types);
        return constants;
    }

    @ReactMethod
    public void initialize(final ReadableMap readableMap) {
        registerReceiver();
        if (readableMap.hasKey("requestPermission") && readableMap.getBoolean("requestPermission")) {
            getUserPermission();
        }
    }

    @ReactMethod
    public void requestPermission() {
        Log.i(MODULE_NAME, "Requesting permission");
        getUserPermission();
    }

    @ReactMethod
    public void checkPermission(final Promise promise) {
        Log.i(MODULE_NAME, "Checking permission");
        promise.resolve(checkPermission() == PackageManager.PERMISSION_GRANTED);
    }

    private int checkPermission() {
        return ActivityCompat.checkSelfPermission(this.getCurrentActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void getUserPermission() {
        ActivityCompat.requestPermissions(this.getCurrentActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    @ReactMethod
    public void add(final ReadableMap readableMap, final Promise promise) {

        final Geofence geofence = createGeofence(readableMap);
        final List<Geofence> geofences = new ArrayList<>();
        geofences.add(geofence);

        final WritableArray geofenceRequestIds = Arguments.createArray();
        for (Geofence g : geofences) {
            geofenceRequestIds.pushString(g.getRequestId());
        }

        GeofencingRequest geofencingRequest = createGeofenceRequest(geofences);

        addGeofence(promise, geofencingRequest, false);
    }

    @ReactMethod
    public void addAll(final ReadableArray readableArray, final Promise promise) {
        final List<Geofence> geofences = createGeofences(readableArray);
        final WritableArray geofenceRequestIds = Arguments.createArray();
        for (Geofence g : geofences) {
            geofenceRequestIds.pushString(g.getRequestId());
        }

        GeofencingRequest geofencingRequest = createGeofenceRequest(geofences);

        addGeofence(promise, geofencingRequest, true);
    }

    private List<Geofence> createGeofences(final ReadableArray readableArray) {
        List<Geofence> geofences = new ArrayList<>();
        for (int i = 0; i < readableArray.size(); ++i) {
            geofences.add(createGeofence(readableArray.getMap(i)));
        }
        return geofences;
    }


    private Geofence createGeofence(final ReadableMap readableMap) {
        GeofenceOptions options = GeofenceOptions.fromReactMap(readableMap);
        return new Geofence.Builder()
                .setRequestId(options.id)
                .setCircularRegion(options.latitude,
                        options.longitude,
                        options.radius)
                .setTransitionTypes(options.transitionTypes)
                .setLoiteringDelay(options.loiteringDelay)
                .setExpirationDuration(options.expirationDuration)
                .build();

    }

    private GeofencingRequest createGeofenceRequest(final List<Geofence> geofences) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(getReactApplicationContext(), GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(getReactApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @SuppressLint("MissingPermission")
    private void addGeofence(final Promise promise, final GeofencingRequest geofencingRequest, final Boolean multiple) {

        List<String> ids = new ArrayList<>();
        for (Geofence geofence : geofencingRequest.getGeofences()) {
            ids.add(geofence.getRequestId());
        }
        mGeofencingClient.addGeofences(
                geofencingRequest,
                getGeofencePendingIntent()
        )
                .addOnSuccessListener(success(ids, promise, multiple))
                .addOnFailureListener(failure(promise));
    }

    @ReactMethod
    public void remove(final String locationId, final Promise promise) {
        List<String> ids = new ArrayList<>(Arrays.asList(locationId));
        mGeofencingClient.removeGeofences(ids).addOnSuccessListener(success(ids, promise, false));
    }

    @ReactMethod
    public void removeAll(final ReadableArray locationIds, final Promise promise) {
        List<String> ids = new ArrayList<>();
        for (Object id : locationIds.toArrayList()) {
            ids.add((String) id);
        }
        mGeofencingClient.removeGeofences(ids).addOnSuccessListener(success(ids, promise, true));
    }

    @ReactMethod
    public void clear(final Promise promise) {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        promise.resolve(Arguments.createMap());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                promise.reject(MODULE_NAME, "Could not remove all locations", e);
            }
        });
    }

    private OnSuccessListener<Void> success(final List<String> ids, final Promise promise, final Boolean multiple) {
        return new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.i(MODULE_NAME, "Successfully added or removed locations: " + ids.toString());
                if (multiple) {
                    promise.resolve(convertListToWriteableArray(ids));
                } else {
                    promise.resolve(ids.get(0));
                }
            }
        };
    }

    private OnFailureListener failure(final Promise promise) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(MODULE_NAME, e.getLocalizedMessage());
                if (e instanceof ApiException) {
                    promise.reject(MODULE_NAME, GeofenceStatusCodes.getStatusCodeString(((ApiException) e).getStatusCode()), e);
                } else {
                    promise.reject(MODULE_NAME, UNKNOWN_ERROR, e);
                }

            }
        };
    }

    private WritableArray convertListToWriteableArray(List<String> stringList) {
        WritableArray writableArray = new WritableNativeArray();
        for (String element : stringList) {
            writableArray.pushString(element);
        }
        return writableArray;
    }

    @Override
    public void onHostResume() {
        Log.d(MODULE_NAME, "onHostResume()");
        registerReceiver();
    }

    @Override
    public void onHostPause() {
        Log.d(MODULE_NAME, "onHostPause()");
        unregisterReceiver();
    }

    @Override
    public void onHostDestroy() {
    }

    private void registerReceiver() {
        mMessageReceiver.setRegistered(true);
        LocalBroadcastManager.getInstance(this.getReactApplicationContext())
                .registerReceiver(mMessageReceiver, new IntentFilter(GeofenceConstants.LOCAL_GEOFENCE_EVENT));
    }

    private void unregisterReceiver() {
        if (mMessageReceiver.isRegistered()) {
            getReactApplicationContext().unregisterReceiver(mMessageReceiver);
            mMessageReceiver.setRegistered(false);
        }
    }

    private static class GeofenceOptions {
        private final String id;
        private final Double longitude;
        private final Double latitude;
        private final float radius;
        private final int transitionTypes;
        private final int loiteringDelay;
        private final long expirationDuration;

        private GeofenceOptions(
                String id,
                Double longitude,
                Double latitude,
                float radius,
                int transitionTypes,
                int loiteringDelay,
                long expirationDuration) {
            this.id = id;
            this.longitude = longitude;
            this.latitude = latitude;
            this.radius = radius;
            this.transitionTypes = transitionTypes;
            this.loiteringDelay = loiteringDelay;
            this.expirationDuration = expirationDuration;
        }

        private static GeofenceOptions fromReactMap(ReadableMap readableMap) {
            String id = readableMap.getString("id");
            Double longitude = readableMap.getDouble("longitude");
            Double latitude = readableMap.getDouble("latitude");
            float radius = readableMap.hasKey("radius") ?
                    (float) readableMap.getDouble("radius") :
                    GeofenceConstants.DEFAULT_RADIUS;
            int transitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER
                    | Geofence.GEOFENCE_TRANSITION_EXIT;
            int loiteringDelay = 10;
            long expirationDuration = readableMap.hasKey("expirationDuration") ?
                    Long.valueOf(readableMap.getInt("expirationDuration")) :
                    Geofence.NEVER_EXPIRE;

            return new GeofenceOptions(id, longitude, latitude, radius, transitionTypes, loiteringDelay, expirationDuration);
        }
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {

        private boolean isRegistered = false;

        public boolean isRegistered() {
            return isRegistered;
        }

        public void setRegistered(boolean registered) {
            isRegistered = registered;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            GeofenceDataObject dataObject = (GeofenceDataObject) intent.getSerializableExtra(GeofenceConstants.LOCAL_GEOFENCE_DATA);
            Log.d(GeofenceConstants.MODULE_NAME, "Got data: " + dataObject);
            getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(GeofenceConstants.TRANSITION, RNGeofenceHelper.convertDataObjectToRNMap(dataObject));
        }
    }

}