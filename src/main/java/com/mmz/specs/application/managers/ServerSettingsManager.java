/*
 * (C) Copyright 2018.  Eugene Zrazhevsky and others.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Contributors:
 * Eugene Zrazhevsky <eugene.zrazhevsky@gmail.com>
 */

package com.mmz.specs.application.managers;

import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.server.ServerException;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.HibernateConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;


public class ServerSettingsManager {
    private static final Properties SERVER_SETTINGS = new Properties();
    private static volatile ServerSettingsManager instance;

    private Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private String connectionFileLocation;

    private ServerSettingsManager() {
        log.info("Loading settings...");
    }


    public static ServerSettingsManager getInstance() {
        ServerSettingsManager localInstance = instance;
        if (localInstance == null) {
            synchronized (ClientSettingsManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ServerSettingsManager();
                }
            }
        }
        return localInstance;
    }

    private void createEmptySettingsFile() throws IOException {
        log.debug("Creating empty settings file at: " + connectionFileLocation);
        SERVER_SETTINGS.clear();
        SERVER_SETTINGS.setProperty(HibernateConstants.DB_CONNECTION_URL_KEY, "");
        SERVER_SETTINGS.setProperty(HibernateConstants.CONNECTION_USERNAME_KEY, "");
        SERVER_SETTINGS.setProperty(HibernateConstants.CONNECTION_PASSWORD_KEY, "");
        updateSettingsFile();
    }

    public String getServerDbConnectionUrl() {
        return SERVER_SETTINGS.getProperty(HibernateConstants.DB_CONNECTION_URL_KEY);
    }

    public void setServerDbConnectionUrl(String url) throws IOException {
        SERVER_SETTINGS.setProperty(HibernateConstants.DB_CONNECTION_URL_KEY, url);
        updateSettingsFile();
    }

    public String getServerDbPassword() {
        return SERVER_SETTINGS.getProperty(HibernateConstants.CONNECTION_PASSWORD_KEY);
    }

    public void setServerDbPassword(String password) throws IOException {
        SERVER_SETTINGS.setProperty(HibernateConstants.CONNECTION_PASSWORD_KEY, password);
        updateSettingsFile();
    }

    public String getServerDbUsername() {
        return SERVER_SETTINGS.getProperty(HibernateConstants.CONNECTION_USERNAME_KEY);
    }

    public void setServerDbUsername(String username) throws IOException {
        SERVER_SETTINGS.setProperty(HibernateConstants.CONNECTION_USERNAME_KEY, username);
        updateSettingsFile();
    }

    private void loadSettings() throws IOException {
        log.info("Trying to load settings file: " + connectionFileLocation);
        SERVER_SETTINGS.loadFromXML(new FileInputStream(connectionFileLocation));
        log.info("Settings file successfully loaded: " + connectionFileLocation);

    }

    public void loadSettingsFile() {
        try {
            loadSettings();
        } catch (FileNotFoundException e) {
            log.warn("Could not find settings file: " + connectionFileLocation);
            log.debug("Creating empty settings file: " + connectionFileLocation);
            try {
                createEmptySettingsFile();
            } catch (IOException e1) {
                log.warn("Could not create empty settings file: " + connectionFileLocation, e);
            }
        } catch (IOException e) {
            log.warn("Could not load settings file:" + connectionFileLocation);
        }
    }

    public void setServerSettings(String serverSettingsFileLocation) throws ServerException {
        if (serverSettingsFileLocation == null) {
            throw new ServerException(new IllegalArgumentException("ServerDBConnectionPool settings path can not be null"));
        } else if (serverSettingsFileLocation.isEmpty()) {
            throw new ServerException(new IllegalArgumentException("ServerDBConnectionPool settings path can not be empty"));
        }
        instance.connectionFileLocation = serverSettingsFileLocation;
    }

    private void updateSettingsFile() throws IOException {
        log.info("Updating / saving settings file: " + connectionFileLocation);
        SERVER_SETTINGS.storeToXML(new FileOutputStream(connectionFileLocation),
                ApplicationConstants.INTERNAL_FULL_NAME + " settings file", ApplicationConstants.DEFAULT_FILE_ENCODING);
        log.info("Settings file successfully updated: " + connectionFileLocation);
    }
}
