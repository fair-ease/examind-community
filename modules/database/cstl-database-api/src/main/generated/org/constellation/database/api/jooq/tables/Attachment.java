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
import org.constellation.database.api.jooq.tables.records.AttachmentRecord;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
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
 * Generated DAO object for table admin.attachment
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Attachment extends TableImpl<AttachmentRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>admin.attachment</code>
     */
    public static final Attachment ATTACHMENT = new Attachment();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<AttachmentRecord> getRecordType() {
        return AttachmentRecord.class;
    }

    /**
     * The column <code>admin.attachment.id</code>.
     */
    public final TableField<AttachmentRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>admin.attachment.content</code>.
     */
    public final TableField<AttachmentRecord, byte[]> CONTENT = createField(DSL.name("content"), SQLDataType.BLOB, this, "");

    /**
     * The column <code>admin.attachment.uri</code>.
     */
    public final TableField<AttachmentRecord, String> URI = createField(DSL.name("uri"), SQLDataType.VARCHAR(500), this, "");

    /**
     * The column <code>admin.attachment.filename</code>.
     */
    public final TableField<AttachmentRecord, String> FILENAME = createField(DSL.name("filename"), SQLDataType.VARCHAR(500), this, "");

    private Attachment(Name alias, Table<AttachmentRecord> aliased) {
        this(alias, aliased, null);
    }

    private Attachment(Name alias, Table<AttachmentRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>admin.attachment</code> table reference
     */
    public Attachment(String alias) {
        this(DSL.name(alias), ATTACHMENT);
    }

    /**
     * Create an aliased <code>admin.attachment</code> table reference
     */
    public Attachment(Name alias) {
        this(alias, ATTACHMENT);
    }

    /**
     * Create a <code>admin.attachment</code> table reference
     */
    public Attachment() {
        this(DSL.name("attachment"), null);
    }

    public <O extends Record> Attachment(Table<O> child, ForeignKey<O, AttachmentRecord> key) {
        super(child, key, ATTACHMENT);
    }

    @Override
    public Schema getSchema() {
        return Admin.ADMIN;
    }

    @Override
    public Identity<AttachmentRecord, Integer> getIdentity() {
        return (Identity<AttachmentRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<AttachmentRecord> getPrimaryKey() {
        return Keys.ATTACHMENT_PK;
    }

    @Override
    public List<UniqueKey<AttachmentRecord>> getKeys() {
        return Arrays.<UniqueKey<AttachmentRecord>>asList(Keys.ATTACHMENT_PK);
    }

    @Override
    public Attachment as(String alias) {
        return new Attachment(DSL.name(alias), this);
    }

    @Override
    public Attachment as(Name alias) {
        return new Attachment(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Attachment rename(String name) {
        return new Attachment(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Attachment rename(Name name) {
        return new Attachment(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, byte[], String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
