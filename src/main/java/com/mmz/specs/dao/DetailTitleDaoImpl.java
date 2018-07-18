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
import com.mmz.specs.model.DetailTitleEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class DetailTitleDaoImpl implements DetailTitleDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public DetailTitleDaoImpl() {

    }

    public DetailTitleDaoImpl(Session session) {
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
    public int addDetailTitle(DetailTitleEntity detailTitlesEntity) {
        Integer id = (Integer) session.save(detailTitlesEntity);
        detailTitlesEntity = getDetailTitleById(id);
        log.debug("DetailTitle successfully saved: " + detailTitlesEntity);
        return id;
    }

    @Override
    @Transactional
    public void updateDetailTitle(DetailTitleEntity detailTitlesEntity) {
        session.merge(detailTitlesEntity);
        log.debug("DetailTitle successfully updated: " + detailTitlesEntity);
    }

    @Override
    @Transactional
    public void removeDetailTitle(int id) {
        DetailTitleEntity detailTitleEntity = session.load(DetailTitleEntity.class, id);
        if (detailTitleEntity != null) {
            session.delete(detailTitleEntity);
        }
        log.debug("DetailTitle successfully removed: " + detailTitleEntity);
    }

    @Override
    @Transactional
    public DetailTitleEntity getDetailTitleById(int id) {
        DetailTitleEntity detailTitleEntity = session.load(DetailTitleEntity.class, id);
        log.debug("DetailTitle successfully found by id:" + id + " " + detailTitleEntity);
        return detailTitleEntity;
    }

    @Override
    @Transactional
    public DetailTitleEntity getDetailTitleByTitle(String title) {
        Query query = session.createQuery("from DetailTitleEntity where title = :title");
        query.setParameter("title", title);

        final DetailTitleEntity entity = (DetailTitleEntity) query.uniqueResult();
        log.debug("DetailTitle found by title: " + title + " " + entity);
        return entity;
    }

    @Override
    @Transactional
    public List<DetailTitleEntity> listDetailTitles() {
        List list = session.createQuery("from DetailTitleEntity").list();
        List<DetailTitleEntity> result = new ArrayList<>(list.size());

        for (Object detailTitleEntity : list) {
            if (detailTitleEntity instanceof DetailTitleEntity) {
                result.add((DetailTitleEntity) detailTitleEntity);
            } else {
                log.warn("Not DetailTitle from list: " + detailTitleEntity);
            }
        }
        return result;
    }

    @Override
    public List<DetailTitleEntity> getDetailTitlesBySearch(String searchText) {
        Query query = session.createQuery("from DetailTitleEntity where UPPER(title) like UPPER( '%" + searchText + "%')");
        ArrayList<DetailTitleEntity> titles = new ArrayList<>();

        List list = query.list();
        for (Object o : list) {
            titles.add((DetailTitleEntity) o);
        }
        System.out.println("for search: " + searchText + " found: " + titles);
        return titles;
    }
}
