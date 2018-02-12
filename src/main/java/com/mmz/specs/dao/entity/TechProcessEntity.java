package com.mmz.specs.dao.entity;

import javax.persistence.*;

@Entity
@Table(name = "TECH_PROCESS")
public class TechProcessEntity {
    private int id;
    private String process;

    @Id
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "PROCESS")
    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TechProcessEntity that = (TechProcessEntity) o;

        if (id != that.id) return false;
        if (process != null ? !process.equals(that.process) : that.process != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (process != null ? process.hashCode() : 0);
        return result;
    }
}
