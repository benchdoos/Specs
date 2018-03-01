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
        return username.toLowerCase();
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UsersEntity that = (UsersEntity) o;

        if (id != that.id) return false;
        if (admin != that.admin) return false;
        if (active != that.active) return false;
        if (editor != that.editor) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (patronymic != null ? !patronymic.equals(that.patronymic) : that.patronymic != null) return false;
        if (surname != null ? !surname.equals(that.surname) : that.surname != null) return false;

        return true;
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

    @ManyToOne
    @JoinColumn(name = "USER_TYPE_ID", referencedColumnName = "ID")
    public UserTypeEntity getUserType() {
        return userType;
    }

    public void setUserType(UserTypeEntity userTypeEntity) {
        this.userType = userTypeEntity;
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
}
