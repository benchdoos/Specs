package com.mmz.specs.service;

import com.mmz.specs.model.UsersEntity;

import java.util.List;

public interface UsersService {
    public void addUser(UsersEntity usersEntity);

    public void updateUser(UsersEntity usersEntity);

    public void removeUser(int id);

    public UsersEntity getUserById(int id);

    public UsersEntity getUserByUsername(String username);

    public List<UsersEntity> listUsers();

}
