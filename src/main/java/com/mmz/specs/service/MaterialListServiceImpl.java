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

import com.mmz.specs.dao.MaterialListDao;
import com.mmz.specs.dao.MaterialListDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.MaterialListEntity;

import java.util.List;

public class MaterialListServiceImpl implements MaterialListService {
    private MaterialListDao materialListDao;

    public MaterialListServiceImpl() {
        materialListDao = new MaterialListDaoImpl();
    }

    public MaterialListServiceImpl(MaterialListDao materialListDao) {
        this.materialListDao = materialListDao;
    }

    @Override
    public void setMaterialListDao(MaterialListDao materialListDao) {
        this.materialListDao = materialListDao;
    }

    @Override
    public MaterialListDao getMaterialListDao() {
        return materialListDao;
    }

    @Override
    public int addMaterialList(MaterialListEntity materialListEntity) {
        return materialListDao.addMaterialList(materialListEntity);
    }

    @Override
    public void updateMaterialList(MaterialListEntity materialListEntity) {
        materialListDao.updateMaterialList(materialListEntity);
    }

    @Override
    public void removeMaterialList(int id) {
        materialListDao.removeMaterialList(id);
    }

    @Override
    public MaterialListEntity getMaterialListById(long id) {
        return materialListDao.getMaterialListById(id);
    }

    @Override
    public List<MaterialListEntity> getMaterialListByDetail(DetailEntity detailEntity) {
        return materialListDao.getMaterialListByDetail(detailEntity);
    }

    @Override
    public List<MaterialListEntity> listMaterialLists() {
        return materialListDao.listMaterialLists();
    }
}
