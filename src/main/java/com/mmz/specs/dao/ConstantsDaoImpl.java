package com.mmz.specs.dao;

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.model.ConstantsEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConstantsDaoImpl implements ConstantsDao {

    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void addConstant(ConstantsEntity constantsEntity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.persist(constantsEntity);
        log.info("Constant successfully saved. " + constantsEntity);
    }

    @Override
    public void updateConstant(ConstantsEntity constantsEntity) {
        Session session = this.sessionFactory.getCurrentSession();
        session.update(constantsEntity);
        log.info("Constant successfully updated. " + constantsEntity);
    }

    @Override
    public void removeConstant(int id) {
        Session session = this.sessionFactory.getCurrentSession();
        ConstantsEntity constantsEntity = (ConstantsEntity) session.load(ConstantsEntity.class, id);
        if (constantsEntity != null) {
            session.delete(constantsEntity);
        }
        log.info("Constant successfully removed. " + constantsEntity);

    }

    @Override
    public ConstantsEntity getConstantById(int id) {
        Session session = this.sessionFactory.getCurrentSession();
        ConstantsEntity constantsEntity = session.load(ConstantsEntity.class, id);
        log.info("Constant successfully found by id:" + id + " " + constantsEntity);
        return constantsEntity;
    }

    @Override
    public ConstantsEntity getConstantByKey(String key) {
        Session session = this.sessionFactory.getCurrentSession();
        Query query = session.createQuery("from ConstantsEntity where ConstantsEntity.key =:key");
        query.setFirstResult(0);
        query.setMaxResults(1);
        List list = query.list();


        if (list.size() > 0) {
            log.info("Constant successfully found by key: " + key + " " + list.get(0));
            return (ConstantsEntity) list.get(0);
        } else {
            log.info("Constant was not found by key: " + key);
            return null;
        }

    }

    @Override
    public List<ConstantsEntity> listConstants() {
        Session session = this.sessionFactory.getCurrentSession();
        List<ConstantsEntity> list = session.createQuery("from ConstantsEntity").list();
        for (ConstantsEntity constantsEntity : list) {
            log.info("Constants list: " + constantsEntity);
        }
        return list;
    }
}
