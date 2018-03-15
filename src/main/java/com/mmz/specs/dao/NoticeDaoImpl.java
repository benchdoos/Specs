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
import com.mmz.specs.model.UsersEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class NoticeDaoImpl implements NoticeDao {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

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
    @Transactional
    public int addNotice(NoticeEntity noticeEntity) {
        Integer id = (Integer) session.save(noticeEntity);
        noticeEntity = getNoticeById(id);
        log.info("User successfully saved: " + noticeEntity);
        return id;
    }

    @Override
    @Transactional
    public void updateNotice(NoticeEntity noticeEntity) {
        session.merge(noticeEntity);
        log.info("Notice successfully updated: " + noticeEntity);
    }

    @Override
    @Transactional
    public void removeNotice(int id) {
        NoticeEntity noticeEntity = session.load(NoticeEntity.class, id);
        if (noticeEntity != null) {
            session.delete(noticeEntity);
        }
        log.info("Notice successfully removed: " + noticeEntity);
    }

    @Override
    @Transactional
    public NoticeEntity getNoticeById(int id) {
        NoticeEntity noticeEntity = session.load(NoticeEntity.class, id);
        log.info("Notice found by id:" + id + " " + noticeEntity);
        return noticeEntity;
    }

    @Override
    @Transactional
    public NoticeEntity getNoticeByNumber(String number) {
        Query query = session.createQuery("from NoticeEntity where number = :number");
        query.setParameter("number", number);

        final NoticeEntity entity = (NoticeEntity) query.uniqueResult();
        log.info("Notice successfully found by number: " + number + " " + entity);
        return entity;
    }

    @Override
    @Transactional
    public List<NoticeEntity> listNoticesByUser(UsersEntity user) {
        if (user == null) return null;

        Query query = session.createQuery("from NoticeEntity where usersByProvidedByUserId =:user");
        query.setParameter("user", user);

        List list = query.list();

        List<NoticeEntity> result = new ArrayList<>(list.size());

        for (Object noticeEntity : list) {
            if (noticeEntity instanceof NoticeEntity) {
                result.add((NoticeEntity) noticeEntity);
                log.info("Notice edited by user: " + user.getUsername() + " from list: " + noticeEntity);
            } else {
                log.warn("Not Notice edited by user: " + user.getUsername() + " from list: " + noticeEntity);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public List<NoticeEntity> listNotices() {
        List list = session.createQuery("from NoticeEntity").list();
        List<NoticeEntity> result = new ArrayList<>(list.size());

        for (Object noticeEntity : list) {
            if (noticeEntity instanceof NoticeEntity) {
                result.add((NoticeEntity) noticeEntity);
                log.info("Notice from list: " + noticeEntity);
            } else {
                log.warn("Not Notice from list: " + noticeEntity);
            }
        }
        return result;
    }
}
