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
import com.mmz.specs.connection.ServerDBConnectionPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class ServerBackgroundService {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private static final ServerBackgroundService ourInstance = new ServerBackgroundService();
    private final Thread serverBackGroundThread;
    private ServerSocketService serverSocketService;
    private ServerMonitoringBackgroundService monitoringBackgroundService;
    private ServerDBConnectionPool serverDBConnectionPool;
    private boolean isServerShuttingDown = false;

    private ServerBackgroundService() {
        Runnable runnable = () -> startServerMainBackgroundService();

        serverBackGroundThread = new Thread(runnable);
        serverBackGroundThread.start();
    }

    public static ServerBackgroundService getInstance() {
        return ourInstance;
    }

    private void startServerMainBackgroundService() {
        try {
            startServerMonitoring();
        } catch (Exception e) {
            log.warn("Could not establish monitoring service", e);
            new ServerLogMessage("Не удалось запустить мониторинг сервера" + e.getLocalizedMessage(), ServerLogMessage.ServerLogMessageLevel.WARN);
        }

        try {
            startServerDBConnectionPool();

            startServerSocketService();

            createConnections();
        } catch (Throwable e) {
            log.warn("Could not establish connections", e);
            new ServerLogMessage("Не удалось создать подключения " + e.getLocalizedMessage(), ServerLogMessage.ServerLogMessageLevel.WARN);
        }
    }

    private void startServerMonitoring() {
        try {
            log.info("Starting server monitoring service");
            this.monitoringBackgroundService = ServerMonitoringBackgroundService.getInstance();
            this.monitoringBackgroundService.startMonitoring();
        } catch (Throwable e) {
            log.warn("On server starting Monitoring got an exception: ", e);
        }
    }

    private void startServerDBConnectionPool() {
        try {
            try {
                log.info("Starting server db connection pool");

                ServerLogMessage message = new ServerLogMessage("Запускается пул подключений к БД",
                        ServerLogMessage.ServerLogMessageLevel.INFO);
                ServerMonitoringBackgroundService.getInstance().addMessage(message);

                this.serverDBConnectionPool = ServerDBConnectionPool.getInstance();
                this.serverDBConnectionPool.startDBConnectionPool();
            } catch (ServerStartException e) {
                log.warn("Could not start DB connection pool in background", e);
                ServerLogMessage message = new ServerLogMessage(
                        "Не удалось запустить пул подключений к БД. " + e.getLocalizedMessage(),
                        ServerLogMessage.ServerLogMessageLevel.WARN);
                ServerMonitoringBackgroundService.getInstance().addMessage(message);

                JOptionPane.showMessageDialog(null,
                        "Не удалось установить подключение к БД\r\n" + e.getLocalizedMessage(),
                        "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Throwable e) {
            log.warn("On server starting DBCP got an exception: ", e);
        }
    }

    private void startServerSocketService() {
        try {
            this.serverSocketService = ServerSocketService.getInstance();
            this.serverSocketService.startSocketService();
        } catch (Throwable e) {
            log.warn("On server starting socket service got an exception: ", e);
        }
    }

    private void createConnections() {
        try {
            try {
                this.serverSocketService.createConnections();
            } catch (IOException e) {
                if (!this.isServerShuttingDown) {
                    log.error("Could not create socket connections", e);
                } else {
                    log.info("ServerSocket is shut down");
                }
            }
        } catch (Throwable e) {
            log.warn("On server starting connections got an exception: ", e);
        }
    }

    public void stopServerMainBackgroundService() {
        log.info("Stopping Server Main Background Service");
        this.isServerShuttingDown = true;
        stopServerSocketService();
        stopServerDBConnectionPool();
        stopMonitoringBackgroundService();

        this.serverBackGroundThread.interrupt();
    }

    private void stopServerSocketService() {
        this.serverSocketService.stopSocketService();
    }

    private void stopServerDBConnectionPool() {
        this.serverDBConnectionPool.stopDBConnectionPool();
    }

    private void stopMonitoringBackgroundService() {
        this.monitoringBackgroundService.stopMonitoring();
    }

    public int getOnlineUsersCount() {
        if (this.serverSocketService != null) {
            return this.serverSocketService.getConnectedClientsCount();
        } else return 0;
    }

    public List<ClientConnection> getConnectedClientsList() {
        if (this.serverSocketService != null) {
            return this.serverSocketService.getConnectedClientsList();
        } else return null;
    }
}
