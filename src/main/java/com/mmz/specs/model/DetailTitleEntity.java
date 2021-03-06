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
@Table(name = "DETAIL_TITLES")
public class DetailTitleEntity implements Comparable<DetailTitleEntity> {
    private int id;
    private String title;
    private boolean active;

    public DetailTitleEntity(int id, String title, boolean active) {
        this.id = id;
        this.title = title;
        this.active = active;
    }

    public DetailTitleEntity() {
    }

    @Override
    public int compareTo(@Nonnull DetailTitleEntity that) {
        return ComparisonChain.start()
                .compare(this.getTitle(), that.getTitle())
                .compareTrueFirst(this.isActive(), that.isActive())
                .result();
    }

    @Override
    public boolean equals(Object o) {
       /* if (o instanceof DetailTitleEntity) {
            DetailTitleEntity that = (DetailTitleEntity) o;
            return this.id == that.getId() && this.title.equalsIgnoreCase(that.getTitle()) && this.active == that.isActive();
        } else {
            return false;
        }*/

        if (this == o) return true;

        if (!(o instanceof DetailTitleEntity)) return false;

        DetailTitleEntity that = (DetailTitleEntity) o;

        return new EqualsBuilder()
                .append(getId(), that.getId())
                .append(getTitle(), that.getTitle())
                .append(isActive(), that.isActive())
                .isEquals();
    }

    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "TITLE")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @Basic
    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("title", title)
                .append("active", active)
                .toString();
    }
}
