/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2017 Geomatys.
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
package org.constellation.api.rest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.inject.Inject;
import org.constellation.api.ServiceDef;
import org.constellation.business.IDataBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.ws.IWSEngine;
import org.constellation.business.ISensorBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.dto.Sensor;
import org.constellation.dto.AcknowlegementType;
import org.constellation.dto.service.config.sos.ObservationFilter;
import org.constellation.dto.ParameterValues;
import org.constellation.dto.SimpleValue;
import org.constellation.exception.ConfigurationException;
import org.constellation.exception.NotRunningServiceException;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ObservationProvider;
import org.constellation.sos.configuration.SOSConfigurer;
import org.constellation.sos.ws.SensorMLGenerator;
import org.geotoolkit.gml.xml.v321.AbstractGeometryType;
import org.geotoolkit.observation.ObservationStore;
import org.geotoolkit.sensor.SensorStore;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sos.netcdf.ExtractionResult;
import org.geotoolkit.sos.netcdf.ExtractionResult.ProcedureTree;
import org.constellation.sos.ws.SOSUtils;
import org.opengis.observation.Observation;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RestController
public class SOSRestAPI {

    @Inject
    private IProviderBusiness providerBusiness;

    @Inject
    private IDataBusiness dataBusiness;

    @Inject
    private IServiceBusiness serviceBusiness;

    @Inject
    private ISensorBusiness sensorBusiness;

    @Inject
    private IWSEngine wsengine;

    @RequestMapping(value="/SOS/{id}/link/{providerID}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity linkSOSProvider(final @PathVariable("id") String id, final @PathVariable("providerID") String providerID) throws Exception {
        final Integer providerId = providerBusiness.getIDFromIdentifier(providerID);
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        serviceBusiness.linkServiceAndProvider(serviceId, providerId);
        return new ResponseEntity(AcknowlegementType.success("Provider correctly linked"), OK);
    }

    @RequestMapping(value="/SOS/{id}/{schema}/build", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity buildDatasourceOM(final @PathVariable("id") String id, final @PathVariable("schema") String schema) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        final AcknowlegementType ack;
        if (getConfigurer().buildDatasource(serviceId, schema)) {
            ack = AcknowlegementType.success("O&M datasource created");
        } else {
            ack = AcknowlegementType.failure("error while creating O&M datasource");
        }
        return new ResponseEntity(ack, OK);
    }

    @RequestMapping(value="/SOS/{id}/sensors", method = PUT, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity importSensorMetadata(final @PathVariable("id") String id, @RequestBody final File sensor) throws Exception {
        Integer serviceId = serviceBusiness.getServiceIdByIdentifierAndType("sos", id);
        return new ResponseEntity(getConfigurer().importSensor(serviceId, sensor.toPath(), "xml"), OK);
    }

    @RequestMapping(value="/SOS/{id}/sensor/{sensorID}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity removeSensor(final @PathVariable("id") String id, final @PathVariable("sensorID") String sensorID) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().removeSensor(serviceId, sensorID), OK);
    }

    @RequestMapping(value="/SOS/{id}/sensors", method = DELETE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity removeAllSensor(final @PathVariable("id") String id) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().removeAllSensors(serviceId), OK);
    }

    @RequestMapping(value="{id}/sensor/{sensorID}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getSensorMetadata(final @PathVariable("id") String id, final @PathVariable("sensorID") String sensorID) throws Exception {
        return new ResponseEntity(sensorBusiness.getSensorMetadata(sensorID, id), OK);
    }

