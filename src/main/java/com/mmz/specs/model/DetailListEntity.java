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
@Table(name = "DETAIL_LIST")
public class DetailListEntity implements Comparable<DetailListEntity> {
    private int id;
    private int quantity;
    private boolean isInterchangeableNode;
    private boolean isActive;
    private DetailEntity detailByParentDetailId;
    private DetailEntity detailByChildDetailId;
    private NoticeEntity noticeByNoticeId;

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
    public int hashCode() {
        int result = id;
        result = 31 * result + quantity;
        return result;
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
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("quantity", quantity)
                .append("isInterchangeableNode", isInterchangeableNode)
                .append("isActive", isActive)
                .append("detailByParentDetailId", detailByParentDetailId)
                .append("detailByChildDetailId", detailByChildDetailId)
                .toString();
    }

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "PARENT_DETAIL_ID", referencedColumnName = "ID", nullable = false)
    public DetailEntity getDetailByParentDetailId() {
        return detailByParentDetailId;
    }

    public void setDetailByParentDetailId(DetailEntity detailByParentDetailId) {
        this.detailByParentDetailId = detailByParentDetailId;
    }

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "CHILD_DETAIL_ID", referencedColumnName = "ID", nullable = false)
    public DetailEntity getDetailByChildDetailId() {
        return detailByChildDetailId;
    }

    public void setDetailByChildDetailId(DetailEntity detailByChildDetailId) {
        this.detailByChildDetailId = detailByChildDetailId;
    }

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "NOTICE_ID", referencedColumnName = "ID", nullable = false)
    public NoticeEntity getNoticeByNoticeId() {
        return noticeByNoticeId;
    }

    public void setNoticeByNoticeId(NoticeEntity noticeByNoticeId) {
        this.noticeByNoticeId = noticeByNoticeId;
    }

    @Override
    public int compareTo(DetailListEntity that) {
        int result = 0;

        result -= this.getDetailByParentDetailId().compareTo(that.getDetailByParentDetailId());
        result -= this.getDetailByChildDetailId().compareTo(that.getDetailByChildDetailId());
        result += this.isActive() == that.isActive() ? 1 : 0;
        result += this.isInterchangeableNode() == that.isInterchangeableNode() ? 10 : 0;

        return result;
    }
}
