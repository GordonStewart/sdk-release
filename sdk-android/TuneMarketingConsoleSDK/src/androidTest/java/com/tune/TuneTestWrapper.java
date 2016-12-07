package com.tune;

import android.content.Context;
import android.content.SharedPreferences;

import com.tune.location.TuneLocationListener;
import com.tune.ma.TuneManager;
import com.tune.ma.configuration.TuneConfiguration;
import com.tune.ma.eventbus.TuneEventBus;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TuneTestWrapper extends Tune {
    // copied from TuneConstants
    private static final String PREFS_LOG_ID_OPEN = "mat_log_id_open";
    private static final String PREFS_TUNE = "com.mobileapptracking";
    
    private static boolean online = true;
    
    private static TuneTestWrapper tune;
    
    public TuneTestWrapper() {
        super();
    }
    
    public static TuneTestWrapper init(final Context context, final String advertiserId, final String key) {
        TuneEventBus.enable();
        TuneConfiguration initialConfig = new TuneConfiguration();
        // initialize TuneManager with useConfiguration set to true so that it initializes configuration player
        ArrayList<String> configurationPlayerFilenames = new ArrayList<String>();
        configurationPlayerFilenames.add("configuration1.json");
        configurationPlayerFilenames.add("configuration2.json");
        initialConfig.setUseConfigurationPlayer(true);
        initialConfig.setConfigurationPlayerFilenames(configurationPlayerFilenames);
        initialConfig.setShouldSendScreenViews(true);
        TuneManager.init(context, initialConfig);

        tune = new TuneTestWrapper();
        tune.mContext = context;
        tune.pubQueue = Executors.newSingleThreadExecutor();
        
        tune.initAll(advertiserId, key);
        tune.locationListener = new TuneLocationListener(context);
        tune.eventQueue = new TuneTestQueue(context, tune);

        tune.setShouldAutoCollectDeviceLocation(false);
        tune.setPackageName(TuneTestConstants.appId);
        tune.setAdvertiserId(TuneTestConstants.advertiserId);

        // update it after initialization because remote config takes priority, so it would overwrite analyticsDispatchPeriod
        TuneManager.getInstance().getConfigurationManager().updateConfigurationFromTuneConfigurationObject(getTestingConfig(configurationPlayerFilenames));
        
        // make fake open id
        String logId = "1234567812345678-201401-" + TuneTestConstants.advertiserId;
        context.getSharedPreferences(PREFS_LOG_ID_OPEN, Context.MODE_PRIVATE).edit().putString(PREFS_TUNE, logId).apply();

        Tune.setInstance(tune);

        return tune;
    }

    public static TuneConfiguration getTestingConfig(List<String> configurationPlayerFilenames) {
        TuneConfiguration config = new TuneConfiguration();
        config.setAnalyticsHostPort("https://qa.ma.tune.com:8443/analytics-receiver/analytics");
        config.setPlaylistHostPort("https://qa.ma.tune.com");
        config.setConfigurationHostPort("https://qa.ma.tune.com");
        config.setConnectedModeHostPort("https://qa.ma.tune.com");
        config.setStaticContentHostPort("https://s3.amazonaws.com/uploaded-assets-qa2");
        config.setAnalyticsDispatchPeriod(TuneTestConstants.ANALYTICS_DISPATCH_PERIOD);
        config.setPlaylistRequestPeriod(TuneTestConstants.PLAYLIST_REQUEST_PERIOD);
        config.setUseConfigurationPlayer(true);
        config.setConfigurationPlayerFilenames(configurationPlayerFilenames);
        config.setShouldSendScreenViews(true);

        return config;
    }
    
    public void clearParams() {
        if (tune != null && tune.params != null) { 
            tune.params.clear();
        }
    }
    
    public static synchronized TuneTestWrapper getInstance() {
        return tune;
    }
    
    public void waitForInit() {
        Date maxWait = new Date(new Date().getTime() + 60000);
        
        while (initialized == false && maxWait.after(new Date())) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }

    public ExecutorService getPubQueue() {
        return tune.pubQueue;
    }

    public void setTuneTestRequest(TuneTestRequest request) {
        tuneRequest = request;
    }

    public synchronized void setOnline( boolean toBeOnline ) {
        online = toBeOnline;
    }

    public synchronized boolean getOnline() {
        return online;
    }

    public static synchronized boolean isOnline(Context context) {
        return online;
    }

    public TuneTestQueue getEventQueue() {
        return (TuneTestQueue)eventQueue;
    }

    @Override
    public void addEventToQueue(String link, String data, JSONObject postBody, boolean firstSession) {
        super.addEventToQueue(link, data, postBody, false);
    }

    @Override
    public void dumpQueue() {
        if (online) {
            super.dumpQueue();
        }
    }

    public void removeBroadcastReceiver() {
        if( isRegistered ) {
            isRegistered = false;
            mContext.unregisterReceiver(networkStateReceiver);
            networkStateReceiver = null;
        }
    }

    public void clearSharedPrefs() {
        SharedPreferences prefs = mContext.getSharedPreferences(TuneConstants.PREFS_TUNE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    
    public String readUserIdKey(String key) {
        return mContext.getSharedPreferences(PREFS_TUNE, Context.MODE_PRIVATE).getString(key, "");
    }

    public void setTimeLastMeasuredSession(long time) {
        this.timeLastMeasuredSession = time;
    }

    public void setIsFirstInstall(boolean isFirstInstall) {
        tune.isFirstInstall = isFirstInstall;
    }
}
