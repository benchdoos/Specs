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
import com.mmz.specs.model.DetailListEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class DetailListDaoImpl implements DetailListDao {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public DetailListDaoImpl() {
        this.session = ServerDBConnectionPool.getInstance().getSession();
    }

    public DetailListDaoImpl(Session session) {
        this.session = session;
    }

    @Override
    public Session getSession() {
        return this.session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    @Transactional
    public int addDetailList(DetailListEntity detailListEntity) {
        Integer id = (Integer) this.session.save(detailListEntity);
        detailListEntity = getDetailListById(id);
        log.info("DetailList successfully saved: " + detailListEntity);
        return id;
    }

    @Override
    @Transactional
    public void updateDetailList(DetailListEntity detailListEntity) {
        this.session.merge(detailListEntity);
        log.info("DetailList successfully updated: " + detailListEntity);
    }

    @Override
    @Transactional
    public void removeDetailList(int id) {
        DetailListEntity detailListEntity = this.session.load(DetailListEntity.class, id);
        if (detailListEntity != null) {
            this.session.delete(detailListEntity);
        }
        log.info("DetailList successfully removed: " + detailListEntity);
    }

    @Override
    @Transactional
    public DetailListEntity getDetailListById(int id) {
        DetailListEntity detailListEntity = this.session.load(DetailListEntity.class, id);
        log.info("DetailList found by id:" + id + " " + detailListEntity);
        return detailListEntity;
    }

    @Override
    @Transactional
    public List<DetailListEntity> getDetailListByParent(DetailEntity parent) {
        Query query = this.session.createQuery("from DetailListEntity where detailByParentDetailId= :parent");
        query.setParameter("parent", parent);

        List list = query.list();
        return getDetailEntity(parent, list);
    }

    @Override
    @Transactional
    public List<DetailListEntity> getDetailListByChild(DetailEntity child) {
        Query query = this.session.createQuery("from DetailListEntity where detailByChildDetailId= :child");
        query.setParameter("child", child);

        List list = query.list();
        return getDetailEntity(child, list);
    }

    @Override
    @Transactional
    public List<DetailListEntity> getDetailListByParent(String detailEntityIndex) {
        DetailDao detailDao = new DetailDaoImpl(session);
        DetailEntity parent = detailDao.getDetailByIndex(detailEntityIndex);

        Query query = this.session.createQuery("from DetailListEntity where detailByParentDetailId= :parent");
        query.setParameter("parent", parent);

        List list = query.list();

        return getDetailEntity(parent, list);
    }

    @Override
    @Transactional
    public List<DetailListEntity> getDetailListByChild(String detailEntityIndex) {

        DetailDao detailDao = new DetailDaoImpl(this.session);
        DetailEntity child = detailDao.getDetailByIndex(detailEntityIndex);

        Query query = this.session.createQuery("from DetailListEntity where detailByChildDetailId= :child");
        query.setParameter("child", child);

        List list = query.list();

        return getDetailEntity(child, list);
    }

    @Override
    public List<DetailEntity> listParents(DetailEntity child) {
        Query query = this.session.createQuery("from DetailListEntity where detailByChildDetailId= :child");
        query.setParameter("child", child);

        List list = query.list();

        List<DetailListEntity> detailListEntities = new ArrayList<>(list.size());
        for (Object object : list) {
            if (object instanceof DetailListEntity) {
                detailListEntities.add((DetailListEntity) object);
            }
        }

        List<DetailEntity> result = new ArrayList<>(detailListEntities.size());

        for (DetailListEntity entity : detailListEntities) {
            if (!containsEntityInList(result, entity.getDetailByParentDetailId())) {
                result.add(entity.getDetailByParentDetailId());
            }
        }
        return result;
    }

    @Override
    public List<DetailEntity> listChildren(DetailEntity parent) {
        Query query = session.createQuery("from DetailListEntity where detailByParentDetailId = :parent");
        query.setParameter("parent", parent);

        List list = query.list();

        List<DetailListEntity> detailListEntities = new ArrayList<>(list.size());

        for (Object object : list) {
            if (object instanceof DetailListEntity) {
                detailListEntities.add((DetailListEntity) object);
            }
        }


        List<DetailEntity> result = new ArrayList<>(detailListEntities.size());

        for (DetailListEntity entity : detailListEntities) {
            if (!containsEntityInList(result, entity.getDetailByChildDetailId())) {
                result.add(entity.getDetailByChildDetailId());
            }
        }

        return result;
    }

    @Override
    @Transactional
    public List<DetailListEntity> listDetailLists() {
        List list = this.session.createQuery("from DetailListEntity").list();
        List<DetailListEntity> result = new ArrayList<>(list.size());

        for (Object detailListEntity : list) {
            if (detailListEntity instanceof DetailListEntity) {
                result.add((DetailListEntity) detailListEntity);
                log.info("DetailList from list: " + detailListEntity);
            } else {
                log.warn("Not DetailList from list: " + detailListEntity);
            }
        }
        return result;
    }

    private boolean containsEntityInList(List<DetailEntity> list, DetailEntity entity) {
        if (list == null) return false;
        if (list.size() <= 0) return false;

        for (DetailEntity detailEntity : list) {
            if (detailEntity.getNumber().equalsIgnoreCase(entity.getNumber())) {
                return true;
            }
        }

        return false;
    }

    private List<DetailListEntity> getDetailEntity(DetailEntity entity, List list) {
        List<DetailListEntity> result = new ArrayList<>(list.size());
        for (Object detailListEntity : list) {
            if (detailListEntity instanceof DetailListEntity) {
                result.add((DetailListEntity) detailListEntity);
                log.info("DetailList successfully found by detail index: " + entity.getNumber() + " DetailList:" + detailListEntity);
            } else {
                log.warn("Not DetailList found by detail index: " + entity.getNumber() + " DetailList:" + detailListEntity);
            }
        }
        return result;
    }
}