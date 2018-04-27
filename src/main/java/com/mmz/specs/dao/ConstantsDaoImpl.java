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

package com.mmz.specs.dao;

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.model.ConstantsEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.transaction.Transactional;
import java.util.ArrayList;
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
        log.debug("Constant successfully saved: " + constantsEntity);
        return id;
    }

    @Override
    @Transactional
    public void updateConstant(ConstantsEntity constantsEntity) {
        session.update(constantsEntity);
        log.debug("Constant successfully updated: " + constantsEntity);
    }

    @Override
    @Transactional
    public void removeConstant(int id) {
        ConstantsEntity constantsEntity = session.load(ConstantsEntity.class, id);
        if (constantsEntity != null) {
            session.delete(constantsEntity);
        }
        log.debug("Constant successfully removed: " + constantsEntity);

    }

    @Override
    @Transactional
    public ConstantsEntity getConstantById(int id) {
        ConstantsEntity constantsEntity = session.load(ConstantsEntity.class, id);
        log.debug("Constant found by id:" + id + " " + constantsEntity);
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
        log.debug("Constant found by key: " + key + " " + entity);
        return entity;

    }

    @Override
    @Transactional
    public List<ConstantsEntity> listConstants() {
        List list = session.createQuery("from ConstantsEntity").list();
        List<ConstantsEntity> result = new ArrayList<>(list.size());

        for (Object constantsEntity : list) {
            if (constantsEntity instanceof ConstantsEntity) {
                result.add((ConstantsEntity) constantsEntity);
            } else {
                log.warn("Not Constant from list: " + constantsEntity);
            }
        }
        return result;
    }
}
