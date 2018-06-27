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
import com.mmz.specs.model.NoticeEntity;
import org.hibernate.Session;

import java.util.List;

public interface DetailListDao {
    Session getSession();

    void setSession(Session session);

    long addDetailList(DetailListEntity detailListEntity);

    void updateDetailList(DetailListEntity detailListEntity);

    void removeDetailList(long id);


    DetailListEntity getDetailListById(long id);

    DetailListEntity getDetailListByParentAndChildAndNotice(DetailEntity parent, DetailEntity child, NoticeEntity latestNotice);

    DetailListEntity getLatestDetailListEntityByParentAndChild(DetailEntity parent, DetailEntity child);

    List<DetailListEntity> getDetailListByParent(DetailEntity detailEntity);

    List<DetailListEntity> getDetailListByChild(DetailEntity detailEntity);

    List<DetailListEntity> getDetailListByParent(String detailEntityIndex);

    List<DetailListEntity> getDetailListByChild(String detailEntityIndex);

    List<DetailListEntity> getDetailListByNoticeId(int id);

    List<DetailListEntity> getDetailListBySearch(String searchText);

    List getDetailListByParentAndChild(DetailEntity parent, DetailEntity child);


    List<DetailEntity> listParents(DetailEntity detailEntity);

    List<DetailEntity> listChildren(DetailEntity detailEntity);

    List<DetailListEntity> listDetailLists();
}
