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
import java.sql.Date;

@Entity
@Table(name = "NOTICE")
public class NoticeEntity implements Comparable<NoticeEntity> {
    private int id;
    private String number;
    private Date date;
    private String description;
    private UsersEntity usersByProvidedByUserId;
    private Date creationDate;
    private UsersEntity authorByUserId;

    public NoticeEntity(int id, String number, Date date, String description, UsersEntity usersByProvidedByUserId) {
        this.id = id;
        this.number = number;
        this.date = date;
        this.description = description;
        this.usersByProvidedByUserId = usersByProvidedByUserId;
    }

    public NoticeEntity() {

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
    @Column(name = "NUMBER")
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Basic
    @Column(name = "DATE")
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Basic
    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne
    @JoinColumn(name = "PROVIDED_BY_USER_ID", referencedColumnName = "ID")
    public UsersEntity getUsersByProvidedByUserId() {
        return usersByProvidedByUserId;
    }

    public void setUsersByProvidedByUserId(UsersEntity usersByProvidedByUserId) {
        this.usersByProvidedByUserId = usersByProvidedByUserId;
    }

    @ManyToOne
    @JoinColumn(name = "AUTHOR_USER_ID", referencedColumnName = "ID")
    public UsersEntity getAuthorByUserId() {
        return authorByUserId;
    }

    public void setAuthorByUserId(UsersEntity usersByCreatedByUserId) {
        this.authorByUserId = usersByCreatedByUserId;
    }

    @Basic
    @Column(name = "CREATION_DATE")
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        /*if (obj instanceof NoticeEntity) {
            NoticeEntity that = (NoticeEntity) obj;
            if (this.id == that.getId()) {
                if (this.number.equals(that.getNumber())) {
                    if (this.date.equals(that.getDate())) {
                        if (this.description.equals(that.getDescription())) {
                            return this.usersByProvidedByUserId.equals(that.getUsersByProvidedByUserId());
                        }
                    }
                }
            }
            return false;
        } else {
            return false;
        }*/


        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NoticeEntity)) {
            return false;
        }

        NoticeEntity that = (NoticeEntity) obj;
        return new EqualsBuilder()
                .append(getId(), that.getId())
                .append(getNumber(), that.getNumber())
                .append(getDate(), that.getDate())
                .append(getDescription(), that.getDescription())
                .isEquals();

    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("number", number)
                .append("date", date)
                .append("description", description.replaceAll("\n", " "))
                .append("usersByProvidedByUserId", usersByProvidedByUserId)
                .toString();
    }

    @Override
    public int compareTo(@Nonnull NoticeEntity that) {
        return ComparisonChain.start()
                .compare(that.getDate(), this.getDate())
                .compare(that.getNumber(), this.getNumber())
                .result();
    }
}
