package com.mmz.specs.domain;

import javax.persistence.*;

@Entity
@Table(name = "DETAIL_LIST")
public class DetailListEntity {
    private int id;
    private int quantity;
    private boolean isInterchangeableNode;
    private boolean isActive;
    private DetailEntity detailByParentDetailId;
    private DetailEntity detailByChildDetailId;

    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "QUANTITY")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Basic
    @Column(name = "IS_INTERCHANGEABLE_NODE")
    public boolean isInterchangeableNode() {
        return isInterchangeableNode;
    }

    public void setInterchangeableNode(boolean interchangeableNode) {
        isInterchangeableNode = interchangeableNode;
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

        DetailListEntity that = (DetailListEntity) o;

        if (id != that.id) return false;
        if (quantity != that.quantity) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + quantity;
        return result;
    }

    @ManyToOne
    @JoinColumn(name = "PARENT_DETAIL_ID", referencedColumnName = "ID", nullable = false)
    public DetailEntity getDetailByParentDetailId() {
        return detailByParentDetailId;
    }

    public void setDetailByParentDetailId(DetailEntity detailByParentDetailId) {
        this.detailByParentDetailId = detailByParentDetailId;
    }

    @ManyToOne
    @JoinColumn(name = "CHILD_DETAIL_ID", referencedColumnName = "ID", nullable = false)
    public DetailEntity getDetailByChildDetailId() {
        return detailByChildDetailId;
    }

    public void setDetailByChildDetailId(DetailEntity detailByChildDetailId) {
        this.detailByChildDetailId = detailByChildDetailId;
    }
}
