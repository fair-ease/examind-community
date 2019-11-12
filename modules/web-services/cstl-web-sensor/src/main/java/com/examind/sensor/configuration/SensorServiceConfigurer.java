/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
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

package com.examind.sensor.configuration;

import com.examind.sensor.component.SensorServiceBusiness;
import java.util.logging.Level;

import org.constellation.dto.AcknowlegementType;
import org.constellation.exception.ConfigurationException;
import org.constellation.dto.service.Instance;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.constellation.ws.ISOSConfigurer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@link OGCConfigurer} implementation for various Sensor service.
 *
 * TODO: implement specific configuration methods
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class SensorServiceConfigurer extends OGCConfigurer implements ISOSConfigurer {

    @Autowired
    private SensorServiceBusiness sensorServBusiness;

    @Override
    public Instance getInstance(final Integer id) throws ConfigurationException {
        final Instance instance = super.getInstance(id);
        try {
            instance.setLayersNumber(sensorServBusiness.getSensorIds(id).size());
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Error while getting sensor count on Sensor instance:" + id, ex);
        }
        return instance;
    }

    @Override
    public void removeSensor(final Integer id, final String sensorID) throws ConfigurationException {
        sensorServBusiness.removeSensor(id, sensorID);
    }

}