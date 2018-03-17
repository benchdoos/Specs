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

import com.mmz.specs.dao.DetailListDao;
import com.mmz.specs.dao.DetailListDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;

import java.util.List;

public class DetailListServiceImpl implements DetailListService {
    private DetailListDao detailListDao;

    public DetailListServiceImpl() {
        this.detailListDao = new DetailListDaoImpl();
    }

    public DetailListServiceImpl(DetailListDao detailListDao) {
        this.detailListDao = detailListDao;
    }

    @Override
    public DetailListDao getDetailListDao() {
        return this.detailListDao;
    }

    @Override
    public void setDetailListDao(DetailListDao detailListDao) {
        this.detailListDao = detailListDao;
    }

    @Override
    public int addDetailList(DetailListEntity detailListEntity) {
        return this.detailListDao.addDetailList(detailListEntity);
    }

    @Override
    public void updateDetailList(DetailListEntity detailListEntity) {
        this.detailListDao.updateDetailList(detailListEntity);
    }

    @Override
    public void removeDetailList(int id) {
        this.detailListDao.removeDetailList(id);
    }

    @Override
    public DetailListEntity getDetailListById(int id) {
        return this.detailListDao.getDetailListById(id);
    }

    @Override
    public List<DetailListEntity> getDetailListByParent(DetailEntity detailEntity) {
        return this.detailListDao.getDetailListByParent(detailEntity);
    }

    @Override
    public List<DetailListEntity> getDetailListByChild(DetailEntity detailEntity) {
        return this.detailListDao.getDetailListByChild(detailEntity);
    }

    @Override
    public List<DetailListEntity> getDetailListByParent(String detailEntityIndex) {
        return this.detailListDao.getDetailListByParent(detailEntityIndex);
    }

    @Override
    public List<DetailListEntity> getDetailListByChild(String detailEntityIndex) {
        return this.detailListDao.getDetailListByChild(detailEntityIndex);
    }

    @Override
    public List<DetailListEntity> getDetailListByNoticeId(int id) {
        return this.detailListDao.getDetailListByNoticeId(id);
    }

    @Override
    public List<DetailEntity> listParents(DetailEntity child) {
        return this.detailListDao.listParents(child);
    }

    public List<DetailEntity> listChildren(DetailEntity parent) {
        return this.detailListDao.listChildren(parent);
    }

    @Override
    public List<DetailListEntity> listDetailLists() {
        return this.detailListDao.listDetailLists();
    }
}
