package com.tune.smartwhere;

import android.content.Context;

import com.tune.TuneUnitTest;

import java.util.HashMap;


/**
 * Created by gordonstewart on 8/17/16.
 *
 * @author gordon@smartwhere.com
 */

public class TuneSmartWhereTests extends TuneUnitTest {

    private TuneSmartWhere testObj;
    private Context context;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        FakeProximityControl.reset();
        context = getContext();

        testObj = TuneSmartWhereForTest.getInstance();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        TuneSmartWhereForTest.setInstance(null);
    }

    public void testIsProximityInstalledReturnsFalseWhenProximityControlClassNotFound() throws Exception {
        TuneSmartWhereForTest.clazz = null;

        assertFalse(testObj.isSmartWhereAvailable());
        assertEquals("Incorrect class name specified", "com.proximity.library.ProximityControl", TuneSmartWhereForTest.capturedClassNameString);
    }

    public void testIsProximityInstalledReturnsTrueWhenProximityControlClassIsFound() throws Exception {
        TuneSmartWhereForTest.clazz = this.getClass();

        assertTrue(testObj.isSmartWhereAvailable());
        assertEquals("Incorrect class name specified", "com.proximity.library.ProximityControl", TuneSmartWhereForTest.capturedClassNameString);
    }

    public void testStartMonitoringConfiguresWithAdIdAndConversionKey() throws Exception {
        String addId = "addId";
        String conversionKey = "conversionKey";

        testObj.startMonitoring(context, addId, conversionKey, "", false);

        HashMap actualConfig = FakeProximityControl.capturedConfig;
        assertTrue(FakeProximityControl.hasConfigureServiceBeenCalled);
        assertTrue(actualConfig.containsKey("API_KEY"));
        assertEquals(actualConfig.get("API_KEY"), addId);
        assertTrue(actualConfig.containsKey("API_SECRET"));
        assertEquals(actualConfig.get("API_SECRET"), conversionKey);
        assertTrue(actualConfig.containsKey("APPLICATION_ID"));
        assertEquals(actualConfig.get("APPLICATION_ID"), addId);
    }

    public void testStartMonitoringSetsDebugLoggingWhenTuneLoggingIsEnabled() throws Exception {
        String addId = "addId";
        String conversionKey = "conversionKey";

        testObj.startMonitoring(context, addId, conversionKey, "", true);

        HashMap actualConfig = FakeProximityControl.capturedConfig;
        assertTrue(FakeProximityControl.hasConfigureServiceBeenCalled);
        assertTrue(actualConfig.containsKey("DEBUG_LOG"));
        assertEquals(actualConfig.get("DEBUG_LOG"), "true");
    }

    public void testStartMonitoringSetsProximityNotificationServiceName() throws Exception {
        String addId = "addId";
        String conversionKey = "conversionKey";

        testObj.startMonitoring(context, addId, conversionKey, "", false);

        HashMap actualConfig = FakeProximityControl.capturedConfig;
        assertTrue(FakeProximityControl.hasConfigureServiceBeenCalled);
        assertTrue(actualConfig.containsKey("NOTIFICATION_HANDLER_SERVICE"));
        assertEquals(actualConfig.get("NOTIFICATION_HANDLER_SERVICE"), "com.tune.smartwhere.TuneSmartWhereNotificationService");
    }

    public void testStartMonitoringSetsPermissionPromptingOff() throws Exception {
        String addId = "addId";
        String conversionKey = "conversionKey";

        testObj.startMonitoring(context, addId, conversionKey, "", false);

        HashMap actualConfig = FakeProximityControl.capturedConfig;
        assertTrue(FakeProximityControl.hasConfigureServiceBeenCalled);
        assertTrue(actualConfig.containsKey("PROMPT_FOR_LOCATION_PERMISSION"));
        assertEquals(actualConfig.get("PROMPT_FOR_LOCATION_PERMISSION"), "false");
    }

    public void testStartMonitoringSetsServiceAutoStartOn() throws Exception {
        String addId = "addId";
        String conversionKey = "conversionKey";

        testObj.startMonitoring(context, addId, conversionKey, "", false);

        HashMap actualConfig = FakeProximityControl.capturedConfig;
        assertTrue(FakeProximityControl.hasConfigureServiceBeenCalled);
        assertTrue(actualConfig.containsKey("SERVICE_AUTO_START"));
        assertEquals(actualConfig.get("SERVICE_AUTO_START"), "true");
    }

    public void testStartMonitoringSetsGeofenceRangingOn() throws Exception {
        String appId = "addId";
        String conversionKey = "conversionKey";

        testObj.startMonitoring(context, appId, conversionKey, "", false);

        HashMap actualConfig = FakeProximityControl.capturedConfig;
        assertTrue(FakeProximityControl.hasConfigureServiceBeenCalled);
        assertTrue(actualConfig.containsKey("ENABLE_GEOFENCE_RANGING"));
        assertEquals(actualConfig.get("ENABLE_GEOFENCE_RANGING"), "true");
    }

    public void testStartMonitoringStartsService() throws Exception {
        String addId = "addId";
        String conversionKey = "conversionKey";

        testObj.startMonitoring(context, addId, conversionKey, "", false);

        assertTrue(FakeProximityControl.hasStartServiceBeenCalled);
    }

    public void testStopMonitoringSetsServiceAutoStartOff() throws Exception {
        testObj.stopMonitoring(context);

        HashMap actualConfig = FakeProximityControl.capturedConfig;
        assertTrue(FakeProximityControl.hasConfigureServiceBeenCalled);
        assertTrue(actualConfig.containsKey("SERVICE_AUTO_START"));
        assertEquals(actualConfig.get("SERVICE_AUTO_START"), "false");
    }

    public void testStopMonitoringStopsService() throws Exception {
        testObj.stopMonitoring(context);

        assertTrue(FakeProximityControl.hasStopServiceBeenCalled);
    }

    public void testSetDebugModeSetsDebugLoggingWhenTrue() throws Exception {
        testObj.setDebugMode(context, true);

        HashMap actualConfig = FakeProximityControl.capturedConfig;
        assertTrue(FakeProximityControl.hasConfigureServiceBeenCalled);
        assertTrue(actualConfig.containsKey("DEBUG_LOG"));
        assertEquals(actualConfig.get("DEBUG_LOG"), "true");
    }

    public void testSetDebugModeSetsDebugLoggingWhenFalse() throws Exception {
        testObj.setDebugMode(context, false);

        HashMap actualConfig = FakeProximityControl.capturedConfig;
        assertTrue(FakeProximityControl.hasConfigureServiceBeenCalled);
        assertTrue(actualConfig.containsKey("DEBUG_LOG"));
        assertEquals(actualConfig.get("DEBUG_LOG"), "false");
    }

    public void testSetPackageNameCallsConfigureServiceWithPackageName() throws Exception {
        String packageName = "com.set.package.name";
        testObj.setPackageName(context, packageName);

        HashMap actualConfig = FakeProximityControl.capturedConfig;
        assertTrue(FakeProximityControl.hasConfigureServiceBeenCalled);
        assertTrue(actualConfig.containsKey("PACKAGE_NAME"));
        assertEquals(actualConfig.get("PACKAGE_NAME"), packageName);
    }

}

