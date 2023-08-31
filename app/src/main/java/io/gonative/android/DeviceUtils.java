package io.gonative.android;

import java.util.Locale;

public class DeviceUtils {

    private static final String[] supportedLanguages = {"fr", "en", "es", "de"};

    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static String getHubloLanguage() {
        String deviceLanguage = getLanguage();
        for (String language : DeviceUtils.supportedLanguages) {
            if (deviceLanguage.equals(language)) {
                return language;
            }
        }
        
        return "en";
    }
}
