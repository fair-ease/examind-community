/*
 * This file is generated by jOOQ.
 */
package org.constellation.database.api.jooq.tables.pojos;


import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Crs implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer dataid;
    private String  crscode;

    public Crs() {}

    public Crs(Crs value) {
        this.dataid = value.dataid;
        this.crscode = value.crscode;
    }

    public Crs(
        Integer dataid,
        String  crscode
    ) {
        this.dataid = dataid;
        this.crscode = crscode;
    }

    /**
     * Getter for <code>admin.crs.dataid</code>.
     */
    @NotNull
    public Integer getDataid() {
        return this.dataid;
    }

    /**
     * Setter for <code>admin.crs.dataid</code>.
     */
    public Crs setDataid(Integer dataid) {
        this.dataid = dataid;
        return this;
    }

    /**
     * Getter for <code>admin.crs.crscode</code>.
     */
    @NotNull
    @Size(max = 64)
    public String getCrscode() {
        return this.crscode;
    }

    /**
     * Setter for <code>admin.crs.crscode</code>.
     */
    public Crs setCrscode(String crscode) {
        this.crscode = crscode;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Crs (");

        sb.append(dataid);
        sb.append(", ").append(crscode);

        sb.append(")");
        return sb.toString();
    }
}
