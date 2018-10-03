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
    long addDetailList(DetailListEntity detailListEntity);

    List<DetailListEntity> getDetailListByChild(DetailEntity detailEntity);

    List<DetailListEntity> getDetailListByChild(String detailEntityIndex);

    DetailListEntity getDetailListById(long id);

    List<DetailListEntity> getDetailListByNoticeId(int id);

    List<DetailListEntity> getDetailListByParent(DetailEntity detailEntity);

    List<DetailListEntity> getDetailListByParent(String detailEntityIndex);

    List<DetailListEntity> getDetailListByParentAndChild(DetailEntity parent, DetailEntity child);

    DetailListEntity getDetailListByParentAndChildAndNotice(DetailEntity parent, DetailEntity child, NoticeEntity latestNotice);

    List<DetailListEntity> getDetailListBySearch(String searchText);

    DetailListEntity getLatestDetailListEntityByParentAndChild(DetailEntity parent, DetailEntity child);

    Session getSession();

    void setSession(Session session);

    List<DetailEntity> listChildren(DetailEntity detailEntity);

    List<DetailListEntity> listDetailLists();

    List<DetailEntity> listParents(DetailEntity detailEntity);

    void removeDetailList(long id);

    void updateDetailList(DetailListEntity detailListEntity);
}
