/*
 *     Examind Community - An open source and standard compliant SDI
 *     https://community.examind.com/
 * 
 *  Copyright 2022 Geomatys.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
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
package com.examind.database.api.jooq.tables.records;


import com.examind.database.api.jooq.tables.MetadataXCsw;

import jakarta.validation.constraints.NotNull;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * Generated DAO object for table admin.metadata_x_csw
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class MetadataXCswRecord extends UpdatableRecordImpl<MetadataXCswRecord> implements Record2<Integer, Integer> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>admin.metadata_x_csw.metadata_id</code>.
     */
    public MetadataXCswRecord setMetadataId(Integer value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>admin.metadata_x_csw.metadata_id</code>.
     */
    @NotNull
    public Integer getMetadataId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>admin.metadata_x_csw.csw_id</code>.
     */
    public MetadataXCswRecord setCswId(Integer value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>admin.metadata_x_csw.csw_id</code>.
     */
    @NotNull
    public Integer getCswId() {
        return (Integer) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<Integer, Integer> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, Integer> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<Integer, Integer> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return MetadataXCsw.METADATA_X_CSW.METADATA_ID;
    }

    @Override
    public Field<Integer> field2() {
        return MetadataXCsw.METADATA_X_CSW.CSW_ID;
    }

    @Override
    public Integer component1() {
        return getMetadataId();
    }

    @Override
    public Integer component2() {
        return getCswId();
    }

    @Override
    public Integer value1() {
        return getMetadataId();
    }

    @Override
    public Integer value2() {
        return getCswId();
    }

    @Override
    public MetadataXCswRecord value1(Integer value) {
        setMetadataId(value);
        return this;
    }

    @Override
    public MetadataXCswRecord value2(Integer value) {
        setCswId(value);
        return this;
    }

    @Override
    public MetadataXCswRecord values(Integer value1, Integer value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MetadataXCswRecord
     */
    public MetadataXCswRecord() {
        super(MetadataXCsw.METADATA_X_CSW);
    }

    /**
     * Create a detached, initialised MetadataXCswRecord
     */
    public MetadataXCswRecord(Integer metadataId, Integer cswId) {
        super(MetadataXCsw.METADATA_X_CSW);

        setMetadataId(metadataId);
        setCswId(cswId);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised MetadataXCswRecord
     */
    public MetadataXCswRecord(com.examind.database.api.jooq.tables.pojos.MetadataXCsw value) {
        super(MetadataXCsw.METADATA_X_CSW);

        if (value != null) {
            setMetadataId(value.getMetadataId());
            setCswId(value.getCswId());
            resetChangedOnNotNull();
        }
    }
}
