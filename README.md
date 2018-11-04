
# react-native-geofencing

react-native-geofencing uses iOS's CoreLocation and Android's location services to create and monitor circular geofences.

## Installation

```shell
npm install react-native-geofencing --save
```

or using yarn:

```shell
yarn add react-native-geofencing
```


### Mostly automatic installation

`$ react-native link react-native-geofencing`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-geofencing` and add `RNGeofence.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNGeofence.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNGeofencePackage;` to the imports at the top of the file
  - Add `new RNGeofencePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-geofencing'
  	project(':react-native-geofencing').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-geofencing/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-geofencing')
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


## Usage
```js
import RNGeofence, { Constants } from 'react-native-geofence';


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

`RNGeofence.requestPermission()` must be called later if permission is not requested during init

---

### requestPermission()

Requests permission from the user.

```js
RNGeofence.requestPermission();
```

---

### checkPermission()

Checks and returns a `true` if location permissions have been granted by the user. Else `false` is returned.

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

Clears all geofences. Available only on iOS. The promise resolves with a number. 

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