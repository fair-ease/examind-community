/*
 *    Examind community - An open source and standard compliant SDI
 *    https://community.examind.com/
 *
 * Copyright 2024 Geomatys.
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
package org.constellation.store.observation.db.sensor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.apache.sis.parameter.Parameters;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.DataStoreProvider;
import org.apache.sis.storage.Resource;
import static org.constellation.store.observation.db.SOSDatabaseObservationStore.SQL_DIALECT;
import org.constellation.store.observation.db.SOSDatabaseObservationStoreFactory;
import static org.constellation.store.observation.db.SOSDatabaseObservationStoreFactory.SCHEMA_PREFIX_NAME;
import org.constellation.store.observation.db.SOSDatabaseParamsUtils;
import org.constellation.store.observation.db.model.OMSQLDialect;
import org.constellation.util.Util;
import static org.geotoolkit.observation.AbstractObservationStoreFactory.OBSERVATION_ID_BASE;
import static org.geotoolkit.observation.AbstractObservationStoreFactory.OBSERVATION_TEMPLATE_ID_BASE;
import static org.geotoolkit.observation.AbstractObservationStoreFactory.PHENOMENON_ID_BASE;
import static org.geotoolkit.observation.AbstractObservationStoreFactory.SENSOR_ID_BASE;
import org.geotoolkit.observation.OMUtils;
import static org.geotoolkit.observation.OMUtils.extractParameter;
import org.geotoolkit.sensor.AbstractSensorStore;
import org.geotoolkit.storage.DataStores;
import org.opengis.metadata.Metadata;
import org.opengis.util.GenericName;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSDatabaseSensorStore extends AbstractSensorStore implements Resource {
    
    public SOSDatabaseSensorStore(Parameters params) throws DataStoreException {
        super(params);
        try {
            DataSource source  =  SOSDatabaseParamsUtils.extractOM2Datasource(params);
            
            String schemaPrefix = params.getValue(SOSDatabaseObservationStoreFactory.SCHEMA_PREFIX);
            if (schemaPrefix == null) {
                schemaPrefix = "";
            } else {
                if (Util.containsForbiddenCharacter(schemaPrefix)) {
                    throw new DataStoreException("Invalid schema prefix value");
                }
            }
            OMSQLDialect dialect = OMSQLDialect.valueOf((params.getValue(SOSDatabaseObservationStoreFactory.SGBDTYPE)).toUpperCase());
            
            final Map<String,Object> properties = new HashMap<>();
            extractParameter(config, PHENOMENON_ID_BASE, properties);
            extractParameter(config, OBSERVATION_ID_BASE, properties);
            extractParameter(config, OBSERVATION_TEMPLATE_ID_BASE, properties);
            extractParameter(config, SENSOR_ID_BASE, properties);
            properties.put(SCHEMA_PREFIX_NAME, schemaPrefix);
            properties.put(SQL_DIALECT, dialect);
        
            this.reader = new OM2SensorReader(source, properties);
        } catch (RuntimeException | IOException ex) {
            throw new DataStoreException("Error in SOS Database sensor store constructor", ex);
        }
    }
    
    @Override
    public DataStoreProvider getProvider() {
        return DataStores.getProviderById(SOSDatabaseSensorStoreFactory.NAME);
    }

    @Override
    public Metadata getMetadata() throws DataStoreException {
        return OMUtils.buildMetadata(SOSDatabaseSensorStoreFactory.NAME);
    }

    @Override
    public Optional<GenericName> getIdentifier() {
        return Optional.empty();
    }
}
