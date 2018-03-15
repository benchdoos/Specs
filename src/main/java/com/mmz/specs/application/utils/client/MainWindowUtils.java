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
import com.mmz.specs.service.DetailListService;
import com.mmz.specs.service.DetailListServiceImpl;
import org.hibernate.Session;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

public class MainWindowUtils {

    /*public TreeSet<DetailEntity> getDetailListFullTree(Session session) {
        TreeSet<DetailEntity> result = new TreeSet<>();
        DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));
        return getList(service);
    }

    public TreeSet<DetailEntity> getList(DetailListService service) { //TODO rename this somehow
        TreeSet<DetailEntity> result = new TreeSet<>();

        List<DetailEntity> listParents = service.listParents(null);
        for (DetailEntity parent : listParents) {
            List<DetailEntity> detailEntities = service.listChildren(parent);
            result.addAll(detailEntities);
        }
        return result;
    }*/

    public DefaultMutableTreeNode getDetailListFullTree(Session session) {
        DefaultMutableTreeNode result = new DefaultMutableTreeNode("Узлы");
        DetailListService service = new DetailListServiceImpl(new DetailListDaoImpl(session));


        ArrayList<String> uniqueRootDetails = new ArrayList<>();

        List<DetailListEntity> fullDetailListList = service.listDetailLists();
        for (DetailListEntity detailListEntity : fullDetailListList) {
            List<DetailEntity> rootParentsList = service.listParents(detailListEntity.getDetailByParentDetailId());
            if (rootParentsList.size() == 0) {
                if (!uniqueRootDetails.contains(detailListEntity.getDetailByParentDetailId().getNumber())) {
                    uniqueRootDetails.add(detailListEntity.getDetailByParentDetailId().getNumber());

                    DefaultMutableTreeNode node = getChildren(service, detailListEntity.getDetailByParentDetailId());
                    result.add(node);
                }
            }
        }
        return result;
    }

    private DefaultMutableTreeNode getChildren(DetailListService service, DetailEntity parent) {
        List<DetailEntity> childes = service.listChildren(parent);
        DefaultMutableTreeNode result = new DefaultMutableTreeNode();
        if (childes.size() > 0) {
            for (DetailEntity entity : childes) {
                result.add(getChildren(service, entity));
            }
        }
        result.setAllowsChildren(parent.isUnit());
        result.setUserObject(parent);
        return result;
    }
}
