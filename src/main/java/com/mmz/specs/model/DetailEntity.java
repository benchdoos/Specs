package com.mmz.specs.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "DETAIL")
public class DetailEntity {
    private int id;
    private String index;
    private boolean unit;
    private Double finishedWeigth;
    private Short workpieceWeight;
    private String imagePath;
    private Boolean isActive;
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
    @Column(name = "INDEX")
    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
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
    @Column(name = "FINISHED_WEIGTH")
    public Double getFinishedWeigth() {
        return finishedWeigth;
    }

    public void setFinishedWeigth(Double finishedWeigth) {
        this.finishedWeigth = finishedWeigth;
    }

    @Basic
    @Column(name = "WORKPIECE_WEIGHT")
    public Short getWorkpieceWeight() {
        return workpieceWeight;
    }

    public void setWorkpieceWeight(Short workpieceWeight) {
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
    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DetailEntity that = (DetailEntity) o;

        if (id != that.id) return false;
        if (unit != that.unit) return false;
        if (index != null ? !index.equals(that.index) : that.index != null) return false;
        if (finishedWeigth != null ? !finishedWeigth.equals(that.finishedWeigth) : that.finishedWeigth != null)
            return false;
        if (workpieceWeight != null ? !workpieceWeight.equals(that.workpieceWeight) : that.workpieceWeight != null)
            return false;
        if (imagePath != null ? !imagePath.equals(that.imagePath) : that.imagePath != null) return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (index != null ? index.hashCode() : 0);
        result = 31 * result + (unit ? 1 : 0);
        result = 31 * result + (finishedWeigth != null ? finishedWeigth.hashCode() : 0);
        result = 31 * result + (workpieceWeight != null ? workpieceWeight.hashCode() : 0);
        result = 31 * result + (imagePath != null ? imagePath.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        return result;
    }

    @ManyToOne
    @JoinColumn(name = "TECH_PROCESS_ID", referencedColumnName = "ID")
    public TechProcessEntity getTechProcessByTechProcessId() {
        return techProcessByTechProcessId;
    }

    public void setTechProcessByTechProcessId(TechProcessEntity techProcessByTechProcessId) {
        this.techProcessByTechProcessId = techProcessByTechProcessId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("index", index)
                .append("unit", unit)
                .append("finishedWeigth", finishedWeigth)
                .append("workpieceWeight", workpieceWeight)
                .append("imagePath", imagePath)
                .append("isActive", isActive)
                .append("techProcessByTechProcessId", techProcessByTechProcessId)
                .toString();
    }
}
