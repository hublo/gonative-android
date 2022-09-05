package io.gonative.android.plugins.oneSignal;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.onesignal.OSNotification;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import io.gonative.gonative_core.LeanUtils;

/**
 * Created by weiyin on 2/10/16.
 */
public class OneSignalNotificationHandler implements OneSignal.OSNotificationOpenedHandler {
    
    public static final String INTENT_TARGET_URL = "targetUrl";
    private static final String TAG = OneSignalNotificationHandler.class.getName();
    private Context context;
    private OneSignalPlugin plugin;
    
    @SuppressWarnings("unused")
    public OneSignalNotificationHandler() {
        // default construct needed to be a broadcast receiver
    }
    
    OneSignalNotificationHandler(Context context, OneSignalPlugin plugin) {
        this.context = context;
        this.plugin = plugin;
    }
    
    @Override
    public void notificationOpened(OSNotificationOpenedResult openedResult) {
        OSNotification notification = openedResult.getNotification();
        
        String launchUrl = notification.getLaunchURL();
        if (launchUrl != null && !launchUrl.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(launchUrl));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            return;
        }
        
        JSONObject additionalData = notification.getAdditionalData();
        String targetUrl = LeanUtils.optString(additionalData, INTENT_TARGET_URL);
        if (targetUrl == null) targetUrl = LeanUtils.optString(additionalData, "u");
        
        plugin.startMainActivity(targetUrl);
    }
}
