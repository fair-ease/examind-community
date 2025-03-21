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
import com.examind.database.api.jooq.Keys;
import com.examind.database.api.jooq.tables.records.PropertyRecord;

import java.util.function.Function;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Function2;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Records;
import org.jooq.Row2;
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
 * Generated DAO object for table admin.property
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Property extends TableImpl<PropertyRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>admin.property</code>
     */
    public static final Property PROPERTY = new Property();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<PropertyRecord> getRecordType() {
        return PropertyRecord.class;
    }

    /**
     * The column <code>admin.property.name</code>.
     */
    public final TableField<PropertyRecord, String> NAME = createField(DSL.name("name"), SQLDataType.VARCHAR(32).nullable(false), this, "");

    /**
     * The column <code>admin.property.value</code>.
     */
    public final TableField<PropertyRecord, String> VALUE = createField(DSL.name("value"), SQLDataType.VARCHAR(1000).nullable(false), this, "");

    private Property(Name alias, Table<PropertyRecord> aliased) {
        this(alias, aliased, null);
    }

    private Property(Name alias, Table<PropertyRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>admin.property</code> table reference
     */
    public Property(String alias) {
        this(DSL.name(alias), PROPERTY);
    }

    /**
     * Create an aliased <code>admin.property</code> table reference
     */
    public Property(Name alias) {
        this(alias, PROPERTY);
    }

    /**
     * Create a <code>admin.property</code> table reference
     */
    public Property() {
        this(DSL.name("property"), null);
    }

    public <O extends Record> Property(Table<O> child, ForeignKey<O, PropertyRecord> key) {
        super(child, key, PROPERTY);
    }

    @Override
    public Schema getSchema() {
        return aliased() ? null : Admin.ADMIN;
    }

    @Override
    public UniqueKey<PropertyRecord> getPrimaryKey() {
        return Keys.PROPERTY_PK;
    }

    @Override
    public Property as(String alias) {
        return new Property(DSL.name(alias), this);
    }

    @Override
    public Property as(Name alias) {
        return new Property(alias, this);
    }

    @Override
    public Property as(Table<?> alias) {
        return new Property(alias.getQualifiedName(), this);
    }

    /**
     * Rename this table
     */
    @Override
    public Property rename(String name) {
        return new Property(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Property rename(Name name) {
        return new Property(name, null);
    }

    /**
     * Rename this table
     */
    @Override
    public Property rename(Table<?> name) {
        return new Property(name.getQualifiedName(), null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<String, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    public <U> SelectField<U> mapping(Function2<? super String, ? super String, ? extends U> from) {
        return convertFrom(Records.mapping(from));
    }

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    public <U> SelectField<U> mapping(Class<U> toType, Function2<? super String, ? super String, ? extends U> from) {
        return convertFrom(toType, Records.mapping(from));
    }
}
