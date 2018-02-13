package com.mmz.specs.application.managers;

import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.server.ServerConstants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class CommonSettingsManager {

    public static String getServerSettingsFilePath() throws IOException {
        String result;
        Properties properties = new Properties();
        properties.loadFromXML(new FileInputStream(ApplicationConstants.SETTINGS_FILE_PATH));
        result = properties.getProperty(ServerConstants.SERVER_SETTINGS_FILE_LOCATION_KEY);
        return result;
    }

    public static void setServerSettingsFilePath(String path) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(ServerConstants.SERVER_SETTINGS_FILE_LOCATION_KEY, path);
        properties.storeToXML(new FileOutputStream(ApplicationConstants.SETTINGS_FILE_PATH),
                ApplicationConstants.INTERNAL_FULL_NAME + " settings file locations",
                ApplicationConstants.DEFAULT_FILE_ENCODING);
    }
}
