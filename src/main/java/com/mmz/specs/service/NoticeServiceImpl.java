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

package com.mmz.specs.service;

import com.mmz.specs.dao.NoticeDao;
import com.mmz.specs.dao.NoticeDaoImpl;
import com.mmz.specs.model.NoticeEntity;
import com.mmz.specs.model.UsersEntity;

import java.util.List;

public class NoticeServiceImpl implements NoticeService {
    private NoticeDao noticeDao;

    public NoticeServiceImpl() {
        noticeDao = new NoticeDaoImpl();
    }

    public NoticeServiceImpl(NoticeDao noticeDao) {
        this.noticeDao = noticeDao;
    }

    @Override
    public void setNoticeDao(NoticeDao noticeDao) {
        this.noticeDao = noticeDao;
    }

    @Override
    public NoticeDao getNoticeDao() {
        return noticeDao;
    }

    @Override
    public int addNotice(NoticeEntity noticeEntity) {
        return noticeDao.addNotice(noticeEntity);
    }

    @Override
    public void updateNotice(NoticeEntity noticeEntity) {
        noticeDao.updateNotice(noticeEntity);
    }

    @Override
    public void removeNotice(int id) {
        noticeDao.removeNotice(id);
    }

    @Override
    public NoticeEntity getNoticeById(int id) {
        return noticeDao.getNoticeById(id);
    }

    @Override
    public NoticeEntity getNoticeByNumber(String number) {
        return noticeDao.getNoticeByNumber(number);
    }

    @Override
    public List<NoticeEntity> listNoticesByUser(UsersEntity entity) {
        return noticeDao.listNoticesByUser(entity);
    }

    @Override
    public List<NoticeEntity> listNotices() {
        return noticeDao.listNotices();
    }
}
