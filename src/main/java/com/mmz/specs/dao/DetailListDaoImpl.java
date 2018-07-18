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
import com.mmz.specs.model.DetailListEntity;
import com.mmz.specs.model.NoticeEntity;
import com.mmz.specs.service.DetailService;
import com.mmz.specs.service.DetailServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class DetailListDaoImpl implements DetailListDao {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public DetailListDaoImpl() {

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
    public long addDetailList(DetailListEntity detailListEntity) {
        Long id = (Long) this.session.save(detailListEntity);
        detailListEntity = getDetailListById(id);
        log.debug("DetailList successfully saved: " + detailListEntity);
        return id;
    }

    @Override
    public void updateDetailList(DetailListEntity detailListEntity) {
        this.session.merge(detailListEntity);
        log.debug("DetailList successfully updated: " + detailListEntity);
    }

    @Override
    public void removeDetailList(long id) {
        DetailListEntity detailListEntity = this.session.load(DetailListEntity.class, id);
        if (detailListEntity != null) {
            this.session.delete(detailListEntity);
        }
        log.debug("DetailList successfully removed: " + detailListEntity);
    }

    @Override
    public DetailListEntity getDetailListById(long id) {
        DetailListEntity detailListEntity = this.session.load(DetailListEntity.class, id);
        log.debug("DetailList found by id:" + id + " " + detailListEntity);
        return detailListEntity;
    }

    @Override
    public DetailListEntity getDetailListByParentAndChildAndNotice(DetailEntity parent, DetailEntity child, NoticeEntity notice) {
        Query query = this.session.createQuery("from DetailListEntity where " +
                "detailByParentDetailId = :parent and " +
                "detailByChildDetailId= :child and noticeByNoticeId=:notice" +
                " order by id asc");
        query.setParameter("parent", parent);
        query.setParameter("child", child);
        query.setParameter("notice", notice);

        final List list = query.list();

        DetailListEntity result = null;
        for (Object o : list) {
            if (o instanceof DetailListEntity) {
                DetailListEntity entity = (DetailListEntity) o;
                if (result != null) {
                    if (result.getId() < entity.getId()) {
                        result = entity;
                    }
                } else {
                    result = entity;
                }
            }
        }
        return result;


        /*CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<DetailListEntity> criteriaQuery = builder.createQuery(DetailListEntity.class);
        Root<DetailListEntity> root = criteriaQuery.from(DetailListEntity.class);

        criteriaQuery.select(root);
        final Predicate detailByParentDetailId = builder.equal(root.get("detailByParentDetailId"), parent);
        final Predicate detailByChildDetailId = builder.equal(root.get("detailByChildDetailId"), child);
        final Predicate noticeByNoticeId = builder.equal(root.get("noticeByNoticeId"), notice);

        criteriaQuery.where(builder.and(detailByParentDetailId, detailByChildDetailId, noticeByNoticeId));

        Query<DetailListEntity> q = session.createQuery(criteriaQuery);
        final List<DetailListEntity> list = q.list();

        System.out.println(">>> (" + list.size() + ")" + list);

        DetailListEntity result = null;
        for (DetailListEntity entity : list) {
            if (result != null) {
                if (entity != null) {
                    if (result.getId() < entity.getId()) {
                        result = entity;
                    }
                }
            } else {
                result = entity;
            }
        }
        return result;*/
    }

    @Override
    public DetailListEntity getLatestDetailListEntityByParentAndChild(DetailEntity parent, DetailEntity child) {
        log.debug("Getting latest DetailListEntity by parent: {} and child: {}", parent.toSimpleString(), child.toSimpleString());
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<DetailListEntity> criteria = builder.createQuery(DetailListEntity.class);
        Root<DetailListEntity> root = criteria.from(DetailListEntity.class);
        criteria.select(root);
        criteria.where(builder.equal(root.get("detailByParentDetailId"), parent), builder.equal(root.get("detailByChildDetailId"), child));
        criteria.orderBy(builder.asc(root.get("noticeByNoticeId").get("creationDate"))/*, builder.desc(root.get("noticeByNoticeId").get("date"))*/);

        final Query<DetailListEntity> query = session.createQuery(criteria);

        query.setFirstResult(0);
        query.setMaxResults(1);
        DetailListEntity entity = null;
        try {
            entity = query.getSingleResult();
            log.debug("Latest detailList successfully found by parent: {} and child: {}; {}", parent.toSimpleString(), child.toSimpleString(), entity);
        } catch (javax.persistence.NoResultException e) {
            log.warn("Can not find latest detailList found by parent and child: {}, {}; {}", parent.toSimpleString(), child.toSimpleString(), null);
        }


        return entity;

    }

    @Override
    public List<DetailListEntity> getDetailListByParent(DetailEntity parent) {
        Query query = this.session.createQuery("from DetailListEntity where detailByParentDetailId= :parent");
        query.setParameter("parent", parent);

        List list = query.list();
        return getDetailEntity(parent, list);
    }

    @Override
    public List<DetailListEntity> getDetailListByChild(DetailEntity child) {
        Query query = this.session.createQuery("from DetailListEntity where detailByChildDetailId= :child");
        query.setParameter("child", child);

        List list = query.list();
        return getDetailEntity(child, list);
    }

    @Override
    public List<DetailListEntity> getDetailListByParent(String detailEntityIndex) {
        DetailService service = new DetailServiceImpl(session);
        DetailEntity parent = service.getDetailByCode(detailEntityIndex);

        Query query = this.session.createQuery("from DetailListEntity where detailByParentDetailId= :parent");
        query.setParameter("parent", parent);

        List list = query.list();

        return getDetailEntity(parent, list);
    }

    @Override
    public List<DetailListEntity> getDetailListByChild(String detailEntityIndex) {

        DetailDao detailDao = new DetailDaoImpl(this.session);
        DetailEntity child = detailDao.getDetailByCode(detailEntityIndex);

        Query query = this.session.createQuery("from DetailListEntity where detailByChildDetailId= :child");
        query.setParameter("child", child);

        List list = query.list();

        return getDetailEntity(child, list);
    }

    @Override
    public List<DetailListEntity> getDetailListByNoticeId(int id) {
        Query query = this.session.createQuery("from DetailListEntity where noticeByNoticeId = " + id);

        List list = query.list();

        List<DetailListEntity> detailListEntities = new ArrayList<>(list.size());
        for (Object object : list) {
            if (object instanceof DetailListEntity) {
                detailListEntities.add((DetailListEntity) object);
            }
        }

        return detailListEntities;
    }

    @Override
    public List<DetailListEntity> getDetailListBySearch(String searchText) {
        log.debug("User is searching: {}", searchText);
        Query query = session.createQuery("from DetailEntity where code like '%" + searchText + "%'");
        ArrayList<DetailEntity> details = new ArrayList<>();

        List list = query.list();
        for (Object o : list) {
            details.add((DetailEntity) o);
        }


        List<DetailListEntity> result = new ArrayList<>();

        for (DetailEntity entity : details) {
            Query query1 = session.createQuery("from DetailListEntity where detailByChildDetailId = " + entity.getId());
            List list1 = query1.list();
            for (Object o : list1) {
                DetailListEntity detailListEntity = (DetailListEntity) o;
                if (detailListEntity.isActive()) {
                    result.add(detailListEntity);
                }
            }
        }

        if (result.isEmpty()) {
            for (DetailEntity entity : details) {
                Query query1 = session.createQuery("from DetailListEntity where detailByParentDetailId = " + entity.getId());
                List list1 = query1.list();
                for (Object o : list1) {
                    DetailListEntity detailListEntity = (DetailListEntity) o;
                    if (detailListEntity.isActive()) {
                        result.add(detailListEntity);
                    }
                }
            }
        }

        log.debug("Search result for {} is: (size: {}), {}", searchText, result.size(), result);

        return result;
    }

    @Override
    public List<DetailListEntity> getDetailListByParentAndChild(DetailEntity parent, DetailEntity child) {
        Query query = session.createQuery("from DetailListEntity where detailByParentDetailId = :parent and detailByChildDetailId = :child");
        query.setParameter("parent", parent);
        query.setParameter("child", child);

        List list = query.list();
        ArrayList<DetailListEntity> arrayList = new ArrayList<>(list.size());
        for (Object o : list) {
            arrayList.add((DetailListEntity) o);
        }
        log.debug("Successfully found DetailList by parent: {}, and child: {} : {}", parent, child, list);

        return arrayList;
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
            if (doesNotContainEntityInList(result, entity.getDetailByParentDetailId())) {
                result.add(entity.getDetailByParentDetailId());
            }
        }
        return result;
    }

    @Override
    public List<DetailEntity> listChildren(DetailEntity parent) {
        Query query = session.createQuery("from DetailListEntity where detailByParentDetailId = :parent");
        query.setParameter("parent", parent);

        final ArrayList<DetailEntity> result = new ArrayList<>();
        for (final Object o : query.list()) {
            final DetailListEntity detailListEntity = (DetailListEntity) o;
            final DetailEntity detailByChildDetailId = detailListEntity.getDetailByChildDetailId();
            if (!result.contains(detailByChildDetailId)) {
                result.add(detailByChildDetailId);
            }
        }
        return result;
    }

    @Override

    public List<DetailListEntity> listDetailLists() {
        List list = this.session.createQuery("from DetailListEntity").list();
        List<DetailListEntity> result = new ArrayList<>(list.size());

        for (Object detailListEntity : list) {
            if (detailListEntity instanceof DetailListEntity) {
                result.add((DetailListEntity) detailListEntity);
            } else {
                log.warn("Not DetailList from list: " + detailListEntity);
            }
        }
        return result;
    }

    private boolean doesNotContainEntityInList(List<DetailEntity> list, DetailEntity entity) {
        if (list == null) return true;
        if (list.size() <= 0) return true;

        for (DetailEntity detailEntity : list) {
            if (detailEntity.getCode().equalsIgnoreCase(entity.getCode())) {
                return false;
            }
        }

        return true;
    }

    private List<DetailListEntity> getDetailEntity(DetailEntity entity, List list) {
        List<DetailListEntity> result = new ArrayList<>(list.size());
        for (Object detailListEntity : list) {
            if (detailListEntity instanceof DetailListEntity) {
                result.add((DetailListEntity) detailListEntity);
            } else {
                log.warn("Not DetailList found by detail index: " + entity.getCode() + " DetailList:" + detailListEntity);
            }
        }
        return result;
    }
}
