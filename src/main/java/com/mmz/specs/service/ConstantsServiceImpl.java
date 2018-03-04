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

import com.mmz.specs.dao.ConstantsDao;
import com.mmz.specs.dao.ConstantsDaoImpl;
import com.mmz.specs.model.ConstantsEntity;

import javax.transaction.Transactional;
import java.util.List;

public class ConstantsServiceImpl implements ConstantsService {
    private ConstantsDao constantsDao;

    public ConstantsServiceImpl() {
        constantsDao = new ConstantsDaoImpl();
    }

    public ConstantsServiceImpl(ConstantsDao constantsDao) {
        this.constantsDao = constantsDao;
    }

    @Override
    public ConstantsDao getConstantsDao() {
        return constantsDao;
    }

    @Override
    public void setConstantsDao(ConstantsDao constantsDao) {
        this.constantsDao = constantsDao;
    }


    @Override
    @Transactional
    public int addConstant(ConstantsEntity constantsEntity) {
        return this.constantsDao.addConstant(constantsEntity);
    }

    @Override
    @Transactional
    public void updateConstant(ConstantsEntity constantsEntity) {
        this.constantsDao.updateConstant(constantsEntity);
    }

    @Override
    @Transactional
    public void removeConstant(int id) {
        this.constantsDao.removeConstant(id);
    }


    @Override
    @Transactional
    public ConstantsEntity getConstantById(int id) {
        return this.constantsDao.getConstantById(id);
    }

    @Override
    @Transactional
    public ConstantsEntity getConstantByKey(String key) {
        return this.constantsDao.getConstantByKey(key);
    }


    @Override
    @Transactional
    public List<ConstantsEntity> listConstants() {
        return this.constantsDao.listConstants();
    }
}
