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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.io.IOException;
import java.net.Socket;

public class ClientConnectionImpl implements ClientConnection {
    private static Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private Socket socket;
    private Session session;

    public ClientConnectionImpl() {
    }

    public ClientConnectionImpl(Socket socket, Session session) {
        this.socket = socket;
        this.session = session;
    }

    @Override
    public Socket getSocket() {
        return socket;
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
            if (session != null) {
                session.close();
            }
            if (socket != null) {
                socket.close();
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

        if (!socket.equals(that.socket)) return false;
        return session.equals(that.session);
    }

    @Override
    public int hashCode() {
        int result = socket != null ? socket.hashCode() : 0;
        result = 31 * result + (session != null ? session.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String sessionString = "";
        if (session != null) {
            sessionString = "some session";
        } else sessionString = "null session";
        return new ToStringBuilder(this)
                .append("socket", socket)
                .append("session", sessionString)
                .toString();
    }
}
