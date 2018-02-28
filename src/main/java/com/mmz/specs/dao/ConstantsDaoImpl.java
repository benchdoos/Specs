package com.mmz.specs.dao;

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.model.ConstantsEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class ConstantsDaoImpl implements ConstantsDao {

    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private Session session;

    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void addConstant(ConstantsEntity constantsEntity) {
        session.persist(constantsEntity);
        log.info("Constant successfully saved. " + constantsEntity);
    }

    @Override
    public void updateConstant(ConstantsEntity constantsEntity) {
        session.update(constantsEntity);
        log.info("Constant successfully updated. " + constantsEntity);
    }

    @Override
    public void removeConstant(int id) {
        ConstantsEntity constantsEntity = (ConstantsEntity) session.load(ConstantsEntity.class, id);
        if (constantsEntity != null) {
            session.delete(constantsEntity);
        }
        log.info("Constant successfully removed. " + constantsEntity);

    }

    @Override
    public ConstantsEntity getConstantById(int id) {
        ConstantsEntity constantsEntity = session.load(ConstantsEntity.class, id);
        log.info("Constant successfully found by id:" + id + " " + constantsEntity);
        return constantsEntity;
    }

    @Override
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
    public List<ConstantsEntity> listConstants() {
        List<ConstantsEntity> list = session.createQuery("from ConstantsEntity").list();
        for (ConstantsEntity constantsEntity : list) {
            log.info("Constants list: " + constantsEntity);
        }
        return list;
    }
}
