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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;

public class ClientConnectionImpl implements ClientConnection {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private Socket socket;
    private UsersEntity user;

    public ClientConnectionImpl() {
    }

    @Override
    public UsersEntity getUser() {
        return user;
    }

    @Override
    public void setUser(UsersEntity entity) {
        this.user = entity;
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
    public void close() throws IOException {
        try {
            if (this.socket != null) {
                log.debug("Closing socket");
                this.socket.close();
                log.info("Socket successfully closed");
            }
        } catch (IOException | RuntimeException e) {
            log.warn("Could not close session or socket", e);
            throw new IOException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClientConnectionImpl)) return false;

        ClientConnectionImpl that = (ClientConnectionImpl) o;

        return new EqualsBuilder()
                .append(this.socket, that.socket)
                .append(this.user, that.user)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(socket)
                .append(user)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("socket", this.socket)
                .append("user", user)
                .toString();
    }
}
