//
//  TuneProximityHelper.h
//  TuneMarketingConsoleSDK
//
//  Created by Gordon Stewart on 8/4/16.
//  Copyright © 2016 Tune. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol SmartWhereDelegate
@end

@interface TuneProximityHelper : NSObject <SmartWhereDelegate>
+(BOOL) isProximityInstalled;
-(void) startMonitoringWithTuneAdvertiserId:(NSString *)aid tuneConversionKey:(NSString *)key;
-(void) stopMonitoring;
+(TuneProximityHelper*) getInstance;
@end

#ifndef EventActionType_Defined
#define EventActionType_Defined

typedef enum EventActionType : NSInteger {
    EventActionTypeUnknown = -1,
    EventActionTypeUrl = 0,
    EventActionTypeUri = 1,
    EventActionTypeCall = 2,
    EventActionTypeSMS = 3,
    EventActionTypeEmail = 5,
    EventActionTypeMarket = 9,
    EventActionTypeCoupon = 12,
    EventActionTypeTwitter = 13,
    EventActionTypeYoutube = 14,
    EventActionTypeHTML = 16,
    EventActionTypeNewAction = 126,
    EventActionTypeCustom = 127
} EventActionType;

typedef enum ProximityTriggerType : NSInteger {
    swNfcTap = 0,
    swQRScan = 1,
    swBleEnter = 10,
    swBleHover = 11,
    swBleDwell = 12,
    swBleExit = 13,
    swGeoFenceEnter = 20,
    swGeoFenceDwell = 21,
    swGeoFenceExit = 22,
} ProximityTriggerType;

#endif

@interface ProximityAction : NSObject<NSCoding>
@property (nonatomic) EventActionType actionType;
@property (nonatomic) NSDictionary * values;
@end

@interface ProximityNotification : NSObject
@property (nonatomic, copy) NSString * title;
@property (nonatomic, copy) NSString * message;
@property (nonatomic) ProximityAction * action;
@property (nonatomic) NSDictionary* proximityObjectProperties;
@property (nonatomic) NSDictionary* eventProperties;
@property (nonatomic) ProximityTriggerType triggerType;
@end







