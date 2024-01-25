package io.gonative.android;

public class UrlUtils {
    private static final String hubloLanguageSegmentName = "hublo-ln";

    public static String rewriteUrl(String url) {
        if (url == null) {
            return null;
        }

        if (!url.contains(hubloLanguageSegmentName)) {
            return url;
        }

        String hubloLanguage = DeviceUtils.getHubloLanguage();
        return url.replace(hubloLanguageSegmentName, hubloLanguage);
    }
}
