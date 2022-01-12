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

import org.constellation.database.api.jooq.tables.Metadata;
import org.constellation.database.api.jooq.tables.records.MetadataRecord;
import org.jooq.Configuration;
import org.jooq.impl.DAOImpl;


/**
 * Generated DAO object for table admin.metadata
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MetadataDao extends DAOImpl<MetadataRecord, org.constellation.database.api.jooq.tables.pojos.Metadata, Integer> {

    /**
     * Create a new MetadataDao without any configuration
     */
    public MetadataDao() {
        super(Metadata.METADATA, org.constellation.database.api.jooq.tables.pojos.Metadata.class);
    }

    /**
     * Create a new MetadataDao with an attached configuration
     */
    public MetadataDao(Configuration configuration) {
        super(Metadata.METADATA, org.constellation.database.api.jooq.tables.pojos.Metadata.class, configuration);
    }

    @Override
    public Integer getId(org.constellation.database.api.jooq.tables.pojos.Metadata object) {
        return object.getId();
    }

    /**
     * Fetch records that have <code>id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Metadata.METADATA.ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchById(Integer... values) {
        return fetch(Metadata.METADATA.ID, values);
    }

    /**
     * Fetch a unique record that has <code>id = value</code>
     */
    public org.constellation.database.api.jooq.tables.pojos.Metadata fetchOneById(Integer value) {
        return fetchOne(Metadata.METADATA.ID, value);
    }

    /**
     * Fetch records that have <code>metadata_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfMetadataId(String lowerInclusive, String upperInclusive) {
        return fetchRange(Metadata.METADATA.METADATA_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>metadata_id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByMetadataId(String... values) {
        return fetch(Metadata.METADATA.METADATA_ID, values);
    }

    /**
     * Fetch records that have <code>data_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfDataId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Metadata.METADATA.DATA_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>data_id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByDataId(Integer... values) {
        return fetch(Metadata.METADATA.DATA_ID, values);
    }

    /**
     * Fetch records that have <code>dataset_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfDatasetId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Metadata.METADATA.DATASET_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>dataset_id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByDatasetId(Integer... values) {
        return fetch(Metadata.METADATA.DATASET_ID, values);
    }

    /**
     * Fetch records that have <code>service_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfServiceId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Metadata.METADATA.SERVICE_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>service_id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByServiceId(Integer... values) {
        return fetch(Metadata.METADATA.SERVICE_ID, values);
    }

    /**
     * Fetch records that have <code>md_completion BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfMdCompletion(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Metadata.METADATA.MD_COMPLETION, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>md_completion IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByMdCompletion(Integer... values) {
        return fetch(Metadata.METADATA.MD_COMPLETION, values);
    }

    /**
     * Fetch records that have <code>owner BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfOwner(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Metadata.METADATA.OWNER, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>owner IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByOwner(Integer... values) {
        return fetch(Metadata.METADATA.OWNER, values);
    }

    /**
     * Fetch records that have <code>datestamp BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfDatestamp(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(Metadata.METADATA.DATESTAMP, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>datestamp IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByDatestamp(Long... values) {
        return fetch(Metadata.METADATA.DATESTAMP, values);
    }

    /**
     * Fetch records that have <code>date_creation BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfDateCreation(Long lowerInclusive, Long upperInclusive) {
        return fetchRange(Metadata.METADATA.DATE_CREATION, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>date_creation IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByDateCreation(Long... values) {
        return fetch(Metadata.METADATA.DATE_CREATION, values);
    }

    /**
     * Fetch records that have <code>title BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfTitle(String lowerInclusive, String upperInclusive) {
        return fetchRange(Metadata.METADATA.TITLE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>title IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByTitle(String... values) {
        return fetch(Metadata.METADATA.TITLE, values);
    }

    /**
     * Fetch records that have <code>profile BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfProfile(String lowerInclusive, String upperInclusive) {
        return fetchRange(Metadata.METADATA.PROFILE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>profile IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByProfile(String... values) {
        return fetch(Metadata.METADATA.PROFILE, values);
    }

    /**
     * Fetch records that have <code>parent_identifier BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfParentIdentifier(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Metadata.METADATA.PARENT_IDENTIFIER, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>parent_identifier IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByParentIdentifier(Integer... values) {
        return fetch(Metadata.METADATA.PARENT_IDENTIFIER, values);
    }

    /**
     * Fetch records that have <code>is_validated BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfIsValidated(Boolean lowerInclusive, Boolean upperInclusive) {
        return fetchRange(Metadata.METADATA.IS_VALIDATED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>is_validated IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByIsValidated(Boolean... values) {
        return fetch(Metadata.METADATA.IS_VALIDATED, values);
    }

    /**
     * Fetch records that have <code>is_published BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfIsPublished(Boolean lowerInclusive, Boolean upperInclusive) {
        return fetchRange(Metadata.METADATA.IS_PUBLISHED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>is_published IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByIsPublished(Boolean... values) {
        return fetch(Metadata.METADATA.IS_PUBLISHED, values);
    }

    /**
     * Fetch records that have <code>level BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfLevel(String lowerInclusive, String upperInclusive) {
        return fetchRange(Metadata.METADATA.LEVEL, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>level IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByLevel(String... values) {
        return fetch(Metadata.METADATA.LEVEL, values);
    }

    /**
     * Fetch records that have <code>resume BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfResume(String lowerInclusive, String upperInclusive) {
        return fetchRange(Metadata.METADATA.RESUME, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>resume IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByResume(String... values) {
        return fetch(Metadata.METADATA.RESUME, values);
    }

    /**
     * Fetch records that have <code>validation_required BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfValidationRequired(String lowerInclusive, String upperInclusive) {
        return fetchRange(Metadata.METADATA.VALIDATION_REQUIRED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>validation_required IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByValidationRequired(String... values) {
        return fetch(Metadata.METADATA.VALIDATION_REQUIRED, values);
    }

    /**
     * Fetch records that have <code>validated_state BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfValidatedState(String lowerInclusive, String upperInclusive) {
        return fetchRange(Metadata.METADATA.VALIDATED_STATE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>validated_state IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByValidatedState(String... values) {
        return fetch(Metadata.METADATA.VALIDATED_STATE, values);
    }

    /**
     * Fetch records that have <code>comment BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfComment(String lowerInclusive, String upperInclusive) {
        return fetchRange(Metadata.METADATA.COMMENT, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>comment IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByComment(String... values) {
        return fetch(Metadata.METADATA.COMMENT, values);
    }

    /**
     * Fetch records that have <code>provider_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfProviderId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Metadata.METADATA.PROVIDER_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>provider_id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByProviderId(Integer... values) {
        return fetch(Metadata.METADATA.PROVIDER_ID, values);
    }

    /**
     * Fetch records that have <code>map_context_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfMapContextId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(Metadata.METADATA.MAP_CONTEXT_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>map_context_id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByMapContextId(Integer... values) {
        return fetch(Metadata.METADATA.MAP_CONTEXT_ID, values);
    }

    /**
     * Fetch records that have <code>type BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfType(String lowerInclusive, String upperInclusive) {
        return fetchRange(Metadata.METADATA.TYPE, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>type IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByType(String... values) {
        return fetch(Metadata.METADATA.TYPE, values);
    }

    /**
     * Fetch records that have <code>is_shared BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfIsShared(Boolean lowerInclusive, Boolean upperInclusive) {
        return fetchRange(Metadata.METADATA.IS_SHARED, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>is_shared IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByIsShared(Boolean... values) {
        return fetch(Metadata.METADATA.IS_SHARED, values);
    }

    /**
     * Fetch records that have <code>is_hidden BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchRangeOfIsHidden(Boolean lowerInclusive, Boolean upperInclusive) {
        return fetchRange(Metadata.METADATA.IS_HIDDEN, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>is_hidden IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.Metadata> fetchByIsHidden(Boolean... values) {
        return fetch(Metadata.METADATA.IS_HIDDEN, values);
    }
}
