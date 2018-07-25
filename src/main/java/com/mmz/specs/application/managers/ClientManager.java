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
import com.mmz.specs.application.gui.client.ClientConfigurationWindow;
import com.mmz.specs.application.gui.client.ClientMainWindow;
import com.mmz.specs.application.utils.FrameUtils;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

public class ClientManager {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static volatile ClientManager instance;

    @SuppressWarnings("MethodRefCanBeReplacedWithLambda")
    private ClientManager() {
        loadClientSettings();

        ClientMainWindow clientMainWindow = new ClientMainWindow();
        if (ClientSettingsManager.getInstance().getClientMainWindowLocation().equals(new Point(-1, -1))) {
            clientMainWindow.setLocation(FrameUtils.getFrameOnCenter(null, clientMainWindow));
        } else {
            clientMainWindow.setLocation(ClientSettingsManager.getInstance().getClientMainWindowLocation());
        }
        clientMainWindow.setVisible(true);
    }

    public static ClientManager getInstance() {
        ClientManager localInstance = instance;
        if (localInstance == null) {
            synchronized (ClientManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ClientManager();
                }
            }
        }
        return localInstance;
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
        }
    }
}
