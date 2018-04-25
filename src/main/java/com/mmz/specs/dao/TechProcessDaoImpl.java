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
import com.mmz.specs.model.TechProcessEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class TechProcessDaoImpl implements TechProcessDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public TechProcessDaoImpl() {
        session = ServerDBConnectionPool.getInstance().getSession();
    }

    public TechProcessDaoImpl(Session session) {
        this.session = session;
    }

    private List<TechProcessEntity> getTechProcessEntityList(List list, List<TechProcessEntity> result) {
        for (Object techProcessEntity : list) {
            if (techProcessEntity instanceof TechProcessEntity) {
                result.add((TechProcessEntity) techProcessEntity);
                log.debug("TechProcess from list: " + techProcessEntity);
            } else {
                log.warn("Not TechProcess from list: " + techProcessEntity);
            }
        }
        return result;
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
    public int addTechProcess(TechProcessEntity techProcessEntity) {
        Integer id = (Integer) session.save(techProcessEntity);
        techProcessEntity = getTechProcessById(id);
        log.debug("TechProcess successfully saved: " + techProcessEntity);
        return id;
    }

    @Override
    @Transactional
    public void updateTechProcess(TechProcessEntity techProcessEntity) {
        session.merge(techProcessEntity);
        log.debug("User successfully updated: " + techProcessEntity);
    }

    @Override
    @Transactional
    public void removeTechProcess(int id) {
        TechProcessEntity techProcessEntity = session.load(TechProcessEntity.class, id);
        if (techProcessEntity != null) {
            session.delete(techProcessEntity);
        }
        log.debug("TechProcess successfully removed: " + techProcessEntity);
    }

    @Override
    @Transactional
    public TechProcessEntity getTechProcessById(int id) {
        TechProcessEntity techProcessEntity = session.load(TechProcessEntity.class, id);
        log.debug("TechProcess found by id:" + id + " " + techProcessEntity);
        return techProcessEntity;
    }

    @Override
    public TechProcessEntity getTechProcessByValue(String value) {
        final Query query = session.createQuery(" from TechProcessEntity where process  = :value");
        query.setParameter("value", value);

        TechProcessEntity techProcessEntity = (TechProcessEntity) query.uniqueResult();
        log.debug("TechProcess found by value:" + value + " " + techProcessEntity);
        return techProcessEntity;
    }

    @Override
    @Transactional
    public List<TechProcessEntity> getTechProcessByAlikeValue(String searchingString) {
        String[] arr = searchingString.split(" ");
        ArrayList<String> arrayList = new ArrayList<>();
        for (String string : arr) {
            if (!string.isEmpty()) {
                arrayList.add(string);
            }
        }

        StringBuilder likeString = new StringBuilder("from TechProcessEntity ");
        likeString.append("where ");
        for (int i = 0; i < arrayList.size(); i++) {
            String string = arrayList.get(i);
            if (i > 0) {
                likeString.append(" and ");
            }
            likeString.append("process like ").append("'%").append(string).append("%'");
        }

        List list = session.createQuery(likeString.toString()).list();
        List<TechProcessEntity> result = new ArrayList<>(list.size());

        result = getTechProcessEntityList(list, result);
        return result;
    }

    @Override
    @Transactional
    public List<TechProcessEntity> listTechProcesses() {
        List list = session.createQuery("from TechProcessEntity").list();
        List<TechProcessEntity> result = new ArrayList<>(list.size());

        result = getTechProcessEntityList(list, result);
        return result;
    }


}
