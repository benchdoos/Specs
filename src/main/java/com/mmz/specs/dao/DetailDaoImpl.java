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

import java.util.List;

public class DetailDaoImpl implements DetailDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    Session session;

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
    public void addDetail(DetailEntity detailEntity) {
        session.save(detailEntity);
        log.info("Detail successfully saved: " + detailEntity);

    }

    @Override
    public void updateDetail(DetailEntity detailEntity) {
        session.merge(detailEntity);
        log.info("Detail successfully updated: " + detailEntity);
    }

    @Override
    public void removeDetail(int id) {
        DetailEntity detailEntity = session.load(DetailEntity.class, id);
        if (detailEntity != null) {
            session.delete(detailEntity);
        }
        log.info("Detail successfully removed: " + detailEntity);
    }

    @Override
    public DetailEntity getDetailById(int id) {
        DetailEntity detailEntity = session.load(DetailEntity.class, id);
        log.info("Detail successfully found by id:" + id + " " + detailEntity);
        return detailEntity;
    }

    @Override
    public DetailEntity getDetailByIndex(String index) {
        Query query = session.createQuery("from DetailEntity where DetailEntity.index = :index");
        query.setParameter("index", index);

        final DetailEntity entity = (DetailEntity) query.uniqueResult();
        log.info("Detail successfully found by index: " + index + " " + entity);
        return entity;
    }

    @Override
    public List<DetailEntity> listDetails() {
        List<DetailEntity> list = session.createQuery("from UsersEntity").list();
        for (DetailEntity detailEntity : list) {
            log.info("Detail list: " + detailEntity);
        }
        return list;
    }
}
