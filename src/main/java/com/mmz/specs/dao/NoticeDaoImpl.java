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
import com.mmz.specs.model.NoticeEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class NoticeDaoImpl implements NoticeDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    Session session;

    public NoticeDaoImpl() {
        session = ServerDBConnectionPool.getInstance().getSession();
    }

    public NoticeDaoImpl(Session session) {
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
    public int addNotice(NoticeEntity noticeEntity) {
        Integer id = (Integer) session.save(noticeEntity);
        noticeEntity = getNoticeById(id);
        log.info("User successfully saved: " + noticeEntity);
        return id;
    }

    @Override
    public void updateNotice(NoticeEntity noticeEntity) {
        session.merge(noticeEntity);
        log.info("Notice successfully updated: " + noticeEntity);
    }

    @Override
    public void removeNotice(int id) {
        NoticeEntity noticeEntity = session.load(NoticeEntity.class, id);
        if (noticeEntity != null) {
            session.delete(noticeEntity);
        }
        log.info("Notice successfully removed: " + noticeEntity);
    }

    @Override
    public NoticeEntity getNoticeById(int id) {
        NoticeEntity noticeEntity = session.load(NoticeEntity.class, id);
        log.info("Notice successfully found by id:" + id + " " + noticeEntity);
        return noticeEntity;
    }

    @Override
    public NoticeEntity getNoticeByNumber(String number) {
        Query query = session.createQuery("from NoticeEntity where NoticeEntity.number = :number");
        query.setParameter("number", number);

        final NoticeEntity entity = (NoticeEntity) query.uniqueResult();
        log.info("Notice successfully found by number: " + number + " " + entity);
        return entity;
    }

    @Override
    public List<NoticeEntity> listNotices() {
        List<NoticeEntity> list = session.createQuery("from NoticeEntity").list();
        for (NoticeEntity noticeEntity : list) {
            log.info("Notice list: " + noticeEntity);
        }
        return list;
    }
}
