// import RCTBridgeModule
#if __has_include(<React/RCTBridgeModule.h>)
#import <React/RCTBridgeModule.h>
#elif __has_include(“RCTBridgeModule.h”)
#import “RCTBridgeModule.h”
#else
#import “React/RCTBridgeModule.h”
#endif

// import RCTEventEmitter
#if __has_include(<React/RCTEventEmitter.h>)
#import <React/RCTEventEmitter.h>
#elif __has_include(“RCTEventEmitter.h”)
#import “RCTEventEmitter.h”
#else
#import “React/RCTEventEmitter.h”
#endif

#import <CoreLocation/CoreLocation.h>

@interface RNGeofence : RCTEventEmitter <RCTBridgeModule, CLLocationManagerDelegate>
@property (strong, nonatomic) CLLocationManager *locationManager;
@end
