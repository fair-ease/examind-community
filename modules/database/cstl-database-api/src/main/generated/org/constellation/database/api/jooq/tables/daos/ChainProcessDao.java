/*
 * This file is generated by jOOQ.
 */
package org.constellation.database.api.jooq.tables.daos;


import java.util.List;

import org.constellation.database.api.jooq.tables.ChainProcess;
import org.constellation.database.api.jooq.tables.records.ChainProcessRecord;
import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ChainProcessDao extends DAOImpl<ChainProcessRecord, org.constellation.database.api.jooq.tables.pojos.ChainProcess, Integer> {

    /**
     * Create a new ChainProcessDao without any configuration
     */
    public ChainProcessDao() {
        super(ChainProcess.CHAIN_PROCESS, org.constellation.database.api.jooq.tables.pojos.ChainProcess.class);
    }

    /**
     * Create a new ChainProcessDao with an attached configuration
     */
    public ChainProcessDao(Configuration configuration) {
        super(ChainProcess.CHAIN_PROCESS, org.constellation.database.api.jooq.tables.pojos.ChainProcess.class, configuration);
    }

    @Override
    public Integer getId(org.constellation.database.api.jooq.tables.pojos.ChainProcess object) {
        return object.getId();
    }

    /**
     * Fetch records that have <code>id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchRangeOfId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(ChainProcess.CHAIN_PROCESS.ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchById(Integer... values) {
        return fetch(ChainProcess.CHAIN_PROCESS.ID, values);
    }

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    public org.constellation.database.api.jooq.tables.pojos.ChainProcess fetchOneById(Integer value) {
        return fetchOne(ChainProcess.CHAIN_PROCESS.ID, value);
    }

    /**
     * Fetch records that have <code>auth BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchRangeOfAuth(String lowerInclusive, String upperInclusive) {
        return fetchRange(ChainProcess.CHAIN_PROCESS.AUTH, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>auth IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchByAuth(String... values) {
        return fetch(ChainProcess.CHAIN_PROCESS.AUTH, values);
    }

    /**
     * Fetch records that have <code>code BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchRangeOfCode(String lowerInclusive, String upperInclusive) {
        return fetchRange(ChainProcess.CHAIN_PROCESS.CODE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>code IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchByCode(String... values) {
        return fetch(ChainProcess.CHAIN_PROCESS.CODE, values);
    }

    /**
     * Fetch records that have <code>config BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchRangeOfConfig(String lowerInclusive, String upperInclusive) {
        return fetchRange(ChainProcess.CHAIN_PROCESS.CONFIG, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>config IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.ChainProcess> fetchByConfig(String... values) {
        return fetch(ChainProcess.CHAIN_PROCESS.CONFIG, values);
    }
}
