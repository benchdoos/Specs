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

import com.mmz.specs.application.core.server.ServerConstants;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.socket.ServerSocketConnectionPool;
import com.mmz.specs.socket.ServerSocketDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerSocketService {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static volatile ServerSocketService instance;
    private final HashMap<ClientConnection, Thread> connections = new HashMap<>();
    private boolean isNotClosing = true;
    private ServerSocketConnectionPool serverSocketConnectionPool;

    private ServerSocketService() {
    }

    public static ServerSocketService getInstance() {
        ServerSocketService localInstance = instance;
        if (localInstance == null) {
            synchronized (ServerSocketService.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ServerSocketService();
                }
            }
        }
        return localInstance;
    }

    void startSocketService() {
        startServerSocketConnectionPool();
    }

    void stopSocketService() {
        stopServerSocketConnections();
    }


    private ClientConnection getClientConnection() throws IOException {
        ClientConnection client = new ClientConnectionImpl();
        client.setSocket(ServerSocketConnectionPool.getInstance().getClient());
        return client;
    }


    void createConnections() throws IOException {
        while (this.isNotClosing) {
            log.info("Server is creating for new connection at: " + this.serverSocketConnectionPool.getServerInfo());

            ClientConnection connection = getClientConnection();
            log.info("Server got a new connection at: " + connection);
            ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                    "Подключён пользователь: " + connection.getSocket().getInetAddress(),
                    ServerLogMessage.ServerLogMessageLevel.INFO));

            ServerSocketDialog dialog = new ServerSocketDialog(connection);
            Thread thread = new Thread(dialog);
            thread.setName("Client-connection-" + thread.getId() + ":" + connection.getSocket().getInetAddress());
            registerClientConnection(connection, thread);
            thread.start();
        }
    }

    private void startServerSocketConnectionPool() {
        log.info("Starting server socket connection pool");
        ServerLogMessage message = new ServerLogMessage(
                "Запускается пул socket-соединений сервера",
                ServerLogMessage.ServerLogMessageLevel.INFO);
        ServerMonitoringBackgroundService.getInstance().addMessage(message);

        this.serverSocketConnectionPool = ServerSocketConnectionPool.getInstance();
        try {
            log.info("Is server currently started: " + this.serverSocketConnectionPool.isServerStarted());
            this.serverSocketConnectionPool.startServer();
            log.info("Is server successfully started: " + this.serverSocketConnectionPool.getServerInfo());

            ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                    "Пул socket-соединений сервера успешно запущен",
                    ServerLogMessage.ServerLogMessageLevel.SUCCESS));
        } catch (IOException e) {
            log.error("Could not start server at: " + this.serverSocketConnectionPool.getServerInfo(), e);
            ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                    "Не удалось запустить пул socket-соединений сервера" + e.getLocalizedMessage(),
                    ServerLogMessage.ServerLogMessageLevel.WARN));
            if (e instanceof BindException) {
                JOptionPane.showMessageDialog(null,
                        "Сервер уже запущен, не удалось зарезервировать порт на: localhost:"
                                + ServerConstants.SERVER_DEFAULT_SOCKET_PORT,
                        "Ошибка инициализации", JOptionPane.ERROR_MESSAGE);
                ServerBackgroundService.getInstance().stopServerMainBackgroundService();
                System.exit(-1);
            }
        }

    }

    private void stopServerSocketConnections() {
        try {
            ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                    "Закрывается пул socket-соединений сервера",
                    ServerLogMessage.ServerLogMessageLevel.SUCCESS));
            this.isNotClosing = false;
            closeAll();
            this.serverSocketConnectionPool.stopServerSocketConnectionPool();
            log.info("ServerSocketService successfully stopped all connections");
            ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                    "Пул socket-соединений сервера успешно закрыт",
                    ServerLogMessage.ServerLogMessageLevel.SUCCESS));
        } catch (IOException e) {
            log.warn("Could not close stop ServerSocket connection pool", e);
            ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                    "Пул socket-соединений сервера не был закрыт: " + e.getLocalizedMessage(),
                    ServerLogMessage.ServerLogMessageLevel.SUCCESS));
        }
    }


    private void registerClientConnection(ClientConnection client, Thread thread) {
        /*if (!connections.containsKey(client)) {
            connections.put(client, runnable);
        }*/
        this.connections.put(client, thread); // TODO test this
    }

    private void unregisterClientConnection(ClientConnection client) {
        client.setUser(null);
        System.out.println("removing " + client + " and contains:" + connections.containsKey(client));
        if (ServerDBConnectionPool.getInstance().equalsTransaction(client.getSocket())) {
            ServerDBConnectionPool.getInstance().unbindTransaction();
        }
        this.connections.remove(client);
    }

    public void closeClientConnection(ClientConnection connection) throws IOException {
        log.debug("Trying to close connection: " + connection);
        String username = "";
        if (connection.getUser() != null) {
            username = connection.getUser().getUsername() + " ";
        }

        ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                "Отключаем пользователя: " + username + "(" + connection.getSocket().getInetAddress() + ")",
                ServerLogMessage.ServerLogMessageLevel.INFO));
        unregisterClientConnection(connection);
        connection.close();
        log.info("Connection successfully closed: " + connection);
    }

    public void closeAll() { //FIXME ConcurrentModificationException, read https://habrahabr.ru/post/325426/
        log.info("Removing all registered connections");
        ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                "Отключаем всех пользователей ",
                ServerLogMessage.ServerLogMessageLevel.INFO));
        AtomicInteger connectionsCount = new AtomicInteger();

        try {
            closeConnections(connectionsCount);
        } catch (Exception e) {
            try {
                closeConnections(connectionsCount);
            } catch (Exception e1) {
                log.warn("Could not close all connections!");
            }
        }

        int totalConnectionsCount = this.connections.size();
        this.connections.clear();
        if (connectionsCount.get() == totalConnectionsCount) {
            log.info("Unbinding transaction");
            ServerDBConnectionPool.getInstance().unbindTransaction();
            log.info("Closed: " + connectionsCount + " connections from total: " + totalConnectionsCount);

            ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                    "Отключено пользователей: " + connectionsCount + " из: " + totalConnectionsCount,
                    ServerLogMessage.ServerLogMessageLevel.SUCCESS));
        } else {
            log.warn("Closed: " + connectionsCount + " connections from total: " + totalConnectionsCount);
            ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                    "Отключено пользователей: " + connectionsCount + " из: " + totalConnectionsCount,
                    ServerLogMessage.ServerLogMessageLevel.WARN));
        }
    }

    private void closeConnections(AtomicInteger connectionsCount) {
        this.connections.forEach((client, thread) -> {
            thread.interrupt();
            log.debug("Found client: " + client);
            try {
                log.debug("Closing client: " + client);
                client.close();
                connectionsCount.getAndIncrement();
            } catch (IOException e) {
                log.warn("Could not close client connection: " + client);
            }
        });
    }

    public int getConnectedClientsCount() {
        return this.connections.size();
    }

    List<ClientConnection> getConnectedClientsList() {
        Object[] objects = this.connections.keySet().toArray();
        List<ClientConnection> result = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof ClientConnection) {
                result.add((ClientConnection) object);
            }
        }
        return result;
    }
}
