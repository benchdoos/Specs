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

package com.mmz.specs.io.utils;

import com.mmz.specs.application.gui.common.utils.managers.ProgressManager;
import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import com.mmz.specs.model.MaterialListEntity;
import com.mmz.specs.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExportSPTUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());
    private Session session;
    private ProgressManager progressManager;

    public ExportSPTUtils(Session session, ProgressManager progressManager) {
        this.session = session;
        this.progressManager = progressManager;
    }

    public JSONArray getFullTree() {
        log.info("Creating root entities");
        progressManager.setText("Формирование корневых каталогов");

        ArrayList<DetailEntity> root = CommonServiceUtils.getRootObjects(session);

        log.debug("Root entities: ({}) {}", root.size(), root);

        JSONArray array = new JSONArray();
        if (root != null) {
            for (int i = 0; i < root.size(); i++) {

                DetailEntity entity = root.get(i);

                log.debug("Creating tree for root entity: {}", entity.toSimpleString());

                progressManager.setText("Формирование корневого каталога: " + (i + 1) + " из " + root.size());

                JSONObject object = new JSONObject();
                object.put("detail", entity);
                object.put("quantity", 1);
                final JSONArray allChildrenForEntity = getAllChildrenForEntity(entity);
                log.debug("Children for {} (size: {}): {}", entity.toSimpleString(), allChildrenForEntity.length(), allChildrenForEntity);
                object.put("children", allChildrenForEntity);

                array.put(object);

                int progress = (int) (((double) i / (root.size() - 1)) * 100);
                progressManager.setCurrentProgress(progress);
            }
        } else {
            progressManager.setText("Не удалось найти корневые каталоги");
        }
        return array;
    }

    private JSONArray getAllChildrenForEntity(DetailEntity parent) {
        JSONArray result = new JSONArray();
        DetailListService service = new DetailListServiceImpl(session);
        final ArrayList<DetailEntity> totalChildrenList = (ArrayList<DetailEntity>) service.listChildren(parent);
        if (totalChildrenList.size() > 0) {
            log.trace("Parent {} has children count: {}", parent.getCode() + " " + parent.getDetailTitleByDetailTitleId().getTitle(), totalChildrenList.size());
            CommonServiceUtils.sortDetailEntityArray(totalChildrenList);

            for (DetailEntity child : totalChildrenList) {
                if (child != null) {
                    if (child.isActive()) {
                        DetailListEntity lastDetailListEntity = service.getLatestDetailListEntityByParentAndChild(parent, child);
                        if (lastDetailListEntity != null) {
                            if (lastDetailListEntity.isActive()) {
                                JSONObject record = new JSONObject();
                                record.put("quantity", lastDetailListEntity.getQuantity());
                                record.put("detail", child);
                                record.put("materials", getAllMaterialsForEntity(child));
                                record.put("interchangeable", lastDetailListEntity.isInterchangeableNode());
                                if (child.isUnit()) {
                                    record.put("children", getAllChildrenForEntity(child));
                                }
                                result.put(record);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private JSONArray getAllMaterialsForEntity(DetailEntity child) {
        JSONArray result = new JSONArray();
        MaterialListService service = new MaterialListServiceImpl(session);
        final List<MaterialListEntity> materialListByDetail = service.getMaterialListByDetail(child);
        for (MaterialListEntity entity : materialListByDetail) {
            if (entity.isActive()) {
                if (entity.getMaterialByMaterialId().isActive()) {
                    result.put(entity.getMaterialByMaterialId());
                }
            }
        }
        return result;
    }


}
