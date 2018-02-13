package com.mmz.specs.application.core.server;

import com.mmz.specs.application.core.ApplicationConstants;
import com.mmz.specs.application.managers.SettingsManager;
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
import java.io.File;

public class Server {
    private static String dbConnectionUrl;
    private static String connectionUsername;
    private static String connectionPassword;
    private static SessionFactory ourSessionFactory = null;
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    public Server() throws ServerStartException {
        prepareServerSettings();
        prepareServer();
    }

    public Server(String connectionUrl, String username, String password) throws ServerStartException {
        log.info("Forcing Server settings");
        dbConnectionUrl = connectionUrl;
        connectionUsername = username;
        connectionPassword = password;
        prepareServer();
    }

    private static void createConnection() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure(new File("src/main/java/hibernate.cfg.xml"));
            /*configuration.setProperty(HibernateConstants.DB_CONNECTION_URL_KEY, "jdbc:firebirdsql:localhost/3050:F:/Developer/Java/specs/db/MMZ_SPECS.GDB?encoding=UTF8");
            configuration.setProperty(HibernateConstants.CONNECTION_USERNAME_KEY, "sysdba");
            configuration.setProperty(HibernateConstants.CONNECTION_PASSWORD_KEY, "m@sT3rK3~Y");*/
            configuration.setProperty(HibernateConstants.DB_CONNECTION_URL_KEY, dbConnectionUrl);
            configuration.setProperty(HibernateConstants.CONNECTION_USERNAME_KEY, connectionUsername);
            configuration.setProperty(HibernateConstants.CONNECTION_PASSWORD_KEY, connectionPassword);
            log.info("Creating Hibernate connection at: " + dbConnectionUrl
                    + " username: " + connectionUsername + " password length: " + connectionPassword.length());
            ourSessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static Session getSession() throws HibernateException {
        if (ourSessionFactory == null) {
            createConnection();
            if (ourSessionFactory == null) {
                throw new HibernateException("Could not create session factory");
            }
            return ourSessionFactory.openSession();
        } else {
            return ourSessionFactory.openSession();
        }
    }

    private static void createDaoSession() {
        try (Session session = getSession()) {
            System.out.println("querying all the managed entities...");
            final Metamodel metamodel = session.getSessionFactory().getMetamodel();
            for (EntityType<?> entityType : metamodel.getEntities()) {
                final String entityName = entityType.getName();
                final Query query = session.createQuery("from " + entityName);
                System.out.println("executing: " + query.getQueryString());
                for (Object o : query.list()) {
                    System.out.println("  " + o);
                }
            }
        }
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

    private void prepareServerSettings() {
        dbConnectionUrl = SettingsManager.getInstance().getServerDbConnectionUrl();
        connectionUsername = SettingsManager.getInstance().getServerDbUsername();
        connectionPassword = SettingsManager.getInstance().getServerDbPassword();
    }

    private void prepareServer() throws ServerStartException {
        log.info("Starting server connection");
        checkConnectionSettings();
        try {
            createDaoSession();
        } catch (PersistenceException e) {
            log.error("Could not establish hibernate connection to server",e);
            throw new ServerStartException();
        }
    }

    private void checkConnectionSettings() throws ServerStartException {
        checkConnectionProperty(SettingsManager.getInstance().getServerDbConnectionUrl(), "Could not find DB Connection URL. Set correct value at settings file: ");

        checkConnectionProperty(SettingsManager.getInstance().getServerDbUsername(), "Could not find DB Username. Set correct value at settings file: ");

        checkConnectionProperty(SettingsManager.getInstance().getServerDbPassword(), "Could not find DB Password. Set correct value at settings file: ");
    }
}
