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

package com.mmz.specs.connection;

import com.mmz.specs.application.ApplicationException;
import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.core.server.ServerStartException;
import com.mmz.specs.application.core.server.service.ServerLogMessage;
import com.mmz.specs.application.core.server.service.ServerMonitoringBackgroundService;
import com.mmz.specs.application.managers.ServerSettingsManager;
import com.mmz.specs.application.utils.Logging;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Metamodel;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.EntityType;
import javax.swing.*;
import java.net.Socket;

public class ServerDBConnectionPool {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private static volatile ServerDBConnectionPool instance;
    private static SessionFactory ourSessionFactory = null;

    private static String dbConnectionUrl;
    private static String connectionUsername;
    private static String connectionPassword;
    private Socket transactionClient = null;

    private ServerDBConnectionPool() {
        prepareServerSettings();
    }

    public static ServerDBConnectionPool getInstance() {
        ServerDBConnectionPool localInstance = instance;
        if (localInstance == null) {
            synchronized (ServerDBConnectionPool.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ServerDBConnectionPool();
                }
            }
        }
        return localInstance;
    }

    private static SessionFactory createConnection() throws ApplicationException {
        try {
            SessionFactory factory;
            Configuration configuration = new Configuration();
            configuration.configure(ServerDBConnectionPool.class.getResource("/hibernate/hibernate.cfg.xml"));
            configuration.setProperty(HibernateConstants.CP_DB_CONNECTION_URL_KEY, dbConnectionUrl);
            configuration.setProperty(HibernateConstants.CP_CONNECTION_USERNAME_KEY, connectionUsername);
            configuration.setProperty(HibernateConstants.CP_CONNECTION_PASSWORD_KEY, connectionPassword);
            log.info("Creating Hibernate connection at: " + dbConnectionUrl
                    + " username: " + connectionUsername + " password length: " + connectionPassword.length());
            factory = configuration.buildSessionFactory();
            log.info("Hibernate connection at: " + dbConnectionUrl + " successfully started.");
            return factory;
        } catch (Throwable ex) {
            throw new ApplicationException(ex);
        }
    }


    private static void testConnection(Session session) {
        System.out.println("querying all the managed entities...");
        final Metamodel metamodel = session.getSessionFactory().getMetamodel();
        for (EntityType<?> entityType : metamodel.getEntities()) {
            final String entityName = entityType.getName();
            Query query = session.createQuery("select count(*) from " + entityName);
            long count = ((Long) query.uniqueResult()).intValue();
            System.out.println("executing: " + query.getQueryString() + " Row count: " + count);
        }
        ServerLogMessage message = new ServerLogMessage("Пул подключений к БД успешно запущен",
                ServerLogMessage.ServerLogMessageLevel.SUCCESS);
        ServerMonitoringBackgroundService.getInstance().addMessage(message);
    }

    private static void checkConnectionProperty(String serverDbConnectionUrl, String exceptionMessage) throws ServerStartException {
        if (dbConnectionUrl == null) {
            dbConnectionUrl = serverDbConnectionUrl;
            ServerStartException exception = new ServerStartException(exceptionMessage
                    + ApplicationConstants.SETTINGS_FILE_PATH);
            if (dbConnectionUrl != null) {
                if (dbConnectionUrl.isEmpty()) {
                    throw exception;
                }
            } else throw exception;
        }
    }

    public void startDBConnectionPool() throws ServerStartException {
        prepareServer();
    }

    public Session getSession() throws HibernateException {
        try {
            SessionFactory localFactory = ourSessionFactory;
            if (localFactory == null) {
                synchronized (SessionFactory.class) {
                    localFactory = ourSessionFactory;
                    if (localFactory == null) {
                        ourSessionFactory = localFactory = createConnection();
                    }
                }
            }
            return localFactory.openSession();
        } catch (ApplicationException e) {
            JOptionPane.showMessageDialog(null, "Не удалось создать подключение к БД.",
                    "Ошибка подключения", JOptionPane.WARNING_MESSAGE);
            throw new HibernateException("Could not create DB connection pool at: " + dbConnectionUrl
                    + " username: " + connectionUsername + " password length: " + connectionPassword.length(), e);
        }
    }

    private void createDaoSession() {
        try (Session session = getSession()) {
            testConnection(session);
        }
    }

    private void prepareServerSettings() {
        dbConnectionUrl = ServerSettingsManager.getInstance().getServerDbConnectionUrl();
        connectionUsername = ServerSettingsManager.getInstance().getServerDbUsername();
        connectionPassword = ServerSettingsManager.getInstance().getServerDbPassword();
    }

    private void prepareServer() throws ServerStartException {
        log.info("Starting server DB connection pool");
        if (ourSessionFactory == null) {
            try {
                checkConnectionSettings();
                createDaoSession();
            } catch (ServerStartException e) {
                log.error("DB connection settings are incorrect", e);
                throw new ServerStartException(e);
            } catch (PersistenceException e) {
                log.error("Could not establish hibernate connection to server", e);
            }
        } else throw new ServerStartException("DB connection already started");
    }

    private void checkConnectionSettings() throws ServerStartException {
        checkConnectionProperty(ServerSettingsManager.getInstance().getServerDbConnectionUrl(), "Could not find DB Connection URL. Set correct value at settings file: ");

        checkConnectionProperty(ServerSettingsManager.getInstance().getServerDbUsername(), "Could not find DB Username. Set correct value at settings file: ");

        checkConnectionProperty(ServerSettingsManager.getInstance().getServerDbPassword(), "Could not find DB Password. Set correct value at settings file: ");
    }

    public void stopDBConnectionPool() {
        ourSessionFactory.close();
        ourSessionFactory = null;
    }

    public boolean bindTransaction(Socket client) {
        if (transactionClient == null) {
            transactionClient = client;
            return true;
        }
        return false;
    }

    public void unbindTransaction() {
        transactionClient = null;
        log.info("Transaction successfully unbinded");
    }

    public boolean equalsTransaction(Socket client) {
        if (transactionClient != null) {
            return transactionClient.equals(client);
        }
        return false;
    }

    public boolean transactionAlive() {
        return transactionClient != null;
    }
}
