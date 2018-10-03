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

package com.mmz.specs.application.gui.common;

import com.mmz.specs.application.utils.CommonUtils;
import com.mmz.specs.dao.DetailTitleDaoImpl;
import com.mmz.specs.model.DetailTitleEntity;
import com.mmz.specs.service.DetailTitleService;
import com.mmz.specs.service.DetailTitleServiceImpl;
import org.hibernate.Session;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class CommonComboBoxUtils {
    private Session session;

    public CommonComboBoxUtils(Session session) {
        this.session = session;
    }

    public ComboBoxModel<DetailTitleEntity> getTitleComboBoxModel(JComboBox<DetailTitleEntity> detailTitleComboBox) {
        DefaultComboBoxModel<DetailTitleEntity> model = (DefaultComboBoxModel<DetailTitleEntity>) detailTitleComboBox.getModel();
        DetailTitleService service = new DetailTitleServiceImpl(new DetailTitleDaoImpl(session));
        List<DetailTitleEntity> detailTitleEntities = service.listDetailTitles();
        Collections.sort(detailTitleEntities);

        model.removeAllElements();

        for (DetailTitleEntity entity : detailTitleEntities) {
            if (entity != null) {
                if (entity.isActive()) {
                    model.addElement(entity);
                }
            }
        }
        return model;
    }

    public DefaultListCellRenderer getTitleComboBoxRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof DetailTitleEntity) {
                    DetailTitleEntity detailTitleEntity = (DetailTitleEntity) value;
                    String title = detailTitleEntity.getTitle();
                    return super.getListCellRendererComponent(list, CommonUtils.substring(35, title), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        };
    }
}
