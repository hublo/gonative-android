package io.gonative.android;

import android.content.Context;
import android.util.Log;

import com.segment.analytics.kotlin.android.AndroidAnalyticsKt;

import kotlin.Unit;

public class SegmentManager {
    private final static String TAG = SegmentManager.class.getName();

    private Context context;

    SegmentManager(Context context) {
        this.context = context;
        this.initialize();
    }

    private void initialize() {
        Log.i(TAG, "SegmentManager.initialize()");

        AndroidAnalyticsKt.Analytics(BuildConfig.SEGMENT_API_KEY, this.context, configuration -> {
            configuration.setFlushAt(3);
            configuration.setFlushInterval(10);
            configuration.setCollectDeviceId(true);
            configuration.setTrackApplicationLifecycleEvents(true);
            configuration.setTrackDeepLinks(true);
            return Unit.INSTANCE;
        });
    }
}
