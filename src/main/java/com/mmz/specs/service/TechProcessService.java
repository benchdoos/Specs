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
import com.mmz.specs.model.TechProcessEntity;

import java.util.List;

public interface TechProcessService {
    int addTechProcess(TechProcessEntity techProcessEntity);

    List<TechProcessEntity> getTechProcessByAlikeValue(String searchingString);

    TechProcessEntity getTechProcessById(int id);

    TechProcessEntity getTechProcessByValue(String value);

    TechProcessDao getTechProcessDao();

    void setTechProcessDao(TechProcessDao techProcessDao);

    List<TechProcessEntity> listTechProcesses();

    void removeTechProcess(int id);

    void updateTechProcess(TechProcessEntity techProcessEntity);
}
