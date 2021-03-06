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

import com.mmz.specs.model.DetailTitleEntity;
import org.hibernate.Session;

import java.util.List;

public interface DetailTitleDao {
    int addDetailTitle(DetailTitleEntity detailTitlesEntity);

    DetailTitleEntity getDetailTitleById(int id);

    DetailTitleEntity getDetailTitleByTitle(String title);

    List<DetailTitleEntity> getDetailTitlesBySearch(String searchText);

    Session getSession();

    void setSession(Session session);

    List<DetailTitleEntity> listDetailTitles();

    void removeDetailTitle(int id);

    void updateDetailTitle(DetailTitleEntity detailTitlesEntity);
}
