package com.mmz.specs.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "NOTICE")
public class NoticeEntity {
    private int id;
    private String number;
    private Date date;
    private String description;
    private UsersEntity usersByProvidedByUserId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoticeEntity that = (NoticeEntity) o;

        if (id != that.id) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @ManyToOne
    @JoinColumn(name = "PROVIDED_BY_USER_ID", referencedColumnName = "ID")
    public UsersEntity getUsersByProvidedByUserId() {
        return usersByProvidedByUserId;
    }

    public void setUsersByProvidedByUserId(UsersEntity usersByProvidedByUserId) {
        this.usersByProvidedByUserId = usersByProvidedByUserId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("number", number)
                .append("date", date)
                .append("description", description)
                .append("usersByProvidedByUserId", usersByProvidedByUserId)
                .toString();
    }
}
