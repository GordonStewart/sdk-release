//
//  TuneProximityHelperTests.m
//  TuneMarketingConsoleSDK
//
//  Created by Gordon Stewart on 8/4/16.
//  Copyright © 2016 Tune. All rights reserved.
//

#import <XCTest/XCTest.h>
#import <OCMock/OCMock.h>
#import "TuneManager.h"
#import "TuneProximityHelper.h"
#import "TuneUtils.h"
#import "TuneConfiguration.h"

@interface TuneProximityHelper (Testing)

-(void) startProximityMonitoringWithAppId: (NSString*) appId
                               withApiKey: (NSString*) apiKey
                            withApiSecret: (NSString*) apiSecret
                               withConfig: (NSDictionary*) config;

@end

@interface TuneProximityHelperTests : XCTestCase {
    TuneProximityHelper *testObj;
    id mockTuneManager;
    id mockTuneUtils;
}

@end

@implementation TuneProximityHelperTests

- (void)setUp {
    [super setUp];
    
    mockTuneUtils = OCMStrictClassMock([TuneUtils class]);
    mockTuneManager = OCMStrictClassMock([TuneManager class]);
    
    testObj = [TuneProximityHelper new];
}

- (void)tearDown {
    [mockTuneManager stopMocking];
    [mockTuneUtils stopMocking];
    [super tearDown];
}

#pragma mark - isProximityEnabled tests

- (void)testIsProximityEnabledChecksAutoCollectLocationConfiguration {
    [self setTuneConfigurationMockWithAutoCollect:NO withDebug:NO];
    
    XCTAssertFalse([TuneProximityHelper isProximityEnabled]);
    
    [mockTuneManager verify];
}

- (void)testIsProximityEnabledReturnsFalseWhenSmartWhereClassNotFound{
    [self setTuneConfigurationMockWithAutoCollect:YES withDebug:NO];
    
    [[[[mockTuneUtils expect] classMethod] andReturn:nil] getClassFromString:@"SmartWhere"];
    XCTAssertFalse([TuneProximityHelper isProximityEnabled]);
    
    [mockTuneManager verify];
    [mockTuneUtils verify];
}

- (void)testIsProximityEnabledReturnsTrueWhenSmartWhereClassIsFound{
    [self setTuneConfigurationMockWithAutoCollect:YES withDebug:NO];
    [self setTuneUtilsGetClassFromStringToAnObject];
    
    XCTAssertTrue([TuneProximityHelper isProximityEnabled]);
    
    [mockTuneManager verify];
    [mockTuneUtils verify];
}

#pragma mark - startMonitoringWithTuneAdvertiserId:tuneConversionKey: tests

- (void)testStartMonitoringDoesntStartWhenAutoCollectLocationIsNO{
    [self setTuneConfigurationMockWithAutoCollect:NO withDebug:NO];
    
    id mockTestObj = OCMPartialMock(testObj);
    [[mockTestObj reject] startProximityMonitoringWithAppId: OCMOCK_ANY
                                                 withApiKey: OCMOCK_ANY
                                              withApiSecret: OCMOCK_ANY
                                                 withConfig: OCMOCK_ANY];
    
    [mockTestObj startMonitoringWithTuneAdvertiserId:@"aid" tuneConversionKey:@"key"];
    
    [mockTestObj verify];
    [mockTuneManager verify];
}

- (void)testStartMonitoringStartsProximityMonitoringWithAdIdAndConversionKey {
    [self setTuneConfigurationMockWithAutoCollect:YES withDebug:NO];
    [self setTuneUtilsGetClassFromStringToAnObject];
    
    NSString* aid = @"aid";
    NSString* conversionKey = @"key";
    
    id mockTestObj = OCMPartialMock(testObj);
    [[mockTestObj expect] startProximityMonitoringWithAppId: aid
                                                 withApiKey: aid
                                              withApiSecret: conversionKey
                                                 withConfig: [OCMArg checkWithBlock:^BOOL(id value){
        if ([value isKindOfClass:[NSDictionary class]]){
            NSDictionary* actualConfig = value;
            if ((actualConfig[@"ENABLE_NOTIFICATION_PERMISSION_PROMPTING"] && [actualConfig[@"ENABLE_NOTIFICATION_PERMISSION_PROMPTING"]  isEqual: @"false"]) &&
                (actualConfig[@"ENABLE_LOCATION_PERMISSION_PROMPTING"] && [actualConfig[@"ENABLE_LOCATION_PERMISSION_PROMPTING"]  isEqual: @"false"]) &&
                (actualConfig[@"ENABLE_GEOFENCE_RANGING"] && [actualConfig[@"ENABLE_GEOFENCE_RANGING"]  isEqual: @"true"]) &&
                (actualConfig[@"DELEGATE_NOTIFICATIONS"] && [actualConfig[@"DELEGATE_NOTIFICATIONS"]  isEqual: @"true"])){
                return YES;
            }
        }
        return NO;
    }]];
    
    [mockTestObj startMonitoringWithTuneAdvertiserId:@"aid" tuneConversionKey:@"key"];
    
    [mockTestObj verify];
}

- (void)testStartMonitoringSetsDebugLoggingWhenTuneLoggingIsEnabled {
    [self setTuneConfigurationMockWithAutoCollect:YES withDebug:YES];
    [self setTuneUtilsGetClassFromStringToAnObject];
    
    NSString* aid = @"aid";
    NSString* conversionKey = @"key";
    
    id mockTestObj = OCMPartialMock(testObj);
    [[mockTestObj expect] startProximityMonitoringWithAppId: aid
                                                 withApiKey: aid
                                              withApiSecret: conversionKey
                                                 withConfig: [OCMArg checkWithBlock:^BOOL(id value){
        if ([value isKindOfClass:[NSDictionary class]]){
            NSDictionary* actualConfig = value;
            if (actualConfig[@"DEBUG_LOGGING"] && [actualConfig[@"DEBUG_LOGGING"] isEqual: @"true"]){
                return YES;
            }
        }
        return NO;
    }]];
    
    [mockTestObj startMonitoringWithTuneAdvertiserId:@"aid" tuneConversionKey:@"key"];
    
    [mockTestObj verify];
}

#pragma mark - test helpers
- (void)setTuneConfigurationMockWithAutoCollect: (BOOL) value withDebug: (BOOL) debug;{
    TuneConfiguration* config = [TuneConfiguration new];
    config.shouldAutoCollectDeviceLocation = value;
    config.debugMode = (debug) ? [NSNumber numberWithInt:1] : [NSNumber numberWithInt:0];
    [[[[mockTuneManager stub] classMethod] andReturn:mockTuneManager] currentManager];
    [[[mockTuneManager stub] andReturn:config] configuration];
}

- (void)setTuneUtilsGetClassFromStringToAnObject {
    id obj = [NSObject new];
    [[[[mockTuneUtils expect] classMethod] andReturn:obj] getClassFromString:@"SmartWhere"];
}



@end
