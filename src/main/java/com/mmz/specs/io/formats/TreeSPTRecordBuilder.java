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

package com.mmz.specs.io.formats;

import com.mmz.specs.model.DetailEntity;
import com.mmz.specs.model.MaterialEntity;

import java.util.ArrayList;
import java.util.List;

public class TreeSPTRecordBuilder {
    TreeSPTRecord record = new TreeSPTRecord();


    public TreeSPTRecordBuilder() {
    }

    public TreeSPTRecordBuilder(TreeSPTRecord record) {
        this.record = record;
    }

    public TreeSPTRecordBuilder setDetail(DetailEntity detail) {
        record.setDetail(detail);
        return this;
    }

    public TreeSPTRecordBuilder setQuantity(int quantity) {
        record.setQuantity(quantity);
        return this;
    }


    public TreeSPTRecordBuilder setMaterials(List<Object> materials) {
        ArrayList<MaterialEntity> list = new ArrayList<>();
        for (Object o : materials) {
            if (o instanceof MaterialEntity) {
                list.add((MaterialEntity) o);
            }
        }
        record.setMaterials(list);
        return this;
    }

    public TreeSPTRecordBuilder setInterchangeable(boolean interchangeable) {
        record.setInterchangeable(interchangeable);
        return this;
    }

    public TreeSPTRecord getTreeSPTRecord() {
        return record;
    }
}
