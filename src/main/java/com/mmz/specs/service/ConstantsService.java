package com.mmz.specs.service;

import com.mmz.specs.model.ConstantsEntity;
import org.hibernate.Session;

import java.util.List;

public interface ConstantsService {
    public Session getSession();

    public void addConstant(ConstantsEntity constantsEntity);

    public void updateConstant(ConstantsEntity constantsEntity);

    public void removeConstant(int id);

    public ConstantsEntity getConstantById(int id);

    public ConstantsEntity getConstantByKey(String key);

    public List<ConstantsEntity> listConstants();

}
