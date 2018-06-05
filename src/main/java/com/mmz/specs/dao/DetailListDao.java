/*
 * (C) Copyright 2018.  Eugene Zrazhevsky and others.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Contributors:
 * Eugene Zrazhevsky <eugene.zrazhevsky@gmail.com>
 */

package com.mmz.specs.dao;

import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import org.hibernate.Session;

import java.util.List;

public interface DetailListDao {
    public Session getSession();

    public void setSession(Session session);

    public long addDetailList(DetailListEntity detailListEntity);

    public void updateDetailList(DetailListEntity detailListEntity);

    public void removeDetailList(long id);


    public DetailListEntity getDetailListById(long id);


    public List<DetailListEntity> getDetailListByParent(DetailEntity detailEntity);

    public List<DetailListEntity> getDetailListByChild(DetailEntity detailEntity);

    public List<DetailListEntity> getDetailListByParent(String detailEntityIndex);

    public List<DetailListEntity> getDetailListByChild(String detailEntityIndex);

    public List<DetailListEntity> getDetailListByNoticeId(int id);

    public List<DetailListEntity> getDetailListBySearch(String searchText);

    public List<DetailListEntity> getDetailListByParentAndChild(DetailEntity parent, DetailEntity child);


    public List<DetailEntity> listParents(DetailEntity detailEntity);

    public List<DetailEntity> listChildren(DetailEntity detailEntity);

    public List<DetailListEntity> listDetailLists();
}
