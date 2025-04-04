/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2019 Geomatys.
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
package com.examind.sensor.component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.business.ISensorBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.dto.Sensor;
import org.constellation.dto.service.config.sos.SensorMLTree;
import org.constellation.exception.ConfigurationException;
import org.constellation.exception.ConstellationStoreException;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ObservationProvider;
import org.constellation.provider.SensorProvider;
import com.examind.sensor.ws.SensorUtils;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.sis.util.collection.BackingStoreException;
import static org.constellation.api.CommonConstants.OBSERVATION_QNAME;
import org.constellation.business.IDataBusiness;
import org.constellation.business.ISensorServiceBusiness;
import org.constellation.dto.service.Service;
import org.geotoolkit.observation.model.ObservationDataset;
import org.geotoolkit.observation.model.ProcedureDataset;
import org.constellation.dto.service.config.sos.SOSConfiguration;
import org.constellation.exception.ConstellationException;
import org.constellation.exception.TargetNotFoundException;
import org.constellation.provider.SensorData;
import org.constellation.util.NamedId;
import org.geotoolkit.filter.FilterUtilities;
import org.geotoolkit.gml.xml.v321.TimeInstantType;
import org.geotoolkit.gml.xml.v321.TimePeriodType;
import org.geotoolkit.nio.ZipUtilities;
import org.geotoolkit.observation.model.ComplexResult;
import static org.geotoolkit.observation.model.ResponseMode.INLINE;
import org.geotoolkit.observation.query.DatasetQuery;
import org.geotoolkit.observation.query.ObservedPropertyQuery;
import org.geotoolkit.observation.query.ProcedureQuery;
import org.geotoolkit.observation.query.ResultQuery;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLUtilities;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.geotoolkit.observation.model.Observation;
import org.geotoolkit.observation.model.Phenomenon;
import org.opengis.temporal.TemporalPrimitive;
import org.opengis.util.GenericName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Component("sensorServiceBusiness")
public class SensorServiceBusiness implements ISensorServiceBusiness {

    private static final Logger LOGGER = Logger.getLogger("com.examind.sensor.component");

    @Autowired
    protected ISensorBusiness sensorBusiness;

    @Autowired
    protected IServiceBusiness serviceBusiness;

    @Autowired
    protected IDataBusiness dataBusiness;

    protected final FilterFactory ff = FilterUtilities.FF;

    @Override
    public boolean importSensor(final Integer serviceID, final Path sensorFile, final String type) throws ConfigurationException {
        LOGGER.info("Importing sensor");

        final List<Path> files;
        switch (type) {
            case "zip" -> {
                try  {
                    files = ZipUtilities.unzip(sensorFile, null);
                } catch (IOException ex) {
                    throw new ConfigurationException(ex);
                }
            }
            case "xml" -> files = Arrays.asList(sensorFile);
            default    -> throw new ConfigurationException("Unexpected file extension, accepting zip or xml");
        }

        try {
            int smlProviderId = getSensorProviderId(serviceID, SensorProvider.class);
            for (Path importedFile: files) {
                if (importedFile != null) {
                    final Object sensor = sensorBusiness.unmarshallSensor(importedFile);
                    if (sensor instanceof AbstractSensorML sml) {
                        final String sensorID             = SensorMLUtilities.getSmlID(sml);
                        final String smlType              = SensorMLUtilities.getSensorMLType(sml);
                        final String omType               = SensorMLUtilities.getOMType(sml).orElse(null);
                        final String name                 = SensorMLUtilities.getSmlName(sml).orElse(sensorID);
                        final String description          = SensorMLUtilities.getSmlDescription(sml).orElse(null);
                        final List<SensorMLTree> children = SensorUtils.getChildren(sml);
                        sensorBusiness.create(sensorID, name, description, smlType, omType, null, sensor, System.currentTimeMillis(), smlProviderId);
                        for (SensorMLTree child : children) {
                            importSensorChild(child, sensorID, smlProviderId);
                        }
                    } else {
                        throw new ConfigurationException("Only handle SensorML for now");
                    }
                } else {
                    throw new ConfigurationException("An imported file is null");
                }
            }
            return true;
        } catch (ConstellationException ex) {
            LOGGER.log(Level.WARNING, "Exception while unmarshalling imported file", ex);
        }
        return false;
    }

