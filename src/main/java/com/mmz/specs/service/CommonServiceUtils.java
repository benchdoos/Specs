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

import com.google.common.collect.ComparisonChain;
import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.DetailListEntity;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class CommonServiceUtils {

    public static ArrayList<DetailEntity> getRootObjects(Session session) {
        DetailListService service = new DetailListServiceImpl(session);

        final List<DetailListEntity> listEntities = service.listDetailLists();
        if (!Thread.currentThread().isInterrupted()) {
            listEntities.sort((o1, o2) -> ComparisonChain.start()
                    .compare(o1.getDetailByParentDetailId().getCode(), o2.getDetailByParentDetailId().getCode())
                    .compareTrueFirst(o1.isActive(), o2.isActive())
                    .result());

            ArrayList<DetailEntity> roots = new ArrayList<>();
            ArrayList<DetailEntity> children = new ArrayList<>();

            for (DetailListEntity current : listEntities) {
                final DetailEntity parentDetailEntity = current.getDetailByParentDetailId();

                if (!roots.contains(parentDetailEntity)) {
                    if (!children.contains(parentDetailEntity)) {
                        List<DetailEntity> parents = service.listParents(parentDetailEntity);
                        if (parents.size() == 0) {
                            roots.add(parentDetailEntity);
                        } else {
                            children.add(parentDetailEntity);
                        }
                    }
                }
            }
            return roots;
        }
        return new ArrayList<>();
    }

    public static void sortDetailEntityArray(ArrayList<DetailEntity> totalChildrenList) {
        if (totalChildrenList.size() > 1) {
            totalChildrenList.sort((o1, o2) -> ComparisonChain.start()
                    .compareTrueFirst(o1.isUnit(), o2.isUnit())
                    .compare(o1.getCode(), o2.getCode())
                    .compareTrueFirst(o1.isActive(), o2.isActive())
                    .result());
        }
    }
}
