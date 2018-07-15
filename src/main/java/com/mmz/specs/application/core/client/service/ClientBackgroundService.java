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

import com.mmz.specs.application.core.server.ServerConstants;
import com.mmz.specs.application.managers.ClientSettingsManager;
import com.mmz.specs.application.utils.FtpUtils;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.HibernateConstants;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.model.UsersEntity;
import com.mmz.specs.socket.SocketConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Metamodel;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.json.JSONObject;

import javax.persistence.metamodel.EntityType;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import static com.mmz.specs.connection.HibernateConstants.*;

public class ClientBackgroundService {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private static final ClientBackgroundService ourInstance = new ClientBackgroundService();
    private static SessionFactory factory;
    private DataOutputStream outputStream;
    private DataInputStream dataInputStream;
    private Socket socket;
    private String serverAddress;
    private int serverPort;

    public static ClientBackgroundService getInstance() {
        return ourInstance;
    }

    public void createConnection() throws IOException {
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

    private void createNewServerConnection() throws IOException {
        serverAddress = ClientSettingsManager.getInstance().getServerAddress();
        serverPort = ServerConstants.SERVER_DEFAULT_SOCKET_PORT;
        socket = new Socket(serverAddress, serverPort);
        socket.setSoTimeout(3000);
        outputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());
    }

    public Session getSession() {
        if (factory != null) {
            return factory.openSession();
        } else {
            if (outputStream != null) {
                factory = getSessionFactoryFromServer();
                if (factory != null) {
                    return factory.openSession();
                }
            }
        }
        return null;
    }

    private SessionFactory getSessionFactoryFromServer() {
        String params = null;
        try {
            log.info("Asking server for session, command: " + SocketConstants.GIVE_SESSION);
            outputStream.writeUTF(SocketConstants.GIVE_SESSION);
            params = dataInputStream.readUTF();
            JSONObject obj = new JSONObject(params);

            String dbAddress = obj.getString(CP_DB_CONNECTION_URL_KEY);
            String dbUsername = obj.getString(CP_CONNECTION_USERNAME_KEY);
            String dbPassword = obj.getString(CP_CONNECTION_PASSWORD_KEY);

            log.info("Got configuration from server: {} {} ({})", dbAddress, dbUsername, dbPassword.length());

            return createFactory(dbAddress, dbUsername, dbPassword);
        } catch (Throwable e) {
            log.warn("Could not ask server for session, params: {}", params, e);
        }

        return null;
    }

    private SessionFactory createFactory(String dbAddress, String dbUsername, String dbPassword) {
        log.info("Creating configuration");
        Configuration configuration = new Configuration();
        log.info("Loading hibernate configuration file");
        configuration.configure(ServerDBConnectionPool.class.getResource("/hibernate/hibernate.cfg.xml"));
        log.info("Configuration file successfully loaded");
        log.info("Setting hibernate connection configuration from server");

        configuration.setProperty(CP_DB_CONNECTION_URL_KEY, dbAddress);
        configuration.setProperty(CP_CONNECTION_USERNAME_KEY, dbUsername);
        configuration.setProperty(CP_CONNECTION_PASSWORD_KEY, dbPassword);

        configuration.setProperty(HibernateConstants.CP_CONNECTION_POOL_SIZE, "25");

        log.info("Creating Hibernate connection at: " + dbAddress
                + " username: " + dbUsername + " password length: " + dbPassword.length());
        return configuration.buildSessionFactory();
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

    public void userLogin(UsersEntity usersEntity) throws IOException {
        if (isConnected()) {
            log.info("Writing to server that user {} is connected.", usersEntity.getUsername());
            outputStream.writeUTF(SocketConstants.USER_LOGIN);
            outputStream.writeUTF(usersEntity.getUsername());
        }
    }

    public void userLogout(UsersEntity usersEntity) throws IOException {
        if (isConnected()) {
            log.info("Writing to server that user {} is disconnected.", usersEntity.getUsername());
            outputStream.writeUTF(SocketConstants.USER_LOGOUT);
        }
    }

    public void refreshSession(Session session) {
        log.debug("Refreshing session for all entities");
        if (session != null) {
            final Metamodel metamodel = getSession().getSessionFactory().getMetamodel();
            for (EntityType<?> entityType : metamodel.getEntities()) {
                refreshCurrentEntityType(session, entityType.getName());
            }
        }
        log.debug("Refreshing session for all entities successfully finished.");
    }

    public void refreshSession(Session session, Class class_) {
        log.debug("Refreshing session for: {}", class_.getName());
        if (session != null) {
            refreshCurrentEntityType(session, class_.getName());
        }
    }

    public void refreshObject(Session session, Object o) {
        session.refresh(o);
    }


    private void refreshCurrentEntityType(Session session, String name) {
        try {
            final Query query = session.createQuery("from " + name);
            final List list = query.list();
            log.debug("Refreshing list size: {}", list.size());
            for (Object o : list) {
                session.refresh(o);
            }
            log.debug("Refreshing session for: {} successfully finished.", name);
        } catch (Exception e) {
            log.warn("Could not refresh current entity type: {}", name, e);
        }
    }

    public boolean testConnection(String address, int port) {
        boolean result = false;
        try (Socket testingSocket = new Socket(address, port)) {
            if (testingSocket.isConnected()) {
                if (!testingSocket.isClosed()) {
                    try (DataOutputStream dataOutputStream = new DataOutputStream(testingSocket.getOutputStream())) {
                        dataOutputStream.writeUTF(SocketConstants.TESTING_CONNECTION_COMMAND);
                        result = true;
                    }
                }
            }
        } catch (Exception ignored) {
            /*NOP*/
        }
        return result;
    }
}