    private void importSensorChild(SensorMLTree sensor, String parentId, int providerId) throws ConfigurationException {
        sensorBusiness.create(sensor.getIdentifier(), sensor.getName(), sensor.getDescription(), sensor.getType(), null, parentId, sensor.getSml(), System.currentTimeMillis(), providerId);
        for (SensorMLTree child : sensor.getChildren()) {
            importSensorChild(child, sensor.getIdentifier(), providerId);
        }
    }

    @Override
    public boolean removeSensor(final Integer id, final String sensorID) throws ConstellationException {
        final ObservationProvider pr = getSensorProvider(id, ObservationProvider.class);
        try {
            final SensorMLTree tree = sensorBusiness.getSensorMLTree(sensorID);

            // for a System sensor, we delete also his components
            final List<NamedId> toRemove = new ArrayList<>();
            if (tree != null) {
                toRemove.addAll(tree.getAllChildrenNamedIds());
            } else {
                // tree should no be null
                toRemove.add(new NamedId(-1, sensorID));
            }
            final String datasourceKey = pr.getDatasourceKey();
            for (NamedId sid : toRemove) {
                sensorBusiness.removeSensorFromService(id, sid.getId());
               /*
                * if another service, using the SAME datasource, use this sensor, we don't remove it from the datasource
                */
                boolean rmFromDatasource = true;
                List<Integer> serviceIds = sensorBusiness.getLinkedServiceIds(sid.getId());
                for (Integer serviceId : serviceIds) {
                    ObservationProvider servProv  = getSensorProvider(serviceId, ObservationProvider.class);
                    if (servProv.getDatasourceKey().equals(datasourceKey)) {
                        rmFromDatasource = false;
                        break;
                    }
                }
                if (rmFromDatasource) {
                    pr.removeProcedure(sid.getIdentifier());
                }
            }

            // if the sensor has a System parent, we must update his component list
            if (tree != null && tree.getParent() != null) {
                final String parentID = tree.getParent().getIdentifier();
                if (!"root".equals(parentID)) {
                    try {
                        final AbstractSensorML sml = (AbstractSensorML) sensorBusiness.getSensorMetadata(parentID);
                        SensorUtils.removeComponent(sml, sensorID);
                        sensorBusiness.updateSensorMetadata(parentID, sml);
                    } catch (Exception ex) {
                        LOGGER.warning("Unable to read/update parent sensor metadata");
                    }
                }
            }
            return true;
        } catch (ConstellationStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public boolean removeAllSensors(final Integer id) throws ConfigurationException {
        final ObservationProvider pr = getSensorProvider(id, ObservationProvider.class);
        try {
            final Collection<Sensor> sensors = sensorBusiness.getByServiceId(id);
            for (Sensor sensor : sensors) {
                sensorBusiness.removeSensorFromService(id, sensor.getId());
                pr.removeProcedure(sensor.getIdentifier());
            }
            serviceBusiness.restart(id);
            return true;
        } catch (ConstellationStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public Collection<String> getSensorIds(final Integer id) throws ConfigurationException {
        if (isDirectProviderMode(id)) {
            final ObservationProvider pr = getSensorProvider(id, ObservationProvider.class);
            try {
                return pr.getIdentifiers(new ProcedureQuery());
            } catch (ConstellationStoreException ex) {
                throw new ConfigurationException(ex);
            }
        } else {
            return sensorBusiness.getLinkedSensorIdentifiers(id, null);
        }
    }

    @Override
    public long getSensorCount(final Integer id) throws ConfigurationException {
        if (isDirectProviderMode(id)) {
            final ObservationProvider pr = getSensorProvider(id, ObservationProvider.class);
            try {
                return pr.getCount(new ProcedureQuery());
            } catch (ConstellationStoreException ex) {
                throw new ConfigurationException(ex);
            }
        } else {
            return sensorBusiness.getCountByServiceId(id);
        }
    }

    @Override
    public Collection<String> getObservedPropertiesForSensorId(final Integer id, final String sensorID, final boolean decompose) throws ConfigurationException {
        final ObservationProvider pr = getSensorProvider(id, ObservationProvider.class);
        try {
            final SensorMLTree current;
            if (isDirectProviderMode(id)) {
                final SensorProvider sp = getSensorProvider(id, SensorProvider.class);
                SensorData sd = (SensorData) sp.get(null, sensorID);
                Sensor s = SensorUtils.getSensorFromData(sd, null);
                current = new SensorMLTree(s);
            } else {
                current = sensorBusiness.getSensorMLTree(sensorID);
            }
            return SensorUtils.getPhenomenonFromSensor(current, pr, decompose);

        } catch (ConstellationStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public TemporalPrimitive getTimeForSensorId(final Integer id, final String sensorID) throws ConfigurationException {
        final ObservationProvider pr = getSensorProvider(id, ObservationProvider.class);
        try {
            return pr.getTimeForProcedure(sensorID);
        } catch (ConstellationStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }

    /**
     * Return the observation provider linked to the specified data.
     *
     * @param dataID Data identifier.
     *
     * @return An observation provider.
     *
     * @throws ConfigurationException If the data does not exist, or if the data provider is not an observation one.
     */
    protected ObservationProvider getDataObservationProvider(Integer dataID) throws ConfigurationException {
        final Integer providerId = dataBusiness.getDataProvider(dataID);
        if (providerId == null) throw new TargetNotFoundException("The specified data does not exist");
        final DataProvider dataProvider = DataProviders.getProvider(providerId);
        if (dataProvider instanceof ObservationProvider omProvider) return omProvider;
        throw new ConfigurationException("data provider is not an observation store.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDataObservationsFromServices(final Integer dataID) throws ConstellationException {
        final ObservationProvider omProvider = getDataObservationProvider(dataID);
        List<Service> services = serviceBusiness.getDataLinkedSensorServices(dataID);
        removeDataObservationsFromServices(services, omProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDataObservationsFromService(final Integer sid, final Integer dataID) throws ConstellationException {
        final ObservationProvider omProvider = getDataObservationProvider(dataID);
        final Integer serviceProviderId = getSensorProviderId(sid, ObservationProvider.class);
        List<Service> services = serviceBusiness.getProviderLinkedSensorServices(serviceProviderId);
        removeDataObservationsFromServices(services, omProvider);
    }

    private void removeDataObservationsFromServices(final List<Service> services, final ObservationProvider omProvider) throws ConstellationException {
        final ObservationDataset dataset = omProvider.extractResults(new DatasetQuery());
        // multiple services can use the same provider.
        // so we list the providers linked to the service and then remove the data from them
        Map<String, List<String>> removedSensors = new HashMap<>();
        for (Service service : services) {
            final ObservationProvider serviceProvider = getSensorProvider(service.getId(), ObservationProvider.class);

            // 1. perform the removal on the provider
            List<String> removedSensor;
            if (!removedSensors.containsKey(serviceProvider.getId())) {
                removedSensor = serviceProvider.removeDataset(dataset);
                removedSensors.put(serviceProvider.getId(), removedSensor);
            } else {
                removedSensor = removedSensors.get(serviceProvider.getId());
            }

            // 2; unlink the sensors from the service in database
            for (String sensorId : removedSensor) {
                Sensor s = sensorBusiness.getSensor(sensorId);
                sensorBusiness.removeSensorFromService(service.getId(), s.getId());
            }
        }
    }

    @Override
    public void importObservationsFromData(final Integer sid, final Integer dataID) throws ConstellationException {
        final ObservationProvider omProvider = getDataObservationProvider(dataID);
        final Integer smlId = getSensorProviderId(sid, SensorProvider.class);
        final ObservationDataset result = omProvider.extractResults(new DatasetQuery());

        // import in O&M database
        importObservations(sid, result.observations, result.phenomenons);

        // SensorML generation
        for (ProcedureDataset process : result.procedures) {
            sensorBusiness.generateSensor(process, smlId, null, dataID);
            updateSensorLocation(sid, process);
        }
    }

    private void updateSensorLocation(final Integer serviceId, final ProcedureDataset sensor) throws ConstellationException {
        final ObservationProvider pr = getSensorProvider(serviceId, ObservationProvider.class);
        //record location
        final Geometry location = sensor.spatialBound.getLastGeometry();;
        if (location != null) {
            pr.writeLocation(sensor.getId(), location);
        }
        for (ProcedureDataset child : sensor.children) {
            updateSensorLocation(serviceId, child);
        }
    }

    @Override
    public void importObservations(final Integer id, final List<Observation> observations, final List<Phenomenon> phenomenons) throws ConfigurationException {
        final ObservationProvider writer = getSensorProvider(id, ObservationProvider.class);
        try {
            final long start = System.currentTimeMillis();
            writer.writePhenomenons(phenomenons);
            for (Observation obs : observations) {
                writer.writeObservation(obs);
            }
            LOGGER.log(Level.INFO, "observations imported in :{0} ms", (System.currentTimeMillis() - start));
        } catch (ConstellationStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public Collection<String> getObservedPropertiesIds(Integer id) throws ConfigurationException {
        final ObservationProvider pr = getSensorProvider(id, ObservationProvider.class);
        try {
            // TODO this results are not filtered on linked sensors
            return pr.getIdentifiers(new ObservedPropertyQuery());
        } catch (ConstellationStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public void writeProcedure(final Integer id, final ProcedureDataset procedure) throws ConfigurationException {
        final ObservationProvider pr = getSensorProvider(id, ObservationProvider.class);
        try {
            pr.writeProcedure(procedure);
        } catch (ConstellationStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public String getWKTSensorLocation(final Integer id, final String sensorID) throws ConfigurationException {
        final ObservationProvider provider = getSensorProvider(id, ObservationProvider.class);
        try {
            final SensorMLTree current;
            if (isDirectProviderMode(id)) {
                final SensorProvider sp = getSensorProvider(id, SensorProvider.class);
                SensorData sd = (SensorData) sp.get(null, sensorID);
                Sensor s = SensorUtils.getSensorFromData(sd, null);
                current = new SensorMLTree(s);
            } else {
                current = sensorBusiness.getSensorMLTree(sensorID);
            }
            if (current != null) {
                final List<Geometry> jtsGeometries = provider.getJTSGeometryFromSensor(current);
                if (jtsGeometries.size() == 1) {
                    final WKTWriter writer = new WKTWriter();
                    return writer.write(jtsGeometries.get(0));
                } else if (!jtsGeometries.isEmpty()) {
                    final Geometry[] geometries   = jtsGeometries.toArray(Geometry[]::new);
                    final GeometryCollection coll = new GeometryCollection(geometries, new GeometryFactory());
                    final WKTWriter writer        = new WKTWriter();
                    return writer.write(coll);
                }
            }
            return "";
        } catch (ConstellationStoreException | BackingStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }

    @Override
    public Object getResultsCsv(final Integer id, final String sensorID, final List<String> observedProperties, final List<String> foi, final Date start, final Date end, final Integer width, final String resultFormat, final boolean timeforProfile, final boolean includeIdInDatablock) throws ConfigurationException {
        try {
            final ObservationProvider pr = getSensorProvider(id, ObservationProvider.class);
            ResultQuery query = new ResultQuery(OBSERVATION_QNAME, INLINE, sensorID, resultFormat);
            query.setIncludeIdInDataBlock(includeIdInDatablock);
            query.setIncludeTimeForProfile(timeforProfile);
            query.setSelection(buildFilter(start, end, observedProperties, foi));
            if (width != null) {
                query.setDecimationSize(width);
            }
            Object result = pr.getResults(query);
            if (result instanceof ComplexResult cr) {
                if (cr.getDataArray() != null) {
                    return cr.getDataArray();
                } else {
                    return cr.getValues();
                }
            }
            // keep legacy behavor until geotk interface will return "Result" type.
            return result;
        } catch (ConstellationStoreException ex) {
            throw new ConfigurationException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getSensorMetadata(final Integer id, final String sensorID) throws ConstellationException {
        if (isDirectProviderMode(id)) {
            SensorProvider sp = getSensorProvider(id, SensorProvider.class);
            SensorData sd = (SensorData) sp.get(null, sensorID);
            return sd.getSensorMetadata();
        } else {
            return sensorBusiness.getSensorMetadata(sensorID);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateSensorFromOMProvider(Integer id) throws ConstellationException {
        ObservationProvider omProvider = getSensorProvider(id, ObservationProvider.class);
        Integer smlPid = getSensorProviderId(id, SensorProvider.class);
        final List<ProcedureDataset> procedures = omProvider.getProcedureTrees(new DatasetQuery());
        for (ProcedureDataset process : procedures) {
            // we should not write over the procedured as we are generating sensor from them.
            // I let this in comment in case i'm missing something i can't see for now
            //writeProcedure(id, process);
            Integer sid =  sensorBusiness.generateSensor(process, smlPid, null, null);
            sensorBusiness.addSensorToService(id, sid);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importSensor(Integer id, String sensorID) throws ConstellationException {
        final Sensor sensor               = sensorBusiness.getSensor(sensorID);
        if (sensor == null) throw new TargetNotFoundException("unable to fina a sensor with id: " + sensorID);
        final List<Sensor> sensorChildren = sensorBusiness.getChildren(sensor.getId());
        final Collection<String> previous = getSensorIds(id);
        final List<String> sensorIds      = new ArrayList<>();
        final List<Integer> dataProviders = new ArrayList<>();

        // hack for not importing already inserted sensor. must be reviewed when the SOS/STS service will share an O&M dataProvider
        if (!previous.contains(sensorID)) {
            dataProviders.addAll(sensorBusiness.getLinkedDataProviderIds(sensor.getId()));
        }

        sensorBusiness.addSensorToService(id, sensor.getId());
        sensorIds.add(sensorID);

        //import sensor children
        for (Sensor child : sensorChildren) {
            // hack for not importing already inserted sensor. must be reviewed when the SOS/STS service will share an O&M dataProvider
            if (!previous.contains(child.getIdentifier())) {
                dataProviders.addAll(sensorBusiness.getLinkedDataProviderIds(child.getId()));
            }
            sensorBusiness.addSensorToService(id, child.getId());
            sensorIds.add(child.getIdentifier());
        }

        // look for dataProvider ids (remove doublon)
        final Set<Integer> providerIDs = new HashSet<>();
        for (Integer dataProvider : dataProviders) {
            providerIDs.add(dataProvider);
        }

        // import observations
        for (Integer providerId : providerIDs) {
            final DataProvider provider = DataProviders.getProvider(providerId);
            if (provider instanceof ObservationProvider omProvider) {
                final ObservationDataset result = omProvider.extractResults(new DatasetQuery(sensorIds));

                // update sensor location
                for (ProcedureDataset process : result.procedures) {
                    writeProcedure(id, process);
                }

                // import in O&M database
                importObservations(id, result.observations, result.phenomenons);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SensorMLTree getServiceSensorMLTree(Integer id) throws ConstellationException {
        if (isDirectProviderMode(id)) {
            try {
                SensorProvider sp = getSensorProvider(id, SensorProvider.class);
                Integer sid       = getSensorProviderId(id, SensorProvider.class);
                List<SensorMLTree> ss = sp.getKeys().stream()
                                    .map(gn -> getData(sp, gn))
                                    .map(sd -> SensorUtils.getSensorFromData(sd, sid))
                                    .map(s -> new SensorMLTree(s))
                                    .collect(Collectors.toList());
                return SensorMLTree.buildTree(ss, true);
            } catch (BackingStoreException ex) {
                throw (ConstellationException) ex.getCause();
            }
        } else {
            return sensorBusiness.getServiceSensorMLTree(id);
        }
    }

    /**
     * Only here to use the SensorProvider#get in a stream.
     * Change the ConstellationStoreException into a BackingStoreException.
     */
    private static SensorData getData(SensorProvider sp, GenericName gn) {
        try {
            return (SensorData)sp.get(gn);
        } catch (ConstellationStoreException ex) {
            throw new BackingStoreException("Unable to get sensor data:" + gn, ex);
        }
    }


    private Filter buildFilter(final Date start, final Date end, List<String> observedProperties, List<String> featuresOfInterest)  {
        final List<Filter> filters = new ArrayList<>();
        if (start != null && end != null) {
            final var period = new TimePeriodType(null, new Timestamp(start.getTime()).toInstant(), new Timestamp(end.getTime()).toInstant());
            filters.add(ff.during(ff.property("resultTime"), ff.literal(period)));
        } else if (start != null) {
            final var time = new TimeInstantType(new Timestamp(start.getTime()).toInstant());
            filters.add(ff.after(ff.property("resultTime"), ff.literal(time)));
        } else if (end != null) {
            final var time = new TimeInstantType(new Timestamp(end.getTime()).toInstant());
            filters.add(ff.before(ff.property("resultTime"), ff.literal(time)));
        }

        if (observedProperties != null) {
            for (String observedProperty : observedProperties) {
                filters.add(ff.equal(ff.property("observedProperty"), ff.literal(observedProperty)));
            }
        }
        if (featuresOfInterest != null) {
            for (String featureOfInterest : featuresOfInterest) {
                filters.add(ff.equal(ff.property("featureOfInterest"), ff.literal(featureOfInterest)));
            }
        }
        if (filters.size() == 1) {
            return filters.get(0);
        } else if (filters.size() > 1) {
            return ff.and(filters);
        } else {
            return Filter.include();
        }
    }

    private <T> Integer getSensorProviderId(final Integer serviceID, Class<T> c) throws ConfigurationException {
        final List<Integer> providers = serviceBusiness.getLinkedProviders(serviceID);
        for (Integer providerID : providers) {
            final DataProvider p = DataProviders.getProvider(providerID);
            if (c.isInstance(p)) {
                // TODO for now we only take one dataProvider by type
                return providerID;
            }
        }
        throw new ConfigurationException("there is no sensor provider linked to this ID:" + serviceID);
    }


    private <T> T getSensorProvider(final Integer serviceID, Class<T> c) throws ConfigurationException {
        final List<Integer> providers = serviceBusiness.getLinkedProviders(serviceID);
        for (Integer providerID : providers) {
            final DataProvider p = DataProviders.getProvider(providerID);
            if(c.isInstance(p)){
                // TODO for now we only take one dataProvider by type
                return (T) p;
            }
        }
        throw new ConfigurationException("there is no OM provider linked to this ID:" + serviceID);
    }

    private boolean isDirectProviderMode(final Integer id) throws ConfigurationException {
        final SOSConfiguration conf = getServiceConfiguration(id);
        return conf.getBooleanParameter("directProvider", false);
    }

    private SOSConfiguration getServiceConfiguration(final Integer id) throws ConfigurationException {
        try {
            // we get the CSW configuration file
            return (SOSConfiguration) serviceBusiness.getConfiguration(id);
        } catch (Exception ex) {
            throw new ConfigurationException("Error while getting the Sensor service configuration for:" + id, ex);
        }
    }
}
