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
package org.constellation.database.api.jooq.tables;


import java.util.Arrays;
import java.util.List;

import org.constellation.database.api.jooq.Admin;
import org.constellation.database.api.jooq.Keys;
import org.constellation.database.api.jooq.tables.records.InternalSensorRecord;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row3;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * Generated DAO object for table admin.internal_sensor
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class InternalSensor extends TableImpl<InternalSensorRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>admin.internal_sensor</code>
     */
    public static final InternalSensor INTERNAL_SENSOR = new InternalSensor();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<InternalSensorRecord> getRecordType() {
        return InternalSensorRecord.class;
    }

    /**
     * The column <code>admin.internal_sensor.id</code>.
     */
    public final TableField<InternalSensorRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>admin.internal_sensor.sensor_id</code>.
     */
    public final TableField<InternalSensorRecord, String> SENSOR_ID = createField(DSL.name("sensor_id"), SQLDataType.VARCHAR(100).nullable(false), this, "");

    /**
     * The column <code>admin.internal_sensor.metadata</code>.
     */
    public final TableField<InternalSensorRecord, String> METADATA = createField(DSL.name("metadata"), SQLDataType.CLOB.nullable(false), this, "");

    private InternalSensor(Name alias, Table<InternalSensorRecord> aliased) {
        this(alias, aliased, null);
    }

    private InternalSensor(Name alias, Table<InternalSensorRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>admin.internal_sensor</code> table reference
     */
    public InternalSensor(String alias) {
        this(DSL.name(alias), INTERNAL_SENSOR);
    }

    /**
     * Create an aliased <code>admin.internal_sensor</code> table reference
     */
    public InternalSensor(Name alias) {
        this(alias, INTERNAL_SENSOR);
    }

    /**
     * Create a <code>admin.internal_sensor</code> table reference
     */
    public InternalSensor() {
        this(DSL.name("internal_sensor"), null);
    }

    public <O extends Record> InternalSensor(Table<O> child, ForeignKey<O, InternalSensorRecord> key) {
        super(child, key, INTERNAL_SENSOR);
    }

    @Override
    public Schema getSchema() {
        return Admin.ADMIN;
    }

    @Override
    public Identity<InternalSensorRecord, Integer> getIdentity() {
        return (Identity<InternalSensorRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<InternalSensorRecord> getPrimaryKey() {
        return Keys.INTERNAL_SENSOR_PK;
    }

    @Override
    public List<UniqueKey<InternalSensorRecord>> getKeys() {
        return Arrays.<UniqueKey<InternalSensorRecord>>asList(Keys.INTERNAL_SENSOR_PK);
    }

    @Override
    public List<ForeignKey<InternalSensorRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<InternalSensorRecord, ?>>asList(Keys.INTERNAL_SENSOR__INTERNAL_SENSOR_ID_FK);
    }

    private transient Sensor _sensor;

    public Sensor sensor() {
        if (_sensor == null)
            _sensor = new Sensor(this, Keys.INTERNAL_SENSOR__INTERNAL_SENSOR_ID_FK);

        return _sensor;
    }

    @Override
    public InternalSensor as(String alias) {
        return new InternalSensor(DSL.name(alias), this);
    }

    @Override
    public InternalSensor as(Name alias) {
        return new InternalSensor(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public InternalSensor rename(String name) {
        return new InternalSensor(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public InternalSensor rename(Name name) {
        return new InternalSensor(name, null);
    }

    // -------------------------------------------------------------------------
    // Row3 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, String, String> fieldsRow() {
        return (Row3) super.fieldsRow();
    }
}
