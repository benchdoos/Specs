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

import com.mmz.specs.application.utils.Logging;
import com.mmz.specs.dao.DetailListDaoImpl;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import com.mmz.specs.model.NoticeEntity;
import com.mmz.specs.service.DetailListService;
import com.mmz.specs.service.DetailListServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainWindowUtils {
    private static final Logger log = LogManager.getLogger(Logging.getCurrentClassName());

    private Session session;

    public MainWindowUtils(Session session) {
        this.session = session;
    }

    public DefaultMutableTreeNode getDetailListFullTree(List<DetailListEntity> askedListRoot) {

        DefaultMutableTreeNode result = new DefaultMutableTreeNode();
        DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));


        ArrayList<String> uniqueRootDetails = new ArrayList<>();

        for (DetailListEntity detailListEntity : askedListRoot) {
            List<DetailEntity> rootParentsList = service.listParents(detailListEntity.getDetailByParentDetailId());
            if (rootParentsList.size() == 0) {
                addUniqueResult(result, service, uniqueRootDetails, detailListEntity);
            }
        }
        return result;
    }

    private void addUniqueResult(DefaultMutableTreeNode result, DetailListService service, ArrayList<String> uniqueRootDetails, DetailListEntity detailListEntity) {
        //testme, does it work properly
        if (!uniqueRootDetails.contains(detailListEntity.getDetailByParentDetailId().getCode())) {
            if (detailListEntity.getDetailByParentDetailId().isActive()) {
                uniqueRootDetails.add(detailListEntity.getDetailByParentDetailId().getCode());

                DefaultMutableTreeNode node = getChildren(service, detailListEntity.getDetailByParentDetailId());
                result.add(node);
            }
        }
    }

    public DefaultMutableTreeNode getDetailListTreeByDetailList(List<DetailListEntity> askedListRoot) {

        DefaultMutableTreeNode result = new DefaultMutableTreeNode();
        DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));


        ArrayList<String> uniqueRootDetails = new ArrayList<>();

        for (DetailListEntity detailListEntity : askedListRoot) {
            List<DetailEntity> rootParentsList = service.listChildren(detailListEntity.getDetailByParentDetailId());
            if (rootParentsList.size() > 0) {
                addUniqueResult(result, service, uniqueRootDetails, detailListEntity);
            }
        }
        return result;
    }

    public DefaultMutableTreeNode getChildren(DetailListService service, DetailEntity parent) {
        List<DetailEntity> childes = service.listChildren(parent);
        DefaultMutableTreeNode result = new DefaultMutableTreeNode();
        if (childes.size() > 0) {
            for (DetailEntity entity : childes) {

                List<DetailListEntity> detailListEntities = service.getDetailListByParent(parent);
                NoticeEntity lastNotice = null;
                for (DetailListEntity detailListEntity : detailListEntities) {
                    if (entity != null) {
                        ArrayList<NoticeEntity> notices = new ArrayList<>();

                        if (detailListEntity.getDetailByChildDetailId().getId() == entity.getId()) {
                            notices.add(detailListEntity.getNoticeByNoticeId());

                            Collections.sort(notices);

                            if (notices.size() > 0) {
                                lastNotice = notices.get(notices.size() - 1);
                                String entityInfo = entity.getId() + " " + entity.getCode() + " " + entity.getDetailTitleByDetailTitleId().getTitle();
                                log.trace(">>> latest notice for " + entityInfo + " is: " + lastNotice);
                            }
                        }
                    }
                }

//                List<DetailListEntity> detailListEntitiesByParentAndChild = getDetailListEntitiesByParentAndChild(parent, entity);
                List<DetailListEntity> detailListByParentAndChild = new DetailListServiceImpl(new DetailListDaoImpl(session)).getDetailListByParentAndChild(parent, entity);
                DetailListEntity lastDetailListEntity = null;
                for (DetailListEntity e : detailListByParentAndChild) {
                    if (e.getNoticeByNoticeId().equals(lastNotice)) {
                        log.trace("Got equal notices, current: {}, last: {}", e, lastNotice);
                        int id = e.getNoticeByNoticeId().getId();
                        if (id >= lastNotice.getId()) {
                            lastNotice = e.getNoticeByNoticeId();
                            lastDetailListEntity = e;
                        }
                    }
                }

                log.trace("Finally got params: entity: {}, lastDetailListForEntity: {}", entity, lastDetailListEntity);
                if (entity != null) {
                    if (lastDetailListEntity != null) {
                        if (lastDetailListEntity.isActive()) {
                            if (entity.isActive()) {
                                result.add(getChildren(service, entity));
                            }
                        }
                    }
                }

            }
        }
        result.setAllowsChildren(parent.isUnit());
        result.setUserObject(parent);
        return result;
    }

   /* public List<DetailListEntity> getDetailListEntitiesByParentAndChild(DetailEntity parent, DetailEntity child) {
        DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
        List<DetailListEntity> list = service.listDetailLists();
        List<DetailListEntity> result = new ArrayList<>();

        for (DetailListEntity current : list) {
            if (current.getDetailByParentDetailId().getId() == parent.getId()) {
                if (current.getDetailByChildDetailId().getId() == child.getId()) {
                    result.add(current);
                }
            }
        }
        return result;
    }*/


    public DetailListEntity getLatestDetailListEntity(List<DetailListEntity> result) {
        DetailListEntity latest = null;
        log.trace("Getting latest result from :" + result);
        for (DetailListEntity entity : result) {
            if (latest != null) {
                boolean before = latest.getNoticeByNoticeId().getDate().before(entity.getNoticeByNoticeId().getDate());
                boolean equals = latest.getNoticeByNoticeId().getDate().equals(entity.getNoticeByNoticeId().getDate());
                log.trace("Comparing latest " + latest + " and current" + entity);
                log.trace("Latest is before current: " + before + " latest date equals current: " + equals);
                if (before || equals) {
                    log.trace("Changing latest from " + latest + " to " + entity);
                    latest = entity;
                }
            } else {
                latest = entity;
            }
        }
        log.trace("Latest entity: " + latest + " from " + result);
        return latest;
    }
}
