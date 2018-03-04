package com.mmz.specs.dao;

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.model.ConstantsEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.transaction.Transactional;
import java.util.List;

public class ConstantsDaoImpl implements ConstantsDao {

    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private Session session;

    public ConstantsDaoImpl() {
        session = ServerDBConnectionPool.getInstance().getSession();
    }

    public ConstantsDaoImpl(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    @Transactional
    public int addConstant(ConstantsEntity constantsEntity) {
        Integer id = (Integer) session.save(constantsEntity);
        constantsEntity = getConstantById(id);
        log.info("Constant successfully saved: " + constantsEntity);
        return id;
    }

    @Override
    @Transactional
    public void updateConstant(ConstantsEntity constantsEntity) {
        session.update(constantsEntity);
        log.info("Constant successfully updated: " + constantsEntity);
    }

    @Override
    @Transactional
    public void removeConstant(int id) {
        ConstantsEntity constantsEntity = (ConstantsEntity) session.load(ConstantsEntity.class, id);
        if (constantsEntity != null) {
            session.delete(constantsEntity);
        }
        log.info("Constant successfully removed: " + constantsEntity);

    }

    @Override
    @Transactional
    public ConstantsEntity getConstantById(int id) {
        ConstantsEntity constantsEntity = session.load(ConstantsEntity.class, id);
        log.info("Constant successfully found by id:" + id + " " + constantsEntity);
        return constantsEntity;
    }

    @Override
    @Transactional
    public ConstantsEntity getConstantByKey(String key) {
        Query query = session.createQuery("from ConstantsEntity where key =:key");
        query.setParameter("key", key);
        query.setFirstResult(0);
        query.setMaxResults(1);
        final ConstantsEntity entity = (ConstantsEntity) query.getSingleResult();
        log.info("Constant successfully found by key: " + key + " " + entity);
        return entity;

    }

    @Override
    @Transactional
    public List<ConstantsEntity> listConstants() {
        List<ConstantsEntity> list = session.createQuery("from ConstantsEntity").list();
        for (ConstantsEntity constantsEntity : list) {
            log.info("Constants list: " + constantsEntity);
        }
        return list;
    }
}
