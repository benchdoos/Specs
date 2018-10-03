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

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.dao.MaterialDao;
import com.mmz.specs.dao.MaterialDaoImpl;
import com.mmz.specs.model.MaterialEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.List;

public class MaterialServiceImpl implements MaterialService {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private MaterialDao materialDao;

    public MaterialServiceImpl() {
        materialDao = new MaterialDaoImpl();
    }

    public MaterialServiceImpl(MaterialDao materialDao) {
        this.materialDao = materialDao;
    }

    public MaterialServiceImpl(Session session) {
        this.materialDao = new MaterialDaoImpl(session);
    }

    @Override

    public int addMaterial(MaterialEntity materialEntity) {
        return this.materialDao.addMaterial(materialEntity);
    }

    @Override

    public MaterialEntity getMaterialById(int id) {
        return this.materialDao.getMaterialById(id);
    }

    @Override

    public MaterialEntity getMaterialByShortMarkAndProfile(String shortMark, String shortProfile) {
        return this.materialDao.getMaterialByShortMarkAndProfile(shortMark, shortProfile);
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

    public List<MaterialEntity> listMaterials() {
        return this.materialDao.listMaterials();
    }

    @Override
    public MaterialEntity migrate(MaterialEntity oldEntity, MaterialEntity newEntity) {
        oldEntity.setLongMark(newEntity.getLongMark());
        oldEntity.setLongProfile(newEntity.getLongProfile());
        oldEntity.setShortMark(newEntity.getShortMark());
        oldEntity.setShortProfile(newEntity.getShortProfile());
        oldEntity.setActive(newEntity.isActive());
        log.debug("Merged material: " + oldEntity);
        return oldEntity;
    }

    @Override

    public void removeMaterial(int id) {
        this.materialDao.removeMaterial(id);
    }

    @Override

    public void updateMaterial(MaterialEntity materialEntity) {
        this.materialDao.updateMaterial(materialEntity);
    }
}
