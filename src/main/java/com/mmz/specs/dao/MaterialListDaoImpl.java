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
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.MaterialListEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class MaterialListDaoImpl implements MaterialListDao {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public MaterialListDaoImpl() {

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
    public long addMaterialList(MaterialListEntity materialListEntity) {
        Long id = (Long) session.save(materialListEntity);
        materialListEntity = getMaterialListById(id);
        log.debug("MaterialList successfully saved: " + materialListEntity);
        return id;
    }

    @Override
    @Transactional
    public void updateMaterialList(MaterialListEntity materialListEntity) {
        session.merge(materialListEntity);
        log.debug("MaterialList successfully updated: " + materialListEntity);
    }

    @Override
    @Transactional
    public void removeMaterialList(long id) {
        MaterialListEntity materialListEntity = session.load(MaterialListEntity.class, id);
        if (materialListEntity != null) {
            session.delete(materialListEntity);
        }
        log.debug("MaterialList successfully removed: " + materialListEntity);
    }

    @Override
    @Transactional
    public MaterialListEntity getMaterialListById(long id) {
        MaterialListEntity materialListEntity = session.load(MaterialListEntity.class, id);
        log.debug("MaterialList found by id:" + id + " " + materialListEntity);
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
            } else {
                log.warn("Not MaterialList found by detail index: " + detailEntity.getCode() + " material:" + materialListEntity);
            }
        }
        return result;
    }

    @Override
    public List<MaterialListEntity> getUnusedMaterialLists() {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<MaterialListEntity> criteriaQuery = builder.createQuery(MaterialListEntity.class);
        Root<MaterialListEntity> root = criteriaQuery.from(MaterialListEntity.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("active"), false));
        Query<MaterialListEntity> q = session.createQuery(criteriaQuery);
        final List<MaterialListEntity> result = q.list();
        log.debug("MaterialListEntities found unused: ({}), {} ", result.size(), result);
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
            } else {
                log.warn("Not MaterialList from list: " + materialListEntity);
            }
        }
        return result;
    }
}
