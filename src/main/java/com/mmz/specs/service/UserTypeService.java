package com.mmz.specs.service;

import com.mmz.specs.dao.UserTypeDao;
import com.mmz.specs.model.UserTypeEntity;

import java.util.List;

public interface UserTypeService {

    public void setUserTypeDao(UserTypeDao userTypeDao);

    public UserTypeDao getUserTypeDao();


    public void updateUserType(UserTypeEntity entity);


    public UserTypeEntity getUserTypeById(int id);


    public List<UserTypeEntity> listUserTypes();
}
