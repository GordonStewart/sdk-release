//
//  TuneProximityHelper.m
//  TuneMarketingConsoleSDK
//
//  Created by Gordon Stewart on 8/4/16.
//  Copyright © 2016 Tune. All rights reserved.
//

#import "TuneProximityHelper.h"
#import "TuneUtils.h"

static SmartWhere* _smartWhere;
static TuneProximityHelper* tuneSharedProximityHelper;

@implementation TuneProximityHelper

- (TuneProximityHelper*) init{
    self = [super init];
    tuneSharedProximityHelper = self;
    return self;
}

- (void)startMonitoringWithTuneAdvertiserId:(NSString *)aid tuneConversionKey:(NSString *)key{
    if ([TuneProximityHelper isProximityEnabled]){
        NSMutableDictionary *config = [NSMutableDictionary new];
        
        config[@"ENABLE_NOTIFICATION_PERMISSION_PROMPTING"] = @"false";
        config[@"ENABLE_LOCATION_PERMISSION_PROMPTING"] = @"false";
        config[@"ENABLE_GEOFENCE_RANGING"] = @"true";
        config[@"DELEGATE_NOTIFICATIONS"] = @"true";
        if ([[TuneManager currentManager].configuration.debugMode boolValue]){
            config[@"DEBUG_LOGGING"] = @"true";
        }
        
        [self startProximityMonitoringWithAppId:aid withApiKey:aid withApiSecret:key withConfig:config];
    }
}

+(BOOL) isProximityEnabled{
    if ([TuneManager currentManager].configuration.shouldAutoCollectDeviceLocation) {
        Class exists = [TuneUtils getClassFromString:@"SmartWhere"];
        if (exists){
            return YES;
        }
    }
    return NO;
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
    [invocation setTarget:[classProximity new]];
    [invocation setSelector:selInitWithAppId];
    [invocation setArgument:&appId atIndex:2];
    [invocation setArgument:&apiKey atIndex:3];
    [invocation setArgument:&apiSecret atIndex:4];
    [invocation setArgument:&config atIndex:5];
    [invocation invoke];
    [invocation getReturnValue:&_smartWhere];
    
    [_smartWhere performSelector:@selector(setDelegate:) withObject:self];
#pragma clang diagnostic pop
}

#pragma mark - handle smartWhere location events

- (void)smartWhere:(SmartWhere *)smartwhere didReceiveLocalNotification:(ProximityNotification *)notification{
    // Handle notification here.  e.g. put up a dialog or add to a list.  The notification is used to fire events like interstitials,
    // custom events as deep links.  etc...
    // Use [_smartwhere fireLocalNotificationAction: notification] to execute the event
    
    ProximityAction* action = notification.action;
    NSString * message = [NSString stringWithFormat:@"didReceiveLocalNotification: %ld %@ withProperties: %@ triggeredBy: %ld",(long) action.actionType, action.values, notification.eventProperties, (long)notification.triggerType];
    NSLog(@"%@", message);;
}

- (void)smartWhere:(SmartWhere *)smartwhere didReceiveCustomBeaconAction:(ProximityAction*)action withBeaconProperties:(NSDictionary*) beaconProperties triggeredBy:(ProximityTriggerType) trigger{
    NSString * message = [NSString stringWithFormat:@"didReceiveCustomBeaconAction: %ld %@ withBeaconProperties: %@ triggeredBy: %ld",(long) action.actionType, action.values, beaconProperties, (long)trigger];
    NSLog(@"%@", message);;
}

- (void)smartWhere:(SmartWhere *)smartwhere didReceiveCustomFenceAction:(ProximityAction*)action withFenceProperties:(NSDictionary*) fenceProperties triggeredBy:(ProximityTriggerType) trigger{
    NSString * message = [NSString stringWithFormat:@"didReceiveCustomFenceAction: %ld %@ withFenceProperties: %@ triggeredBy: %ld",(long) action.actionType, action.values, fenceProperties, (long)trigger];
    NSLog(@"%@", message);;
}

- (void)smartWhere:(SmartWhere *)smartwhere didReceiveCommunicationError:(NSError *)error{
    NSString * message = [NSString stringWithFormat:@"didReceiveCommunicationError: %@ %ld", error.domain, (long)error.code];
    NSLog(@"%@", message);
}

@end
