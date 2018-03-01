package com.mmz.specs.dao;

import com.mmz.specs.model.ConstantsEntity;
import org.hibernate.Session;

import java.util.List;

public interface ConstantsDao {
    public Session getSession();

    /**
     * Add new constant //if needed for future releases
     */
    public void addConstant(ConstantsEntity constantsEntity);

    /**
     * Update existing constant
     */
    public void updateConstant(ConstantsEntity constantsEntity);


    /**
     * Remove constant by id
     * @param id of the removing book
     */
    public void removeConstant(int id);

    /**
     * @param id Constant id
     * @return Constant by id
     */
    public ConstantsEntity getConstantById(int id);

    /**
     * @param key for value.
     * @return ConstantsEntity by key
     */
    public ConstantsEntity getConstantByKey(String key);


    /**
     * @return constants list
     * */
    public List<ConstantsEntity> listConstants();
}
