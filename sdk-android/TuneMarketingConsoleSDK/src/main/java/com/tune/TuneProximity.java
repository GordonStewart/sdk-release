package com.tune;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by gordonstewart on 8/17/16.
 * @author gordon@smartwhere.com
 */

public class TuneProximity {
    private static volatile TuneProximity instance = null;

    public static final String COM_PROXIMITY_LIBRARY_PROXIMITYCONTROL = "com.proximity.library.ProximityControl";
    public static final String PROXIMITY_NOTIFICATION_SERVICE = "com.tune.TuneProximityNotificationService";

    protected TuneProximity() {
    }

    public static synchronized TuneProximity getInstance(){
        if (instance == null){
            instance = new TuneProximity();
        }
        return instance;
    }

    public boolean isProximityInstalled() {
        return classForName(COM_PROXIMITY_LIBRARY_PROXIMITYCONTROL) != null;
    }

    public void startMonitoring(Context context, String appId, String apiSecret, boolean debugMode) {
        Class targetClass = classForName(COM_PROXIMITY_LIBRARY_PROXIMITYCONTROL);
        if (targetClass != null){
            HashMap<String, String> config = new HashMap<>();

            config.put("API_KEY", appId);
            config.put("API_SECRET", apiSecret);
            config.put("APPLICATION_ID", appId);
            config.put("SERVICE_AUTO_START", "true");
            config.put("ENABLE_GEOFENCE_RANGING", "true");
            config.put("PROMPT_FOR_LOCATION_PERMISSION", "false");
            config.put("NOTIFICATION_HANDLER_SERVICE", PROXIMITY_NOTIFICATION_SERVICE);
            if (debugMode){
                config.put("DEBUG_LOG", "true");
            }

            try {
                @SuppressWarnings("unchecked")
                Method configureService = targetClass.getMethod("configureService", Context.class, HashMap.class);
                configureService.invoke(targetClass, context, config);

                @SuppressWarnings("unchecked")
                Method startService = targetClass.getMethod("startService", Context.class);
                startService.invoke(targetClass, context);
            } catch (Exception e) {
                TuneUtils.log("TuneProximity.startMonitoring: " + e.getLocalizedMessage());
            }
        }
    }

    public void stopMonitoring(Context context) {
        Class targetClass = classForName(COM_PROXIMITY_LIBRARY_PROXIMITYCONTROL);
        if (targetClass != null){
            HashMap<String, String> config = new HashMap<>();

            config.put("SERVICE_AUTO_START", "false");

            try {
                @SuppressWarnings("unchecked")
                Method configureService = targetClass.getMethod("configureService", Context.class, HashMap.class);
                configureService.invoke(targetClass, context, config);

                @SuppressWarnings("unchecked")
                Method stopService = targetClass.getMethod("stopService", Context.class);
                stopService.invoke(targetClass, context);
            } catch (Exception e) {
                TuneUtils.log("TuneProximity.stopMonitoring: " + e.getLocalizedMessage());
            }
        }
    }

    public void setDebugMode(Context context, final boolean mode) {
        Class targetClass = classForName(COM_PROXIMITY_LIBRARY_PROXIMITYCONTROL);
        if (targetClass != null){
            HashMap<String, String> config = new HashMap<String,String>(){{
                put("DEBUG_LOG",(mode) ? "true": "false");
            }};
            try {
                @SuppressWarnings("unchecked")
                Method configureService = targetClass.getMethod("configureService", Context.class, HashMap.class);
                configureService.invoke(targetClass, context, config);
            } catch (Exception e) {
                TuneUtils.log("TuneProximity.setDebugMode: " + e.getLocalizedMessage());
            }
        }
    }

    protected static synchronized void setInstance(TuneProximity tuneProximity){
        instance = tuneProximity;
    }

    protected Class classForName(String name){
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
