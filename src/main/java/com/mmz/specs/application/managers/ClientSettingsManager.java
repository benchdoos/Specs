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

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ClientSettingsManager {
    private static volatile ClientSettingsManager instance;
    private final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private final Properties CLIENT_SETTINGS = new Properties();
    private final String connectionFileLocation = ApplicationConstants.CLIENT_SETTINGS_FILE;


    private ClientSettingsManager() {
        log.info("Loading settings...");
    }

    public static ClientSettingsManager getInstance() {
        ClientSettingsManager localInstance = instance;
        if (localInstance == null) {
            synchronized (ClientSettingsManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ClientSettingsManager();
                }
            }
        }
        return localInstance;
    }

    void loadSettingsFile() throws IOException {
        log.info("Trying to load settings file: " + connectionFileLocation);
        CLIENT_SETTINGS.loadFromXML(new FileInputStream(connectionFileLocation));
        log.info("Settings file successfully loaded: " + connectionFileLocation);
    }

    boolean isSettingsFileCorrect() {
        String serverAddress = CLIENT_SETTINGS.getProperty(ClientConstants.CLIENT_SERVER_ADDRESS_KEY);
        if (serverAddress == null) {
            return false;
        } else return !serverAddress.isEmpty();
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
        return ServerConstants.SERVER_DEFAULT_SOCKET_PORT;
    }

    public Properties getProperties() {
        return CLIENT_SETTINGS;
    }

    public Dimension getClientMainWindowDimension() {
        try {
            String property = CLIENT_SETTINGS.getProperty(ClientConstants.MAIN_WINDOW_DIMENSION);
            String[] strings = property.split(",");
            if (strings.length == 2) {
                try {
                    int x = Integer.parseInt(strings[0]);
                    int y = Integer.parseInt(strings[1]);
                    if ((x > 0 && y > 0) && (x < Integer.MAX_VALUE && y < Integer.MAX_VALUE)) {
                        return new Dimension(x, y);
                    }
                } catch (NumberFormatException ignore) {/*NOP*/}
            }
        } catch (Exception ignore) {
            /*NOP*/
        }
        return ClientConstants.MAIN_WINDOW_DEFAULT_DIMENSION;
    }

    public void setClientMainWindowDimension(Dimension dimension) throws IOException {
        String value = dimension.width + "," + dimension.height;
        CLIENT_SETTINGS.setProperty(ClientConstants.MAIN_WINDOW_DIMENSION, value);
        updateSettingsFile();
    }

    public boolean isClientMainWindowExtended() {
        try {
            String property = CLIENT_SETTINGS.getProperty(ClientConstants.MAIN_WINDOW_EXTENDED);
            return Boolean.valueOf(property);
        } catch (Exception ignore) {
            /*NOP*/
        }
        return ClientConstants.MAIN_WINDOW_DEFAULT_EXTENDED;
    }

    public void setClientMainWindowExtended(boolean extended) throws IOException {
        CLIENT_SETTINGS.setProperty(ClientConstants.MAIN_WINDOW_EXTENDED, Boolean.toString(extended));
        updateSettingsFile();
    }

    Point getClientMainWindowLocation() {
        try {
            String property = CLIENT_SETTINGS.getProperty(ClientConstants.MAIN_WINDOW_POSITION);
            String[] strings = property.split(",");
            if (strings.length == 2) {
                try {
                    int x = Integer.parseInt(strings[0]);
                    int y = Integer.parseInt(strings[1]);

                    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                    int width = gd.getDisplayMode().getWidth();
                    int height = gd.getDisplayMode().getHeight();

                    if ((x > 0 && y > 0) && (x < width && y < height)) {
                        return new Point(x, y);
                    }
                } catch (NumberFormatException ignore) {/*NOP*/}
            }
        } catch (Exception ignore) {
            /*NOP*/
        }
        return new Point(-1, -1);
    }

    public void setClientMainWindowLocation(Point point) throws IOException {
        String value = point.x + "," + point.y;
        CLIENT_SETTINGS.setProperty(ClientConstants.MAIN_WINDOW_POSITION, value);
        updateSettingsFile();
    }

    public boolean getBoostRootUnitsLoading() {
        try {
            String property = CLIENT_SETTINGS.getProperty(ClientConstants.BOOST_ROOT_UNITS_LOADING);
            return Boolean.valueOf(property);
        } catch (Exception ignore) {
            /*NOP*/
        }
        return ClientConstants.BOOST_ROOT_UNITS_DEFAULT_LOADING;
    }

    public void setBoostRootUnitsLoading(boolean boost) throws IOException {
        CLIENT_SETTINGS.setProperty(ClientConstants.BOOST_ROOT_UNITS_LOADING, Boolean.toString(boost));
        updateSettingsFile();
    }
}
