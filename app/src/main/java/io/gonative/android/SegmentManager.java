package io.gonative.android;

import android.content.Context;
import android.util.Log;

import com.segment.analytics.kotlin.android.AndroidAnalyticsKt;
import com.segment.analytics.kotlin.core.Analytics;

import kotlin.Unit;

public class SegmentManager {
    private final static String TAG = SegmentManager.class.getName();

    private Analytics analytics;

    private Context context;

    SegmentManager(Context context) {
        this.context = context;
        this.initialize();
    }

    private void initialize() {
        Log.i(TAG, "SegmentManager.initialize()");

        this.analytics = AndroidAnalyticsKt.Analytics(BuildConfig.SEGMENT_API_KEY, this.context, configuration -> {
            configuration.setFlushAt(3);
            configuration.setFlushInterval(10);
            configuration.setCollectDeviceId(false);
            configuration.setTrackApplicationLifecycleEvents(true);
            configuration.setTrackDeepLinks(true);
            return Unit.INSTANCE;
        });
    }

    public void identify(String userId) {
        if (userId != null && !userId.equalsIgnoreCase("") && !userId.equalsIgnoreCase("null")) {
            this.analytics.identify(userId);
        } else {
            this.analytics.reset();
        }
    }
}
