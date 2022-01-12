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
import org.constellation.database.api.jooq.tables.records.ChainProcessRecord;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
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
 * Generated DAO object for table admin.chain_process
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ChainProcess extends TableImpl<ChainProcessRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>admin.chain_process</code>
     */
    public static final ChainProcess CHAIN_PROCESS = new ChainProcess();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ChainProcessRecord> getRecordType() {
        return ChainProcessRecord.class;
    }

    /**
     * The column <code>admin.chain_process.id</code>.
     */
    public final TableField<ChainProcessRecord, Integer> ID = createField(DSL.name("id"), SQLDataType.INTEGER.nullable(false).identity(true), this, "");

    /**
     * The column <code>admin.chain_process.auth</code>.
     */
    public final TableField<ChainProcessRecord, String> AUTH = createField(DSL.name("auth"), SQLDataType.VARCHAR(512), this, "");

    /**
     * The column <code>admin.chain_process.code</code>.
     */
    public final TableField<ChainProcessRecord, String> CODE = createField(DSL.name("code"), SQLDataType.VARCHAR(512), this, "");

    /**
     * The column <code>admin.chain_process.config</code>.
     */
    public final TableField<ChainProcessRecord, String> CONFIG = createField(DSL.name("config"), SQLDataType.CLOB, this, "");

    private ChainProcess(Name alias, Table<ChainProcessRecord> aliased) {
        this(alias, aliased, null);
    }

    private ChainProcess(Name alias, Table<ChainProcessRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>admin.chain_process</code> table reference
     */
    public ChainProcess(String alias) {
        this(DSL.name(alias), CHAIN_PROCESS);
    }

    /**
     * Create an aliased <code>admin.chain_process</code> table reference
     */
    public ChainProcess(Name alias) {
        this(alias, CHAIN_PROCESS);
    }

    /**
     * Create a <code>admin.chain_process</code> table reference
     */
    public ChainProcess() {
        this(DSL.name("chain_process"), null);
    }

    public <O extends Record> ChainProcess(Table<O> child, ForeignKey<O, ChainProcessRecord> key) {
        super(child, key, CHAIN_PROCESS);
    }

    @Override
    public Schema getSchema() {
        return Admin.ADMIN;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.CHAIN_PROCESS_IDX);
    }

    @Override
    public Identity<ChainProcessRecord, Integer> getIdentity() {
        return (Identity<ChainProcessRecord, Integer>) super.getIdentity();
    }

    @Override
    public UniqueKey<ChainProcessRecord> getPrimaryKey() {
        return Keys.CHAIN_PROCESS_PK;
    }

    @Override
    public List<UniqueKey<ChainProcessRecord>> getKeys() {
        return Arrays.<UniqueKey<ChainProcessRecord>>asList(Keys.CHAIN_PROCESS_PK);
    }

    @Override
    public ChainProcess as(String alias) {
        return new ChainProcess(DSL.name(alias), this);
    }

    @Override
    public ChainProcess as(Name alias) {
        return new ChainProcess(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ChainProcess rename(String name) {
        return new ChainProcess(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public ChainProcess rename(Name name) {
        return new ChainProcess(name, null);
    }

    // -------------------------------------------------------------------------
    // Row4 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row4<Integer, String, String, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }
}
