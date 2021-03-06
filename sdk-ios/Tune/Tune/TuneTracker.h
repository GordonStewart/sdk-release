//
//  TuneTracker.h
//  Tune
//
//  Created by John Bender on 2/28/14.
//  Copyright (c) 2014 Tune. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@class TuneEvent;
@class TuneRegionMonitor;

FOUNDATION_EXPORT const NSTimeInterval TUNE_SESSION_QUEUING_DELAY;


@protocol TuneDelegate;
@protocol TuneTrackerDelegate;

@interface TuneTracker : NSObject

@property (nonatomic, assign) id <TuneDelegate> delegate;
@property (nonatomic, assign) id <TuneTrackerDelegate> trackerDelegate;

@property (nonatomic, assign) BOOL shouldUseCookieTracking;
@property (nonatomic, assign) BOOL fbLogging;
@property (nonatomic, assign) BOOL fbLimitUsage;

@property (nonatomic, readonly) TuneRegionMonitor *regionMonitor;

#if TESTING
@property (nonatomic, assign) BOOL allowDuplicateRequests;
#endif

- (void)startTracker;

- (void)applicationDidOpenURL:(NSString *)urlString sourceApplication:(NSString *)sourceApplication;

- (void)measureEvent:(TuneEvent *)event;

- (void)setMeasurement:(NSString*)targetAppPackageName
          advertiserId:(NSString*)targetAppAdvertiserId
               offerId:(NSString*)offerId
           publisherId:(NSString*)publisherId
              redirect:(BOOL)shouldRedirect;

- (BOOL)isiAdAttribution;

- (void)urlStringForEvent:(TuneEvent *)event
             trackingLink:(NSString**)trackingLink
            encryptParams:(NSString**)encryptParams;
@end

@protocol TuneTrackerDelegate <NSObject>
@optional
- (void)_tuneURLTestingCallbackWithParamsToBeEncrypted:(NSString*)paramsToBeEncrypted withPlaintextParams:(NSString*)plaintextParams;
@end
