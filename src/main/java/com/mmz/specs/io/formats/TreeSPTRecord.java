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
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;

public class TreeSPTRecord {
    DetailEntity detail;
    int quantity;
    boolean interchangeable;
    ArrayList<MaterialEntity> materials;

    public DetailEntity getDetail() {
        return detail;
    }

    public void setDetail(DetailEntity detail) {
        this.detail = detail;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isInterchangeable() {
        return interchangeable;
    }

    public void setInterchangeable(boolean interchangeable) {
        this.interchangeable = interchangeable;
    }

    public ArrayList<MaterialEntity> getMaterials() {
        return materials;
    }

    public void setMaterials(ArrayList<MaterialEntity> materials) {
        this.materials = materials;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("detail", detail)
                .append("quantity", quantity)
                .append("interchangeable", interchangeable)
                .append("materials", materials)
                .toString();
    }
}
