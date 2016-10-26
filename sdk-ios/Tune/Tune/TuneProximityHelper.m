//
//  TuneProximityHelper.m
//  TuneMarketingConsoleSDK
//
//  Created by Gordon Stewart on 8/4/16.
//  Copyright © 2016 Tune. All rights reserved.
//

#import "TuneProximityHelper.h"
#import "TuneUtils.h"

static id _smartWhere;
static TuneProximityHelper* tuneSharedProximityHelper = nil;
static dispatch_once_t proximityHelperToken;

@implementation TuneProximityHelper

+ (TuneProximityHelper*) getInstance{
    dispatch_once(&proximityHelperToken, ^{
        tuneSharedProximityHelper = [[TuneProximityHelper alloc] init];
    });
    return tuneSharedProximityHelper;
}

- (void)startMonitoringWithTuneAdvertiserId:(NSString *)aid tuneConversionKey:(NSString *)key{
    @synchronized(self) {
        if (_smartWhere == nil){
            _aid = aid;
            _key = key;
            [self performSelectorOnMainThread:@selector(startMonitoring) withObject:nil waitUntilDone:YES];
        }
    }
}

-(void) startMonitoring{
    NSMutableDictionary *config = [NSMutableDictionary new];
    
    config[@"ENABLE_NOTIFICATION_PERMISSION_PROMPTING"] = @"false";
    config[@"ENABLE_LOCATION_PERMISSION_PROMPTING"] = @"false";
    config[@"ENABLE_GEOFENCE_RANGING"] = @"true";
    config[@"DELEGATE_NOTIFICATIONS"] = @"true";
    if ([[TuneManager currentManager].configuration.debugMode boolValue]){
        config[@"DEBUG_LOGGING"] = @"true";
    }
    [self startProximityMonitoringWithAppId:_aid withApiKey:_aid withApiSecret:_key withConfig:config];
}

-(void) stopMonitoring{
    @synchronized(self) {
        if (_smartWhere){
            [_smartWhere invalidate];
            _smartWhere = nil;
        }
    }
}

-(void) setDebugMode:(BOOL) mode{
    @synchronized(self) {
        if (_smartWhere){
            NSMutableDictionary *config = [NSMutableDictionary new];
            config[@"DEBUG_LOGGING"] = (mode) ? @"true" : @"false";
            [self setConfig:config];
        }
    }
}

+(BOOL) isProximityInstalled{
    return ([TuneUtils getClassFromString:@"SmartWhere"] != nil);
}

#pragma mark - SmartWhere methods

-(void) startProximityMonitoringWithAppId: (NSString*) appId
                               withApiKey: (NSString*) apiKey
                            withApiSecret: (NSString*) apiSecret
                               withConfig: (NSDictionary*) config{
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wundeclared-selector"
    SEL selInitWithAppId = @selector(initWithAppId:apiKey:apiSecret:withConfig:);
    Class classProximity = NSClassFromString(@"SmartWhere");
    
    NSMethodSignature* signature = [classProximity instanceMethodSignatureForSelector:selInitWithAppId];
    NSInvocation* invocation = [NSInvocation invocationWithMethodSignature:signature];
    [invocation retainArguments];
    [invocation setTarget:[classProximity alloc]];
    [invocation setSelector:selInitWithAppId];
    [invocation setArgument:&appId atIndex:2];
    [invocation setArgument:&apiKey atIndex:3];
    [invocation setArgument:&apiSecret atIndex:4];
    [invocation setArgument:&config atIndex:5];
    [invocation invoke];
    
    id __unsafe_unretained tempResultSet;
    [invocation getReturnValue:&tempResultSet];
    _smartWhere = tempResultSet;
    
    [_smartWhere performSelector:@selector(setDelegate:) withObject:self];
#pragma clang diagnostic pop
}

-(void) setConfig:(NSDictionary*) config{
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wundeclared-selector"
    SEL selSetConfig = @selector(configWithDictionary:);
    Class classProximity = NSClassFromString(@"SmartWhere");
    
    NSMethodSignature* signature = [classProximity methodSignatureForSelector:selSetConfig];
    NSInvocation* invocation = [NSInvocation invocationWithMethodSignature:signature];
    [invocation retainArguments];
    [invocation setTarget:classProximity];
    [invocation setSelector:selSetConfig];
    [invocation setArgument:&config atIndex:2];
    [invocation invoke];
#pragma clang diagnostic pop

}

#pragma mark - handle smartWhere location events

- (void)smartWhere:(id)smartwhere didReceiveLocalNotification:(ProximityNotification *)notification{
    // Handle notification here.  e.g. put up a dialog or add to a list.  The notification is used to fire events like interstitials,
    // custom events as deep links.  etc...
    // Use [_smartwhere fireLocalNotificationAction: notification] to execute the event
    
    ProximityAction* action = notification.action;
    NSString * message = [NSString stringWithFormat:@"didReceiveLocalNotification: %ld %@ withProperties: %@ triggeredBy: %ld",(long) action.actionType, action.values, notification.eventProperties, (long)notification.triggerType];
    NSLog(@"%@", message);;
}

- (void)smartWhere:(id)smartwhere didReceiveCustomBeaconAction:(ProximityAction*)action withBeaconProperties:(NSDictionary*) beaconProperties triggeredBy:(ProximityTriggerType) trigger{
    NSString * message = [NSString stringWithFormat:@"didReceiveCustomBeaconAction: %ld %@ withBeaconProperties: %@ triggeredBy: %ld",(long) action.actionType, action.values, beaconProperties, (long)trigger];
    NSLog(@"%@", message);;
}

- (void)smartWhere:(id)smartwhere didReceiveCustomFenceAction:(ProximityAction*)action withFenceProperties:(NSDictionary*) fenceProperties triggeredBy:(ProximityTriggerType) trigger{
    NSString * message = [NSString stringWithFormat:@"didReceiveCustomFenceAction: %ld %@ withFenceProperties: %@ triggeredBy: %ld",(long) action.actionType, action.values, fenceProperties, (long)trigger];
    NSLog(@"%@", message);;
}

- (void)smartWhere:(id)smartwhere didReceiveCommunicationError:(NSError *)error{
    NSString * message = [NSString stringWithFormat:@"didReceiveCommunicationError: %@ %ld", error.domain, (long)error.code];
    NSLog(@"%@", message);
}


#pragma mark - getters and setters for test
-(void) setSmartWhere:(id) smartWhere{
    _smartWhere = smartWhere;
}

-(id) getSmartWhere{
    return _smartWhere;
}

+(void) invalidateForTesting{
    _smartWhere = nil;
    tuneSharedProximityHelper = nil;
    proximityHelperToken = 0;
}

@end
