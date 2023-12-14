/*
 *    Examind - An open source and standard compliant SDI
 *    https://community.examind.com
 *
 * Copyright 2022 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.store.observation.db;

import java.sql.Connection;
import org.constellation.util.SQLResult;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import org.apache.sis.storage.DataStoreException;
import org.constellation.store.observation.db.model.ProcedureInfo;
import org.constellation.util.FilterSQLRequest;
import org.geotoolkit.observation.result.ResultBuilder;
import org.geotoolkit.observation.model.Field;
import org.geotoolkit.observation.model.ResultMode;
import static org.geotoolkit.observation.model.TextEncoderProperties.CSV_ENCODING;
import static org.geotoolkit.observation.model.TextEncoderProperties.DEFAULT_ENCODING;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ResultProcessor {

    protected static final Logger LOGGER = Logger.getLogger("org.constellation.store.observation.db");
    
    protected TemporaryResultBuilder values = null;
    protected final List<Field> fields;
    protected final boolean profile;
    protected final boolean includeId;
    protected final boolean includeTimeInProfile;
    protected final boolean includeQuality;
    protected final ProcedureInfo procedure;
    protected final int mainFieldIndex;
    protected final String idSuffix;

    public ResultProcessor(List<Field> fields, boolean includeId, boolean includeQuality, boolean includeTimeInProfile, ProcedureInfo procedure, String idSuffix) {
        this.fields = fields;
        this.profile = "profile".equals(procedure.type);
        this.includeId = includeId;
        this.includeQuality = includeQuality;
        this.includeTimeInProfile = includeTimeInProfile;
        this.procedure = procedure;
        this.mainFieldIndex = fields.indexOf(procedure.mainField);
        this.idSuffix = idSuffix == null ? "" : idSuffix;
    }

    public ResultBuilder initResultBuilder(String responseFormat, boolean countRequest) {
        if ("resultArray".equals(responseFormat)) {
            values = new TemporaryResultBuilder(ResultMode.DATA_ARRAY, null, false);
        } else if ("text/csv".equals(responseFormat)) {
            values = new TemporaryResultBuilder(ResultMode.CSV, CSV_ENCODING, true);
            // Add the header
            values.appendHeaders(fields);
        } else if (countRequest) {
            values = new TemporaryResultBuilder(ResultMode.COUNT, null, false);
        } else {
            values = new TemporaryResultBuilder(ResultMode.CSV, DEFAULT_ENCODING, false);
        }
        return values;
    }

    public void computeRequest(FilterSQLRequest sqlRequest, int fieldOffset, boolean firstFilter, Connection c) throws SQLException {
        StringBuilder select  = new StringBuilder("m.*");
        StringBuilder orderBy = new StringBuilder(" ORDER BY ");
        if (profile) {
            select.append(", o.\"id\" as oid ");
            if (includeTimeInProfile) {
                select.append(", o.\"time_begin\" ");
            }
            orderBy.append(" o.\"time_begin\", ");
        }
        if (includeId) {
            select.append(", o.\"identifier\" ");
        }
        // always order by main field
        orderBy.append("\"").append(procedure.mainField.name).append("\"");
        
        sqlRequest.replaceFirst("m.*", select.toString());
        sqlRequest.append(orderBy.toString());

        if (firstFilter) {
            sqlRequest.replaceFirst("WHERE", "");
        }
    }
    
    public void processResults(SQLResult rs, int fieldOffset) throws SQLException, DataStoreException {
        if (values == null) {
            throw new DataStoreException("initResultBuilder(...) must be called before processing the results");
        }
        FieldParser parser = new FieldParser(fields, values, false, includeId, includeQuality, null);
        while (rs.nextOnField(procedure.mainField.name)) {
            if (includeId) {
                String name = rs.getString("identifier", 0);
                parser.setName(name + idSuffix);
            }
            parser.parseLine(rs, fieldOffset);
        }
    }
}
