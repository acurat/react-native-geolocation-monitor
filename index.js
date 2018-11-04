import {NativeModules, NativeEventEmitter, Platform} from 'react-native';

const {RNGeofence} = NativeModules;

const TAG = 'RNGeofence: ';

const IS_IOS = Platform.OS === 'ios';

export const Constants = {
    TRANSITION_TYPES: RNGeofence.TRANSITION_TYPES,
};

class Geofence {

    nativeEventEmitter;

    getNativeEmitter() {
        if (!this.nativeEventEmitter) {
            this.nativeEventEmitter = new NativeEventEmitter(RNGeofence);
        }
        return this.nativeEventEmitter;
    }

    initialize = (params) => {
        RNGeofence.initialize(params);
    };

    checkPermission = () => RNGeofence.checkPermission();

    requestPermission = () => RNGeofence.requestPermission();

    add = (location) => RNGeofence.add(location);

    addAll = (locations) => RNGeofence.addAll(locations);

    remove = (id) => RNGeofence.remove(id);

    removeAll = (ids) => RNGeofence.removeAll(ids);

    clear = () => RNGeofence.clear();

    count = () => IS_IOS ? RNGeofence.count() : Promise.resolve(-1);

    notify = (callback) => {
        if (!(callback instanceof Function)) {
            throw new Error(TAG + 'Callback should be a function');
        }
        return this.getNativeEmitter().addListener(
            'onTransition',
            (event) => callback(event)
        );
    }

}

export default new Geofence();

