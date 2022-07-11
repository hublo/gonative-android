package io.gonative.android.plugins.oneSignal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.onesignal.OSDeviceState;
import com.onesignal.OSSubscriptionObserver;
import com.onesignal.OSSubscriptionState;
import com.onesignal.OSSubscriptionStateChanges;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.gonative.android.library.AppConfig;
import io.gonative.gonative_core.BaseBridgeModule;
import io.gonative.gonative_core.GoNativeActivity;
import io.gonative.gonative_core.GoNativeContext;
import io.gonative.gonative_core.LeanUtils;

public class OneSignalPlugin extends BaseBridgeModule {
    
    private static final String TAG = OneSignalPlugin.class.getName();
    private static AppConfig appConfig;
    private static ActivityHolder activityHolder;
    private static String currentUrl;
    
    private static Map<String, Object> installationInfo;
    private String callback = "gonative_onesignal_info";
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean oneSignalRegistered = false;
    private int numOneSignalChecks = 0;
    private static boolean oneSignalObserverSetup;
    
    @Override
    public void onApplicationCreate(GoNativeContext context) {
        super.onApplicationCreate(context);
        appConfig = AppConfig.getInstance(context);
        activityHolder = new ActivityHolder();
    }
    
    @Override
    public <T extends Activity & GoNativeActivity> void onActivityCreate(T activity, boolean isRoot) {
        super.onActivityCreate(activity, isRoot);
        activityHolder.addActivity(activity);
        setupOneSignal(activity);
    }
    
    // Initial One Signal SDK setup
    private void setupOneSignal(Context context) {
        if (!appConfig.oneSignalEnabled) return;
        OneSignal.initWithContext(context);
        OneSignal.setAppId(appConfig.oneSignalAppId);
        OneSignal.setRequiresUserPrivacyConsent(appConfig.oneSignalRequiresUserPrivacyConsent);
        OneSignal.setNotificationOpenedHandler(new OneSignalNotificationHandler(context, this));
        Log.d(TAG, "setupOneSignal: One Signal SDK set.");
    }
    
    @Override
    public <T extends Activity & GoNativeActivity> boolean shouldOverrideUrlLoading(T activity, Uri url, JSONObject params) {
        currentUrl = url.toString();
        
        if ("onesignal".equals(url.getHost())) {
            if ("/tags/get".equals(url.getPath()) && params != null) {
                final String callback = params.optString("callback");
                if (callback.isEmpty()) return true;
                
                OneSignal.getTags(new OneSignal.OSGetTagsHandler() {
                    @Override
                    public void tagsAvailable(JSONObject tags) {
                        JSONObject results = new JSONObject();
                        try {
                            results.put("success", true);
                            if (tags != null) {
                                results.put("tags", tags);
                            }
                            final String js = LeanUtils.createJsForCallback(callback, results);
                            
                            // run on main thread
                            Handler mainHandler = new Handler(activity.getMainLooper());
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    runJavascript(js);
                                }
                            });
                        } catch (JSONException e) {
                            Log.e(TAG, "Error json encoding tags", e);
                        }
                    }
                });
                
