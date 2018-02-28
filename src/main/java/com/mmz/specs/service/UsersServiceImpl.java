package com.mmz.specs.service;

import com.mmz.specs.dao.UsersDao;
import com.mmz.specs.model.UsersEntity;

import javax.transaction.Transactional;
import java.util.List;

public class UsersServiceImpl implements UsersService {
    private UsersDao usersDao;

    public void setUsersDao(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    @Override
    @Transactional
    public void addUser(UsersEntity usersEntity) {
        this.usersDao.addUserType(usersEntity);

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
