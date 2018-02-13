package com.mmz.specs.application.managers;

import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.HibernateConstants;
import com.mmz.specs.application.core.server.ServerStartException;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static com.mmz.specs.application.core.ApplicationConstants.SETTINGS_FILE_PATH;

public class SettingsManager {
    private static final Properties SETTINGS = new Properties();
    private static SettingsManager settingsManager = new SettingsManager();
    Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private SettingsManager() {
        log.info("Loading settings...");
        initSettings();
    }

    public static SettingsManager getInstance() {
        return settingsManager;
    }

    public String getServerDbConnectionUrl() {
        return SETTINGS.getProperty(HibernateConstants.DB_CONNECTION_URL_KEY);
    }

    public String getServerDbUsername() {
        return SETTINGS.getProperty(HibernateConstants.CONNECTION_USERNAME_KEY);
    }

    public String getServerDbPassword() {
        return SETTINGS.getProperty(HibernateConstants.CONNECTION_PASSWORD_KEY);
    }

    private void initSettings() {
        try {
            loadSettings();
        } catch (FileNotFoundException e) {
            log.warn("Could not find settings file: " + SETTINGS_FILE_PATH);
            log.debug("Creating empty settings file: " + SETTINGS_FILE_PATH);

            try {
                createEmptySettingsFile();
            } catch (IOException e1) {//TODO what should we do here???
                log.warn("Could not create empty settings file: " + SETTINGS_FILE_PATH, e);
            }
        } catch (IOException e) {
            log.warn("Could not load settings file:");// TODO and here???
        }
    }

    private void createEmptySettingsFile() throws IOException {
        log.debug("Creating empty settings file at: " + SETTINGS_FILE_PATH);
        SETTINGS.clear();
        SETTINGS.setProperty(HibernateConstants.DB_CONNECTION_URL_KEY, "");
        SETTINGS.setProperty(HibernateConstants.CONNECTION_USERNAME_KEY, "");
        SETTINGS.setProperty(HibernateConstants.CONNECTION_PASSWORD_KEY, "");
        updateSettingsFile();
    }

    private void updateSettingsFile() throws IOException {
        log.info("Updating / saving settings file: " + SETTINGS_FILE_PATH);
        SETTINGS.storeToXML(new FileOutputStream(SETTINGS_FILE_PATH),
                ApplicationConstants.INTERNAL_FULL_NAME + " settings file");
        log.info("Settings file successfully updated: " + SETTINGS_FILE_PATH);
    }

    private void loadSettings() throws IOException {
        log.info("Trying to load settings file: " + SETTINGS_FILE_PATH);
        SETTINGS.loadFromXML(new FileInputStream(SETTINGS_FILE_PATH));
    }
}
