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
import com.mmz.specs.model.MaterialListEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class MaterialListDaoImpl implements MaterialListDao {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public MaterialListDaoImpl() {
        session = ServerDBConnectionPool.getInstance().getSession();
    }

    public MaterialListDaoImpl(Session session) {
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
    public int addMaterialList(MaterialListEntity materialListEntity) {
        Integer id = (Integer) session.save(materialListEntity);
        materialListEntity = getMaterialListById(id);
        log.info("MaterialList successfully saved: " + materialListEntity);
        return id;
    }

    @Override
    @Transactional
    public void updateMaterialList(MaterialListEntity materialListEntity) {
        session.merge(materialListEntity);
        log.info("MaterialList successfully updated: " + materialListEntity);
    }

    @Override
    @Transactional
    public void removeMaterialList(int id) {
        MaterialListEntity materialListEntity = session.load(MaterialListEntity.class, id);
        if (materialListEntity != null) {
            session.delete(materialListEntity);
        }
        log.info("MaterialList successfully removed: " + materialListEntity);
    }

    @Override
    @Transactional
    public MaterialListEntity getMaterialListById(int id) {
        MaterialListEntity materialListEntity = session.load(MaterialListEntity.class, id);
        log.info("MaterialList found by id:" + id + " " + materialListEntity);
        return materialListEntity;
    }

    @Override
    @Transactional
    public List<MaterialListEntity> getMaterialListByDetail(DetailEntity detailEntity) {
        Query query = session.createQuery("from MaterialListEntity where detailByDetailId= :detailEntity");
        query.setParameter("detailEntity", detailEntity);

        List list = query.list();
        List<MaterialListEntity> result = new ArrayList<>(list.size());
        for (Object materialListEntity : list) {
            if (materialListEntity instanceof MaterialListEntity) {
                result.add((MaterialListEntity) materialListEntity);
                log.info("MaterialList successfully found by detail index: " + detailEntity.getCode() + " material:" + materialListEntity);
            } else {
                log.warn("Not MaterialList found by detail index: " + detailEntity.getCode() + " material:" + materialListEntity);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public List<MaterialListEntity> listMaterialLists() {
        List list = session.createQuery("from MaterialListEntity").list();
        List<MaterialListEntity> result = new ArrayList<>(list.size());
        for (Object materialListEntity : list) {
            if (materialListEntity instanceof MaterialListEntity) {
                result.add((MaterialListEntity) materialListEntity);
                log.info("MaterialList from list: " + materialListEntity);
            } else {
                log.warn("Not MaterialList from list: " + materialListEntity);
            }
        }
        return result;
    }
}
