package com.mmz.specs.dao;

import com.mmz.specs.model.UsersEntity;

import java.util.List;

public interface UsersDao {
    public void addUserType(UsersEntity usersEntity);

    public void updateUser(UsersEntity usersEntity);

    public void removeUser(int id);

    public UsersEntity getUserById(int id);

    public UsersEntity getUserByUsername(String name);

    public List<UsersEntity> listUsers();
}
