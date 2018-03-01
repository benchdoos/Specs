package com.mmz.specs.dao;

import com.mmz.specs.model.UserTypeEntity;
import org.hibernate.Session;

import java.util.List;

public interface UserTypeDao {

    public Session getSession();

    public UserTypeEntity getUserTypeById(int id);

    public List<UserTypeEntity> listUserTypes();
}
