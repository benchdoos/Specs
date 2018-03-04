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

import java.util.List;

public class MaterialListDaoImpl implements MaterialListDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    Session session;

    public MaterialListDaoImpl() {
        session = ServerDBConnectionPool.getInstance().getSession();
    }

    public MaterialListDaoImpl(Session session) {
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
    public int addMaterialList(MaterialListEntity materialListEntity) {
        Integer id = (Integer) session.save(materialListEntity);
        materialListEntity = getMaterialListById(id);
        log.info("MaterialList successfully saved: " + materialListEntity);
        return id;
    }

    @Override
    public void updateMaterialList(MaterialListEntity materialListEntity) {
        session.merge(materialListEntity);
        log.info("MaterialList successfully updated: " + materialListEntity);
    }

    @Override
    public void removeMaterialList(int id) {
        MaterialListEntity materialListEntity = session.load(MaterialListEntity.class, id);
        if (materialListEntity != null) {
            session.delete(materialListEntity);
        }
        log.info("MaterialList successfully removed: " + materialListEntity);
    }

    @Override
    public MaterialListEntity getMaterialListById(int id) {
        MaterialListEntity materialListEntity = session.load(MaterialListEntity.class, id);
        log.info("MaterialList successfully found by id:" + id + " " + materialListEntity);
        return materialListEntity;
    }

    @Override
    public List<MaterialListEntity> getMaterialListByDetail(DetailEntity detailEntity) {
        Query query = session.createQuery("from MaterialListEntity where MaterialListEntity.detailByDetailId= :detailEntity");
        query.setParameter("detailEntity", detailEntity);

        List<MaterialListEntity> list = query.list();
        for (MaterialListEntity materialListEntity : list) {
            log.info("MaterialList successfully found by detail index: " + detailEntity.getIndex() + " material:" + materialListEntity);
        }
        return list;
    }

    @Override
    public List<MaterialListEntity> listMaterialLists() {
        List<MaterialListEntity> list = session.createQuery("from MaterialListEntity").list();
        for (MaterialListEntity materialListEntity : list) {
            log.info("MaterialList list: " + materialListEntity);
        }
        return list;
    }
}
