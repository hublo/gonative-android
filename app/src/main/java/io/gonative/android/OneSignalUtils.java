package io.gonative.android;

import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;

public class OneSignalUtils {

    public static void promptForPushNotifications() {
        OSDeviceState osDeviceState = OneSignal.getDeviceState();
        if (osDeviceState == null) {
            return;
        }

        if (!osDeviceState.areNotificationsEnabled()) {
            OneSignal.promptForPushNotifications(false);
        }
    }
}
