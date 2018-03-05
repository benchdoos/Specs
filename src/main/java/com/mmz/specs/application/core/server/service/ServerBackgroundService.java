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
        Runnable runnable = this::startServerMainBackgroundService;

        serverBackGroundThread = new Thread(runnable);
        serverBackGroundThread.start();
    }

    public static ServerBackgroundService getInstance() {
        return ourInstance;
    }

    private void startServerMainBackgroundService() {
        startServerMonitoring();

        startServerDBConnectionPool();

        startServerSocketService();

        createConnections();
    }

    private void createConnections() {
        try {
            this.serverSocketService.createConnections();
        } catch (IOException e) {
            if (!this.isServerShuttingDown) {
                log.error("Could not create socket connections", e);
            } else {
                log.info("ServerSocket is shut down");
            }
        }
    }

    private void startServerSocketService() {
        this.serverSocketService = ServerSocketService.getInstance();
        this.serverSocketService.startSocketService();

    }

    private void startServerDBConnectionPool() {
        try {
            log.info("Starting server db connection pool");
            this.serverDBConnectionPool = ServerDBConnectionPool.getInstance();
            this.serverDBConnectionPool.startDBConnectionPool();
        } catch (ServerStartException e) {
            log.warn("Could not start DB connection pool in background", e);
            JOptionPane.showMessageDialog(null,
                    "Не удалось установить подключение к БД\r\n" + e.getLocalizedMessage(),
                    "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startServerMonitoring() {
        log.info("Starting server monitoring service");
        this.monitoringBackgroundService = ServerMonitoringBackgroundService.getInstance();
        this.monitoringBackgroundService.startMonitoring();
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

    public int getServerPort() {
        if (this.serverSocketService != null) {
            return this.serverSocketService.getServerPort();
        } else return 0;
    }
}
