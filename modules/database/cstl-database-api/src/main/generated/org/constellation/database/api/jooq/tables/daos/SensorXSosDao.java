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
package org.constellation.database.api.jooq.tables.daos;


import java.util.List;

import org.constellation.database.api.jooq.tables.SensorXSos;
import org.constellation.database.api.jooq.tables.records.SensorXSosRecord;
import org.jooq.Configuration;
import org.jooq.Record2;
import org.jooq.impl.DAOImpl;


/**
 * Generated DAO object for table admin.sensor_x_sos
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SensorXSosDao extends DAOImpl<SensorXSosRecord, org.constellation.database.api.jooq.tables.pojos.SensorXSos, Record2<Integer, Integer>> {

    /**
     * Create a new SensorXSosDao without any configuration
     */
    public SensorXSosDao() {
        super(SensorXSos.SENSOR_X_SOS, org.constellation.database.api.jooq.tables.pojos.SensorXSos.class);
    }

    /**
     * Create a new SensorXSosDao with an attached configuration
     */
    public SensorXSosDao(Configuration configuration) {
        super(SensorXSos.SENSOR_X_SOS, org.constellation.database.api.jooq.tables.pojos.SensorXSos.class, configuration);
    }

    @Override
    public Record2<Integer, Integer> getId(org.constellation.database.api.jooq.tables.pojos.SensorXSos object) {
        return compositeKeyRecord(object.getSensorId(), object.getSosId());
    }

    /**
     * Fetch records that have <code>sensor_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.SensorXSos> fetchRangeOfSensorId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(SensorXSos.SENSOR_X_SOS.SENSOR_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>sensor_id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.SensorXSos> fetchBySensorId(Integer... values) {
        return fetch(SensorXSos.SENSOR_X_SOS.SENSOR_ID, values);
    }

    /**
     * Fetch records that have <code>sos_id BETWEEN lowerInclusive AND upperInclusive</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.SensorXSos> fetchRangeOfSosId(Integer lowerInclusive, Integer upperInclusive) {
        return fetchRange(SensorXSos.SENSOR_X_SOS.SOS_ID, lowerInclusive, upperInclusive);
    }

    /**
     * Fetch records that have <code>sos_id IN (values)</code>
     */
    public List<org.constellation.database.api.jooq.tables.pojos.SensorXSos> fetchBySosId(Integer... values) {
        return fetch(SensorXSos.SENSOR_X_SOS.SOS_ID, values);
    }
}
