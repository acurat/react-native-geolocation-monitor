
# react-native-geolocation-monitor

react-native-geolocation-monitor uses iOS's CoreLocation and Android's location services to create and monitor circular geofences.

## Installation

```shell
npm install react-native-geolocation-monitor --save
```

or using yarn:

```shell
yarn add react-native-geolocation-monitor
```


### Mostly automatic installation

`$ react-native link react-native-geolocation-monitor`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-geolocation-monitor` and add `RNGeofence.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNGeofence.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNGeofencePackage;` to the imports at the top of the file
  - Add `new RNGeofencePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-geolocation-monitor'
  	project(':react-native-geolocation-monitor').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-geolocation-monitor/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-geolocation-monitor')
  	```

### Post Installation

#### Android

In your `AndroidManifest.xml` add the following

```
    ...
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

    <receiver
        android:name="com.acurat.geofence.RNGeofenceBroadcastReceiver"
        android:enabled="true"
        android:exported="true" />

    ...
```

#### iOS


Add the `NSLocationWhenInUseUsageDescription` key and the `NSLocationAlwaysAndWhenInUseUsageDescription` key 
to your Info.plist file. 
(Xcode displays these keys as "Privacy - Location When In Use Usage Description" and 
"Privacy - Location Always and When In Use Usage Description" in the Info.plist editor.)

If your app supports iOS 10 and earlier, add the `NSLocationAlwaysUsageDescription` key to your Info.plist file.
 (Xcode displays this key as "Privacy - Location Always Usage Description" in the Info.plist editor.)

> If you need updates when the app is in background mode, add the following to your Info.plist 
```
	<key>UIBackgroundModes</key>
	<array>
	    <string>location</string>
	</array>
```

## Usage
```js
import React, { Component } from 'react';
import { Text, View } from 'react-native';

import Geofence, { Constants } from 'react-native-geolocation-monitor';

export default class App extends Component {
  subscription;

  componentDidMount() {
    Geofence.initialize({ requestPermission: true });

    Geofence.add({
      id: 'work',
      radius: 50,
      latitude: 37.422611,
      longitude: -122.0840577,
    })
      .then(result => console.warn(result))
      .catch(error => console.warn(JSON.stringify(error)));

    this.subscription = Geofence.notify(result => {
      console.warn(result);
    });
  }

  componentWillUnmount() {
    if (this.subscription) {
      this.subscription.remove();
    }
  }

  render() {
    return (
      <View
        style={{
          flex: 1,
          justifyContent: 'center',
          alignItems: 'center',
        }}
      >
        <Text>Basic Usage!</Text>
      </View>
    );
  }
}
```

## API

### initialize()

Initializes and requests permission from the user.

```js

const params = {
          requestPermission: true //Or false
      };
RNGeofence.initialize(params);
```

`RNGeofence.requestPermission()` must be called later if permission is not requested during initialization

---

### requestPermission()

Requests permission from the user.

```js
RNGeofence.requestPermission();
```

---

### checkPermission()

Checks and returns `true` if location permissions have been granted by the user. Else `false` is returned.

```js
RNGeofence.checkPermission().then(result => console.info(result));

// result is a boolean 
```

---

### add()

Adds a circular region for monitoring. The promise resolves with an array of strings. 

```js

const region = {
    id: 'work', // Unique identifier for the region, this is returned when a geofence transition occurs.
    longitude: '-90.4436994',
    latitude: '38.5419558',
    radius: 100, // Optional, default: 50 meters
    expirationDuration:  600000 // Optional in milliseconds, Android only, default: Never expires
};

RNGeofence.add(region).then(result => console.info(result));


// result is an String[] of one item containing Id
```

---
 
### addAll()

Adds multiple regions at once for monitoring. The promise resolves with an array of strings. 

```js

const regions = [{
    id: 'work', 
    longitude: '-90.4436994',
    latitude: '38.5419558',
    radius: 100, 
    expirationDuration:  ''
}, {
       id: 'disneyWorld',
       longitude: '-81.5660627',
       latitude: '28.385233',
   }];

RNGeofence.addAll(regions).then(result => console.info(result));


// result is an String[] containing Ids of successfully added regions
```

---

### remove()

Remove a region from monitoring. The promise resolves with an array of strings. 

```js

RNGeofence.remove('work').then(result => console.info(result));


// result is an String[] containing Ids of successfully added regions
```

---

### removeAll()

Removes multiple regions at once. The promise resolves with an array of strings. 

```js

RNGeofence.remove(['work', 'home']).then(result => console.info(result));


// result is an String[] containing Ids of successfully added regions
```
---

### clear()

Clears all geofences. This returns a promise that resolves with a void. 

```js

RNGeofence.clear();

```
---

### count()

Counts all current geofences. Available only on iOS. The promise resolves with a number. 

Android returns -1

```js

RNGeofence.count()
    .then(value => console.warn("# of regions currently monitored", value));


// value is a number
```
---

### notify()

Provide a callback to notify that is called when a geofence transition occurs. 

```js

const subscription = Geofences.notify((response) => console.log(JSON.stringify(response)));

/*
    response contains
    {
        transitionType: 'ENTER' | 'EXIT',
        ids: string[] // Contains region ids that triggered transition
        
    }
 */

// Be sure to `subscription.remove()` to avoid memory leaks (usually in `componentWillUnmount()`)
```
---
