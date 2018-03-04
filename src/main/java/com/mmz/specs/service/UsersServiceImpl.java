package com.mmz.specs.service;

import com.mmz.specs.dao.UsersDao;
import com.mmz.specs.dao.UsersDaoImpl;
import com.mmz.specs.model.UsersEntity;

import javax.transaction.Transactional;
import java.util.List;

public class UsersServiceImpl implements UsersService {
    private UsersDao usersDao;

    public UsersServiceImpl() {
        this.usersDao = new UsersDaoImpl();
    }

    public UsersServiceImpl(UsersDao usersDao) {
        this.usersDao = usersDao;
    }


    @Override
    public void setUsersDao(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    @Override
    public UsersDao getUsersDao() {
        return usersDao;
    }


    @Override
    @Transactional
    public int addUser(UsersEntity usersEntity) {
        return this.usersDao.addUser(usersEntity);
    }

    @Override
    @Transactional
    public void updateUser(UsersEntity usersEntity) {
        this.usersDao.updateUser(usersEntity);
    }

    @Override
    @Transactional
    public void removeUser(int id) {
        this.usersDao.removeUser(id);
    }


    @Override
    @Transactional
    public UsersEntity getUserById(int id) {
        return this.usersDao.getUserById(id);
    }

    @Override
    @Transactional
    public UsersEntity getUserByUsername(String username) {
        return this.usersDao.getUserByUsername(username);
    }


    @Override
    @Transactional
    public List<UsersEntity> listUsers() {
        return this.usersDao.listUsers();
    }
}
