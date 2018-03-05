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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerSocketService {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final ServerSocketService ourInstance = new ServerSocketService();

    private static final ExecutorService executorService = Executors.newFixedThreadPool(ServerConstants.SERVER_SOCKED_THREAD_POOL_SIZE);
    private final HashMap<ClientConnection, Thread> connections = new HashMap<>();
    /*private static LinkedList<ClientConnection> connections = new LinkedList<>();
    private static LinkedList<Runnable> threads = new LinkedList<>();*/
    private long genThreadId = 0;
    private boolean isNotClosing = true;
    private ServerSocketConnectionPool serverSocketConnectionPool;

    private ServerSocketService() {
    }

    public static ServerSocketService getInstance() {
        return ourInstance;
    }

    public void startSocketService() {
        startServerSocketConnectionPool();
    }

    public void stopSocketService() {
        stopServerSocketConnections();
    }


    public ClientConnection getClientConnection() throws IOException {
        ClientConnection client = new ClientConnectionImpl();
        client.setSession(ServerDBConnectionPool.getInstance().getSession());
        client.setSocket(ServerSocketConnectionPool.getInstance().getClient());
        return client;
    }


    public void createConnections() throws IOException {
        while (this.isNotClosing) {
            log.info("Server is waiting for new connection at: " + this.serverSocketConnectionPool.getServerInfo());

            ClientConnection connection = getClientConnection();
            log.info("Server got a new connection at: " + connection);

            ServerSocketDialog dialog = new ServerSocketDialog(connection);
            Thread thread = new Thread(dialog, "Thread-" + this.genThreadId + " for Client connection with socket: " + connection.getSocket());
            registerClientConnection(connection, thread);
            thread.start();
            this.genThreadId++;
        }
    }

    private void startServerSocketConnectionPool() {
        log.info("Starting server socket connection pool");
        this.serverSocketConnectionPool = ServerSocketConnectionPool.getInstance();
        try {
            log.info("Is server currently started: " + this.serverSocketConnectionPool.isServerStarted());
            this.serverSocketConnectionPool.startServer();
            log.info("Is Server successfully started: " + this.serverSocketConnectionPool.getServerInfo());
        } catch (IOException e) {
            log.error("Could not start server at: " + this.serverSocketConnectionPool.getServerInfo());
        }

    }

    private void stopServerSocketConnections() {
        try {
            this.isNotClosing = false;
            closeAll();
            this.serverSocketConnectionPool.stopServerSocketConnectionPool();
            log.info("ServerSocketService successfully stopped all connections");
        } catch (IOException e) {
            log.warn("Could not close stop ServerSocket connection pool", e);
        }
    }


    private void registerClientConnection(ClientConnection client, Thread thread) {
        /*if (!connections.containsKey(client)) {
            connections.put(client, runnable);
        }*/
        this.connections.put(client, thread); // TODO test this
    }

    private void unregisterClientConnection(ClientConnection client) {
        /*connections.get(client).run();*/ //for what????
        this.connections.remove(client);
    }

    public void closeClientConnection(ClientConnection connection) throws IOException {
        log.debug("Trying to close connection: " + connection);
        unregisterClientConnection(connection);
        connection.close();
        log.info("Connection successfully closed: " + connection);

    }

    public void closeAll() { //FixME ConcurrentModificationException, read https://habrahabr.ru/post/325426/
        log.info("Removing all registered connections");
        AtomicInteger connectionsCount = new AtomicInteger();

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

        int totalConnectionsCount = this.connections.size();
        this.connections.clear();
        if (connectionsCount.get() == totalConnectionsCount) {
            log.info("Closed: " + connectionsCount + " connections from total: " + totalConnectionsCount);
        } else {
            log.warn("Closed: " + connectionsCount + " connections from total: " + totalConnectionsCount);
        }
    }

    public int getServerPort() {
        if (this.serverSocketConnectionPool != null) {
            return this.serverSocketConnectionPool.getServerPort();
        } else return 0;
    }

    public int getConnectedClientsCount() {
        return this.connections.size();
    }

    public List<ClientConnection> getConnectedClientsList() {
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
