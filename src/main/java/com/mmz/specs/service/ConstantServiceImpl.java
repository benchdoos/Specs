package com.mmz.specs.service;

import com.mmz.specs.dao.ConstantsDao;
import com.mmz.specs.model.ConstantsEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ConstantServiceImpl implements ConstantsService {
    private ConstantsDao constantsDao;

    public void setConstantsDao(ConstantsDao constantsDao) {
        this.constantsDao = constantsDao;
    }

    @Override
    @Transactional
    public void addConstant(ConstantsEntity constantsEntity) {
        this.constantsDao.addConstant(constantsEntity);
    }

    @Override
    @Transactional
    public void updateConstant(ConstantsEntity constantsEntity) {
        this.constantsDao.updateConstant(constantsEntity);
    }

    @Override
    @Transactional
    public void removeConstant(int id) {
        this.constantsDao.removeConstant(id);
    }

    @Override
    @Transactional
    public ConstantsEntity getConstantById(int id) {
        return this.constantsDao.getConstantById(id);
    }

    @Override
    @Transactional
    public ConstantsEntity getConstantByKey(String key) {
        return this.constantsDao.getConstantByKey(key);
    }

    @Override
    @Transactional
    public List<ConstantsEntity> listConstants() {
        return this.constantsDao.listConstants();
    }
}
