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

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.connection.ServerDBConnectionPool;
import com.mmz.specs.model.UsersEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.transaction.Transactional;
import java.util.List;

public class UsersDaoImpl implements UsersDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    /**
     * Default constructor
     * Use for server only!!! Otherwise use {@link UsersDaoImpl}({@link Session} session)
     */
    public UsersDaoImpl() {
        session = ServerDBConnectionPool.getInstance().getSession();
    }

    public UsersDaoImpl(Session session) {
        this.session = session;
    }


    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }


    @Override
    @Transactional
    public int addUser(UsersEntity usersEntity) {
        Integer id = (Integer) session.save(usersEntity);
        usersEntity = getUserById(id);
        log.info("User successfully saved: " + usersEntity);
        return id;
    }

    @Override
    @Transactional
    public void updateUser(UsersEntity usersEntity) {
        session.merge(usersEntity);
        log.info("User successfully updated: " + usersEntity);
    }

    @Override
    @Transactional
    public void removeUser(int id) {
        UsersEntity usersEntity = session.load(UsersEntity.class, id);
        if (usersEntity != null) {
            session.delete(usersEntity);
        }
        log.info("User successfully removed: " + usersEntity);
    }


    @Override
    @Transactional
    public UsersEntity getUserById(int id) {
        UsersEntity usersEntity = session.load(UsersEntity.class, id);
        log.info("User successfully found by id:" + id + " " + usersEntity);
        return usersEntity;
    }

    @Override
    @Transactional
    public UsersEntity getUserByUsername(String username) {
        Query query = session.createQuery("from UsersEntity where username = :username");
        query.setParameter("username", username);

        final UsersEntity entity = (UsersEntity) query.uniqueResult();
        log.info("User successfully found by username: " + username + " " + entity);
        return entity;
    }


    @Override
    @Transactional
    public List<UsersEntity> listUsers() {
        List<UsersEntity> list = session.createQuery("from UsersEntity").list();
        for (UsersEntity usersEntity : list) {
            log.info("Users list: " + usersEntity);
        }
        return list;
    }
}
