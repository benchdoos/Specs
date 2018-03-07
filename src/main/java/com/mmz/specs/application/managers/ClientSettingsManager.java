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
import com.mmz.specs.application.core.client.ClientConstants;
import com.mmz.specs.application.core.server.ServerConstants;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ClientSettingsManager {
    private static final ClientSettingsManager ourInstance = new ClientSettingsManager();
    private final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private final Properties CLIENT_SETTINGS = new Properties();
    private final String connectionFileLocation = ApplicationConstants.CLIENT_SETTINGS_FILE;


    private ClientSettingsManager() {
        log.info("Loading settings...");
    }

    public static ClientSettingsManager getInstance() {
        return ourInstance;
    }

    public void loadSettingsFile() throws IOException {
        log.info("Trying to load settings file: " + connectionFileLocation);
        CLIENT_SETTINGS.loadFromXML(new FileInputStream(connectionFileLocation));
        log.info("Settings file successfully loaded: " + connectionFileLocation);
    }

    public boolean isSettingsFileCorrect() {
        String serverAddress = CLIENT_SETTINGS.getProperty(ClientConstants.CLIENT_SERVER_ADDRESS_KEY);
        if (serverAddress == null) {
            return false;
        } else if (serverAddress.isEmpty()) {
            return false;
        }

        String serverPort = CLIENT_SETTINGS.getProperty(ClientConstants.CLIENT_SERVER_PORT_KEY);

        if (serverPort == null) {
            return false;
        } else if (serverPort.isEmpty()) {
            return false;
        } else {
            try {
                int i = Integer.parseInt(serverPort);
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public String getServerAddress() {
        return CLIENT_SETTINGS.getProperty(ClientConstants.CLIENT_SERVER_ADDRESS_KEY);
    }

    public void setServerAddress(String address) throws IOException {
        CLIENT_SETTINGS.setProperty(ClientConstants.CLIENT_SERVER_ADDRESS_KEY, address);
        updateSettingsFile();
    }

    private void updateSettingsFile() throws IOException {
        log.info("Updating / saving settings file: " + connectionFileLocation);
        CLIENT_SETTINGS.storeToXML(new FileOutputStream(connectionFileLocation),
                ApplicationConstants.INTERNAL_FULL_NAME + " client settings file", ApplicationConstants.DEFAULT_FILE_ENCODING);
        log.info("Settings file successfully updated: " + connectionFileLocation);
    }

    public int getServerPort() {
        String property = CLIENT_SETTINGS.getProperty(ClientConstants.CLIENT_SERVER_PORT_KEY);
        int port = ServerConstants.SERVER_DEFAULT_SOCKET_PORT;
        try {
            port = Integer.parseInt(property);
        } catch (NumberFormatException e) {/*NOP*/}

        return port;
    }

    public void setServerPort(int port) throws IOException {
        CLIENT_SETTINGS.setProperty(ClientConstants.CLIENT_SERVER_PORT_KEY, port + "");
        updateSettingsFile();
    }

    public Properties getProperties() {
        return CLIENT_SETTINGS;
    }
}
