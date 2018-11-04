#import "RNGeofence.h"

// import RCTBridge
#if __has_include(<React/RCTBridge.h>)
#import <React/RCTBridge.h>
#elif __has_include(“RCTBridge.h”)
#import “RCTBridge.h”
#else
#import “React/RCTBridge.h”
#endif

// import RCTEventDispatcher
#if __has_include(<React/RCTEventDispatcher.h>)
#import <React/RCTEventDispatcher.h>
#elif __has_include(“RCTEventDispatcher.h”)
#import “RCTEventDispatcher.h”
#else
#import “React/RCTEventDispatcher.h”
#endif

#import <React/RCTLog.h>

typedef struct {
  BOOL skipPermissionRequests;
} ConfigOptions;

@implementation RNGeofence

@synthesize bridge = _bridge;

static NSString *const NAME = @"RNGeofence";
static NSString *const ENTER = @"ENTER";
static NSString *const EXIT = @"EXIT";

static NSString *const TRANSITION = @"onTransition";

RCT_EXPORT_MODULE()

- (dispatch_queue_t)methodQueue
{
  return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

- (NSDictionary *)constantsToExport
{
  return @{
           @"TRANSITION_TYPES": @{
               ENTER: ENTER,
               EXIT: EXIT
           }
          };
}

- (NSArray<NSString *> *)supportedEvents
{
  return @[TRANSITION];
}

RCT_EXPORT_METHOD(initialize:(NSDictionary *)params)
{
  checkPermissionConfig();
  if (!self.locationManager) {
    self.locationManager = [CLLocationManager new];
    self.locationManager.delegate = self;
    self.locationManager.allowsBackgroundLocationUpdates = YES;
    self.locationManager.pausesLocationUpdatesAutomatically = false;
    self.locationManager.desiredAccuracy = kCLLocationAccuracyBestForNavigation;
  }
  
  BOOL requestPermission = params[@"requestPermission"];
  
  if(requestPermission == YES) {
    [self getUserPermission];
  }
}

RCT_EXPORT_METHOD(requestPermission)
{
  [self getUserPermission];
}

RCT_EXPORT_METHOD(checkPermission:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  if([CLLocationManager locationServicesEnabled]) {
    CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
    if (status == kCLAuthorizationStatusAuthorizedAlways || status == kCLAuthorizationStatusAuthorizedWhenInUse)
    {
      resolve(@YES);
    } else {
      resolve(@NO);
    }
  } else {
    resolve(@NO);
  }
}

RCT_EXPORT_METHOD(add:(NSDictionary*)location
                  addLocationWithResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  checkPermissionConfig();

  CLLocationCoordinate2D coord =
  CLLocationCoordinate2DMake([location[@"latitude"] doubleValue], [location[@"longitude"] doubleValue]);
  
  if(!CLLocationCoordinate2DIsValid(coord)) {
    NSError *error = [NSError errorWithDomain:NAME code:1 userInfo:@{@"Error": @"Not a valid coordinate"}];
    reject(NAME, @"Not a valid coordinate", error);
  }
  
  CLLocationDistance radius = [location[@"radius"] doubleValue];
  if (radius > self.locationManager.maximumRegionMonitoringDistance) {
    radius = self.locationManager.maximumRegionMonitoringDistance;
  }
  
  CLRegion *geoRegion = [[CLCircularRegion alloc]initWithCenter:coord radius:radius identifier:location[@"id"]];
  
  [self.locationManager startMonitoringForRegion:geoRegion];
    
  resolve(location[@"id"]);
}

RCT_EXPORT_METHOD(addAll:(NSArray*)locations
                  addLocationsWithResolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  checkPermissionConfig();
  NSMutableArray * regionArray = [[NSMutableArray alloc] init];
  NSMutableArray * idArray = [[NSMutableArray alloc] init];

  NSError *error = nil;
  for (NSDictionary* location in locations) {
    CLLocationCoordinate2D coord =
    CLLocationCoordinate2DMake([location[@"latitude"] doubleValue], [location[@"longitude"] doubleValue]);
    
    if(!CLLocationCoordinate2DIsValid(coord)) {
      error = [NSError errorWithDomain:NAME code:1 userInfo:@{@"Error": @"Not a valid coordinate"}];
      break;
    }
  
    CLLocationDistance radius = [location[@"radius"] doubleValue];
    if (radius > self.locationManager.maximumRegionMonitoringDistance) {
      radius = self.locationManager.maximumRegionMonitoringDistance;
    }
    
    CLRegion *geoRegion = [[CLCircularRegion alloc]initWithCenter:coord radius:radius identifier:location[@"id"]];
    
    [regionArray addObject:geoRegion];
    [idArray addObject:location[@"id"]];
  }
  
  if(!error) {
    for(CLRegion *region in regionArray) {
      [self.locationManager startMonitoringForRegion:region];
    }
    resolve(idArray);
  } else {
    reject(NAME, @"Not a valid coordinate", error);
  }
}

RCT_EXPORT_METHOD(remove:(NSString *)locationId removeLocationWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  @try {
    [self delete:locationId];
    resolve(locationId);
  }
  @catch (NSException *exception) {
    NSError *error = [NSError errorWithDomain:NAME code:2 userInfo:@{@"Error": [exception reason]}];
    reject(NAME, @"Could not remove location", error);
  }
}

RCT_EXPORT_METHOD(removeAll:(NSArray *)locationIds removeLocationsWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  @try {
    for (NSString* locId in locationIds) {
      [self delete:locId];
    }
    resolve(locationIds);
  }
  @catch (NSException *exception) {
    NSError *error = [NSError errorWithDomain:NAME code:2 userInfo:@{@"Error": [exception reason]}];
    reject(NAME, @"Could not remove locations", error);
  }
}

RCT_EXPORT_METHOD(clear:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  @try {
    [self deleteAll];
    resolve(@{});
  }
  @catch (NSException *exception) {
    NSError *error = [NSError errorWithDomain:NAME code:2 userInfo:@{@"Error": [exception reason]}];
    reject(NAME, @"Could not remove all locations", error);
  }
}


RCT_EXPORT_METHOD(count: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  NSInteger count = [[self.locationManager monitoredRegions] count];
  NSLog([NAME stringByAppendingString: @" : no of locations monitored : %ld"], (long) count);
  resolve([NSNumber numberWithInteger:count]);
}

#pragma mark - Private methods

-(void) getUserPermission
{
  [self.locationManager requestAlwaysAuthorization];
}

- (void) emitMessageToRN: (NSString *)eventName :(NSDictionary *)params {
  [self sendEventWithName: eventName body: params];
}

- (void) delete:(NSString *)locationId
{
  for(CLRegion *region in [self.locationManager monitoredRegions]){
    if ([region.identifier isEqualToString:locationId]) {
      [self.locationManager stopMonitoringForRegion:region];
    }
  }
}

- (void) deleteAll
{
  for(CLRegion *region in [self.locationManager monitoredRegions]){
    [self.locationManager stopMonitoringForRegion:region];
  }
}

- (void)locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region
{
  NSLog(@"Entered region : %@", region);
  NSArray *ids = @[region.identifier];
  [self emitMessageToRN:TRANSITION :@{
                                        @"ids": ids,
                                        @"transitionType": ENTER
                                    }];
}

- (void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region
{
  NSLog(@"Exited region : %@", region);
  NSArray *ids = @[region.identifier];
  [self emitMessageToRN:TRANSITION :@{
                                        @"ids": ids,
                                        @"transitionType": EXIT
                                      }];
}

static void checkPermissionConfig()
{
#if RCT_DEV
  if (!([[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSLocationWhenInUseUsageDescription"] ||
        [[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSLocationAlwaysUsageDescription"] ||
        [[NSBundle mainBundle] objectForInfoDictionaryKey:@"NSLocationAlwaysAndWhenInUseUsageDescription"])) {
    RCTLogError(@"Either NSLocationWhenInUseUsageDescription or NSLocationAlwaysUsageDescription or NSLocationAlwaysAndWhenInUseUsageDescription key must be present in Info.plist to use geofencing.");
  }
#endif
}

@end
