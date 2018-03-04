package com.mmz.specs.dao;

import com.mmz.specs.model.UserTypeEntity;
import org.hibernate.Session;

import java.util.List;

public interface UserTypeDao {

    public void setSession(Session session);

    public Session getSession();


    public void updateUserType(UserTypeEntity userTypeEntity);

    public UserTypeEntity getUserTypeById(int id);


    public List<UserTypeEntity> listUserTypes();
}
