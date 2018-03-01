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

package com.mmz.specs.application.core.server.service;

import com.mmz.specs.application.core.server.ServerStartException;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.ServerConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

public class ServerMainBackgroundService {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private static ServerMainBackgroundService ourInstance = new ServerMainBackgroundService();
    private static ServerMonitoringBackgroundService monitoringBackgroundService;
    private int onlineUsersCount;

    private ServerMainBackgroundService() {
        startServerMainBackgroundService();
    }

    private void startServerMainBackgroundService() {
        monitoringBackgroundService = ServerMonitoringBackgroundService.getInstance();
        monitoringBackgroundService.startMonitoring();

        try {
            new ServerConnectionPool();
        } catch (ServerStartException e) {
            log.warn("Could not start server in background", e);
            JOptionPane.showMessageDialog(null,
                    "Не удалось установить подключение к БД\n" + e.getLocalizedMessage(),
                    "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void stopServerMainBackgroundService() {
        monitoringBackgroundService.stopMonitoring();
        //TODO stop this....
    }

    public static ServerMainBackgroundService getInstance() {
        return ourInstance;
    }

    public int getOnlineUsersCount() {
        return onlineUsersCount; //TODO find all connected users, return back
    }
}
