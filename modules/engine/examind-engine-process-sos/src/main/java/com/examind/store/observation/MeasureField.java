/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2021 Geomatys.
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
package com.examind.store.observation;

import java.util.List;
import java.util.Map;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.observation.model.FieldType;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MeasureField {

    public final int columnIndex;
    public final String name;
    public final FieldType type;
    public final List<MeasureField> qualityFields;

    /*
    * these attribute will be updated after the creation.
    */
    public String label;
    public String uom;
    public String description;
    public Map<String, Object> properties;

    public MeasureField(int columnIndex, String name, FieldType type, List<MeasureField> qualityFields) {
        ArgumentChecks.ensureNonNull("type", type);
        this.columnIndex = columnIndex;
        this.name = name;
        this.type = type;
        this.qualityFields = qualityFields;
    }
}
