package com.mmz.specs.dao;

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.model.UsersEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class UsersDaoImpl implements UsersDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    /**
     * Default constructor
     * Use for server only!!! Otherwise use {@link UsersDaoImpl}({@link Session} session)
     */
    public UsersDaoImpl() {
        session = ServerDBConnectionPool.getInstance().getSession();
    }

    public UsersDaoImpl(Session session) {
        this.session = session;
    }


    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }


    @Override
    public int addUser(UsersEntity usersEntity) {
        Integer id = (Integer) session.save(usersEntity);
        usersEntity = getUserById(id);
        log.info("User successfully saved: " + usersEntity);
        return id;
    }

    @Override
    public void updateUser(UsersEntity usersEntity) {
        session.merge(usersEntity);
        log.info("User successfully updated: " + usersEntity);
    }

    @Override
    public void removeUser(int id) {
        UsersEntity usersEntity = session.load(UsersEntity.class, id);
        if (usersEntity != null) {
            session.delete(usersEntity);
        }
        log.info("User successfully removed: " + usersEntity);
    }


    @Override
    public UsersEntity getUserById(int id) {
        UsersEntity usersEntity = session.load(UsersEntity.class, id);
        log.info("User successfully found by id:" + id + " " + usersEntity);
        return usersEntity;
    }

    @Override
    public UsersEntity getUserByUsername(String username) {
        Query query = session.createQuery("from UsersEntity where username = :username");
        query.setParameter("username", username);

        final UsersEntity entity = (UsersEntity) query.uniqueResult();
        log.info("User successfully found by username: " + username + " " + entity);
        return entity;
    }


    @Override
    public List<UsersEntity> listUsers() {
        List<UsersEntity> list = session.createQuery("from UsersEntity").list();
        for (UsersEntity usersEntity : list) {
            log.info("Users list: " + usersEntity);
        }
        return list;
    }
}
