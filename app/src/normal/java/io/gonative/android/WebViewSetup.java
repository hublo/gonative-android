package io.gonative.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.Map;

import io.gonative.android.library.AppConfig;

/**
 * Created by weiyin on 9/8/15.
 */
public class WebViewSetup {
    private static final String TAG = WebViewSetup.class.getName();

    public static void setupWebviewForActivity(GoNativeWebviewInterface webview, MainActivity activity) {
        if (!(webview instanceof LeanWebView)) {
            Log.e(TAG, "Expected webview to be of class LeanWebView and not " + webview.getClass().getName());
            return;
        }

        LeanWebView wv = (LeanWebView) webview;

        setupWebview(wv, activity);

        UrlNavigation urlNavigation = new UrlNavigation(activity);
        urlNavigation.setCurrentWebviewUrl(webview.getUrl());

        wv.setWebChromeClient(new GoNativeWebChromeClient(activity, urlNavigation));
        wv.setWebViewClient(new GoNativeWebviewClient(activity, urlNavigation));

        FileDownloader fileDownloader = activity.getFileDownloader();
        if (fileDownloader != null) {
            wv.setDownloadListener(fileDownloader);
            fileDownloader.setUrlNavigation(urlNavigation);
        }

        ProfilePicker profilePicker = activity.getProfilePicker();
        wv.removeJavascriptInterface("gonative_profile_picker");
        if (profilePicker != null) {
            wv.addJavascriptInterface(profilePicker.getProfileJsBridge(), "gonative_profile_picker");
        }

        wv.removeJavascriptInterface("gonative_status_checker");
        wv.addJavascriptInterface(activity.getStatusCheckerBridge(), "gonative_status_checker");

        wv.removeJavascriptInterface("hublo_segment");
        wv.addJavascriptInterface(activity.getSegmentWrapper(), "nativeApp");

        wv.removeJavascriptInterface("gonative_file_writer_sharer");
        wv.addJavascriptInterface(activity.getFileWriterSharer().getJavascriptBridge(), "gonative_file_writer_sharer");

        if (activity.getIntent().getBooleanExtra(MainActivity.EXTRA_WEBVIEW_WINDOW_OPEN, false)) {
            // send to other webview
            Message resultMsg = ((GoNativeApplication) activity.getApplication()).getWebviewMessage();
            if (resultMsg != null) {
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                if (transport != null) {
                    transport.setWebView(wv);
                    resultMsg.sendToTarget();
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void setupWebview(GoNativeWebviewInterface webview, Context context) {
        if (!(webview instanceof LeanWebView)) {
            Log.e(TAG, "Expected webview to be of class LeanWebView and not " + webview.getClass().getName());
            return;
        }

        AppConfig appConfig = AppConfig.getInstance(context);

        LeanWebView wv = (LeanWebView) webview;
        WebSettings webSettings = wv.getSettings();

        webSettings.setBuiltInZoomControls(AppConfig.getInstance(context).allowZoom);

        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        // font size bug fix, see https://stackoverflow.com/questions/41179357/android-webview-rem-units-scale-way-to-large-for-boxes
        webSettings.setMinimumFontSize(1);
        webSettings.setMinimumLogicalFontSize(1);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true);
        }

        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);

        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);
        webSettings.setUserAgentString(appConfig.userAgent);
        webSettings.setSupportMultipleWindows(appConfig.enableWindowOpen);
        webSettings.setGeolocationEnabled(appConfig.usesGeolocation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        if (appConfig.webviewTextZoom > 0) {
            webSettings.setTextZoom(appConfig.webviewTextZoom);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    public static void setupWebviewGlobals(Context context) {
        // WebView debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Map<String, Object> installation = Installation.getInfo(context);
            String dist = (String) installation.get("distribution");
            if (dist != null && (dist.equals("debug") || dist.equals("adhoc"))) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
    }

    public static void removeCallbacks(LeanWebView webview) {
        webview.setWebViewClient(null);
        webview.setWebChromeClient(null);
    }
}
