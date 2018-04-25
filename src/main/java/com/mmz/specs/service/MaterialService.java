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
import com.mmz.specs.model.MaterialEntity;

import java.util.List;

public interface MaterialService {

    public MaterialDao getMaterialDao();

    public void setMaterialDao(MaterialDao materialDao);

    public int addMaterial(MaterialEntity materialEntity);

    public void updateMaterial(MaterialEntity materialEntity);

    public void removeMaterial(int id);

    public MaterialEntity migrate(MaterialEntity oldMaterial, MaterialEntity newMaterial);

    public MaterialEntity getMaterialById(int id);

    public MaterialEntity getMaterialByShortMarkAndProfile(String shortMark, String shortProfile);


    public List<MaterialEntity> listMaterials();
}
