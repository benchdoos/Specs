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

import com.mmz.specs.dao.DetailTitleDao;
import com.mmz.specs.dao.DetailTitleDaoImpl;
import com.mmz.specs.model.DetailTitleEntity;

import java.util.List;

public class DetailTitleServiceImpl implements DetailTitleService {
    private DetailTitleDao detailTitleDao;

    public DetailTitleServiceImpl() {
        detailTitleDao = new DetailTitleDaoImpl();
    }

    public DetailTitleServiceImpl(DetailTitleDao detailTitleDao) {
        this.detailTitleDao = detailTitleDao;
    }

    @Override
    public DetailTitleDao getDetailTitleDao() {
        return detailTitleDao;
    }

    @Override
    public void setDetailTitleDao(DetailTitleDao detailTitleDao) {
        this.detailTitleDao = detailTitleDao;
    }

    @Override
    public int addDetailTitle(DetailTitleEntity detailTitlesEntity) {
        return detailTitleDao.addDetailTitle(detailTitlesEntity);
    }

    @Override
    public void updateDetailTitle(DetailTitleEntity detailTitlesEntity) {
        detailTitleDao.updateDetailTitle(detailTitlesEntity);
    }

    @Override
    public void removeDetailTitle(int id) {
        detailTitleDao.removeDetailTitle(id);
    }

    @Override
    public DetailTitleEntity getDetailTitleById(int id) {
        return detailTitleDao.getDetailTitleById(id);
    }

    @Override
    public DetailTitleEntity getDetailTitleByTitle(String title) {
        return detailTitleDao.getDetailTitleByTitle(title);
    }

    @Override
    public List<DetailTitleEntity> listDetailTitles() {
        return detailTitleDao.listDetailTitles();
    }
}
