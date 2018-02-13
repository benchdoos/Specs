package com.mmz.specs.application.core;

import java.io.File;

public class ApplicationConstants {
    public static final String INTERNAL_FULL_NAME = "com.mmz.specs";
    public static final String LOG_FOLDER = System
            .getProperty("java.io.tmpdir") + File.separator + INTERNAL_FULL_NAME + File.separator + "Logs";
    public static final String APP_LOG_PROPERTY_KEY = "com.mmz.specs:log.folder";

    public static final String SETTINGS_FILE_PATH = new File(LOG_FOLDER).getParent() + File.separator + "settings.xml";

}
