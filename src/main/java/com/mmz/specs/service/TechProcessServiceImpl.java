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

import com.mmz.specs.dao.TechProcessDao;
import com.mmz.specs.dao.TechProcessDaoImpl;
import com.mmz.specs.model.TechProcessEntity;

import java.util.List;

public class TechProcessServiceImpl implements TechProcessService {
    private TechProcessDao techProcessDao;

    public TechProcessServiceImpl() {
        techProcessDao = new TechProcessDaoImpl();
    }

    public TechProcessServiceImpl(TechProcessDao techProcessDao) {
        this.techProcessDao = techProcessDao;
    }

    @Override
    public void setTechProcessDao(TechProcessDao techProcessDao) {
        this.techProcessDao = techProcessDao;
    }

    @Override
    public TechProcessDao getTechProcessDao() {
        return techProcessDao;
    }

    @Override
    public int addTechProcess(TechProcessEntity techProcessEntity) {
        return techProcessDao.addTechProcess(techProcessEntity);
    }

    @Override
    public void updateTechProcess(TechProcessEntity techProcessEntity) {
        techProcessDao.updateTechProcess(techProcessEntity);
    }

    @Override
    public void removeTechProcess(int id) {
        techProcessDao.removeTechProcess(id);
    }

    @Override
    public TechProcessEntity getTechProcessById(int id) {
        return techProcessDao.getTechProcessById(id);
    }

    @Override
    public TechProcessEntity getTechProcessByValue(String value) {
        return techProcessDao.getTechProcessByValue(value);
    }

    @Override
    public List<TechProcessEntity> getTechProcessByAlikeValue(String searchingString) {
        return techProcessDao.getTechProcessByAlikeValue(searchingString);
    }

    @Override
    public List<TechProcessEntity> listTechProcesses() {
        return techProcessDao.listTechProcesses();
    }
}
