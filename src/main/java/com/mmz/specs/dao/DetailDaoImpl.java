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
import com.mmz.specs.model.DetailTitleEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class DetailDaoImpl implements DetailDao {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public DetailDaoImpl() {

    }

    public DetailDaoImpl(Session session) {
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
    public int addDetail(DetailEntity detailEntity) {
        Integer id = (Integer) session.save(detailEntity);
        detailEntity = getDetailById(id);
        log.debug("Detail successfully saved at id:{}, detail: " + detailEntity);
        return id;

    }

    @Override
    public void updateDetail(DetailEntity detailEntity) {
        session.merge(detailEntity);
        log.debug("Detail successfully updated: " + detailEntity);
    }

    @Override
    public void removeDetail(int id) {
        DetailEntity detailEntity = session.load(DetailEntity.class, id);
        if (detailEntity != null) {
            session.delete(detailEntity);
        }
        log.debug("Detail successfully removed: " + detailEntity);
    }

    @Override
    public DetailEntity getDetailById(int id) {
        try {
            DetailEntity detailEntity = session.load(DetailEntity.class, id);
            log.debug("Detail found by id:" + id + " " + detailEntity);
            return detailEntity;
        } catch (Exception e) {
            log.warn("Could not find detail by id: {}", id, e);
            return null;
        }
    }

    @Override
    public DetailEntity getDetailByCode(String code) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<DetailEntity> criteriaQuery = builder.createQuery(DetailEntity.class);
        Root<DetailEntity> root = criteriaQuery.from(DetailEntity.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("code"), code));
        Query<DetailEntity> q = session.createQuery(criteriaQuery);
        final DetailEntity entity = q.uniqueResult();
        log.debug("Detail found by code: " + code + " " + entity);
        return entity;

    }

    @Override
    public List<DetailEntity> getDetailByTitle(DetailTitleEntity titleEntity) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<DetailEntity> criteriaQuery = builder.createQuery(DetailEntity.class);
        Root<DetailEntity> root = criteriaQuery.from(DetailEntity.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("detailTitleByDetailTitleId"), titleEntity));
        Query<DetailEntity> q = session.createQuery(criteriaQuery);
        final List<DetailEntity> result = q.list();
        log.debug("Detail found by titleEntity {}: ", titleEntity, result);
        return result;
    }

    @Override
    public List<DetailEntity> getDetailsBySearch(String searchText) {
        try {
            log.debug("Searching detail by code by search:{}", searchText);
            Query query = session.createQuery("from DetailEntity where UPPER(code) like UPPER('%" + searchText + "%')");
            ArrayList<DetailEntity> details = new ArrayList<>();

            List list = query.list();
            for (Object o : list) {
                details.add((DetailEntity) o);
            }

            return details;
        } catch (Exception e) {
            log.warn("Could not find detail by search: {}", searchText, e);
            return null;
        }
    }

    @Override
    public List<DetailEntity> listDetailsByEditedImage() {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<DetailEntity> criteriaQuery = builder.createQuery(DetailEntity.class);
        Root<DetailEntity> root = criteriaQuery.from(DetailEntity.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.notEqual(root.get("imagePath"), ""));
        Query<DetailEntity> q = session.createQuery(criteriaQuery);
        try {
            final List<DetailEntity> result = q.list();
            log.debug("Details found with edited image: ({}), {} ", result.size(), result);
            return result;
        } catch (Exception e) {
            log.warn("Could not find details with edited image", e);
            return null;
        }
    }

    @Override
    public List<DetailEntity> listDetails() {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<DetailEntity> criteriaQuery = builder.createQuery(DetailEntity.class);
        Root<DetailEntity> root = criteriaQuery.from(DetailEntity.class);
        criteriaQuery.select(root);
        Query<DetailEntity> q = session.createQuery(criteriaQuery);
        final List<DetailEntity> result = q.list();
        log.debug("Full list of details: ({})", result.size(), result);
        return result;

    }
}
