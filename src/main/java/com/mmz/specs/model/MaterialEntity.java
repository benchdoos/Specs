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
@Table(name = "MATERIAL")
public class MaterialEntity implements Comparable<MaterialEntity> {
    private int id;
    private String shortMark;
    private String shortProfile;
    private String longMark;
    private String longProfile;
    private boolean active;

    @Override
    public int compareTo(@Nonnull MaterialEntity that) {
        return ComparisonChain.start()
                .compare(this.getShortMark(), that.getShortMark())
                .compare(this.getShortProfile(), that.getShortProfile())
                .compare(this.getLongMark(), that.getLongMark())
                .compare(this.getLongProfile(), that.getLongProfile())
                .compareTrueFirst(this.isActive(), that.isActive())
                .result();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof MaterialEntity)) return false;

        MaterialEntity that = (MaterialEntity) o;

        return new EqualsBuilder()
                .append(getId(), that.getId())
                .append(isActive(), that.isActive())
                .append(getShortMark(), that.getShortMark())
                .append(getShortProfile(), that.getShortProfile())
                .append(getLongMark(), that.getLongMark())
                .append(getLongProfile(), that.getLongProfile())
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
    @Column(name = "LONG_MARK")
    public String getLongMark() {
        return longMark;
    }

    public void setLongMark(String longMark) {
        this.longMark = longMark;
    }

    @Basic
    @Column(name = "LONG_PROFILE")
    public String getLongProfile() {
        return longProfile;
    }

    public void setLongProfile(String longProfile) {
        this.longProfile = longProfile;
    }

    @Basic
    @Column(name = "SHORT_MARK")
    public String getShortMark() {
        return shortMark;
    }

    public void setShortMark(String shortMark) {
        this.shortMark = shortMark;
    }

    @Basic
    @Column(name = "SHORT_PROFILE")
    public String getShortProfile() {
        return shortProfile;
    }

    public void setShortProfile(String shortProfile) {
        this.shortProfile = shortProfile;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (shortMark != null ? shortMark.hashCode() : 0);
        result = 31 * result + (shortProfile != null ? shortProfile.hashCode() : 0);
        result = 31 * result + (longMark != null ? longMark.hashCode() : 0);
        result = 31 * result + (longProfile != null ? longProfile.hashCode() : 0);
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
                .append("shortMark", shortMark)
                .append("shortProfile", shortProfile)
                .append("longMark", longMark)
                .append("longProfile", longProfile)
                .append("active", active)
                .toString();
    }
}
