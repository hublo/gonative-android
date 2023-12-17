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

    public static boolean shouldCheckStoragePermissionsWhenUsingDownloadManager() {
        /*
        Official doc :
        For applications targeting Build.VERSION_CODES.Q or above, WRITE_EXTERNAL_STORAGE permission
        is not needed and the  dirType must be one of the known public directories like
        Environment#DIRECTORY_DOWNLOADS, Environment#DIRECTORY_PICTURES, Environment#DIRECTORY_MOVIES, etc.
         */
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }
}
