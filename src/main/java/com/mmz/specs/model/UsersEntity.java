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

package com.mmz.specs.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "USERS",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"USERNAME"})})
public class UsersEntity {
    private int id;
    private String username;
    private String password;
    private String name;
    private String patronymic;
    private String surname;
    private boolean admin;
    private boolean editor;
    private boolean active;
    private UserTypeEntity userType;


    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "USERNAME")
    public String getUsername() {
        if (username != null) {
            username = username.toLowerCase();
        }
        return username;
    }

    public void setUsername(String username) {
        if (username != null) {
            username = username.toLowerCase();
        }
        this.username = username;
    }

    @Basic
    @Column(name = "PASSWORD")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Basic
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "PATRONYMIC")
    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String lastname) {
        this.patronymic = lastname;
    }

    @Basic
    @Column(name = "SURNAME")
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Basic
    @Column(name = "ADMIN")
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    @Basic
    @Column(name = "EDITOR")
    public boolean isEditor() {
        return editor;
    }

    public void setEditor(boolean editor) {
        this.editor = editor;
    }

    @Basic
    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (patronymic != null ? patronymic.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (admin ? 1 : 0);
        result = 31 * result + (editor ? 1 : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UsersEntity)) return false;
        final UsersEntity that = (UsersEntity) other;
        return this.username.equals(that.getUsername());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("username", username)
                .append("password", password)
                .append("name", name)
                .append("patronymic", patronymic)
                .append("surname", surname)
                .append("admin", admin)
                .append("editor", editor)
                .append("isActive", active)
                .append("userType", userType)
                .toString();
    }

    @ManyToOne
    @JoinColumn(name = "USER_TYPE_ID", referencedColumnName = "ID")
    public UserTypeEntity getUserType() {
        return userType;
    }

    public void setUserType(UserTypeEntity userTypeEntity) {
        this.userType = userTypeEntity;
    }
}
