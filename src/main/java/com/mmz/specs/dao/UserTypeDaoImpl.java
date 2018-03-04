package com.mmz.specs.dao;

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.model.UserTypeEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.transaction.Transactional;
import java.util.List;

public class UserTypeDaoImpl implements UserTypeDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public UserTypeDaoImpl() {
        session = ServerDBConnectionPool.getInstance().getSession();
    }

    public UserTypeDaoImpl(Session session) {
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
    @Transactional
    public UserTypeEntity getUserTypeById(int id) {
        UserTypeEntity userTypeEntity = session.load(UserTypeEntity.class, id);
        log.info("UserType successfully found by id:" + id + " " + userTypeEntity);
        return userTypeEntity;
    }

    @Override
    @Transactional
    public List<UserTypeEntity> listUserTypes() {
        List<UserTypeEntity> list = session.createQuery("from UserTypeEntity").list();
        for (UserTypeEntity userTypeEntity : list) {
            log.info("UserType list: " + userTypeEntity);
        }
        return list;
    }
}
