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
package com.examind.database.api.jooq.tables;


import com.examind.database.api.jooq.Admin;
import com.examind.database.api.jooq.Indexes;
import com.examind.database.api.jooq.Keys;
import com.examind.database.api.jooq.tables.records.DataRecord;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function22;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row22;
import org.jooq.Schema;
import org.jooq.SelectField;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * Generated DAO object for table admin.data
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Data extends TableImpl<DataRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>admin.data</code>
     */
    public static final Data DATA = new Data();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<DataRecord> getRecordType() {
        return DataRecord.class;
    }

    /**
     * The column <code>admin.data.id</code>.
     */
    public final TableField<DataRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>admin.data.name</code>.
     */
    public final TableField<DataRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(512).nullable(false), this, "");

    /**
     * The column <code>admin.data.namespace</code>.
     */
    public final TableField<DataRecord, String> NAMESPACE = createField(DSL.name("namespace"), SQLDataType.VARCHAR(256).nullable(false), this, "");

    /**
     * The column <code>admin.data.provider</code>.
     */
    public final TableField<DataRecord, Integer> PROVIDER = createField(DSL.name("provider"), SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>admin.data.type</code>.
     */
    public final TableField<DataRecord, String> TYPE = createField(DSL.name("type"), SQLDataType.VARCHAR(32).nullable(false), this, "");

    /**
     * The column <code>admin.data.subtype</code>.
     */
    public final TableField<DataRecord, String> SUBTYPE = createField(DSL.name("subtype"), SQLDataType.VARCHAR(32).nullable(false).defaultValue(DSL.field(DSL.raw("''::character varying"), SQLDataType.VARCHAR)), this, "");

    /**
     * The column <code>admin.data.included</code>.
     */
    public final TableField<DataRecord, Boolean> INCLUDED = createField(DSL.name("included"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("true"), SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>admin.data.sensorable</code>.
     */
    public final TableField<DataRecord, Boolean> SENSORABLE = createField(DSL.name("sensorable"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("false"), SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>admin.data.date</code>.
     */
    public final TableField<DataRecord, Long> DATE = createField(DSL.name("date"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>admin.data.owner</code>.
     */
    public final TableField<DataRecord, Integer> OWNER = createField(DSL.name("owner"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>admin.data.metadata</code>.
     */
    public final TableField<DataRecord, String> METADATA = createField(DSL.name("metadata"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>admin.data.dataset_id</code>.
     */
    public final TableField<DataRecord, Integer> DATASET_ID = createField(DSL.name("dataset_id"), SQLDataType.INTEGER, this, "");

    /**
     * The column <code>admin.data.feature_catalog</code>.
     */
    public final TableField<DataRecord, String> FEATURE_CATALOG = createField(DSL.name("feature_catalog"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>admin.data.stats_result</code>.
     */
    public final TableField<DataRecord, String> STATS_RESULT = createField(DSL.name("stats_result"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>admin.data.rendered</code>.
     */
    public final TableField<DataRecord, Boolean> RENDERED = createField(DSL.name("rendered"), SQLDataType.BOOLEAN, this, "");

    /**
     * The column <code>admin.data.stats_state</code>.
     */
    public final TableField<DataRecord, String> STATS_STATE = createField(DSL.name("stats_state"), SQLDataType.CLOB, this, "");

    /**
     * The column <code>admin.data.hidden</code>.
     */
    public final TableField<DataRecord, Boolean> HIDDEN = createField(DSL.name("hidden"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("false"), SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>admin.data.cached_info</code>.
     */
    public final TableField<DataRecord, Boolean> CACHED_INFO = createField(DSL.name("cached_info"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("false"), SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>admin.data.has_time</code>.
     */
    public final TableField<DataRecord, Boolean> HAS_TIME = createField(DSL.name("has_time"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("false"), SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>admin.data.has_elevation</code>.
     */
    public final TableField<DataRecord, Boolean> HAS_ELEVATION = createField(DSL.name("has_elevation"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("false"), SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>admin.data.has_dim</code>.
     */
    public final TableField<DataRecord, Boolean> HAS_DIM = createField(DSL.name("has_dim"), SQLDataType.BOOLEAN.nullable(false).defaultValue(DSL.field(DSL.raw("false"), SQLDataType.BOOLEAN)), this, "");

    /**
     * The column <code>admin.data.crs</code>.
     */
    public final TableField<DataRecord, String> CRS = createField(DSL.name("crs"), SQLDataType.VARCHAR(100000), this, "");

    private Data(Name alias, Table<DataRecord> aliased) {
        this(alias, aliased, null);
    }

    private Data(Name alias, Table<DataRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>admin.data</code> table reference
     */
    public Data(String alias) {
        this(DSL.name(alias), DATA);
    }

    /**
     * Create an aliased <code>admin.data</code> table reference
     */
    public Data(Name alias) {
        this(alias, DATA);
    }

    /**
     * Create a <code>admin.data</code> table reference
     */
    public Data() {
        this(DSL.name("data"), null);
    }

    public <O extends Record> Data(Table<O> child, ForeignKey<O, DataRecord> key) {
        super(child, key, DATA);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Admin.ADMIN;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.asList(Indexes.DATA_OWNER_IDX, Indexes.DATA_PROVIDER_IDX);
    }

    @Override
    public Identity<DataRecord, Integer> getIdentity() {
        return (Identity<DataRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<DataRecord> getPrimaryKey() {
        return Keys.DATA_PK;
    }

    @Override
    public List<ForeignKey<DataRecord, ?>> getReferences() {
        return Arrays.asList(Keys.DATA__DATA_PROVIDER_FK, Keys.DATA__DATA_OWNER_FK, Keys.DATA__DATA_DATASET_ID_FK);
    }

    private transient Provider _provider;
    private transient CstlUser _cstlUser;
    private transient Dataset _dataset;

    /**
     * Get the implicit join path to the <code>admin.provider</code> table.
     */
    public Provider provider() {
        if (_provider == null)
            _provider = new Provider(this, Keys.DATA__DATA_PROVIDER_FK);

        return _provider;
    }

    /**
     * Get the implicit join path to the <code>admin.cstl_user</code> table.
     */
    public CstlUser cstlUser() {
        if (_cstlUser == null)
            _cstlUser = new CstlUser(this, Keys.DATA__DATA_OWNER_FK);

        return _cstlUser;
    }

    /**
     * Get the implicit join path to the <code>admin.dataset</code> table.
     */
    public Dataset dataset() {
        if (_dataset == null)
            _dataset = new Dataset(this, Keys.DATA__DATA_DATASET_ID_FK);

        return _dataset;
    }

    @Override
    public Data as(String alias) {
        return new Data(DSL.name(alias), this);
    }

    @Override
    public Data as(Name alias) {
        return new Data(alias, this);
    }

    @Override
    public Data as(Table<?> alias) {
        return new Data(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Data rename(String name) {
        return new Data(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Data rename(Name name) {
        return new Data(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Data rename(Table<?> name) {
        return new Data(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row22 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row22<Integer, String, String, Integer, String, String, Boolean, Boolean, Long, Integer, String, Integer, String, String, Boolean, String, Boolean, Boolean, Boolean, Boolean, Boolean, String> fieldsRow() {
        return (Row22) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function22<? super Integer, ? super String, ? super String, ? super Integer, ? super String, ? super String, ? super Boolean, ? super Boolean, ? super Long, ? super Integer, ? super String, ? super Integer, ? super String, ? super String, ? super Boolean, ? super String, ? super Boolean, ? super Boolean, ? super Boolean, ? super Boolean, ? super Boolean, ? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function22<? super Integer, ? super String, ? super String, ? super Integer, ? super String, ? super String, ? super Boolean, ? super Boolean, ? super Long, ? super Integer, ? super String, ? super Integer, ? super String, ? super String, ? super Boolean, ? super String, ? super Boolean, ? super Boolean, ? super Boolean, ? super Boolean, ? super Boolean, ? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
