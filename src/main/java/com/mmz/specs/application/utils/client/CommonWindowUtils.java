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

package com.mmz.specs.application.utils.client;

import com.mmz.specs.dao.DetailListDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import com.mmz.specs.service.DetailListServiceImpl;
import org.hibernate.Session;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommonWindowUtils {
    private Session session;

    public CommonWindowUtils(Session session) {
        this.session = session;
    }

    public DefaultListModel<DetailEntity> getEffectList(int id) {
        DefaultListModel<DetailEntity> model = new DefaultListModel<>();

        List<DetailListEntity> list = new DetailListServiceImpl(new DetailListDaoImpl(session)).getDetailListByNoticeId(id);
        ArrayList<String> unique = new ArrayList<>();

        for (DetailListEntity entity : list) {
            if (!unique.contains(entity.getDetailByParentDetailId().getNumber())) {
                unique.add(entity.getDetailByParentDetailId().getNumber());
                model.addElement(entity.getDetailByParentDetailId());
            }

            if (!unique.contains(entity.getDetailByChildDetailId().getNumber())) {
                unique.add(entity.getDetailByChildDetailId().getNumber());
                model.addElement(entity.getDetailByChildDetailId());
            }
        }

        Collections.sort(unique);
        model = sortModel(unique, model);

        return model;
    }

    public DefaultListModel<DetailEntity> sortModel(ArrayList<String> unique, DefaultListModel<DetailEntity> model) {
        DefaultListModel<DetailEntity> result = new DefaultListModel<>();
        for (String name : unique) {
            for (int i = 0; i < model.getSize(); i++) {
                DetailEntity entity = model.getElementAt(i);
                if (name.equalsIgnoreCase(entity.getNumber())) {
                    result.addElement(entity);
                }
            }
        }
        return result;
    }
}
