package com.tune.http;

import android.net.Uri;

import com.tune.TuneConstants;
import com.tune.TuneDeeplinkListener;
import com.tune.TuneDeeplinker;
import com.tune.TuneUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TuneUrlRequester implements UrlRequester {

    @Override
    public void requestDeeplink(String deeplinkURL, String conversionKey, TuneDeeplinkListener listener) {
        if (listener == null) {
            return; // no one is listening!
        }

        InputStream is = null;

        try {
            URL myurl = new URL(deeplinkURL);
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            // Set TUNE conversion key in request header
            conn.setRequestProperty("X-MAT-Key", conversionKey);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            
            conn.connect();
            
            boolean error = false;
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
            } else {
                error = true;
                is = conn.getErrorStream();
            }
            
            String response = TuneUtils.readStream(is);
            if (error) {
                // Notify listener of error
                listener.didFailDeeplink(response);
            } else {
                // Notify listener of deeplink url
                listener.didReceiveDeeplink(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Does an HTTP request to the given url, GET or POST based on whether json was passed or not
     * @param url the url to hit
     * @param json JSONObject with event item and IAP verification json, if not null or empty then will POST to url
     * @return JSONObject of the server response, null if request failed
     */
    @Override
    public JSONObject requestUrl(String url, JSONObject json, boolean debugMode) {
        InputStream is = null;
        
        try {
            URL myurl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setReadTimeout(TuneConstants.TIMEOUT);
            conn.setConnectTimeout(TuneConstants.TIMEOUT);
            conn.setDoInput(true);
            
            // If no JSON passed, do HttpGet
            if (json == null || json.length() == 0) {
                conn.setRequestMethod("GET");
            } else {
                // Put JSON as entity for HttpPost
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestMethod("POST");
                
                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();
            }
            
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (debugMode) {
                TuneUtils.log("Request completed with status " + responseCode);
            }
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }
            
            String responseAsString = TuneUtils.readStream(is);
            if (debugMode) {
                // Output server response
                TuneUtils.log("Server response: " + responseAsString);
            }
            // Try to parse response and print
            JSONObject responseJson = new JSONObject();
            try {
                JSONTokener tokener = new JSONTokener(responseAsString);
                responseJson = new JSONObject(tokener);
                if (debugMode) {
                    logResponse(responseJson);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            String matResponderHeader = conn.getHeaderField("X-MAT-Responder");
            if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
                return responseJson;
            }
            // for HTTP 400, if it's from our server, drop the request and don't retry
            else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST && matResponderHeader != null) {
                if (debugMode) {
                    TuneUtils.log("Request received 400 error from TUNE server, won't be retried");
                }
                return null; // don't retry
            }
            // for all other codes, assume the server/connection is broken and will be fixed later
        } catch (Exception e) {
            if (debugMode) {
                TuneUtils.log("Request error with URL " + url);
            }
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return new JSONObject(); // marks this request for retry
    }
    
    // Helper to log request success/failure/errors
    private static void logResponse(JSONObject response) {
        if (response.length() > 0) {
            try {
                // Output if any errors occurred
                if (response.has("errors") && response.getJSONArray("errors").length() != 0) {
                    String errorMsg = response.getJSONArray("errors").getString(0);
                    TuneUtils.log("Event was rejected by server with error: " + errorMsg);
                } else if (response.has("log_action") && 
                           !response.getString("log_action").equals("null") && 
                           !response.getString("log_action").equals("false") &&
                           !response.getString("log_action").equals("true")) {
                    // Read whether event was accepted or rejected from log_action if exists
                    JSONObject logAction = response.getJSONObject("log_action");
                    if (logAction.has("conversion")) {
                        JSONObject conversion = logAction.getJSONObject("conversion");
                        if (conversion.has("status")) {
                            String status = conversion.getString("status");
                            if (status.equals("rejected")) {
                                String statusCode = conversion.getString("status_code");
                                TuneUtils.log("Event was rejected by server: status code " + statusCode);
                            } else {
                                TuneUtils.log("Event was accepted by server");
                            }
                        }
                    }
                } else {
                    // Read whether event was accepted or rejected from options if exists
                    if (response.has("options")) {
                        JSONObject options = response.getJSONObject("options");
                        if (options.has("conversion_status")) {
                            String conversionStatus = options.getString("conversion_status");
                            TuneUtils.log("Event was " + conversionStatus + " by server");
                        }
                    }
                }
            } catch (JSONException e) {
                TuneUtils.log("Server response status could not be parsed");
                e.printStackTrace();
            }
        }
    }
}