class TuneSmartWhereForTest extends TuneSmartWhere {
    public static Class clazz;
    public static String capturedClassNameString;

    public static HashMap<String, String> config;

    public static synchronized TuneSmartWhere getInstance() {
        clazz = FakeProximityControl.class;
        config = null;
        return new TuneSmartWhereForTest();
    }

    @Override
    protected Class classForName(String name) {
        TuneSmartWhereForTest.capturedClassNameString = name;
        return clazz;
    }
}

@SuppressWarnings("unused")
class FakeProximityControl {
    public static Context context;
    public static Object proximityNotification;
    public static String code;
    public static HashMap capturedConfig;
    public static boolean permissionResult;

    public static boolean hasFireNotificationBeenCalled;
    public static boolean hasProcessScanBeenCalled;
    public static boolean hasStartServiceBeenCalled;
    public static boolean hasStopServiceBeenCalled;
    public static boolean hasConfigureServiceBeenCalled;
    public static boolean hasSetPermissionRquestResultBeenCalled;

    public static void reset() {
        FakeProximityControl.permissionResult = false;
        FakeProximityControl.context = null;
        FakeProximityControl.capturedConfig = null;
        FakeProximityControl.proximityNotification = null;
        FakeProximityControl.code = null;

        FakeProximityControl.hasFireNotificationBeenCalled = false;
        FakeProximityControl.hasProcessScanBeenCalled = false;
        FakeProximityControl.hasStartServiceBeenCalled = false;
        FakeProximityControl.hasStopServiceBeenCalled = false;
        FakeProximityControl.hasConfigureServiceBeenCalled = false;
        FakeProximityControl.hasSetPermissionRquestResultBeenCalled = false;
    }

    public static void fireNotification(Context context, Object proximityNotification) {
        hasFireNotificationBeenCalled = true;
        FakeProximityControl.context = context;
        FakeProximityControl.proximityNotification = proximityNotification;
    }

    public static void processScan(Context context, String code) {
        hasProcessScanBeenCalled = true;
        FakeProximityControl.context = context;
        FakeProximityControl.code = code;
    }

    public static void startService(Context context) {
        hasStartServiceBeenCalled = true;
        FakeProximityControl.context = context;
    }

    public static void stopService(Context context) {
        hasStopServiceBeenCalled = true;
        FakeProximityControl.context = context;
    }

    public static void configureService(Context context, HashMap config) {
        hasConfigureServiceBeenCalled = true;
        FakeProximityControl.context = context;
        FakeProximityControl.capturedConfig = config;
    }

    public static void setPermissionRequestResult(Context context, boolean permissionResult) {
        hasSetPermissionRquestResultBeenCalled = true;
        FakeProximityControl.context = context;
        FakeProximityControl.permissionResult = permissionResult;
    }
}
