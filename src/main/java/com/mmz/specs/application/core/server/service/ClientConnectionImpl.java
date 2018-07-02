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

package com.mmz.specs.application.core.server.service;

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.model.UsersEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.io.IOException;
import java.net.Socket;

public class ClientConnectionImpl implements ClientConnection {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private Socket socket;
    private Session session;
    private UsersEntity entity;

    ClientConnectionImpl() {
    }

    public ClientConnectionImpl(Socket socket, Session session, UsersEntity entity) {
        this.socket = socket;
        this.session = session;
    }

    @Override
    public UsersEntity getUserEntity() {
        return entity;
    }

    @Override
    public void setUserEntity(UsersEntity entity) {
        this.entity = entity;
    }

    @Override
    public Socket getSocket() {
        return this.socket;
    }

    @Override
    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public Session getSession() {
        return this.session;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.session != null) {
                this.session.close();
            }
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException | RuntimeException e) {
            log.warn("Could not close session or socket", e);
            throw new IOException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientConnectionImpl that = (ClientConnectionImpl) o;

        if (!this.socket.equals(that.socket)) return false;
        return this.session.equals(that.session);
    }

    @Override
    public int hashCode() {
        int result = this.socket != null ? this.socket.hashCode() : 0;
        result = 31 * result + (this.session != null ? this.session.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String sessionString;
        if (this.session != null) {
            sessionString = "some session";
        } else sessionString = "null session";
        return new ToStringBuilder(this)
                .append("socket", this.socket)
                .append("session", sessionString)
                .toString();
    }
}
