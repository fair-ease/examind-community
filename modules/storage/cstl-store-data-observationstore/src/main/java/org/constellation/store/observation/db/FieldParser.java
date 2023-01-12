/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import static org.geotoolkit.observation.OMUtils.dateFromTS;
import org.geotoolkit.observation.result.ResultBuilder;
import org.geotoolkit.observation.model.Field;

/**
 *
 * @author guilhem
 */
public class FieldParser {

    public Date firstTime = null;
    public Date lastTime  = null;
    public int nbValue    = 0;
    
    private final List<Field> fields;
    private final boolean profileWithTime;
    private final boolean includeID;
    private final boolean includeQuality;
    private final ResultBuilder values;
    private String name;

    private boolean first = false;

    public FieldParser(List<Field> fields, ResultBuilder values, boolean profileWithTime, boolean includeID, boolean includeQuality, String name) {
        this.profileWithTime = profileWithTime;
        this.fields = fields;
        this.includeID = includeID;
        this.includeQuality = includeQuality;
        this.name = name;
        this.values = values;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void parseLine(ResultSet rs,  int offset) throws SQLException {
        values.newBlock();
        for (int i = 0; i < fields.size(); i++) {

            Field field = fields.get(i);
            parseField(field, rs, i <= offset, null);

            if (includeQuality && field.qualityFields != null) {
                for (Field qField : field.qualityFields) {
                    parseField(qField, rs, false, field);
                }
            }
        }
        nbValue = values.endBlock();
    }

    private void parseField(Field field, ResultSet rs,  boolean beforeMain, Field parent) throws SQLException {
        String fieldName;
        if (parent != null) {
           fieldName = parent.name + "_quality_" + field.name;
        } else {
           fieldName = field.name;
        }
        switch (field.type) {
            case TIME:
                // profile with time field
                if (profileWithTime && beforeMain) {
                    values.appendTime(firstTime);
                } else {
                    Date t = dateFromTS(rs.getTimestamp(fieldName));
                    values.appendTime(t);
                    if (first) {
                        firstTime = t;
                        first = false;
                    }
                    lastTime = t;
                }
                break;
            case QUANTITY:
                Double d =  rs.getDouble(fieldName);
                if (rs.wasNull()) {
                    d = Double.NaN;
                }
                values.appendDouble(d);
                break;
            case BOOLEAN:
                boolean bvalue = rs.getBoolean(fieldName);
                values.appendBoolean(bvalue);
                break;
            default:
                String svalue = rs.getString(fieldName);
                if (includeID && fieldName.equals("id")) {
                    svalue = name + '-' + svalue;
                }
                values.appendString(svalue);
                break;
        }
    }

}
