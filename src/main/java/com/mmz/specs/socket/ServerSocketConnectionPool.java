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

package com.mmz.specs.socket;

import com.mmz.specs.application.ApplicationException;
import com.mmz.specs.application.core.server.ServerConstants;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketConnectionPool {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final ServerSocketConnectionPool ourInstance = new ServerSocketConnectionPool();

    private ServerSocket server;

    private int serverSocketPort;

    private ServerSocketConnectionPool() {
        serverSocketPort = ServerConstants.SERVER_DEFAULT_SOCKET_PORT;
    }

    public static ServerSocketConnectionPool getInstance() {
        return ourInstance;
    }

    /**
     * Overloads serverSocketPort
     */
    public void setServerSocketPort(int serverSocketPort) throws ApplicationException {
        if (isServerStarted()) {
            this.serverSocketPort = serverSocketPort;
        } else {
            throw new ApplicationException("Could not set server port " + serverSocketPort + ", server is already running: " + server);
        }
    }


    public void startServer() throws IOException {
        log.info("Starting server if not started. Is started: " + isServerStarted());

        if (!isServerStarted()) {
            server = new ServerSocket(serverSocketPort, 0, InetAddress.getByName(ServerConstants.SERVER_DEFAULT_ADDRESS_BIND));
            log.info("Server successfully started: " + server);
        } else {
            throw new IOException("Server already started: " + server);
        }
    }

    public Socket getClient() throws IOException {
        return server.accept();
    }

    public boolean isServerStarted() {
        return server != null && !server.isClosed();
    }

    public String getServerInfo() {
        if (server != null) {
            return server.toString();
        } else return "";
    }

    public void stopServerSocketConnectionPool() throws IOException {
        if (server != null) {
            if (!server.isClosed()) {
                server.close();
            }
        }
    }

    public int getServerPort() {
        return serverSocketPort;
    }
}
