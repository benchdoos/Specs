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

import com.mmz.specs.dao.DetailDao;
import com.mmz.specs.dao.DetailDaoImpl;
import com.mmz.specs.model.DetailEntity;

import java.util.List;

public class DetailServiceImpl implements DetailService {
    private DetailDao detailDao;

    public DetailServiceImpl() {
        detailDao = new DetailDaoImpl();
    }

    public DetailServiceImpl(DetailDao detailDao) {
        this.detailDao = detailDao;
    }

    @Override
    public void setDetailDao(DetailDao detailDao) {
        this.detailDao = detailDao;
    }

    @Override
    public DetailDao getDetailDao() {
        return detailDao;
    }

    @Override
    public void addDetail(DetailEntity detailEntity) {
        detailDao.addDetail(detailEntity);
    }

    @Override
    public void updateDetail(DetailEntity detailEntity) {
        detailDao.updateDetail(detailEntity);
    }

    @Override
    public void removeDetail(int id) {
        detailDao.removeDetail(id);
    }

    @Override
    public DetailEntity getDetailById(int id) {
        return detailDao.getDetailById(id);
    }

    @Override
    public DetailEntity getDetailByIndex(String index) {
        return detailDao.getDetailByIndex(index);
    }

    @Override
    public List<DetailEntity> listDetails() {
        return detailDao.listDetails();
    }
}
