package com.mmz.specs.application.core;

import java.io.File;

public class ApplicationConstants {
    public static final String INTERNAL_FULL_NAME = "com.mmz.specs";
    public static final String APPLICATION_NAME = "MMZ - Спецификации";
    public static final String APPLICATION_NAME_POSTFIX_SERVER = " - Сервер";
    public static final String USER_HOME_LOCATION = System.getProperty("user.home");
    public static final String LOG_FOLDER = System
            .getProperty("java.io.tmpdir") + File.separator + INTERNAL_FULL_NAME + File.separator + "logs";
    public static final String APP_LOG_PROPERTY_KEY = "com.mmz.specs:log.folder";

    public static final String SETTINGS_FILE_PATH = new File(LOG_FOLDER).getParent() + File.separator + "settings.xml";
    public static final String DEFAULT_FILE_ENCODING = "UTF-8";

    public static final String LOG_PREFIX_SERVER = File.separator + "server_";
    public static final String LOG_PREFIX_CLIENT = File.separator + "client_";
}
