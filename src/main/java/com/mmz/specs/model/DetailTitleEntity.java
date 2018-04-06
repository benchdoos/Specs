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

    @Basic
    @Column(name = "IS_ACTIVE")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DetailTitleEntity) {
            DetailTitleEntity that = (DetailTitleEntity) o;
            return this.id == that.getId() && this.title.equalsIgnoreCase(that.getTitle()) && this.active == that.isActive();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("title", title)
                .append("active", active)
                .toString();
    }

    @Override
    public int compareTo(DetailTitleEntity that) {
        int result = 0;
        result -= that.getTitle().compareTo(this.getTitle());
        result += that.isActive() == this.isActive() ? 1 : -1;
        return result;
    }
}
