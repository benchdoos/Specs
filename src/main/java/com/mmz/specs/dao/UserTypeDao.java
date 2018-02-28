package com.mmz.specs.dao;

import com.mmz.specs.model.UserTypeEntity;

import java.util.List;

public interface UserTypeDao {

    public UserTypeEntity getUserTypeById(int id);

    public List<UserTypeEntity> listUserTypes();
}
