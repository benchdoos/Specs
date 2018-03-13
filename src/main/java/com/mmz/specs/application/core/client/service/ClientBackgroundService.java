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

package com.mmz.specs.application.core.client.service;

import com.mmz.specs.application.managers.ClientManager;
import com.mmz.specs.application.managers.ClientSettingsManager;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.socket.SocketConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientBackgroundService {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final ClientBackgroundService ourInstance = new ClientBackgroundService();
    private DataOutputStream outputStream;
    private DataInputStream dataInputStream;
    private Socket socket;
    private String serverAddress;
    private int serverPort;


    private ClientBackgroundService() {

    }

    public static ClientBackgroundService getInstance() {
        return ourInstance;
    }

    public void createConnection() {
        if (!isConnected()) {
            createNewServerConnection();
        }
    }

    public boolean isConnected() {
        if (socket != null) {
            if (socket.isClosed()) {
                return false;
            } else {
                try {
                    /*return socket.getInetAddress().isReachable(3000);*/
                    outputStream.writeUTF(SocketConstants.TESTING_CONNECTION_COMMAND);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private void createNewServerConnection() {
        serverAddress = ClientSettingsManager.getInstance().getServerAddress();
        serverPort = ClientSettingsManager.getInstance().getServerPort();
        Runnable runnable = () -> {
            log.info("Trying to connect to " + serverAddress + ":" + serverPort);
            try {
                socket = new Socket(serverAddress, serverPort);
                outputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                outputStream.writeUTF(SocketConstants.HELLO_COMMAND);
                String answer = dataInputStream.readUTF();
                System.out.println("answer is :" + answer);
                outputStream.writeUTF("Client id: " + Thread.currentThread().getId() + " address:" + InetAddress.getLocalHost().getHostAddress());
                log.info("Successfully connected to server: " + socket);
            } catch (IOException e) {
                log.warn("Could not establish connection", e);
                ClientManager.getInstance().notifyClientMainWindow(ClientManager.ClientConnectionStatus.CONNECTION_REFUSED);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void closeConnection() throws IOException {
        log.info("Trying to close connection to server");
        if (socket != null) {
            if (!socket.isClosed()) {
                if (outputStream != null) {
                    System.out.println("writing to server that we're quiting");
                    outputStream.writeUTF(SocketConstants.QUIT_COMMAND);
                }
                socket.close();
                log.info("Connection to server successfully closed");
            }
        }
    }
}