                return true;
            }
            
            if ("/tags/set".equals(url.getPath()) && params != null) {
                JSONObject tags = params.optJSONObject("tags");
                if (tags == null) {
                    try {
                        tags = new JSONObject(params.optString("tags"));
                    } catch (JSONException e) {
                        Log.e(TAG, "GoNative OneSignal JSONException", e);
                        return true;
                    }
                }
                OneSignal.sendTags(tags);
                return true;
            }
            
            if ("/promptLocation".equals(url.getPath())) {
                OneSignal.promptLocation();
                return true;
            }
            
            if ("/userPrivacyConsent/grant".equals(url.getPath())) {
                OneSignal.provideUserConsent(true);
                return true;
            }
            
            if ("/userPrivacyConsent/revoke".equals(url.getPath())) {
                OneSignal.provideUserConsent(false);
                return true;
            }
            
            if ("/showTagsUI".equals(url.getPath())) {
                startSubscriptionActivity(activity);
                return true;
            }
            
            if ("/iam/addTrigger".equals(url.getPath()) && params != null) {
                String key = params.optString("key");
                if (TextUtils.isEmpty(key)) return true;
                String value = params.optString("value");
                if (TextUtils.isEmpty(value)) return true;
                
                OneSignal.addTrigger(key, value);
                return true;
            }
            
            if ("/iam/addTriggers".equals(url.getPath()) && params != null) {
                Map<String, Object> mapObject;
                Object json = params.optString("map").isEmpty() ? params : params.optString("map");
                mapObject = LeanUtils.jsonToMap(json);
                OneSignal.addTriggers(mapObject);
                return true;
            }
            
            if ("/iam/removeTriggerForKey".equals(url.getPath()) && params != null) {
                String key = params.optString("key");
                if (TextUtils.isEmpty(key)) return true;
                
                OneSignal.removeTriggerForKey(key);
                return true;
            }
            
            if ("/iam/getTriggerValueForKey".equals(url.getPath()) && params != null) {
                String key = params.optString("key");
                if (TextUtils.isEmpty(key)) return true;
                
                String value = (String) OneSignal.getTriggerValueForKey(key);
                HashMap<String, Object> map = new HashMap<>();
                map.put("key", key);
                map.put("value", value);
                
                JSONObject jsonObject = new JSONObject(map);
                String js = LeanUtils.createJsForCallback("gonative_iam_trigger_value", jsonObject);
                runJavascript(js);
                return true;
            }
            
            if ("/iam/pauseInAppMessages".equals(url.getPath()) && params != null) {
                boolean value = params.optBoolean("pause");
                OneSignal.pauseInAppMessages(value);
                return true;
            }
            
            if ("/iam/setInAppMessageClickHandler".equals(url.getPath()) && params != null) {
                String handler = params.optString("handler");
                if (TextUtils.isEmpty(handler)) return true;
                
                OneSignal.setInAppMessageClickHandler(action -> {
                    Log.d(TAG, "In-app message clicked");
                    
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("clickName", action.getClickName());
                    map.put("clickUrl", action.getClickUrl());
                    map.put("firstClick", action.isFirstClick());
                    map.put("closesMessage", action.doesCloseMessage());
                    
                    JSONObject jsonObject = new JSONObject(map);
                    String js = LeanUtils.createJsForCallback(handler, jsonObject);
                    runJavascript(js);
                });
                return true;
            }
            
            if ("/externalUserId/set".equals(url.getPath()) && params != null) {
                String externalId = params.optString("externalId");
                OneSignal.setExternalUserId(externalId);
                return true;
            }
            
            if ("/externalUserId/remove".equals(url.getPath())) {
                OneSignal.removeExternalUserId();
                return true;
            }
        }
        
        if ("run".equals(url.getHost())) {
            if ("/gonative_onesignal_info".equals(url.getPath())) {
                callback = "gonative_onesignal_info";
                if (params != null) {
                    callback = params.optString("callback", "gonative_onesignal_info");
                }
                sendOneSignalInfo(activity, installationInfo);
            }
        }
        
        return false;
    }
    
    @Override
    public <T extends Activity & GoNativeActivity> void onSendInstallationInfo(T activity, Map info, String currentUrl) {
        super.onSendInstallationInfo(activity, info, currentUrl);
        installationInfo = info;
        this.currentUrl = currentUrl;
    
        if (!oneSignalObserverSetup) {
            oneSignalObserverSetup = true;
            setupOneSignalObserver(activity, info);
            Log.d(TAG, "onSendInstallationInfo: Observer and Installation info set");
        } else {
            sendOneSignalInfo(activity, info);
        }
    }
    
    private void setupOneSignalObserver(Context context, Map info) {
        OneSignal.addSubscriptionObserver(new OSSubscriptionObserver() {
            @Override
            public void onOSSubscriptionChanged(OSSubscriptionStateChanges stateChanges) {
                OSSubscriptionState to = stateChanges.getTo();
                
                sendOneSignalSubscriptionChanged();
                
                if (to.isSubscribed()) {
                    oneSignalRegistered = true;
                }
                
                sendOneSignalInfo(context, info);
            }
        });
    }
    
    public void sendOneSignalInfo(Context context, Map installationInfo) {
        boolean doNativeBridge = true;
        
        if (currentUrl != null) {
            doNativeBridge = LeanUtils.checkNativeBridgeUrls(currentUrl, context);
        }
        
        // onesignal javsacript callback
        if (AppConfig.getInstance(context).oneSignalEnabled && doNativeBridge) {
            try {
                String userId = null;
                String pushToken = null;
                boolean subscribed = false;
                
                OSDeviceState state = OneSignal.getDeviceState();
                if (state != null) {
                    userId = state.getUserId();
                    pushToken = state.getPushToken();
                    subscribed = state.isSubscribed();
                }
                
                JSONObject jsonObject = new JSONObject(installationInfo);
                if (userId != null) {
                    jsonObject.put("oneSignalUserId", userId);
                }
                if (pushToken != null) {
                    // registration id is old GCM name, but keep it for compatibility
                    jsonObject.put("oneSignalRegistrationId", pushToken);
                    jsonObject.put("oneSignalPushToken", pushToken);
                }
                jsonObject.put("oneSignalSubscribed", subscribed);
                jsonObject.put("oneSignalRequiresUserPrivacyConsent", !OneSignal.userProvidedPrivacyConsent());
                String js = LeanUtils.createJsForCallback(callback, jsonObject);
                runJavascript(js);
            } catch (Exception e) {
                Log.e(TAG, "Error with onesignal javscript callback", e);
            }
        }
    }
    
    // This function is to trigger an registrationDataChanged on RegistrationManager
    private void sendOneSignalSubscriptionChanged() {
        for (Activity act : activityHolder.getActivitiesToNotify()) {
            if (act instanceof GoNativeActivity) {
                ((GoNativeActivity) act).onSubscriptionChanged();
            }
        }
    }
    
    private void runJavascript(String js) {
        for (Activity act : activityHolder.getActivitiesToNotify()) {
            if (act instanceof GoNativeActivity) {
                ((GoNativeActivity) act).runJavascript(js);
            }
        }
    }
    
    private void startSubscriptionActivity(Context context) {
        Intent intent = new Intent(context, SubscriptionsActivity.class);
        context.startActivity(intent);
    }

    /*
    private void startSubscriptionActivity() {
        for (Activity act : activityHolder.getActivitiesToNotify()) {
            if (act instanceof GoNativeActivity) {
                ((GoNativeActivity) act).startSubscriptionActivity();
            }
        }
    }
     */
    
    @Override
    public <T extends Activity & GoNativeActivity> void onActivityStart(T activity) {
        super.onActivityStart(activity);
        if (appConfig.oneSignalEnabled) {
            OneSignal.clearOneSignalNotifications();
        }
    }
    
    public void startMainActivity(String extra) {
        for (Activity act : activityHolder.getActivitiesToNotify()) {
            if (act instanceof GoNativeActivity) {
                ((GoNativeActivity) act).launchNotificationActivity(extra);
            }
        }
    }
    
    @Override
    public Map<String, Object> getAnalyticsProviderInfo() {
        if (appConfig.oneSignalEnabled) {
            Map<String, Object> providerInfo = new HashMap<>();
            OSDeviceState state = OneSignal.getDeviceState();
            if (state != null) {
                providerInfo.put("oneSignalUserId", state.getUserId());
                
                if (state.getPushToken() != null) {
                    providerInfo.put("oneSignalRegistrationId", state.getPushToken());
                    providerInfo.put("oneSignalPushToken", state.getPushToken());
                }
                providerInfo.put("oneSignalSubscribed", state.isSubscribed());
                providerInfo.put("oneSignalRequiresUserPrivacyConsent", !OneSignal.userProvidedPrivacyConsent());
            }
            return providerInfo;
        } else {
            return null;
        }
    }
    
    @Override
    public <T extends Activity & GoNativeActivity> void onPageFinish(T activity, boolean doNativeBridge) {
        super.onPageFinish(activity, doNativeBridge);
        if (installationInfo == null) return;
        sendOneSignalInfo(activity, installationInfo);
    }
}
