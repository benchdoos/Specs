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
import com.mmz.specs.model.UserTypeEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

public class UserTypeDaoImpl implements UserTypeDao {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;

    public UserTypeDaoImpl() {
    }

    public UserTypeDaoImpl(Session session) {
        this.session = session;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    @Transactional
    public void updateUserType(UserTypeEntity userTypeEntity) {
        session.merge(userTypeEntity);
        log.info("UserType successfully updated: " + userTypeEntity);
    }

    @Override
    @Transactional
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    @Transactional
    public UserTypeEntity getUserTypeById(int id) {
        UserTypeEntity userTypeEntity = session.load(UserTypeEntity.class, id);
        log.debug("UserType found by id:" + id + " " + userTypeEntity);
        return userTypeEntity;
    }

    @Override
    @Transactional
    public List<UserTypeEntity> listUserTypes() {
        List list = session.createQuery("from UserTypeEntity").list();
        List<UserTypeEntity> result = new ArrayList<>(list.size());
        for (Object userTypeEntity : list) {
            if (userTypeEntity instanceof UserTypeEntity) {
                result.add((UserTypeEntity) userTypeEntity);
            } else {
                log.warn("Not UserType from list: " + userTypeEntity);
            }
        }
        return result;
    }
}
