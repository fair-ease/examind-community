/*
 * This file is generated by jOOQ.
 */
package org.constellation.database.api.jooq.tables.daos;


import java.util.List;

import org.constellation.database.api.jooq.tables.Style;
import org.constellation.database.api.jooq.tables.records.StyleRecord;
import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class StyleDao extends DAOImpl<StyleRecord, org.constellation.database.api.jooq.tables.pojos.Style, Integer> {

    /**
     * Create a new StyleDao without any configuration
     */
    public StyleDao() {
        super(Style.STYLE, org.constellation.database.api.jooq.tables.pojos.Style.class);
    }

    /**
     * Create a new StyleDao with an attached configuration
     */
    public StyleDao(Configuration configuration) {
        super(Style.STYLE, org.constellation.database.api.jooq.tables.pojos.Style.class, configuration);
    }

    @Override
    public Integer getId(org.constellation.database.api.jooq.tables.pojos.Style object) {
        return object.getId();
    }

    /**
     * Fetch records that have <code>id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchRangeOfId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Style.STYLE.ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchById(Integer... values) {
        return fetch(Style.STYLE.ID, values);
    }

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    public org.constellation.database.api.jooq.tables.pojos.Style fetchOneById(Integer value) {
        return fetchOne(Style.STYLE.ID, value);
    }

    /**
     * Fetch records that have <code>name BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchRangeOfName(String lowerInclusive, String upperInclusive) {
        return fetchRange(Style.STYLE.NAME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>name IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchByName(String... values) {
        return fetch(Style.STYLE.NAME, values);
    }

    /**
     * Fetch records that have <code>provider BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchRangeOfProvider(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Style.STYLE.PROVIDER, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>provider IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchByProvider(Integer... values) {
        return fetch(Style.STYLE.PROVIDER, values);
    }

    /**
     * Fetch records that have <code>type BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchRangeOfType(String lowerInclusive, String upperInclusive) {
        return fetchRange(Style.STYLE.TYPE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>type IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchByType(String... values) {
        return fetch(Style.STYLE.TYPE, values);
    }

    /**
     * Fetch records that have <code>date BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchRangeOfDate(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(Style.STYLE.DATE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>date IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchByDate(Long... values) {
        return fetch(Style.STYLE.DATE, values);
    }

    /**
     * Fetch records that have <code>body BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchRangeOfBody(String lowerInclusive, String upperInclusive) {
        return fetchRange(Style.STYLE.BODY, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>body IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchByBody(String... values) {
        return fetch(Style.STYLE.BODY, values);
    }

    /**
     * Fetch records that have <code>owner BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchRangeOfOwner(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Style.STYLE.OWNER, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>owner IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchByOwner(Integer... values) {
        return fetch(Style.STYLE.OWNER, values);
    }

    /**
     * Fetch records that have <code>is_shared BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchRangeOfIsShared(Boolean lowerInclusive, Boolean upperInclusive) {
        return fetchRange(Style.STYLE.IS_SHARED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>is_shared IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Style> fetchByIsShared(Boolean... values) {
        return fetch(Style.STYLE.IS_SHARED, values);
    }
}
