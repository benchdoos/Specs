package com.mmz.specs.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;

@Entity
@Table(name = "MATERIAL")
public class MaterialEntity {
    private int id;
    private String shortMark;
    private String shortProfile;
    private String longMark;
    private String longProfile;
    private Boolean isActive;

    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

        MaterialEntity that = (MaterialEntity) o;

        if (id != that.id) return false;
        if (shortMark != null ? !shortMark.equals(that.shortMark) : that.shortMark != null) return false;
        if (shortProfile != null ? !shortProfile.equals(that.shortProfile) : that.shortProfile != null) return false;
        if (longMark != null ? !longMark.equals(that.longMark) : that.longMark != null) return false;
        if (longProfile != null ? !longProfile.equals(that.longProfile) : that.longProfile != null) return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (shortMark != null ? shortMark.hashCode() : 0);
        result = 31 * result + (shortProfile != null ? shortProfile.hashCode() : 0);
        result = 31 * result + (longMark != null ? longMark.hashCode() : 0);
        result = 31 * result + (longProfile != null ? longProfile.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("shortMark", shortMark)
                .append("shortProfile", shortProfile)
                .append("longMark", longMark)
                .append("longProfile", longProfile)
                .append("isActive", isActive)
                .toString();
    }
}
