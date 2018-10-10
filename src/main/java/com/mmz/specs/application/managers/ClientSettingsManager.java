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
        try {
            loadSettingsFile();
        } catch (IOException e) {
            log.warn("Could not load settings", e);
        }
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

    public Dimension getClientMainWindowDimension() {
        return getDimension(ClientConstants.MAIN_WINDOW_DIMENSION, ClientConstants.MAIN_WINDOW_DEFAULT_DIMENSION);
    }

    public void setClientMainWindowDimension(Dimension dimension) throws IOException {
        String value = dimension.width + "," + dimension.height;
        CLIENT_SETTINGS.setProperty(ClientConstants.MAIN_WINDOW_DIMENSION, value);
        updateSettingsFile();
    }

    Point getClientMainWindowLocation() {
        return getPoint(ClientConstants.MAIN_WINDOW_POSITION);
    }

    public void setClientMainWindowLocation(Point point) throws IOException {
        String value = point.x + "," + point.y;
        CLIENT_SETTINGS.setProperty(ClientConstants.MAIN_WINDOW_POSITION, value);
        updateSettingsFile();
    }

    private Dimension getDimension(String imagePreviewWindowDimension, Dimension defaultDimension) {
        try {
            String property = CLIENT_SETTINGS.getProperty(imagePreviewWindowDimension);
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
        return defaultDimension;
    }

    public Dimension getImagePreviewWindowDimension() {
        return getDimension(ClientConstants.IMAGE_PREVIEW_WINDOW_DIMENSION, ClientConstants.IMAGE_PREVIEW_WINDOW_DEFAULT_DIMENSION);
    }

    public void setImagePreviewWindowDimension(Dimension dimension) throws IOException {
        String value = dimension.width + "," + dimension.height;
        CLIENT_SETTINGS.setProperty(ClientConstants.IMAGE_PREVIEW_WINDOW_DIMENSION, value);
        updateSettingsFile();
    }

    public Point getImagePreviewWindowLocation() {
        return getPoint(ClientConstants.IMAGE_PREVIEW_WINDOW_POSITION);
    }

    public void setImagePreviewWindowLocation(Point point) throws IOException {
        if (point != null) {
            String value = point.x + "," + point.y;
            CLIENT_SETTINGS.setProperty(ClientConstants.IMAGE_PREVIEW_WINDOW_POSITION, value);
            updateSettingsFile();
        } else throw new IOException("Point can not be null");
    }

    private Point getPoint(String imagePreviewWindowPosition) {
        try {
            String property = CLIENT_SETTINGS.getProperty(imagePreviewWindowPosition);
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

    public Properties getProperties() {
        return CLIENT_SETTINGS;
    }

    public String getServerAddress() {
        return CLIENT_SETTINGS.getProperty(ClientConstants.CLIENT_SERVER_ADDRESS_KEY);
    }

    public void setServerAddress(String address) throws IOException {
        CLIENT_SETTINGS.setProperty(ClientConstants.CLIENT_SERVER_ADDRESS_KEY, address);
        updateSettingsFile();
    }

    public int getServerPort() {
        return ServerConstants.SERVER_DEFAULT_SOCKET_PORT;
    }

    public boolean isAutoUpdateEnabled() {
        try {
            String property = CLIENT_SETTINGS.getProperty(ClientConstants.AUTO_UPDATE_ENABLED);
            if (property != null) {
                final Boolean b = Boolean.valueOf(property);
                log.debug("Auto-update enabled: {}", b);
                return b;
            }
        } catch (Exception ignore) {
            /*NOP*/
        }
        return ClientConstants.AUTO_UPDATE_DEFAULT_ENABLED;
    }

    public void setAutoUpdateEnabled(boolean enabled) throws IOException {
        CLIENT_SETTINGS.setProperty(ClientConstants.AUTO_UPDATE_ENABLED, Boolean.toString(enabled));
        updateSettingsFile();
    }

    public boolean isBoostRootUnitsLoading() {
        try {
            String property = CLIENT_SETTINGS.getProperty(ClientConstants.BOOST_ROOT_UNITS_LOADING);
            if (property != null) {
                return Boolean.valueOf(property);
            }
        } catch (Exception ignore) {
            /*NOP*/
        }
        return ClientConstants.BOOST_ROOT_UNITS_DEFAULT_LOADING;
    }

    public void setBoostRootUnitsLoading(boolean boost) throws IOException {
        CLIENT_SETTINGS.setProperty(ClientConstants.BOOST_ROOT_UNITS_LOADING, Boolean.toString(boost));
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

    boolean isSettingsFileCorrect() {
        String serverAddress = CLIENT_SETTINGS.getProperty(ClientConstants.CLIENT_SERVER_ADDRESS_KEY);
        if (serverAddress == null) {
            return false;
        } else return !serverAddress.isEmpty();
    }

    public boolean isNewImageViewerUsing() {
        try {
            String property = CLIENT_SETTINGS.getProperty(ClientConstants.NEW_IMAGE_VIEWER);
            if (property != null) {
                return Boolean.valueOf(property);
            }
        } catch (Exception ignore) {
            /*NOP*/
        }
        return ClientConstants.NEW_IMAGE_VIEWER_DEFAULT_VALUE;
    }

    public void setNewImageViewerUsing(boolean using) throws IOException {
        CLIENT_SETTINGS.setProperty(ClientConstants.NEW_IMAGE_VIEWER, Boolean.toString(using));
        updateSettingsFile();
    }


    void loadSettingsFile() throws IOException {
        log.info("Trying to load settings file: " + connectionFileLocation);
        CLIENT_SETTINGS.loadFromXML(new FileInputStream(connectionFileLocation));
        log.info("Settings file successfully loaded: " + connectionFileLocation);
    }

    private synchronized void updateSettingsFile() throws IOException {
        log.info("Updating / saving settings file: " + connectionFileLocation);
        CLIENT_SETTINGS.storeToXML(new FileOutputStream(connectionFileLocation),
                ApplicationConstants.INTERNAL_FULL_NAME + " client settings file", ApplicationConstants.DEFAULT_FILE_ENCODING);
        log.info("Settings file successfully updated: " + connectionFileLocation);
    }
}
