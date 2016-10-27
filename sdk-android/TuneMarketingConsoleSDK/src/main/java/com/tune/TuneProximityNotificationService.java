package com.tune;

import android.app.IntentService;
import android.content.Intent;

import java.lang.reflect.Method;

/**
 * Created by gordon stewart on 8/18/16.
 * @author gordon@smartwhere.com
 */

public class TuneProximityNotificationService extends IntentService {

    public TuneProximityNotificationService() {
        super("TuneProximityNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        TuneUtils.log("TuneProximityNotificationService: onHandleIntent");
        if (intent != null && intent.getAction() != null && intent.getAction().equals("proximity-notification")) {
            Object proximityNotification = (intent.hasExtra("proximityNotification")) ? intent.getSerializableExtra("proximityNotification") : null;
            if (proximityNotification != null){
                TuneUtils.log("TuneProximityNotificationService: " + getTitleFromProximityNotification(proximityNotification));
            } else {
                TuneUtils.log("TuneProximityNotificationService: proximityNotification is null");
            }
        }
    }

    private String getTitleFromProximityNotification(Object proximityNotification) {
        Method getTitle;
        try {
            getTitle = proximityNotification.getClass().getMethod("getTitle");
            return (String) getTitle.invoke(proximityNotification);
        } catch (Exception e) {
            return null;
        }
    }
}
