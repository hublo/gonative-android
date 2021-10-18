package io.gonative.android;

import android.os.Bundle;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewPropertyAnimator;
import android.webkit.WebSettings;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;

/**
 * Created by weiyin on 9/8/15.
 */
public interface GoNativeWebviewInterface {
    // webview stuff
    void loadUrl(String url);
    void loadUrlDirect(String url);
    void loadDataWithBaseURL (String baseUrl, String data, String mimeType, String encoding, String historyUrl);
    String getUrl();
    void reload();
    boolean canGoBack();
    void goBack();
    boolean canGoForward();
    void goForward();
    void onPause();
    void onResume();
    void stopLoading();
    ViewParent getParent();
    void destroy();
    int getProgress();
    String getTitle();
    boolean exitFullScreen();
    void clearCache(boolean includeDiskFiles);

    // view stuff
    void setAlpha(float alpha);
    ViewPropertyAnimator animate();
    int getWidth();
    int getScrollY();
    int getScrollX();
    int getMaxHorizontalScroll();
    void clearFocus();
    void flingScroll(int vx, int vy);
    void zoomBy(float zoom);
    boolean zoomOut();
    boolean isZoomed();

    void runJavascript(String js);
    boolean checkLoginSignup();
    void setCheckLoginSignup(boolean checkLoginSignup);

    void saveStateToBundle(Bundle outBundle);
    void restoreStateFromBundle(Bundle inBundle);
    
    /**
     * @deprecated use GoNativeEdgeSwipeLayout in place of LeanWebView's swipe listener.
     */
    LeanWebView.OnSwipeListener getOnSwipeListener();
    /**
     * @deprecated use GoNativeEdgeSwipeLayout in place of LeanWebView's swipe listener.
     */
    void setOnSwipeListener(LeanWebView.OnSwipeListener listener);

    WebSettings getSettings();
    void evaluateJavascript(String script, ValueCallback<String> resultCallback);
}
