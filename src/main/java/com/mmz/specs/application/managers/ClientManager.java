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
import com.mmz.specs.application.core.client.service.ClientBackgroundService;
import com.mmz.specs.application.gui.client.ClientConfigurationWindow;
import com.mmz.specs.application.gui.client.ClientMainWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientManager {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final ClientManager ourInstance = new ClientManager();
    private final ClientMainWindow clientMainWindow;

    private ClientManager() {
        loadClientSettings();

        ClientBackgroundService clientBackgroundService = ClientBackgroundService.getInstance();
        clientBackgroundService.createConnection();

        clientMainWindow = new ClientMainWindow();
        clientMainWindow.setLocation(FrameUtils.getFrameOnCenter(null, clientMainWindow));
        clientMainWindow.setVisible(true);
    }

    private void loadClientSettings() {
        log.info("Loading client settings");
        ClientSettingsManager settingsManager = ClientSettingsManager.getInstance();
        try {
            settingsManager.loadSettingsFile();
            if (!settingsManager.isSettingsFileCorrect()) {
                throw new Exception("Settings file is not correct");
            }
        } catch (Exception e) {
            log.warn("Could not load client settings file at: " + ApplicationConstants.CLIENT_SETTINGS_FILE, e);

            ClientConfigurationWindow clientConfigurationWindow = new ClientConfigurationWindow();
            clientConfigurationWindow.setLocation(FrameUtils.getFrameOnCenter(null, clientConfigurationWindow));
            clientConfigurationWindow.setVisible(true);
            //loadClientSettings(); //TODO check this properly
        }
    }

    public static ClientManager getInstance() {
        return ourInstance;
    }

    public void notifyClientMainWindow(ClientConnectionStatus status) {
        switch (status) {
            case CONNECTION_REFUSED:
                clientMainWindow.notifyConnectionRefused();
                break;
            case ERROR:
                break;
            case SECCUSS:
                break;
        }
    }

    public enum ClientConnectionStatus {SECCUSS, ERROR, CONNECTION_REFUSED}
}
