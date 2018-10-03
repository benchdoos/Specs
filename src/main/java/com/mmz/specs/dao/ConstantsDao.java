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

package com.mmz.specs.dao;

import com.mmz.specs.model.ConstantsEntity;
import org.hibernate.Session;

import java.util.List;

public interface ConstantsDao {
    /**
     * Add new constant //if needed for future releases
     */
    int addConstant(ConstantsEntity constantsEntity);

    /**
     * @param id Constant id
     * @return Constant by id
     */
    ConstantsEntity getConstantById(int id);

    /**
     * @param key for value.
     * @return ConstantsEntity by key
     */
    ConstantsEntity getConstantByKey(String key);

    Session getSession();

    /**
     * @return constants list
     */
    List<ConstantsEntity> listConstants();

    /**
     * Remove constant by id
     *
     * @param id of the removing book
     */
    void removeConstant(int id);

    /**
     * Update existing constant
     */
    void updateConstant(ConstantsEntity constantsEntity);
}
