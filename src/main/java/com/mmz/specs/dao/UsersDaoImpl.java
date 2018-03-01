package com.mmz.specs.dao;

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.model.UsersEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class UsersDaoImpl implements UsersDao {
    private Session session;
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());


    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void addUserType(UsersEntity usersEntity) {
        session.persist(usersEntity);
        log.info("User successfully saved. " + usersEntity);
    }

    @Override
    public void updateUser(UsersEntity usersEntity) {
        session.update(usersEntity);
        log.info("User successfully updated. " + usersEntity);
    }

    @Override
    public void removeUser(int id) {
        UsersEntity usersEntity = session.load(UsersEntity.class, id);
        if (usersEntity != null) {
            session.delete(usersEntity);
        }
        log.info("User successfully removed. " + usersEntity);
    }

    @Override
    public UsersEntity getUserById(int id) {
        UsersEntity usersEntity = session.load(UsersEntity.class, id);
        log.info("User successfully found by id:" + id + " " + usersEntity);
        return usersEntity;
    }

    @Override
    public UsersEntity getUserByUsername(String username) {
        Query query = session.createQuery("from UsersEntity where UsersEntity.username =: name");
        query.setParameter("name", username);
        query.setFirstResult(0);
        query.setMaxResults(1);
        final UsersEntity entity = (UsersEntity) query.getSingleResult();
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