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

package com.mmz.specs.service;

import com.mmz.specs.dao.DetailDao;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailTitleEntity;

import java.util.List;

public interface DetailService {

    int addDetail(DetailEntity detailEntity);

    DetailEntity getDetailByCode(String code);

    DetailEntity getDetailById(int id);

    DetailDao getDetailDao();

    void setDetailDao(DetailDao detailDao);

    List<DetailEntity> getDetailsBySearch(String searchText);

    List<DetailEntity> getDetailsByTitle(DetailTitleEntity titleEntity);

    List<DetailEntity> listDetails();

    List<DetailEntity> listDetailsByEditedImage();

    void removeDetail(int id);

    void updateDetail(DetailEntity detailEntity);
}
