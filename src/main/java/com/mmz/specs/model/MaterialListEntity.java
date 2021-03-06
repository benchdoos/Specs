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

package com.mmz.specs.model;

import com.google.common.collect.ComparisonChain;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;

@Entity
@Table(name = "MATERIAL_LIST", schema = "", catalog = "")
public class MaterialListEntity implements Comparable<MaterialListEntity> {
    private long id;
    private boolean isActive;
    private DetailEntity detailByDetailId;
    private MaterialEntity materialByMaterialId;
    private boolean isMainMaterial;

    @Override
    public int compareTo(@Nonnull MaterialListEntity that) {
        return ComparisonChain.start()
                .compare(this.getMaterialByMaterialId().getLongMark(), that.getMaterialByMaterialId().getLongMark())
                .compare(this.getMaterialByMaterialId().getLongProfile(), that.getMaterialByMaterialId().getLongProfile())
                .compareFalseFirst(this.isMainMaterial(), that.isMainMaterial())
                .compareFalseFirst(this.isActive(), that.isActive())
                .result();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaterialListEntity)) return false;
        MaterialListEntity that = (MaterialListEntity) o;

        EqualsBuilder eb = new EqualsBuilder();
        eb.append(id, that.id);
        return eb.isEquals();
    }

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "DETAIL_ID", referencedColumnName = "ID", nullable = false)
    public DetailEntity getDetailByDetailId() {
        return detailByDetailId;
    }

    public void setDetailByDetailId(DetailEntity detailByDetailId) {
        this.detailByDetailId = detailByDetailId;
    }

    @Id
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "MATERIAL_ID", referencedColumnName = "ID", nullable = false)
    public MaterialEntity getMaterialByMaterialId() {
        return materialByMaterialId;
    }

    public void setMaterialByMaterialId(MaterialEntity materialByMaterialId) {
        this.materialByMaterialId = materialByMaterialId;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Basic
    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "IS_MAIN_MATERIAL", nullable = false)
    public boolean isMainMaterial() {
        return isMainMaterial;
    }

    public void setMainMaterial(boolean mainMaterial) {
        isMainMaterial = mainMaterial;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("isActive", isActive)
                .append("isMainMaterial", isMainMaterial)
                .append("detailByDetailId", detailByDetailId)
                .append("materialByMaterialId", materialByMaterialId)
                .toString();
    }
}
