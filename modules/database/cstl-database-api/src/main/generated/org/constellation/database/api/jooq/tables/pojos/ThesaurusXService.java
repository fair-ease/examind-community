/*
 * This file is generated by jOOQ.
 */
package org.constellation.database.api.jooq.tables.pojos;


import java.io.Serializable;

import javax.validation.constraints.NotNull;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ThesaurusXService implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer serviceId;
    private Integer thesaurusId;

    public ThesaurusXService() {}

    public ThesaurusXService(ThesaurusXService value) {
        this.serviceId = value.serviceId;
        this.thesaurusId = value.thesaurusId;
    }

    public ThesaurusXService(
        Integer serviceId,
        Integer thesaurusId
    ) {
        this.serviceId = serviceId;
        this.thesaurusId = thesaurusId;
    }

    /**
     * Getter for <code>admin.thesaurus_x_service.service_id</code>.
     */
    @NotNull
    public Integer getServiceId() {
        return this.serviceId;
    }

    /**
     * Setter for <code>admin.thesaurus_x_service.service_id</code>.
     */
    public ThesaurusXService setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    /**
     * Getter for <code>admin.thesaurus_x_service.thesaurus_id</code>.
     */
    @NotNull
    public Integer getThesaurusId() {
        return this.thesaurusId;
    }

    /**
     * Setter for <code>admin.thesaurus_x_service.thesaurus_id</code>.
     */
    public ThesaurusXService setThesaurusId(Integer thesaurusId) {
        this.thesaurusId = thesaurusId;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ThesaurusXService (");

        sb.append(serviceId);
        sb.append(", ").append(thesaurusId);

        sb.append(")");
        return sb.toString();
    }
}
