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

public class ServerBackgroundService {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private static ServerBackgroundService ourInstance = new ServerBackgroundService();
    private Thread serverBackGroundThread;
    private ServerSocketService serverSocketService;
    private ServerMonitoringBackgroundService monitoringBackgroundService;
    private ServerDBConnectionPool serverDBConnectionPool;
    private boolean isServerShuttingDown = false;

    private int onlineUsersCount;

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
            serverSocketService.createConnections();
        } catch (IOException e) {
            if (!isServerShuttingDown) {
                log.error("Could not create socket connections", e);
            } else {
                log.info("ServerSocket is shut down");
            }
        }
    }

    private void startServerSocketService() {
        serverSocketService = ServerSocketService.getInstance();
        serverSocketService.startSocketService();

    }

    private void startServerDBConnectionPool() {
        try {
            log.info("Starting server db connection pool");
            serverDBConnectionPool = ServerDBConnectionPool.getInstance();
            serverDBConnectionPool.startDBConnectionPool();
        } catch (ServerStartException e) {
            log.warn("Could not start DB connection pool in background", e);
            JOptionPane.showMessageDialog(null,
                    "Не удалось установить подключение к БД\r\n" + e.getLocalizedMessage(),
                    "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startServerMonitoring() {
        log.info("Starting server monitoring service");
        monitoringBackgroundService = ServerMonitoringBackgroundService.getInstance();
        monitoringBackgroundService.startMonitoring();
    }


    public void stopServerMainBackgroundService() {
        log.info("Stopping Server Main Background Service");
        isServerShuttingDown = true;
        stopServerSocketService();
        stopServerDBConnectionPool();
        stopMonitoringBackgroundService();

        serverBackGroundThread.interrupt();
    }

    private void stopServerSocketService() {
        serverSocketService.stopSocketService();
    }

    private void stopServerDBConnectionPool() {
        serverDBConnectionPool.stopDBConnectionPool();
    }

    private void stopMonitoringBackgroundService() {
        monitoringBackgroundService.stopMonitoring();
    }

    public int getOnlineUsersCount() {
        return onlineUsersCount; //TODO find all connected users, return back
    }
}
