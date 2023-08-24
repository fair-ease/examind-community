/*
 *    Examind comunity - An open source and standard compliant SDI
 *    https://community.examind.com/
 *
 * Copyright 2023 Geomatys.
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
package org.constellation.business;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.constellation.dto.service.config.sos.ProcedureDataset;
import org.constellation.dto.service.config.sos.SensorMLTree;
import org.constellation.exception.ConfigurationException;
import org.constellation.exception.ConstellationException;
import org.opengis.observation.Observation;
import org.opengis.observation.Phenomenon;
import org.opengis.temporal.TemporalGeometricPrimitive;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface ISensorServiceBusiness {

    boolean importSensor(final Integer serviceID, final Path sensorFile, final String type) throws ConfigurationException;

    boolean removeSensor(final Integer id, final String sensorID) throws ConstellationException;

    boolean removeAllSensors(final Integer id) throws ConfigurationException;

    Collection<String> getSensorIds(final Integer id)  throws ConfigurationException;

    long getSensorCount(final Integer id) throws ConfigurationException;

    Collection<String> getObservedPropertiesForSensorId(final Integer id, final String sensorID, final boolean decompose) throws ConfigurationException;

    TemporalGeometricPrimitive getTimeForSensorId(final Integer id, final String sensorID) throws ConfigurationException;

    /**
     * Remove an observation data from all the services in which the data is integrated.
     *
     * @param dataID Data identifier.
     *
     * @throws ConstellationException If the data does not exist, or if the data provider is not an observation one.
     */
    void removeDataObservationsFromServices(final Integer dataID) throws ConstellationException;

    /**
     * Remove an observation data from a service in which the data is integrated.
     * however some sensor service share their provider so the removal can occurs on multiple service
     *
     * @param sid service identifier.
     * @param dataID data identifier.
     *
     * @throws ConstellationException  If the data does not exist, or if the data provider is not an observation one.
     */
    void removeDataObservationsFromService(final Integer sid, final Integer dataID) throws ConstellationException;

    void importObservationsFromData(final Integer sid, final Integer dataID) throws ConstellationException;

    void importObservations(final Integer id, final List<Observation> observations, final List<Phenomenon> phenomenons) throws ConfigurationException;

    Collection<String> getObservedPropertiesIds(Integer id) throws ConfigurationException;

    void writeProcedure(final Integer id, final ProcedureDataset procedure) throws ConfigurationException;

    String getWKTSensorLocation(final Integer id, final String sensorID) throws ConfigurationException;

    Object getResultsCsv(final Integer id, final String sensorID, final List<String> observedProperties, final List<String> foi, final Date start, final Date end, final Integer width, final String resultFormat, final boolean timeforProfile, final boolean includeIdInDatablock) throws ConfigurationException;

    Object getSensorMetadata(final Integer id, final String sensorID) throws ConstellationException;

    boolean importSensor(Integer id, String sensorID) throws ConstellationException;

    SensorMLTree getServiceSensorMLTree(Integer id) throws ConstellationException;
}
