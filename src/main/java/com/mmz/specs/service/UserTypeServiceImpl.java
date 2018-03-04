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

import com.mmz.specs.dao.UserTypeDao;
import com.mmz.specs.dao.UserTypeDaoImpl;
import com.mmz.specs.model.UserTypeEntity;

import java.util.List;

public class UserTypeServiceImpl implements UserTypeService {

    private UserTypeDao userTypeDao;

    public UserTypeServiceImpl() {
        userTypeDao = new UserTypeDaoImpl();
    }

    public UserTypeServiceImpl(UserTypeDao userTypeDao) {
        this.userTypeDao = userTypeDao;
    }

    @Override
    public UserTypeDao getUserTypeDao() {
        return userTypeDao;
    }

    @Override
    public void updateUserType(UserTypeEntity entity) {
        this.userTypeDao.updateUserType(entity);
    }

    @Override
    public void setUserTypeDao(UserTypeDao userTypeDao) {
        this.userTypeDao = userTypeDao;
    }

    @Override
    public UserTypeEntity getUserTypeById(int id) {
        return userTypeDao.getUserTypeById(id);
    }


    @Override
    public List<UserTypeEntity> listUserTypes() {
        return userTypeDao.listUserTypes();
    }
}
