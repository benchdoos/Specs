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

import com.mmz.specs.application.core.server.service.ClientConnection;
import com.mmz.specs.application.core.server.service.ServerSocketService;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import static com.mmz.specs.socket.SocketConstants.HELLO_COMMAND;
import static com.mmz.specs.socket.SocketConstants.QUIT_COMMAND;

public class ServerSocketDialog implements Runnable {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    Socket client;
    private ClientConnection connection;

    public ServerSocketDialog(ClientConnection connection) {
        this.connection = connection;
        this.client = connection.getSocket();

    }

    public ClientConnection getConnection() {
        return connection;
    }

    @Override
    public void run() {
        log.info("Thread started for client: " + connection);

        try {
            while (!client.isClosed()) {
                Thread currentThread = Thread.currentThread();
                final boolean interrupted = currentThread.isInterrupted();
                log.trace("Current thread name: " + currentThread.getName());
                log.trace("Current thread is interrupted: " + interrupted);

                if (!interrupted) {
                    DataInputStream in = new DataInputStream(client.getInputStream());
                    manageCommand(in.readUTF());
                    in.close();
                }
            }
        } catch (IOException e) {
            log.warn("Got IOException from: " + connection, e);
        }
    }

    private void manageCommand(final String command) throws IOException {
        log.debug("Got command from client: " + command);
        switch (command) {
            case HELLO_COMMAND:
                String name = new ServerSocketDialogUtils(client).getPcName();
                log.info("User connected: " + name); // TODO manage this to server somewhere
                break;
            case QUIT_COMMAND:
                onQuitCommand();
                break;
            default:
                log.warn("Unknown command: " + command);
                break;
        }
    }

    private void onQuitCommand() throws IOException {
        ServerSocketService.getInstance().closeClientConnection(connection);
        if (!client.isClosed()) {
            client.close();
        }

        try {
            connection.close();
        } catch (IOException e) {
            log.warn("Could not close connection at ServerSocketDialog: " + connection);
        }
    }
}