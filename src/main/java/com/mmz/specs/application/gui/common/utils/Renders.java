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

package com.mmz.specs.application.gui.common.utils;

import com.mmz.specs.application.utils.client.CommonWindowUtils;
import com.mmz.specs.model.MaterialEntity;
import com.mmz.specs.model.MaterialListEntity;

import javax.swing.*;
import java.awt.*;

public class Renders {
    public static DefaultListCellRenderer DEFAULT_LIST_MATERIAL_LIST_CELL_RENDERER = new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            try {
                if (value instanceof MaterialListEntity) {
                    MaterialListEntity entity = (MaterialListEntity) value;
                    final String shortMark = entity.getMaterialByMaterialId().getShortMark();
                    String shortProfile = entity.getMaterialByMaterialId().getShortProfile();
                    setOpaque(true);

                    shortProfile = CommonWindowUtils.getCanonicalProfile(shortProfile);

                    return super.getListCellRendererComponent(list, shortMark + " " + shortProfile + (entity.isMainMaterial() ? " (Основной)" : ""), index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            } catch (Exception e) {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    };

    public static DefaultListCellRenderer DEFAULT_LIST_MATERIAL_CELL_RENDERER = new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            try {
                if (value instanceof MaterialEntity) {
                    MaterialEntity entity = (MaterialEntity) value;
                    final String shortProfile = entity.getShortProfile();
                    final String shortMark = entity.getShortMark();
                    setOpaque(true);

                    return super.getListCellRendererComponent(list, shortMark + " " + shortProfile, index, isSelected, cellHasFocus);
                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            } catch (Exception e) {
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    };
}