    @RequestMapping(value="/SOS/{id}/sensors", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getSensorTree(final @PathVariable("id") String id) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().getSensorTree(serviceId), OK);
    }

    @RequestMapping(value="/SOS/{id}/sensors/identifiers", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getSensorIds(final @PathVariable("id") String id) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().getSensorIds(serviceId), OK);
    }

    @RequestMapping(value="/SOS/{id}/sensors/identifiers/id", method = POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getSensorIdsForObservedProperty(final @PathVariable("id") String id, final @RequestParam("observedProperty") String observedProperty) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().getSensorIdsForObservedProperty(serviceId, observedProperty), OK);
    }

    @RequestMapping(value="/SOS/{id}/sensors/count", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getSensortCount(final @PathVariable("id") String id) throws Exception {
        return new ResponseEntity(new SimpleValue(sensorBusiness.getCountByServiceId(id)), OK);
    }

    @RequestMapping(value="/SOS/{id}/sensor/location/{sensorID}", method = PUT, consumes = APPLICATION_XML_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity updateSensorLocation(final @PathVariable("id") String id, final @PathVariable("sensorID") String sensorID, final @RequestBody AbstractGeometryType location) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().updateSensorLocation(serviceId, sensorID, location), OK);
    }

    @RequestMapping(value="/SOS/{id}/sensor/location/{sensorID}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getWKTSensorLocation(final @PathVariable("id") String id, final @PathVariable("sensorID") String sensorID) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().getWKTSensorLocation(serviceId, sensorID), OK);
    }

    @RequestMapping(value="/SOS/{id}/observedProperty/identifiers/{sensorID}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getObservedPropertiesForSensor(final @PathVariable("id") String id, final @PathVariable("sensorID") String sensorID) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().getObservedPropertiesForSensorId(serviceId, sensorID), OK);
    }

    @RequestMapping(value="/SOS/{id}/time/{sensorID}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getTimeForSensor(final @PathVariable("id") String id, final @PathVariable("sensorID") String sensorID) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().getTimeForSensorId(serviceId, sensorID), OK);
    }

    @RequestMapping(value="/SOS/{id}/observations", method = POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getDecimatedObservations(final @PathVariable("id") String id, final @RequestBody ObservationFilter filter) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().getDecimatedObservationsCsv(serviceId, filter.getSensorID(), filter.getObservedProperty(), filter.getFoi(), filter.getStart(), filter.getEnd(), filter.getWidth()), OK);
    }

    @RequestMapping(value="/SOS/{id}/observations/raw", method = POST, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getObservations(final @PathVariable("id") String id, final @RequestBody ObservationFilter filter) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().getObservationsCsv(serviceId, filter.getSensorID(), filter.getObservedProperty(), filter.getFoi(), filter.getStart(), filter.getEnd()), OK);
    }

    @RequestMapping(value="/SOS/{id}/observations", method = PUT, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity importObservation(final @PathVariable("id") String id, final File obs) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().importObservations(serviceId, obs.toPath()), OK);
    }

    @RequestMapping(value="/SOS/{id}/observation/{observationID}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity removeObservation(final @PathVariable("id") String id, final @PathVariable("observationID") String observationID) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().removeSingleObservation(serviceId, observationID), OK);
    }

    @RequestMapping(value="/SOS/{id}/observation/procedure/{procedureID}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity removeObservationForProcedure(final @PathVariable("id") String id, final @PathVariable("procedureID") String procedureID) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().removeObservationForProcedure(serviceId, procedureID), OK);
    }

    @RequestMapping(value="/SOS/{id}/observedProperties/identifiers", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getObservedPropertiesIds(final @PathVariable("id") String id) throws Exception {
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        return new ResponseEntity(getConfigurer().getObservedPropertiesIds(serviceId), OK);
    }

    @RequestMapping(value="/SOS/{id}/data/{dataID}", method = PUT, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity importSensorFromData(final @PathVariable("id") String id, final @PathVariable("dataID") Integer dataID) throws Exception {
        final Integer providerId = dataBusiness.getDataProvider(dataID);
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);

        if (providerId != null) {

            final DataProvider provider = DataProviders.getProvider(providerId);
            final ObservationStore store = SOSUtils.getObservationStore(provider);
            final ExtractionResult result;
            if (store != null) {
                result = store.getResults();
            } else {
                return new ResponseEntity(new AcknowlegementType("Failure", "Available only on Observation provider (and netCDF coverage) for now"), OK);
            }

            final SOSConfigurer configurer = getConfigurer();

            // import in O&M database
            configurer.importObservations(serviceId, result.observations, result.phenomenons);

            // SensorML generation
            for (ProcedureTree process : result.procedures) {
                generateSensorML(serviceId, process, result, configurer, null);
            }

            return new ResponseEntity(new AcknowlegementType("Success", "The specified observations have been imported in the SOS"), OK);
        } else {
            return new ResponseEntity(new AcknowlegementType("Failure", "The specified data does not exist"), OK);
        }
    }

    @RequestMapping(value="/SOS/{id}/data/{dataID}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity removeDataFromSOS(final @PathVariable("id") String id, final @PathVariable("dataID") Integer dataID) throws Exception {
        final Integer providerId = dataBusiness.getDataProvider(dataID);
        final Integer serviceId  = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);

        if (providerId != null) {
            final DataProvider provider = DataProviders.getProvider(providerId);
            final ObservationStore store = SOSUtils.getObservationStore(provider);
            final ExtractionResult result;
            if (store != null) {
                result = store.getResults();
            } else {
                return new ResponseEntity(new AcknowlegementType("Failure", "Available only on Observation provider (and netCDF coverage) for now"), OK);
            }

            final SOSConfigurer configurer = getConfigurer();

            // remove from O&M database
            for (Observation obs : result.observations) {
                if (obs.getName() != null) {
                    configurer.removeSingleObservation(serviceId, obs.getName().getCode());
                }
            }

            return new ResponseEntity(new AcknowlegementType("Success", "The specified observations have been removed from the SOS"), OK);
        } else {
            return new ResponseEntity(new AcknowlegementType("Failure", "The specified data does not exist"), OK);
        }
    }


    private void generateSensorML(final Integer serviceId, final ProcedureTree process, final ExtractionResult result, final SOSConfigurer configurer, String parentID) throws ConfigurationException {
        final Properties prop = new Properties();
        prop.put("id",         process.id);
        if (process.spatialBound.dateStart != null) {
            prop.put("beginTime",  process.spatialBound.dateStart);
        }
        if (process.spatialBound.dateEnd != null) {
            prop.put("endTime",    process.spatialBound.dateEnd);
        }
        if (process.spatialBound.minx != null) {
            prop.put("longitude",  process.spatialBound.minx);
        }
        if (process.spatialBound.miny != null) {
            prop.put("latitude",   process.spatialBound.miny);
        }
        prop.put("phenomenon", result.fields);

        final List<String> component = new ArrayList<>();
        for (ProcedureTree child : process.children) {
            component.add(child.id);
        }
        prop.put("component", component);

        final AbstractSensorML sml = SensorMLGenerator.getTemplateSensorML(prop, process.type);

        sensorBusiness.create(process.id, process.type, parentID, sml, System.currentTimeMillis(), getSensorProviderId(serviceId));

        for (ProcedureTree child : process.children) {
            generateSensorML(serviceId, child, result, configurer, process.id);
        }

        //record location
        final AbstractGeometryType geom = (AbstractGeometryType) process.spatialBound.getGeometry("2.0.0");
        if (geom != null) {
            configurer.updateSensorLocation(serviceId, process.id, geom);
        }
    }

    private void writeProcedures(final Integer id, final ProcedureTree process, final String parent, final SOSConfigurer configurer) throws ConfigurationException {
        final AbstractGeometryType geom = (AbstractGeometryType) process.spatialBound.getGeometry("2.0.0");
        configurer.writeProcedure(id, process.id, geom, parent, process.type);
        for (ProcedureTree child : process.children) {
            writeProcedures(id, child, process.id, configurer);
        }
    }

    @RequestMapping(value="/SOS/{id}/sensor/import/{sensorID}", method = PUT, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity importSensor(final @PathVariable("id") String id, final @PathVariable("sensorID") String sensorID) throws Exception {
        final Integer sid                 = serviceBusiness.getServiceIdByIdentifierAndType("SOS", id);
        final Sensor sensor               = sensorBusiness.getSensor(sensorID);
        final List<Sensor> sensorChildren = sensorBusiness.getChildren(sensor.getIdentifier());
        final List<Integer> dataProviders = sensorBusiness.getLinkedDataProviderIds(sensor.getId());
        final SOSConfigurer configurer    = getConfigurer();
        final List<String> sensorIds      = new ArrayList<>();

        sensorBusiness.addSensorToService(sid, sensor.getId());
        sensorIds.add(sensorID);

        //import sensor children
        for (Sensor child : sensorChildren) {
            dataProviders.addAll(sensorBusiness.getLinkedDataProviderIds(child.getId()));
            sensorBusiness.addSensorToService(sid, child.getId());
            sensorIds.add(child.getIdentifier());
        }

        // look for provider ids (remove doublon)
        final Set<Integer> providerIDs = new HashSet<>();
        for (Integer dataProvider : dataProviders) {
            providerIDs.add(dataProvider);
        }

        // import observations
        for (Integer providerId : providerIDs) {
            final DataProvider provider = DataProviders.getProvider(providerId);
            if (provider instanceof ObservationProvider) {
                final ObservationStore store = (ObservationStore) ((ObservationProvider) provider).getMainStore();
                final ExtractionResult result;
                if (store != null) {
                    result = store.getResults(sensorID, sensorIds);
                } else {
                    return new ResponseEntity(new AcknowlegementType("Failure", "Available only on Observation provider (and netCDF coverage) for now"), OK);
                }

                // update sensor location
                for (ProcedureTree process : result.procedures) {
                    writeProcedures(sid, process, null, configurer);
                }

                // import in O&M database
                configurer.importObservations(sid, result.observations, result.phenomenons);
            } else {
                return new ResponseEntity(new AcknowlegementType("Failure", "Available only on Observation provider (and netCDF coverage) for now"), OK);
            }
        }


        return new ResponseEntity(new AcknowlegementType("Success", "The specified sensor has been imported in the SOS"), OK);
    }

    private Integer getSensorProviderId(final Integer serviceID) throws ConfigurationException {
        final List<Integer> providers = serviceBusiness.getLinkedProviders(serviceID);
        for (Integer providerID : providers) {
            final DataProvider p = DataProviders.getProvider(providerID);
            if(p.getMainStore() instanceof SensorStore){
                // TODO for now we only take one provider by type
                return providerID;
            }
        }
        throw new ConfigurationException("there is no sensor provider linked to this ID:" + serviceID);
    }

    private SOSConfigurer getConfigurer() throws NotRunningServiceException {
        return (SOSConfigurer) wsengine.newInstance(ServiceDef.Specification.SOS);
    }
}
