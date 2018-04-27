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
import com.mmz.specs.model.DetailEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class DetailDaoImpl implements DetailDao {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public DetailDaoImpl() {
        session = ServerDBConnectionPool.getInstance().getSession();
    }

    public DetailDaoImpl(Session session) {
        this.session = session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    @Transactional
    public int addDetail(DetailEntity detailEntity) {
        Integer id = (Integer) session.save(detailEntity);
        detailEntity = getDetailById(id);
        log.debug("Detail successfully saved: " + detailEntity);
        return id;

    }

    @Override
    @Transactional
    public void updateDetail(DetailEntity detailEntity) {
        session.merge(detailEntity);
        log.debug("Detail successfully updated: " + detailEntity);
    }

    @Override
    @Transactional
    public void removeDetail(int id) {
        DetailEntity detailEntity = session.load(DetailEntity.class, id);
        if (detailEntity != null) {
            session.delete(detailEntity);
        }
        log.debug("Detail successfully removed: " + detailEntity);
    }

    @Override
    @Transactional
    public DetailEntity getDetailById(int id) {
        DetailEntity detailEntity = session.load(DetailEntity.class, id);
        log.debug("Detail found by id:" + id + " " + detailEntity);
        return detailEntity;
    }

    @Override
    @Transactional
    public DetailEntity getDetailByIndex(String code) {
        Query query = session.createQuery("from DetailEntity where code = :code");

        query.setParameter("code", code);

        final DetailEntity entity = (DetailEntity) query.uniqueResult();
        log.debug("Detail found by code: " + code + " " + entity);
        return entity;
    }

    @Override
    @Transactional
    public List<DetailEntity> listDetails() {
        List list = session.createQuery("from DetailEntity").list();
        List<DetailEntity> result = new ArrayList<>(list.size());

        for (Object detailEntity : list) {
            if (detailEntity instanceof DetailEntity) {
                result.add((DetailEntity) detailEntity);
            } else {
                log.warn("Not Detail from list: " + detailEntity);
            }
        }
        return result;
    }
}
