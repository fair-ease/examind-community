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


import com.examind.database.api.jooq.tables.DataTimes;

import jakarta.validation.constraints.NotNull;

import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * Generated DAO object for table admin.data_times
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DataTimesRecord extends UpdatableRecordImpl<DataTimesRecord> implements Record2<Integer, Long> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>admin.data_times.data_id</code>.
     */
    public DataTimesRecord setDataId(Integer value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>admin.data_times.data_id</code>.
     */
    @NotNull
    public Integer getDataId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>admin.data_times.date</code>.
     */
    public DataTimesRecord setDate(Long value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>admin.data_times.date</code>.
     */
    @NotNull
    public Long getDate() {
        return (Long) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record2<Integer, Long> key() {
        return (Record2) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, Long> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<Integer, Long> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return DataTimes.DATA_TIMES.DATA_ID;
    }

    @Override
    public Field<Long> field2() {
        return DataTimes.DATA_TIMES.DATE;
    }

    @Override
    public Integer component1() {
        return getDataId();
    }

    @Override
    public Long component2() {
        return getDate();
    }

    @Override
    public Integer value1() {
        return getDataId();
    }

    @Override
    public Long value2() {
        return getDate();
    }

    @Override
    public DataTimesRecord value1(Integer value) {
        setDataId(value);
        return this;
    }

    @Override
    public DataTimesRecord value2(Long value) {
        setDate(value);
        return this;
    }

    @Override
    public DataTimesRecord values(Integer value1, Long value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached DataTimesRecord
     */
    public DataTimesRecord() {
        super(DataTimes.DATA_TIMES);
    }

    /**
     * Create a detached, initialised DataTimesRecord
     */
    public DataTimesRecord(Integer dataId, Long date) {
        super(DataTimes.DATA_TIMES);

        setDataId(dataId);
        setDate(date);
        resetChangedOnNotNull();
    }

    /**
     * Create a detached, initialised DataTimesRecord
     */
    public DataTimesRecord(com.examind.database.api.jooq.tables.pojos.DataTimes value) {
        super(DataTimes.DATA_TIMES);

        if (value != null) {
            setDataId(value.getDataId());
            setDate(value.getDate());
            resetChangedOnNotNull();
        }
    }
}
