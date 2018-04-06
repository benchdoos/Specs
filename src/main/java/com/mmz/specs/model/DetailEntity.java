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

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "DETAIL")
public class DetailEntity implements Comparable<DetailEntity> {
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

    @ManyToOne
    @JoinColumn(name = "TECH_PROCESS_ID", referencedColumnName = "ID")
    public TechProcessEntity getTechProcessByTechProcessId() {
        return techProcessByTechProcessId;
    }

    public void setTechProcessByTechProcessId(TechProcessEntity techProcessByTechProcessId) {
        this.techProcessByTechProcessId = techProcessByTechProcessId;
    }

    @ManyToOne
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DetailEntity that = (DetailEntity) o;

        if (id != that.id) return false;
        if (unit != that.unit) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (finishedWeight != null ? !finishedWeight.equals(that.finishedWeight) : that.finishedWeight != null)
            return false;
        if (workpieceWeight != null ? !workpieceWeight.equals(that.workpieceWeight) : that.workpieceWeight != null)
            return false;
        if (imagePath != null ? !imagePath.equals(that.imagePath) : that.imagePath != null) return false;
        if (active != ((DetailEntity) o).active) return false;

        return true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("index", code)
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
    public int compareTo(DetailEntity that) {
        int result = 0;
        result -= that.getCode().compareTo(this.getCode());
        result += (that.isActive() == this.isActive()) ? 1 : 0;
        return result;

    }
}
