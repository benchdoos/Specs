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
import com.mmz.specs.model.MaterialEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class MaterialDaoImpl implements MaterialDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public MaterialDaoImpl() {

    }

    public MaterialDaoImpl(Session session) {
        this.session = session;
    }

    @Override
    @Transactional
    public int addMaterial(MaterialEntity materialEntity) {
        Integer id = (Integer) session.save(materialEntity);
        materialEntity = getMaterialById(id);
        log.debug("Material successfully saved: " + materialEntity);
        return id;
    }

    @Override
    @Transactional
    public MaterialEntity getMaterialById(int id) {
        try {
            MaterialEntity materialEntity = session.load(MaterialEntity.class, id);
            log.debug("Material successfully found by id: {}, {}", id, materialEntity);
            return materialEntity;
        } catch (Exception e) {
            log.warn("Could not get Material by id: {}", id, e);
            return null;
        }
    }

    @Override
    @Transactional
    public MaterialEntity getMaterialByShortMarkAndProfile(String shortMark, String shortProfile) {
        Query query = session.createQuery("from MaterialEntity where shortMark = :shortMark " +
                "and shortProfile=:shortProfile");
        query.setParameter("shortMark", shortMark);
        query.setParameter("shortProfile", shortProfile);

        final MaterialEntity entity = (MaterialEntity) query.uniqueResult();
        log.debug("Material found by short mark and short profile: " + shortMark + " " + shortProfile
                + "; " + entity);
        return entity;
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
    public List<MaterialEntity> listMaterials() {
        List list = session.createQuery("from MaterialEntity").list();
        List<MaterialEntity> result = new ArrayList<>(list.size());

        for (Object materialEntity : list) {
            if (materialEntity instanceof MaterialEntity) {
                result.add((MaterialEntity) materialEntity);
            } else {
                log.warn("Not Material from list: " + materialEntity);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void removeMaterial(int id) {
        MaterialEntity materialEntity = session.load(MaterialEntity.class, id);
        if (materialEntity != null) {
            session.delete(materialEntity);
        }
        log.debug("Material successfully removed: " + materialEntity);
    }

    @Override
    @Transactional
    public void updateMaterial(MaterialEntity materialEntity) {
        session.merge(materialEntity);
        log.debug("Material successfully updated: " + materialEntity);
    }
}
