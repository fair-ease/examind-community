/*
 *     Examind Community - An open source and standard compliant SDI
 *     https://community.examind.com/
 * 
 *  Copyright 2022 Geomatys.
 * 
 *  Licensed under the Apache License, Version 2.0 (    the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package org.constellation.database.api.jooq.tables.daos;


import java.util.List;

import org.constellation.database.api.jooq.tables.Data;
import org.constellation.database.api.jooq.tables.records.DataRecord;
import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;


/**
 * Generated DAO object for table admin.data
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DataDao extends DAOImpl<DataRecord, org.constellation.database.api.jooq.tables.pojos.Data, Integer> {

    /**
     * Create a new DataDao without any configuration
     */
    public DataDao() {
        super(Data.DATA, org.constellation.database.api.jooq.tables.pojos.Data.class);
    }

    /**
     * Create a new DataDao with an attached configuration
     */
    public DataDao(Configuration configuration) {
        super(Data.DATA, org.constellation.database.api.jooq.tables.pojos.Data.class, configuration);
    }

    @Override
    public Integer getId(org.constellation.database.api.jooq.tables.pojos.Data object) {
        return object.getId();
    }

    /**
     * Fetch records that have <code>id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Data.DATA.ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchById(Integer... values) {
        return fetch(Data.DATA.ID, values);
    }

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    public org.constellation.database.api.jooq.tables.pojos.Data fetchOneById(Integer value) {
        return fetchOne(Data.DATA.ID, value);
    }

    /**
     * Fetch records that have <code>name BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfName(String lowerInclusive, String upperInclusive) {
        return fetchRange(Data.DATA.NAME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>name IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByName(String... values) {
        return fetch(Data.DATA.NAME, values);
    }

    /**
     * Fetch records that have <code>namespace BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfNamespace(String lowerInclusive, String upperInclusive) {
        return fetchRange(Data.DATA.NAMESPACE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>namespace IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByNamespace(String... values) {
        return fetch(Data.DATA.NAMESPACE, values);
    }

    /**
     * Fetch records that have <code>provider BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfProvider(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Data.DATA.PROVIDER, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>provider IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByProvider(Integer... values) {
        return fetch(Data.DATA.PROVIDER, values);
    }

    /**
     * Fetch records that have <code>type BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfType(String lowerInclusive, String upperInclusive) {
        return fetchRange(Data.DATA.TYPE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>type IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByType(String... values) {
        return fetch(Data.DATA.TYPE, values);
    }

    /**
     * Fetch records that have <code>subtype BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfSubtype(String lowerInclusive, String upperInclusive) {
        return fetchRange(Data.DATA.SUBTYPE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>subtype IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchBySubtype(String... values) {
        return fetch(Data.DATA.SUBTYPE, values);
    }

    /**
     * Fetch records that have <code>included BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfIncluded(Boolean lowerInclusive, Boolean upperInclusive) {
        return fetchRange(Data.DATA.INCLUDED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>included IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByIncluded(Boolean... values) {
        return fetch(Data.DATA.INCLUDED, values);
    }

    /**
     * Fetch records that have <code>sensorable BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfSensorable(Boolean lowerInclusive, Boolean upperInclusive) {
        return fetchRange(Data.DATA.SENSORABLE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>sensorable IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchBySensorable(Boolean... values) {
        return fetch(Data.DATA.SENSORABLE, values);
    }

    /**
     * Fetch records that have <code>date BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfDate(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(Data.DATA.DATE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>date IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByDate(Long... values) {
        return fetch(Data.DATA.DATE, values);
    }

    /**
     * Fetch records that have <code>owner BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfOwner(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Data.DATA.OWNER, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>owner IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByOwner(Integer... values) {
        return fetch(Data.DATA.OWNER, values);
    }

    /**
     * Fetch records that have <code>metadata BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfMetadata(String lowerInclusive, String upperInclusive) {
        return fetchRange(Data.DATA.METADATA, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>metadata IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByMetadata(String... values) {
        return fetch(Data.DATA.METADATA, values);
    }

    /**
     * Fetch records that have <code>dataset_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfDatasetId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Data.DATA.DATASET_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>dataset_id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByDatasetId(Integer... values) {
        return fetch(Data.DATA.DATASET_ID, values);
    }

    /**
     * Fetch records that have <code>feature_catalog BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfFeatureCatalog(String lowerInclusive, String upperInclusive) {
        return fetchRange(Data.DATA.FEATURE_CATALOG, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>feature_catalog IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByFeatureCatalog(String... values) {
        return fetch(Data.DATA.FEATURE_CATALOG, values);
    }

    /**
     * Fetch records that have <code>stats_result BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfStatsResult(String lowerInclusive, String upperInclusive) {
        return fetchRange(Data.DATA.STATS_RESULT, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>stats_result IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByStatsResult(String... values) {
        return fetch(Data.DATA.STATS_RESULT, values);
    }

    /**
     * Fetch records that have <code>rendered BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfRendered(Boolean lowerInclusive, Boolean upperInclusive) {
        return fetchRange(Data.DATA.RENDERED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>rendered IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByRendered(Boolean... values) {
        return fetch(Data.DATA.RENDERED, values);
    }

    /**
     * Fetch records that have <code>stats_state BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfStatsState(String lowerInclusive, String upperInclusive) {
        return fetchRange(Data.DATA.STATS_STATE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>stats_state IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByStatsState(String... values) {
        return fetch(Data.DATA.STATS_STATE, values);
    }

    /**
     * Fetch records that have <code>hidden BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchRangeOfHidden(Boolean lowerInclusive, Boolean upperInclusive) {
        return fetchRange(Data.DATA.HIDDEN, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>hidden IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Data> fetchByHidden(Boolean... values) {
        return fetch(Data.DATA.HIDDEN, values);
    }
}
