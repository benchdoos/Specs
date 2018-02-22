package com.mmz.specs.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "MATERIAL_LIST", schema = "", catalog = "")
public class MaterialListEntity {
    private long id;
    private boolean isActive;
    private DetailEntity detailByDetailId;
    private MaterialEntity materialByMaterialId;

    @Id
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaterialListEntity that = (MaterialListEntity) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @ManyToOne
    @JoinColumn(name = "DETAIL_ID", referencedColumnName = "ID", nullable = false)
    public DetailEntity getDetailByDetailId() {
        return detailByDetailId;
    }

    public void setDetailByDetailId(DetailEntity detailByDetailId) {
        this.detailByDetailId = detailByDetailId;
    }

    @ManyToOne
    @JoinColumn(name = "MATERIAL_ID", referencedColumnName = "ID", nullable = false)
    public MaterialEntity getMaterialByMaterialId() {
        return materialByMaterialId;
    }

    public void setMaterialByMaterialId(MaterialEntity materialByMaterialId) {
        this.materialByMaterialId = materialByMaterialId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("isActive", isActive)
                .append("detailByDetailId", detailByDetailId)
                .append("materialByMaterialId", materialByMaterialId)
                .toString();
    }
}
