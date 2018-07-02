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
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nonnull;
import javax.persistence.*;

@Entity
@Table(name = "DETAIL")
public class DetailEntity implements Comparable<DetailEntity>, SimpleOutput {
    private int id;
    private String code;
    private boolean unit;
    private Double finishedWeight;
    private Double workpieceWeight;
    private String imagePath;
    private boolean active;
    private DetailTitleEntity detailTitleByDetailTitleId;
    private TechProcessEntity techProcessByTechProcessId;

    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "CODE")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Basic
    @Column(name = "UNIT")
    public boolean isUnit() {
        return unit;
    }

    public void setUnit(boolean unit) {
        this.unit = unit;
    }

    @Basic
    @Column(name = "FINISHED_WEIGHT")
    public Double getFinishedWeight() {
        return finishedWeight;
    }

    public void setFinishedWeight(Double finishedWeigth) {
        this.finishedWeight = finishedWeigth;
    }

    @Basic
    @Column(name = "WORKPIECE_WEIGHT")
    public Double getWorkpieceWeight() {
        return workpieceWeight;
    }

    public void setWorkpieceWeight(Double workpieceWeight) {
        this.workpieceWeight = workpieceWeight;
    }

    @Basic
    @Column(name = "IMAGE_PATH")
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Basic
    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "TECH_PROCESS_ID", referencedColumnName = "ID")
    public TechProcessEntity getTechProcessByTechProcessId() {
        return techProcessByTechProcessId;
    }

    public void setTechProcessByTechProcessId(TechProcessEntity techProcessByTechProcessId) {
        this.techProcessByTechProcessId = techProcessByTechProcessId;
    }

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "TITLE_ID", referencedColumnName = "ID")
    public DetailTitleEntity getDetailTitleByDetailTitleId() {
        return detailTitleByDetailTitleId;
    }

    public void setDetailTitleByDetailTitleId(DetailTitleEntity detailTitleByDetailTitleId) {
        this.detailTitleByDetailTitleId = detailTitleByDetailTitleId;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (unit ? 1 : 0);
        result = 31 * result + (finishedWeight != null ? finishedWeight.hashCode() : 0);
        result = 31 * result + (workpieceWeight != null ? workpieceWeight.hashCode() : 0);
        result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
        result = 31 * result + Boolean.hashCode(active);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        /*if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof DetailEntity)) return false;

        DetailEntity that = (DetailEntity) o;

        final boolean codeE = getCode() != null && that.getCode() != null && getCode().equals(that.getCode());
        final boolean titleE = getDetailTitleByDetailTitleId() != null && that.getDetailTitleByDetailTitleId() != null
                && getDetailTitleByDetailTitleId().equals(that.getDetailTitleByDetailTitleId());
        return codeE && titleE;

        if (this == o) return true;*/

        if (!(o instanceof DetailEntity)) return false;

        DetailEntity that = (DetailEntity) o;

        return new EqualsBuilder()
                .append(getId(), that.getId())
                .append(getDetailTitleByDetailTitleId().getTitle(), that.getDetailTitleByDetailTitleId().getTitle())
                .append(isActive(), that.isActive())
                .append(isUnit(), that.isUnit())
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("code", code)
                .append("unit", unit)
                .append("finishedWeigth", finishedWeight)
                .append("workpieceWeight", workpieceWeight)
                .append("imagePath", imagePath)
                .append("active", active)
                .append("techProcessByTechProcessId", techProcessByTechProcessId)
                .append("detailTitleByDetailTitleId", detailTitleByDetailTitleId)
                .toString();
    }

    @Override
    public int compareTo(@Nonnull DetailEntity that) {
        return ComparisonChain.start()
                .compare(this.getCode(), that.getCode())
                .result();
    }

    @Override
    public String toSimpleString() {
        return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                .append("id", id)
                .append("code", code)
                .append("title", detailTitleByDetailTitleId.getTitle())
                .toString();
    }
}
