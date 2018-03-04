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

import com.mmz.specs.dao.MaterialDao;
import com.mmz.specs.dao.MaterialDaoImpl;
import com.mmz.specs.model.MaterialEntity;

import javax.transaction.Transactional;
import java.util.List;

public class MaterialServiceImpl implements MaterialService {
    private MaterialDao materialDao;

    public MaterialServiceImpl() {
        materialDao = new MaterialDaoImpl();
    }

    public MaterialServiceImpl(MaterialDao materialDao) {
        this.materialDao = materialDao;
    }

    @Override
    public MaterialDao getMaterialDao() {
        return materialDao;
    }

    @Override
    public void setMaterialDao(MaterialDao materialDao) {
        this.materialDao = materialDao;
    }


    @Override
    @Transactional
    public int addMaterial(MaterialEntity materialEntity) {
        return materialDao.addMaterial(materialEntity);
    }

    @Override
    @Transactional
    public void updateMaterial(MaterialEntity materialEntity) {

    }

    @Override
    @Transactional
    public void removeMaterial(int id) {

    }


    @Override
    @Transactional
    public MaterialEntity getMaterialById(int id) {
        return null;
    }

    @Override
    @Transactional
    public MaterialEntity getMaterialByShortMarkAndProfile(String shortMark, String shortProfile) {
        return null;
    }


    @Override
    @Transactional
    public List<MaterialEntity> listMaterials() {
        return null;
    }
}
