package com.mmz.specs.service;

import com.mmz.specs.dao.UserTypeDao;
import com.mmz.specs.model.UserTypeEntity;

import java.util.List;

public interface UserTypeService {
    public UserTypeDao getUserDao();

    public UserTypeEntity getUserTypeById(int id);

    public List<UserTypeEntity> listUserTypes();
}
