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
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.HibernateConstants;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.socket.SocketConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Metamodel;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javax.persistence.metamodel.EntityType;
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
    private Session session;

    private ClientBackgroundService() {
        createConnection();
    }

    public void createConnection() {
        if (!isConnected()) {
            createNewServerConnection();
        }
    }

    public boolean isConnected() {
        final FtpUtils ftpUtils = FtpUtils.getInstance();
        if (socket != null) {
            if (socket.isClosed()) {
                if (ftpUtils.isConnected()) {
                    try {
                        ftpUtils.disconnect();
                    } catch (IOException ignore) {
                    }
                }
                return false;
            } else {
                try {
                    outputStream.writeUTF(SocketConstants.TESTING_CONNECTION_COMMAND);
                    return true;
                } catch (IOException e) {
                    if (ftpUtils.isConnected()) {
                        try {
                            ftpUtils.disconnect();
                        } catch (IOException ignore) {
                        }
                    }
                    return false;
                }
            }
        } else {
            if (ftpUtils.isConnected()) {
                try {
                    ftpUtils.disconnect();
                } catch (IOException ignore) {
                }
            }
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
                socket.setSoTimeout(3000);
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

    public static ClientBackgroundService getInstance() {
        return ourInstance;
    }

    public Session getSession() {
        if (session != null) {
            return session;
        } else {
            if (outputStream != null) {
                return getSessionFromServer();

            } else return null;
        }
    }

    public Session getSessionFromServer() {
        try {
            outputStream.writeUTF(SocketConstants.GIVE_SESSION);
            String dbAddress = dataInputStream.readUTF();
            String dbUsername = dataInputStream.readUTF();
            String dbPassword = dataInputStream.readUTF();
            dbAddress = dbAddress.replace("localhost", socket.getInetAddress().getHostAddress());

            Configuration configuration = new Configuration();
            configuration.configure(ServerDBConnectionPool.class.getResource("/hibernate/hibernate.cfg.xml"));
            configuration.setProperty(HibernateConstants.CP_DB_CONNECTION_URL_KEY, dbAddress);
            configuration.setProperty(HibernateConstants.CP_CONNECTION_USERNAME_KEY, dbUsername);
            configuration.setProperty(HibernateConstants.CP_CONNECTION_PASSWORD_KEY, dbPassword);
            configuration.setProperty(HibernateConstants.CP_CONNECTION_POOL_SIZE, "1"); // only 1 connection for client, important!!!
            log.info("Creating Hibernate connection at: " + dbAddress
                    + " username: " + dbUsername + " password length: " + dbPassword.length());
            SessionFactory factory = configuration.buildSessionFactory();
            Session session = factory.openSession();
            this.session = session;
            return session;
        } catch (Exception e) {
            log.warn("Could not ask server for session", e);
        }

        return null;
    }

    public void closeConnection() throws IOException {
        log.info("Trying to close connection to server");
        if (isConnected()) {
            log.info("Writing to server that we're quiting");
            outputStream.writeUTF(SocketConstants.QUIT_COMMAND);
            socket.close();
        }
        log.info("Connection to server successfully closed");
    }

    public Socket getSocket() {
        return socket;
    }

    public void refreshSession() {
        if (session != null) {
            final Metamodel metamodel = session.getSessionFactory().getMetamodel();
            for (EntityType<?> entityType : metamodel.getEntities()) {
                final String entityName = entityType.getName();
                final Query query = session.createQuery("from " + entityName);
                for (Object o : query.list()) {
                    session.refresh(o);
                }
            }
        }
    }
}
