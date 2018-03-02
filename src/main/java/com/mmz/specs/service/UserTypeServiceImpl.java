package com.mmz.specs.service;

import com.mmz.specs.dao.UserTypeDao;
import com.mmz.specs.dao.UserTypeDaoImpl;
import com.mmz.specs.model.UserTypeEntity;

import java.util.List;

public class UserTypeServiceImpl implements UserTypeService {

    private UserTypeDao userTypeDao;

    public UserTypeServiceImpl() {
        userTypeDao = new UserTypeDaoImpl();
    }

    public UserTypeServiceImpl(UserTypeDao userTypeDao) {
        this.userTypeDao = userTypeDao;
    }

    @Override
    public UserTypeDao getUserDao() {
        return userTypeDao;
    }

    @Override
    public UserTypeEntity getUserTypeById(int id) {
        return userTypeDao.getUserTypeById(id);
    }

    @Override
    public List<UserTypeEntity> listUserTypes() {
        return userTypeDao.listUserTypes();
    }
}