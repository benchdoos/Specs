package com.mmz.specs.application.core;

import java.io.File;

public class ApplicationConstants {
    private static String INTERNAL_FULL_NAME = "com.mmz.specs";
    private static final String LOG_FOLDER = System
            .getProperty("java.io.tmpdir") + File.separator + INTERNAL_FULL_NAME + File.separator + "Logs";
    private static String APP_LOG_PROPERTY_KEY = "com.mmz.specs:log.folder";

    public static String getLogFolder() {
        return LOG_FOLDER;
    }

    public static String getAppLogPropertyKey() {
        return APP_LOG_PROPERTY_KEY;
    }
}
