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

import com.mmz.specs.dao.UsersDao;
import com.mmz.specs.dao.UsersDaoImpl;
import com.mmz.specs.model.UsersEntity;
import org.hibernate.Session;

import java.util.List;

public class UsersServiceImpl implements UsersService {
    private UsersDao usersDao;

    public UsersServiceImpl() {
        this.usersDao = new UsersDaoImpl();
    }

    public UsersServiceImpl(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    public UsersServiceImpl(Session session) {
        this.usersDao = new UsersDaoImpl(session);
    }

    @Override

    public int addUser(UsersEntity usersEntity) {
        return this.usersDao.addUser(usersEntity);
    }

    @Override

    public UsersEntity getUserById(int id) {
        return this.usersDao.getUserById(id);
    }

    @Override

    public UsersEntity getUserByUsername(String username) {
        return this.usersDao.getUserByUsername(username);
    }

    @Override
    public UsersDao getUsersDao() {
        return usersDao;
    }

    @Override
    public void setUsersDao(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    @Override

    public List<UsersEntity> listUsers() {
        return this.usersDao.listUsers();
    }

    @Override

    public void removeUser(int id) {
        this.usersDao.removeUser(id);
    }

    @Override

    public void updateUser(UsersEntity usersEntity) {
        this.usersDao.updateUser(usersEntity);
    }
}
