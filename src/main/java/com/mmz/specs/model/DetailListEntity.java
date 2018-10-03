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
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;
import javax.persistence.*;

@Entity
@Table(name = "DETAIL_LIST")
public class DetailListEntity implements Comparable<DetailListEntity> {
    private long id;
    private int quantity;
    private boolean isInterchangeableNode;
    private boolean isActive;
    private DetailEntity detailByParentDetailId;
    private DetailEntity detailByChildDetailId;
    private NoticeEntity noticeByNoticeId;

    @Override
    public int compareTo(@Nonnull DetailListEntity that) {
        return ComparisonChain.start()
                .compare(this.getDetailByParentDetailId(), that.getDetailByParentDetailId())
                .compare(this.getDetailByChildDetailId(), that.getDetailByChildDetailId())
                .compareTrueFirst(this.isActive(), that.isActive())
                .compareTrueFirst(this.isInterchangeableNode(), that.isInterchangeableNode())
                .result();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DetailListEntity that = (DetailListEntity) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(quantity, that.quantity)
                .append(isInterchangeableNode, that.isInterchangeableNode)
                .append(isActive, that.isActive)
                .isEquals();
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
    @JoinColumn(name = "PARENT_DETAIL_ID", referencedColumnName = "ID", nullable = false)
    public DetailEntity getDetailByParentDetailId() {
        return detailByParentDetailId;
    }

    public void setDetailByParentDetailId(DetailEntity detailByParentDetailId) {
        this.detailByParentDetailId = detailByParentDetailId;
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
    @JoinColumn(name = "NOTICE_ID", referencedColumnName = "ID", nullable = false)
    public NoticeEntity getNoticeByNoticeId() {
        return noticeByNoticeId;
    }

    public void setNoticeByNoticeId(NoticeEntity noticeByNoticeId) {
        this.noticeByNoticeId = noticeByNoticeId;
    }



    /*@Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DetailListEntity that = (DetailListEntity) o;

        if (id != that.id) return false;
        if (quantity != that.quantity) return false;

        return true;
    }*/

    @Basic
    @Column(name = "QUANTITY")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(quantity)
                .append(isInterchangeableNode)
                .append(isActive)
                .toHashCode();
    }

    @Basic
    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Basic
    @Column(name = "IS_INTERCHANGEABLE_NODE")
    public boolean isInterchangeableNode() {
        return isInterchangeableNode;
    }

    public void setInterchangeableNode(boolean interchangeableNode) {
        isInterchangeableNode = interchangeableNode;
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
                .append("noticeByNoticeId", noticeByNoticeId)
                .toString();
    }
}
