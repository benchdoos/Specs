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
import com.mmz.specs.model.UsersEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class UsersDaoImpl implements UsersDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    /**
     * Default constructor
     * Use for server only!!! Otherwise use {@link UsersDaoImpl}({@link Session} session)
     */
    public UsersDaoImpl() {
    }

    public UsersDaoImpl(Session session) {
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
    public Session getSession() {
        return session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    @Transactional
    public UsersEntity getUserById(int id) {
        UsersEntity usersEntity = session.load(UsersEntity.class, id);
        log.debug("User found by id:" + id + " " + usersEntity);
        return usersEntity;
    }

    @Override
    @Transactional
    public UsersEntity getUserByUsername(String username) { //todo use criterias allover the code.
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<UsersEntity> criteriaQuery = builder.createQuery(UsersEntity.class);
        Root<UsersEntity> root = criteriaQuery.from(UsersEntity.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("username"), username));
        Query<UsersEntity> q = session.createQuery(criteriaQuery);
        final UsersEntity entity = q.uniqueResult();
        log.debug("User found by username " + username + ": " + entity);
        return entity;
    }

    @Override
    @Transactional
    public List<UsersEntity> listUsers() {
        List list = session.createQuery("from UsersEntity").list();
        List<UsersEntity> result = new ArrayList<>(list.size());

        for (Object usersEntity : list) {
            if (usersEntity instanceof UsersEntity) {
                result.add((UsersEntity) usersEntity);
            } else {
                log.warn("Not User from list: " + usersEntity);
            }
        }
        return result;
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
    public void updateUser(UsersEntity usersEntity) {
        session.merge(usersEntity);
        log.info("User successfully updated: " + usersEntity);
    }
}
