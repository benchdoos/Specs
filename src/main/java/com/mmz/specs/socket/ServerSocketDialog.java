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
import com.mmz.specs.application.core.server.service.ServerLogMessage;
import com.mmz.specs.application.core.server.service.ServerMonitoringBackgroundService;
import com.mmz.specs.application.core.server.service.ServerSocketService;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.service.UsersService;
import com.mmz.specs.service.UsersServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import static com.mmz.specs.socket.SocketConstants.*;

public class ServerSocketDialog implements Runnable {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private final Socket client;
    private final ClientConnection connection;

    public ServerSocketDialog(ClientConnection connection) {
        this.connection = connection;
        this.client = connection.getSocket();

    }

    @Override
    public void run() {
        log.info("Thread started for client: " + connection);
        Thread currentThread = Thread.currentThread();

        try (DataInputStream in = new DataInputStream(client.getInputStream())) {
            while (!client.isClosed()) {
                final boolean interrupted = currentThread.isInterrupted();

                if (!interrupted) {
                    manageCommand(in.readUTF());
                }
            }
        } catch (IOException e) {
            log.warn("Got IOException from: " + connection, e);
            try {
                ServerSocketService.getInstance().closeClientConnection(connection);
            } catch (IOException e1) {
                log.warn("Could not close connection", e1);
                ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                        "Не удалось закрыть socket-соединение с: " + connection.getSocket().getInetAddress() + " " + e1.getLocalizedMessage(),
                        ServerLogMessage.ServerLogMessageLevel.WARN));
            }
        } finally {
            try {
                log.info("Trying to close ClientConnection " + connection);
                connection.close();
                log.info("ClientConnection closed successfully.");
            } catch (IOException e) {
                log.warn("Could not close ClientConnection properly");
            }
        }
    }

    private void manageCommand(final String command) throws IOException {
        switch (command) {
            case TESTING_CONNECTION_COMMAND:
                break;

            case GIVE_SESSION:
                log.info("User asking Session, giving it. Command: " + command);
                new ServerSocketDialogUtils(client).sendSessionInfo();
                break;

            case USER_LOGIN:
                log.info("User {} is logged in.", client);
                try {
                    String userName = new ServerSocketDialogUtils(client).getUserName();
                    UsersService usersService = new UsersServiceImpl();
                    UsersEntity userByUsername = usersService.getUserByUsername(userName);
                    if (userByUsername != null) {
                        connection.setUser(userByUsername);
                        log.info("User {} is logged in as: {}", client, userName);
                        ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                                "Пользователь " + userByUsername.getUsername()
                                        + " (" + userByUsername.getName() + " " + userByUsername.getSurname() + ")" +
                                        " вошел в систему с компьютера: " + client.getInetAddress(),
                                ServerLogMessage.ServerLogMessageLevel.INFO));
                    }
                } catch (Exception e) {
                    log.warn("Could not get username from client: {}", client, e);
                }
                break;


            case USER_LOGOUT:
                if (connection.getUser() != null) {
                    log.info("User {} is logged out.", connection.getUser().getUsername());
                    ServerMonitoringBackgroundService.getInstance().addMessage(new ServerLogMessage(
                            "Пользователь " + connection.getUser().getUsername()
                                    + " (" + connection.getUser().getName() + " "
                                    + connection.getUser().getSurname() + ")" +
                                    " вышел из системы с компьютера: " + client.getInetAddress(),
                            ServerLogMessage.ServerLogMessageLevel.INFO));
                    connection.setUser(null);
                }
                break;
            case QUIT_COMMAND:
                log.info("Quiting connection for client: " + connection + " Command: " + command);
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