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

package com.mmz.specs.application.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Metamodel;
import org.hibernate.Session;
import org.hibernate.query.Query;

import javax.persistence.metamodel.EntityType;
import java.util.List;

public class SessionUtils {
    public static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    public static void refreshSession(Session session, Class class_) {
        final String name = class_.getName();
        log.debug("Refreshing session for: {}", name);
        if (session != null) {
            try {
                final Query query = session.createQuery("from " + name);
                final List list = query.list();
                log.debug("Refreshing list size: {}", list.size());
                for (Object o : list) {
                    session.refresh(o);
                }
                log.debug("Refreshing session for: {} successfully finished.", name);
            } catch (Exception e) {
                log.warn("Could not refresh current entity type: {}", name, e);
            }
        }
    }

    public static void refreshSession(Session session, Object o) {
        session.refresh(o);
    }

    public static void closeSessionSilently(Session session) {
        try {
            session.getTransaction().rollback();
        } catch (Exception e) {
            log.warn("Could not rollback transaction", e);
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                log.warn("Could not close session");
            }
        }
    }

    public void refreshSession(Session session) {
        log.debug("Refreshing session for all entities");
        if (session != null) {
            final Metamodel metamodel = session.getSessionFactory().getMetamodel();
            for (EntityType<?> entityType : metamodel.getEntities()) {
                SessionUtils.refreshSession(session, entityType.getClass());
            }
        }
        log.debug("Refreshing session for all entities successfully finished.");
    }
}
