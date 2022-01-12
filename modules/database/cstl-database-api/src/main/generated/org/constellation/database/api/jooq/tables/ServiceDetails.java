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
import org.constellation.database.api.jooq.Indexes;
import org.constellation.database.api.jooq.Keys;
import org.constellation.database.api.jooq.tables.records.ServiceDetailsRecord;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row4;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * Generated DAO object for table admin.service_details
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ServiceDetails extends TableImpl<ServiceDetailsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>admin.service_details</code>
     */
    public static final ServiceDetails SERVICE_DETAILS = new ServiceDetails();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ServiceDetailsRecord> getRecordType() {
        return ServiceDetailsRecord.class;
    }

    /**
     * The column <code>admin.service_details.id</code>.
     */
    public final TableField<ServiceDetailsRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>admin.service_details.lang</code>.
     */
    public final TableField<ServiceDetailsRecord, String> LANG = createField(DSL.name("lang"), SQLDataType.VARCHAR(3).nullable(false), this, "");

    /**
     * The column <code>admin.service_details.content</code>.
     */
    public final TableField<ServiceDetailsRecord, String> CONTENT = createField(DSL.name("content"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>admin.service_details.default_lang</code>.
     */
    public final TableField<ServiceDetailsRecord, Boolean> DEFAULT_LANG = createField(DSL.name("default_lang"), SQLDataType.BOOLEAN, this, "");

    private ServiceDetails(Name alias, Table<ServiceDetailsRecord> aliased) {
        this(alias, aliased, null);
    }

    private ServiceDetails(Name alias, Table<ServiceDetailsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>admin.service_details</code> table reference
     */
    public ServiceDetails(String alias) {
        this(DSL.name(alias), SERVICE_DETAILS);
    }

    /**
     * Create an aliased <code>admin.service_details</code> table reference
     */
    public ServiceDetails(Name alias) {
        this(alias, SERVICE_DETAILS);
    }

    /**
     * Create a <code>admin.service_details</code> table reference
     */
    public ServiceDetails() {
        this(DSL.name("service_details"), null);
    }

    public <O extends Record> ServiceDetails(Table<O> child, ForeignKey<O, ServiceDetailsRecord> key) {
        super(child, key, SERVICE_DETAILS);
    }

    @Override
    public Schema getSchema() {
        return Admin.ADMIN;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.SERVICE_DETAILS_ID_IDX);
    }

    @Override
    public UniqueKey<ServiceDetailsRecord> getPrimaryKey() {
        return Keys.SERVICE_DETAILS_PK;
    }

    @Override
    public List<UniqueKey<ServiceDetailsRecord>> getKeys() {
        return Arrays.<UniqueKey<ServiceDetailsRecord>>asList(Keys.SERVICE_DETAILS_PK);
    }

    @Override
    public List<ForeignKey<ServiceDetailsRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ServiceDetailsRecord, ?>>asList(Keys.SERVICE_DETAILS__SERVICE_DETAILS_SERVICE_ID_FK);
    }

    private transient Service _service;

    public Service service() {
        if (_service == null)
            _service = new Service(this, Keys.SERVICE_DETAILS__SERVICE_DETAILS_SERVICE_ID_FK);

        return _service;
    }

    @Override
    public ServiceDetails as(String alias) {
        return new ServiceDetails(DSL.name(alias), this);
    }

    @Override
    public ServiceDetails as(Name alias) {
        return new ServiceDetails(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ServiceDetails rename(String name) {
        return new ServiceDetails(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ServiceDetails rename(Name name) {
        return new ServiceDetails(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, String, String, Boolean> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
