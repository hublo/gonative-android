package io.gonative.android;

import android.content.pm.PackageManager;
import android.os.Build;

public class PermissionUtils {
    public static boolean isAllGranted(final String[] permissions, final int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean shouldCheckStoragePermissions() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S;
    }
}
