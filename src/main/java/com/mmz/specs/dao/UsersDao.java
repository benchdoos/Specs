package com.mmz.specs.dao;

import com.mmz.specs.model.UsersEntity;
import org.hibernate.Session;

import java.util.List;

public interface UsersDao {
    public Session getSession();

    public void setSession(Session session);


    public int addUser(UsersEntity usersEntity);

    public void updateUser(UsersEntity usersEntity);

    public void removeUser(int id);


    public UsersEntity getUserById(int id);

    public UsersEntity getUserByUsername(String name);


    public List<UsersEntity> listUsers();
}
