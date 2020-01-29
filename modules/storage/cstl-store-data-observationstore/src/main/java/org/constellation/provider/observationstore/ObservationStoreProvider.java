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
package org.constellation.provider.observationstore;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import org.apache.sis.internal.storage.ResourceOnFileSystem;
import org.apache.sis.internal.storage.query.SimpleQuery;
import org.apache.sis.internal.system.DefaultFactories;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.Query;
import org.apache.sis.storage.Resource;
import org.apache.sis.util.ArraysExt;
import org.constellation.api.CommonConstants;
import static org.constellation.api.CommonConstants.MEASUREMENT_QNAME;
import static org.constellation.api.CommonConstants.OBSERVATION_MODEL;
import static org.constellation.api.CommonConstants.OBSERVATION_QNAME;
import org.constellation.api.DataType;
import org.constellation.dto.service.config.sos.Offering;
import org.constellation.dto.service.config.sos.ProcedureTree;
import org.constellation.dto.service.config.sos.SOSProviderCapabilities;
import org.constellation.exception.ConstellationException;
import org.constellation.exception.ConstellationStoreException;
import org.constellation.provider.AbstractDataProvider;
import static org.constellation.provider.AbstractDataProvider.getLogger;
import org.constellation.provider.Data;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.ObservationProvider;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.v321.TimeInstantType;
import org.geotoolkit.gml.xml.v321.TimePeriodType;
import org.geotoolkit.observation.ObservationFilterReader;
import org.geotoolkit.observation.ObservationReader;
import org.geotoolkit.observation.ObservationStore;
import org.geotoolkit.sos.netcdf.ExtractionResult;
import org.geotoolkit.sos.netcdf.GeoSpatialBound;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.ResponseModeType;
import static org.geotoolkit.sos.xml.ResponseModeType.INLINE;
import static org.geotoolkit.sos.xml.ResponseModeType.RESULT_TEMPLATE;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildOffering;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildTimePeriod;
import org.geotoolkit.storage.DataStores;
import org.geotoolkit.storage.ResourceType;
import org.geotoolkit.swe.xml.PhenomenonProperty;
import org.opengis.filter.Id;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.temporal.After;
import org.opengis.filter.temporal.Before;
import org.opengis.filter.temporal.Begins;
import org.opengis.filter.temporal.BegunBy;
import org.opengis.filter.temporal.During;
import org.opengis.filter.temporal.EndedBy;
import org.opengis.filter.temporal.Ends;
import org.opengis.filter.temporal.Meets;
import org.opengis.filter.temporal.OverlappedBy;
import org.opengis.filter.temporal.TContains;
import org.opengis.filter.temporal.TEquals;
import org.opengis.filter.temporal.TOverlaps;
import org.opengis.geometry.Geometry;
import org.opengis.observation.Observation;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.Process;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalGeometricPrimitive;
import org.opengis.util.GenericName;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ObservationStoreProvider extends AbstractDataProvider implements ObservationProvider {

    private final Set<GenericName> index = new LinkedHashSet<>();
    private ObservationStore store;

    private static final int GET_OBS  = 0;
    private static final int GET_FEA  = 1;
    private static final int GET_PHEN = 2;
    private static final int GET_PROC = 3;
    private static final int GET_OFF  = 4;

    public ObservationStoreProvider(String providerId, DataProviderFactory service, ParameterValueGroup param) throws DataStoreException{
        super(providerId,service,param);
        visit();
    }

    @Override
    public Set<GenericName> getKeys() {
        return Collections.unmodifiableSet(index);
    }

    @Override
    public Data get(GenericName key) {
        return get(key, null);
    }

    @Override
    public DataType getDataType() {
        final org.apache.sis.storage.DataStoreProvider provider = getMainStore().getProvider();
        final ResourceType[] resourceTypes = DataStores.getResourceTypes(provider);
        if (ArraysExt.contains(resourceTypes, ResourceType.COVERAGE)
             || ArraysExt.contains(resourceTypes, ResourceType.GRID)
             || ArraysExt.contains(resourceTypes, ResourceType.PYRAMID)) {
            return DataType.COVERAGE;
        } else if (ArraysExt.contains(resourceTypes, ResourceType.VECTOR)) {
            return DataType.VECTOR;
        } else if (ArraysExt.contains(resourceTypes, ResourceType.SENSOR)) {
            return DataType.SENSOR;
        } else if (ArraysExt.contains(resourceTypes, ResourceType.METADATA)) {
            return DataType.METADATA;
        } else {
            return DataType.VECTOR; // unknown
        }
    }

    @Override
    public synchronized DataStore getMainStore() {
         if(store==null){
            store = createBaseStore();
        }
        return (DataStore) store;
    }

    @Override
    public Data get(GenericName key, Date version) {
         key = fullyQualified(key);
        if(!contains(key)){
            return null;
        }

        return new DefaultObservationData(key, store);
    }

    @Override
    public Path[] getFiles() throws ConstellationException {
        DataStore currentStore = (DataStore) store;
        if (!(currentStore instanceof ResourceOnFileSystem)) {
            throw new ConstellationException("Store is not made of files.");
        }

        final ResourceOnFileSystem fileStore = (ResourceOnFileSystem)currentStore;
        try {
            return fileStore.getComponentFiles();
        } catch (DataStoreException ex) {
            throw new ConstellationException(ex);
        }
    }

    @Override
    public DefaultMetadata getStoreMetadata() throws ConstellationStoreException {
        return null;
    }

    @Override
    public String getCRSName() throws ConstellationStoreException {
        return null;
    }

    protected synchronized void visit() {
        store = createBaseStore();

        try {

            for (final Resource rs : DataStores.flatten((DataStore)store, true)) {
                Optional<GenericName> name = rs.getIdentifier();
                if (name.isPresent()) {
                    if (rs instanceof FeatureSet) {
                        if (!index.contains(name.get())) {
                            index.add(name.get());
                        }
                    }
                }
            }

        } catch (DataStoreException ex) {
            //Looks like we failed to retrieve the list of featuretypes,
            //the layers won't be indexed and the getCapability
            //won't be able to find thoses layers.
            getLogger().log(Level.SEVERE, "Failed to retrive list of available feature types.", ex);
        }
    }

    protected ObservationStore createBaseStore() {
        //parameter is a choice of different types
        //extract the first one
        ParameterValueGroup param = getSource();
        param = param.groups("choice").get(0);
        ParameterValueGroup factoryconfig = null;
        for(GeneralParameterValue val : param.values()){
            if(val instanceof ParameterValueGroup){
                factoryconfig = (ParameterValueGroup) val;
                break;
            }
        }

        if(factoryconfig == null){
            getLogger().log(Level.WARNING, "No configuration for observation store source.");
            return null;
        }
        try {
            //create the store
            org.apache.sis.storage.DataStoreProvider provider = DataStores.getProviderById(factoryconfig.getDescriptor().getName().getCode());
            org.apache.sis.storage.DataStore tmpStore = provider.open(factoryconfig);
            if (tmpStore == null) {
                throw new DataStoreException("Could not create observation store for parameters : "+factoryconfig);
            } else if (!(tmpStore instanceof ObservationStore)) {
                throw new DataStoreException("Could not create observation store for parameters : "+factoryconfig + " (not a observation store)");
            }
            return (ObservationStore) tmpStore;
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public List<ProcedureTree> getProcedures() throws ConstellationStoreException {
        List<ProcedureTree> results = new ArrayList<>();
        try {
            List<ExtractionResult.ProcedureTree> procedures = store.getProcedures();
            for (ExtractionResult.ProcedureTree pt : procedures) {
                results.add(toDto(pt));
            }
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
        return results;
    }

    private ProcedureTree toDto(ExtractionResult.ProcedureTree pt) {
        GeoSpatialBound bound = pt.spatialBound;
        ProcedureTree result  = new ProcedureTree(pt.id,
                                                  pt.type,
                                                  pt.omType,
                                                  bound.dateStart,
                                                  bound.dateEnd,
                                                  bound.minx,
                                                  bound.maxx,
                                                  bound.miny,
                                                  bound.maxy,
                                                  pt.fields);
        for (ExtractionResult.ProcedureTree child: pt.children) {
            result.getChildren().add(toDto(child));
        }
        return result;
    }

    @Override
    public boolean isSensorAffectable() {
        return true;
    }

    @Override
    public Collection<String> getPhenomenonNames(Query q, final Map<String,String> hints) throws ConstellationStoreException {
        try {
            final ObservationFilterReader localOmFilter = store.getFilter();
            localOmFilter.initFilterGetPhenomenon();
            handleQuery(q, localOmFilter, GET_PHEN, hints);
            return localOmFilter.filterPhenomenon();
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public List<Phenomenon> getPhenomenon(Query q, final Map<String,String> hints) throws ConstellationStoreException {
        try {
            // we clone the filter for this request
            final ObservationFilterReader localOmFilter = store.getFilter();
            localOmFilter.initFilterGetPhenomenon();
            handleQuery(q, localOmFilter, GET_PHEN, hints);
            return localOmFilter.getPhenomenons(hints);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public Collection<String> getProcedureNames(Query q, final Map<String,String> hints) throws ConstellationStoreException {
        try {
            final ObservationFilterReader localOmFilter = store.getFilter();
            localOmFilter.initFilterGetSensor();
            handleQuery(q, localOmFilter, GET_PROC, hints);
            return localOmFilter.filterProcedure();
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public Collection<String> getFeatureOfInterestNames(Query q, final Map<String,String> hints) throws ConstellationStoreException {
        try {
            final ObservationFilterReader localOmFilter = store.getFilter();
            localOmFilter.initFilterGetFeatureOfInterest();
            handleQuery(q, localOmFilter, GET_FEA, hints);
            return localOmFilter.filterFeatureOfInterest();
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public Collection<String> getOfferingNames(Query q, final Map<String,String> hints) throws ConstellationStoreException {
        try {
            final ObservationFilterReader localOmFilter = store.getFilter();
            localOmFilter.initFilterOffering();
            handleQuery(q, localOmFilter, GET_OFF, hints);
            return localOmFilter.filterOffering();
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public Collection<String> getObservationNames(Query q, QName resultModel, String responseMode, Map<String, String> hints) throws ConstellationStoreException {
        try {
            ResponseModeType mode;
            if (responseMode != null) {
                mode = ResponseModeType.fromValue(responseMode);
            } else {
                mode = ResponseModeType.INLINE;
            }
            final ObservationFilterReader localOmFilter = store.getFilter();
            localOmFilter.initFilterObservation(mode, resultModel, hints);
            handleQuery(q, localOmFilter, GET_OBS, hints);

            return localOmFilter.filterObservation();
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }



    @Override
    public SOSProviderCapabilities getCapabilities() throws ConstellationStoreException {
        SOSProviderCapabilities capa = new SOSProviderCapabilities();
        try {
            ObservationReader reader = store.getReader();
            capa.responseFormats = reader.getResponseFormats();
            final List<String> arm = new ArrayList<>();
            reader.getResponseModes().stream().forEach((rm) -> {
                arm.add(rm.value());
            });
            capa.responseModes = arm;
            ObservationFilterReader filter = store.getFilter();
            if (filter != null) {
                capa.queryableResultProperties = filter.supportedQueryableResultProperties();
            }
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
        return capa;
    }

    @Override
    public  AbstractGeometry getSensorLocation(String sensorID, String gmlVersion) throws ConstellationStoreException {
        try {
            return store.getReader().getSensorLocation(sensorID, gmlVersion);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public Offering getOffering(String name, String version) throws ConstellationStoreException {
        try {
            ObservationOffering off = store.getReader().getObservationOffering(name, version);
            if (off != null) {
                return new Offering(off.getId(),
                                    off.getDescription(),
                                    off.getSrsName(),
                                    off.getResultModel(),
                                    off.getProcedures(),
                                    off.getFeatureOfInterestIds(),
                                    off.getObservedProperties());
            }
            return null;
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public void updateOffering(Offering offering) throws ConstellationStoreException {
        try {
            if (offering != null) {
                String procedure = null;
                if (offering.getProcedures() != null && !offering.getProcedures().isEmpty()) {
                    procedure = offering.getProcedures().get(0);
                }
                String foi = null;
                if (offering.getFeatureOfInterest()!= null && !offering.getFeatureOfInterest().isEmpty()) {
                    foi = offering.getFeatureOfInterest().get(0);
                }
                store.getWriter().updateOffering(offering.getId(),
                                                 procedure,
                                                 offering.getObservedProperties(),
                                                 foi);
            }
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public void writeOffering(Offering offering,  List<? extends Object> observedProperties, List<String> smlFormats, String version) throws ConstellationStoreException {
        try {
            if (offering != null) {

                // for the eventime of the offering we take the time of now.
                final Timestamp t = new Timestamp(System.currentTimeMillis());
                final Period time = buildTimePeriod(version, null, t.toString(), null);

                // observed properties
                final List<String> obsPropsV100             = new ArrayList<>();
                final List<PhenomenonProperty> obsPropsV200 = new ArrayList<>();
                for (Object obsProp : observedProperties) {
                    if (obsProp instanceof PhenomenonProperty) {
                        PhenomenonProperty pp = (PhenomenonProperty)obsProp;
                        obsPropsV200.add(pp);
                        obsPropsV100.add(pp.getHref());

                    } else {
                        throw new ClassCastException("Not a phenomenonProperty");
                    }
                }

                ///we create a list of accepted responseMode (fixed)
                final List<ResponseModeType> responses = Arrays.asList(RESULT_TEMPLATE, INLINE);
                final List<QName> resultModel = Arrays.asList(OBSERVATION_QNAME, MEASUREMENT_QNAME);
                final List<String> resultModelV200 = Arrays.asList(OBSERVATION_MODEL);
                final List<String> offeringOutputFormat = Arrays.asList("text/xml; subtype=\"om/1.0.0\"");

                store.getWriter().writeOffering(buildOffering(version,
                                                offering.getId(),
                                                offering.getId(),
                                                offering.getDescription(),
                                                offering.getAvailableSrs(),
                                                time,
                                                offering.getProcedures(),
                                                obsPropsV200,
                                                obsPropsV100,
                                                offering.getFeatureOfInterest(),
                                                offeringOutputFormat,
                                                resultModel,
                                                resultModelV200,
                                                responses,
                                                smlFormats));
            }
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public boolean existPhenomenon(String phenomenonName) throws ConstellationStoreException {
        try {
            return store.getReader().existPhenomenon(phenomenonName);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public boolean existProcedure(String procedureName) throws ConstellationStoreException {
        try {
            return store.getReader().existProcedure(procedureName);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public boolean existFeatureOfInterest(String foiName) throws ConstellationStoreException {
        try {
            return store.getReader().getFeatureOfInterestNames().contains(foiName);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public boolean existOffering(String offeringName, String version) throws ConstellationStoreException {
        try {
            return store.getReader().getOfferingNames(version).contains(offeringName);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public TemporalGeometricPrimitive getTime(String version) throws ConstellationStoreException {
        try {
            return (TemporalGeometricPrimitive) store.getReader().getEventTime(version);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public TemporalGeometricPrimitive getTimeForProcedure(String version, String sensorID) throws ConstellationStoreException {
        try {
            return store.getReader().getTimeForProcedure("2.0.0", sensorID);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public TemporalGeometricPrimitive getTimeForFeatureOfInterest(String version, String fid) throws ConstellationStoreException {
        try {
            return (TemporalGeometricPrimitive) store.getReader().getFeatureOfInterestTime(fid, version);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public void removeProcedure(String procedureID) throws ConstellationStoreException {
        try {
            store.getWriter().removeProcedure(procedureID);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public void removeObservation(String observationID) throws ConstellationStoreException {
        try {
            store.getWriter().removeObservation(observationID);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public void writeProcedure(String procedureID, Object position, String parent, String type, String omType) throws ConstellationStoreException {
        if (position != null && !(position instanceof AbstractGeometry)) {
            throw new ConstellationStoreException("Unexpected geometry type. GML geometry expected.");
        }
        try {
            store.getWriter().writeProcedure(procedureID, (AbstractGeometry) position, parent, type, omType);
        } catch (DataStoreException ex) {
             throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public void updateProcedureLocation(String procedureID, Object position) throws ConstellationStoreException {
        if (!(position instanceof AbstractGeometry)) {
            throw new ConstellationStoreException("Unexpected geometry type. GML geometry expected.");
        }
        try {
            store.getWriter().recordProcedureLocation(procedureID, (AbstractGeometry) position);
        } catch (DataStoreException ex) {
             throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public String writeObservation(Observation observation) throws ConstellationStoreException {
        try {
            String oid = store.getWriter().writeObservation(observation);
            store.getFilter().refresh();
            return oid;
        } catch (DataStoreException ex) {
             throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public void writePhenomenons(List<Phenomenon> phens) throws ConstellationStoreException {
        try {
            store.getWriter().writePhenomenons(phens);
            store.getFilter().refresh();
        } catch (DataStoreException ex) {
             throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public void writeLocation(String procedureId, Geometry position) throws ConstellationStoreException {
        try {
            store.getWriter().recordProcedureLocation(procedureId, (AbstractGeometry) position);
        } catch (DataStoreException ex) {
             throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public List<SamplingFeature> getFeatureOfInterest(Query q, Map<String, String> hints) throws ConstellationStoreException {
        try {
            final ObservationFilterReader localOmFilter = store.getFilter();
            localOmFilter.initFilterGetFeatureOfInterest();
            handleQuery(q, localOmFilter, GET_FEA, hints);

            return localOmFilter.getFeatureOfInterests(hints);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public Observation getTemplate(String sensorId, String version) throws ConstellationStoreException {
        try {
            return store.getReader().getTemplateForProcedure(sensorId, version);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public List<Observation> getObservations(Query q, QName resultModel, String responseMode, final Map<String,String> hints) throws ConstellationStoreException {
        try {
            ResponseModeType mode;
            if (responseMode != null) {
                mode = ResponseModeType.fromValue(responseMode);
            } else {
                mode = ResponseModeType.INLINE;
            }

            // we clone the filter for this request
            final ObservationFilterReader localOmFilter = store.getFilter();
            localOmFilter.initFilterObservation(mode, resultModel, hints);
            handleQuery(q, localOmFilter, GET_OBS, hints);

            if (ResponseModeType.RESULT_TEMPLATE.equals(mode)) {
                return localOmFilter.getObservationTemplates(hints);
            } else {
                return localOmFilter.getObservations(hints);
            }
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public List<Process> getProcedures(Query q, Map<String, String> hints) throws ConstellationStoreException {
        try {
            final ObservationFilterReader localOmFilter = store.getFilter();
            localOmFilter.initFilterGetSensor();
            handleQuery(q, localOmFilter, GET_PROC, hints);
            return localOmFilter.getProcesses(hints);

        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public String getResults(final String sensorID, final List<String> observedProperties, final List<String> foi, final Date start, final Date end, Integer decimationSize) throws ConstellationStoreException {
        try {
            final ObservationFilterReader localOmFilter = store.getFilter();
            localOmFilter.initFilterGetResult(sensorID, CommonConstants.OBSERVATION_QNAME, Collections.emptyMap());
            if (observedProperties.isEmpty()) {
                final FilterFactory ff = DefaultFactories.forBuildin(FilterFactory.class);
                SimpleQuery query = new SimpleQuery();
                query.setFilter(ff.equals(ff.property("observedProperty"), ff.literal(sensorID)));
                Collection<Phenomenon> phenos = getPhenomenon(query, Collections.emptyMap());
                phenos.forEach(p -> observedProperties.add(((org.geotoolkit.swe.xml.Phenomenon)p).getName().getCode()));
            }
            localOmFilter.setObservedProperties(observedProperties);
            localOmFilter.setFeatureOfInterest(foi);
            localOmFilter.setResponseFormat("text/csv");

            if (start != null && end != null) {
                final Period period = new TimePeriodType(new Timestamp(start.getTime()), new Timestamp(end.getTime()));
                localOmFilter.setTimeDuring(period);
            } else if (start != null) {
                final Instant time = new TimeInstantType(new Timestamp(start.getTime()));
                localOmFilter.setTimeAfter(time);
            } else if (end != null) {
                final Instant time = new TimeInstantType(new Timestamp(end.getTime()));
                localOmFilter.setTimeBefore(time);
            }

            if (decimationSize != null) {
                return localOmFilter.getDecimatedResults(decimationSize);
            } else {
                return localOmFilter.getResults();
            }
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    private void handleQuery(Query q, final ObservationFilterReader localOmFilter, final int mode, Map<String, String> hints) throws ConstellationStoreException, DataStoreException {
        List<String> observedProperties = new ArrayList<>();
        List<String> procedures         = new ArrayList<>();
        List<String> fois               = new ArrayList<>();

        if (q instanceof SimpleQuery) {
                SimpleQuery query = (SimpleQuery) q;
                handleFilter(mode, query.getFilter(), localOmFilter, observedProperties, procedures, fois);
                if (query.getLimit() != SimpleQuery.UNLIMITED) {
                    hints.put("limit", Long.toString(query.getLimit()));
                }
                if (query.getOffset()!= 0) {
                    hints.put("offset", Long.toString(query.getOffset()));
                }

        } else if (q != null) {
            throw new ConstellationStoreException("Only SimpleQuery are supported for now");
        }

        // TODO Spatial BBOX
        localOmFilter.setObservedProperties(observedProperties);
        localOmFilter.setProcedure(procedures);
        localOmFilter.setFeatureOfInterest(fois);
    }

    private void handleFilter(int mode, Filter filter, final ObservationFilterReader localOmFilter, List<String> observedProperties, List<String> procedures, List<String> fois) throws ConstellationStoreException, DataStoreException {
        if (Filter.INCLUDE.equals(filter)) {
            return;
        }

        if (filter instanceof And) {
            for (Filter f : ((And) filter).getChildren()) {
                handleFilter(mode, f, localOmFilter, observedProperties, procedures, fois);
            }
        } else

            // The operation Time Equals
        if (filter instanceof TEquals) {
            final TEquals tf = (TEquals) filter;

            // we get the property name (not used for now)
            // String propertyName = time.getTBefore().getPropertyName();
            final Object timeFilter = tf.getExpression2();
            localOmFilter.setTimeEquals(timeFilter);

            // The operation Time before
        } else if (filter instanceof Before) {
            final Before tf = (Before) filter;

            // we get the property name (not used for now)
            // String propertyName = time.getTBefore().getPropertyName();
            final Object timeFilter = tf.getExpression2();
            localOmFilter.setTimeBefore(timeFilter);

            // The operation Time after
        } else if (filter instanceof After) {
            final After tf = (After) filter;

            // we get the property name (not used for now)
            //String propertyName = time.getTAfter().getPropertyName();
            final Object timeFilter = tf.getExpression2();
            localOmFilter.setTimeAfter(timeFilter);

            // The time during operation
        } else if (filter instanceof During) {
            final During tf = (During) filter;

            // we get the property name (not used for now)
            //String propertyName = time.getTDuring().getPropertyName();
            final Object timeFilter = tf.getExpression2();
            localOmFilter.setTimeDuring(timeFilter);

        } else if (filter instanceof Id) {
            final Id idf = (Id) filter;
            List<String> ids = new ArrayList<>();
            idf.getIDs().stream().forEach(id -> ids.add((String) id));

            switch (mode) {
                case GET_FEA:
                    localOmFilter.setFeatureOfInterest(ids);
                    break;
                case GET_OBS:
                    localOmFilter.setObservationIds(ids);
                    break;
                case GET_PHEN:
                    localOmFilter.setObservedProperties(ids);
                    break;
                case GET_PROC:
                    localOmFilter.setProcedure(ids);
                    break;
                case GET_OFF:
                    localOmFilter.setOfferings(ids);
                    break;
                default:
                    break;
            }

        } else if (filter instanceof PropertyIsEqualTo) {
            final PropertyIsEqualTo ef = (PropertyIsEqualTo) filter;
            PropertyName name = (PropertyName) ef.getExpression1();
            Literal value = (Literal) ef.getExpression2();
            if (name.getPropertyName().equals("observedProperty")) {
                observedProperties.add((String) value.getValue());
            } else if (name.getPropertyName().equals("procedure")) {
                procedures.add((String) value.getValue());
            } else if (name.getPropertyName().equals("featureOfInterest")) {
                fois.add((String) value.getValue());
            } else if (name.getPropertyName().equals("observationId")) {
                localOmFilter.setObservationIds(Arrays.asList((String) value.getValue()));
            } else if (name.getPropertyName().equals("offering")) {
                localOmFilter.setOfferings(Arrays.asList((String) value.getValue()));
            }  else if (name.getPropertyName().equals("sensorType")) {
                localOmFilter.setProcedureType((String) value.getValue());
            }

        } else if (filter instanceof Begins || filter instanceof BegunBy || filter instanceof TContains || filter instanceof EndedBy || filter instanceof Ends || filter instanceof Meets
                || filter instanceof TOverlaps || filter instanceof OverlappedBy) {
            throw new ConstellationStoreException("This operation is not take in charge by the Web Service, supported one are: TM_Equals, TM_After, TM_Before, TM_During");
        } else {
            throw new ConstellationStoreException("Unknow filter operation.\nAnother possibility is that the content of your time filter is empty or unrecognized.");
        }
    }
}