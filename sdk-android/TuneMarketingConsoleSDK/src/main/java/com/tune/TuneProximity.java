package com.tune;

import android.content.Context;

import com.tune.ma.configuration.TuneConfiguration;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Created by gordon stewart on 8/17/16.
 * @author gordon@smartwhere.com
 */

public class TuneProximity {

    public static final String COM_PROXIMITY_LIBRARY_PROXIMITYCONTROL = "com.proximity.library.ProximityControl";
    public static final String PROXIMITY_NOTIFICATION_SERVICE = "com.tune.TuneProximityNotificationService";

    public boolean isProximityEnabled(TuneConfiguration configuration) {
        return configuration.shouldAutoCollectDeviceLocation() &&
                classForName(COM_PROXIMITY_LIBRARY_PROXIMITYCONTROL) != null;
    }

    public void startMonitoring(Context context, String appId, String apiSecret, TuneConfiguration tuneConfiguration) {
        if (isProximityEnabled(tuneConfiguration)){
            Class targetClass;
            targetClass = classForName(COM_PROXIMITY_LIBRARY_PROXIMITYCONTROL);
            if (targetClass != null){
                HashMap<String, String> config = new HashMap<>();

                config.put("API_KEY", appId);
                config.put("API_SECRET", apiSecret);
                config.put("APPLICATION_ID", appId);
                config.put("SERVICE_AUTO_START", "true");
                config.put("ENABLE_GEOFENCE_RANGING", "true");
                config.put("PROMPT_FOR_LOCATION_PERMISSION", "false");
                config.put("NOTIFICATION_HANDLER_SERVICE", PROXIMITY_NOTIFICATION_SERVICE);
                if (tuneConfiguration.debugLoggingOn()){
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

    }

    protected Class classForName(String name){
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
           return null;
        }
    }

}
