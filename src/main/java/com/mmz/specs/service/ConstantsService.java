package com.mmz.specs.service;

import com.mmz.specs.dao.ConstantsDao;
import com.mmz.specs.model.ConstantsEntity;

import java.util.List;

public interface ConstantsService {

    public void setConstantsDao(ConstantsDao constantsDao);

    public ConstantsDao getConstantsDao();


    public void addConstant(ConstantsEntity constantsEntity);

    public void updateConstant(ConstantsEntity constantsEntity);

    public void removeConstant(int id);


    public ConstantsEntity getConstantById(int id);

    public ConstantsEntity getConstantByKey(String key);


    public List<ConstantsEntity> listConstants();

}
