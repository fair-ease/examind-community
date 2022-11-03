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

package org.constellation.store.observation.db;

import org.constellation.util.FilterSQLRequest;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.referencing.CRS;
import org.apache.sis.storage.DataStoreException;
import static org.constellation.api.CommonConstants.MEASUREMENT_QNAME;
import static org.constellation.api.CommonConstants.RESPONSE_MODE;
import org.geotoolkit.observation.model.Field;
import org.geotoolkit.observation.result.ResultBuilder;
import static org.constellation.store.observation.db.OM2BaseReader.defaultCRS;
import static org.constellation.store.observation.db.OM2Utils.buildComplexResult;
import static org.constellation.store.observation.db.OM2Utils.buildTime;
import static org.constellation.store.observation.db.OM2Utils.buildFoi;
import static org.geotoolkit.observation.OMUtils.*;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.geometry.jts.SRIDGenerator;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.observation.model.FieldPhenomenon;
import org.geotoolkit.observation.model.OMEntity;
import org.geotoolkit.observation.ObservationStoreException;
import org.geotoolkit.observation.model.FieldType;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.observation.xml.OMXmlFactory;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import org.geotoolkit.sos.xml.ResponseModeType;
import static org.geotoolkit.sos.xml.ResponseModeType.INLINE;
import static org.geotoolkit.sos.xml.ResponseModeType.RESULT_TEMPLATE;

import static org.geotoolkit.sos.xml.SOSXmlFactory.*;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.TextBlock;
import org.locationtech.jts.geom.Polygon;
import org.opengis.geometry.Geometry;
import org.opengis.metadata.quality.Element;
import org.opengis.observation.Observation;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.observation.Process;
import org.opengis.temporal.TemporalGeometricPrimitive;
import org.opengis.util.FactoryException;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class OM2ObservationFilterReader extends OM2ObservationFilter {

    public OM2ObservationFilterReader(final OM2ObservationFilter omFilter) {
        super(omFilter);
    }

    public OM2ObservationFilterReader(final DataSource source, final boolean isPostgres, final String schemaPrefix, final Map<String, Object> properties, final boolean timescaleDB) throws DataStoreException {
        super(source, isPostgres, schemaPrefix, properties, timescaleDB);
    }

    @Override
    public List<Observation> getObservations() throws DataStoreException {
        if (RESULT_TEMPLATE.equals(responseMode)) {
            if (MEASUREMENT_QNAME.equals(resultModel)) {
                return getMesurementTemplates();
            } else {
                return getObservationTemplates();
            }
        } else if (INLINE.equals(responseMode)) {
            if (MEASUREMENT_QNAME.equals(resultModel)) {
                return getMesurements();
            } else {
                return getComplexObservations();
            }
        } else {
            throw new DataStoreException("Unsupported response mode:" + responseMode);
        }
    }

    private List<Observation> getObservationTemplates() throws DataStoreException {
        if (firstFilter) {
            sqlRequest.replaceFirst("WHERE", "");
        }
        sqlRequest.append(" ORDER BY \"procedure\" ");
        sqlRequest = appendPaginationToRequest(sqlRequest);

        final List<Observation> observations = new ArrayList<>();
        final Map<String, Process> processMap = new HashMap<>();

        LOGGER.fine(sqlRequest.getRequest());
        try (final Connection c            = source.getConnection();
             final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
             final ResultSet rs            = pstmt.executeQuery()) {
             final TextBlock encoding = getDefaultTextEncoding(version);

            while (rs.next()) {
                final String procedure = rs.getString("procedure");
                final String procedureID;
                if (procedure.startsWith(sensorIdBase)) {
                    procedureID = procedure.substring(sensorIdBase.length());
                } else {
                    procedureID = procedure;
                }
                final String obsID = "obs-" + procedureID;
                final String name = observationTemplateIdBase + procedureID;
                final Phenomenon phen = getGlobalCompositePhenomenon(version, c, procedure);
                String featureID = null;
                FeatureProperty foi = null;
                if (includeFoiInTemplate) {
                    featureID = rs.getString("foi");
                    final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                    foi = buildFeatureProperty(version, feature);
                }
                TemporalGeometricPrimitive tempTime = null;
                if (includeTimeInTemplate) {
                    tempTime = getTimeForTemplate(c, procedure, null, featureID, version);
                }
                List<Field> fields = readFields(procedure, c);
                /*
                 *  BUILD RESULT
                 */
                final List<AnyScalar> scal = new ArrayList<>();
                for (Field f : fields) {
                    scal.add(f.getScalar(version));
                }
                final Process proc = processMap.computeIfAbsent(procedure, f -> {return getProcessSafe(version, procedure, c);});
                final Object result = buildComplexResult(version, scal, 0, encoding, null, observations.size());
                Observation observation = OMXmlFactory.buildObservation(version, obsID, name, null, foi, phen, proc, result, tempTime, null);
                observations.add(observation);
            }


        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        } catch (DataStoreException ex) {
            throw new DataStoreException("the service has throw a Datastore Exception:" + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new DataStoreException("the service has throw a Runtime Exception:" + ex.getMessage(), ex);
        }
        return observations;
    }

    private List<Observation> getMesurementTemplates() throws DataStoreException {
        if (firstFilter) {
            sqlRequest.replaceFirst("WHERE", "");
        }
        sqlRequest.append(" ORDER BY \"procedure\", pd.\"order\" ");
        sqlRequest = appendPaginationToRequest(sqlRequest);

        final List<Observation> observations = new ArrayList<>();
        final Map<String, Process> processMap = new HashMap<>();

        LOGGER.fine(sqlRequest.getRequest());
        try (final Connection c              = source.getConnection();
             final PreparedStatement pstmt   = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
             final ResultSet rs              = pstmt.executeQuery()) {

            while (rs.next()) {
                final String procedure = rs.getString("procedure");
                final String procedureID;
                if (procedure.startsWith(sensorIdBase)) {
                    procedureID = procedure.substring(sensorIdBase.length());
                } else {
                    procedureID = procedure;
                }
                final String obsID = "obs-" + procedureID;
                final String name = observationTemplateIdBase + procedureID;
                final String observedProperty = rs.getString("obsprop");
                final int phenIndex   = rs.getInt("order");
                final Field field     = getFieldByIndex(procedure, phenIndex, true, c);

                // skip the main field for timeseries
                if (phenIndex == 1 && field.type == FieldType.TIME) {
                    continue;
                }
                
                final Phenomenon phen = getPhenomenon(version, observedProperty, c);
                String featureID = null;
                FeatureProperty foi = null;
                if (includeFoiInTemplate) {
                    featureID = rs.getString("foi");
                    final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                    foi = buildFeatureProperty(version, feature);
                }

                /*
                 *  BUILD RESULT
                 */
                final Process proc = processMap.computeIfAbsent(procedure, f -> {return getProcessSafe(version, procedure, c);});
                
                TemporalGeometricPrimitive tempTime = null;
                if (includeTimeInTemplate) {
                    tempTime = getTimeForTemplate(c, procedure, observedProperty, featureID, version);
                }
                Object result = null;
                final String observationType;
                if (field.type == FieldType.QUANTITY) {
                    result =buildMeasure(version, field.uom, null);
                    observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement";
                } else if (field.type == FieldType.BOOLEAN) {
                    observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation";
                } else if (field.type == FieldType.TEXT) {
                    result = "";
                    observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";
                } else if (field.type == FieldType.TIME) {
                    observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TemporalObservation";
                } else {
                    observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";
                }
                final List<Element> resultQuality = buildResultQuality(field, null);

                observations.add(OMXmlFactory.buildMeasurement(version, obsID + '-' + phenIndex, name + '-' + phenIndex, null, foi, phen, proc, result, tempTime, null, resultQuality, observationType));
                
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        } catch (DataStoreException ex) {
            throw new DataStoreException("the service has throw a Datastore Exception:" + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new DataStoreException("the service has throw a Runtime Exception:" + ex.getMessage(), ex);
        }
        return observations;
    }

    private List<Observation> getComplexObservations() throws DataStoreException {
        final Map<String, Observation> observations = new LinkedHashMap<>();
        final Map<String, Process> processMap       = new LinkedHashMap<>();
        final Map<String, List<Field>> fieldMap     = new LinkedHashMap<>();
        final TextBlock encoding                    = getDefaultTextEncoding(version);
        if (resultMode == null) {
            resultMode = ResultMode.CSV;
        }
        final ResultBuilder values          = new ResultBuilder(resultMode, encoding, false);
        if (firstFilter) {
            sqlRequest.replaceFirst("WHERE", "");
        }
        LOGGER.fine(sqlRequest.getRequest());
        try(final Connection c              = source.getConnection();
            final PreparedStatement pstmt   = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
            final ResultSet rs              = pstmt.executeQuery()) {

            while (rs.next()) {
                int nbValue = 0;
                final String procedure   = rs.getString("procedure");
                final String featureID   = rs.getString("foi");
                final int oid            = rs.getInt("id");
                Observation observation  = observations.get(procedure + '-' + featureID);
                final String measureJoin = getMeasureTableJoin(getPIDFromProcedure(procedure, c));
                final Field mainField    = getMainField(procedure, c);
                boolean profile          = !FieldType.TIME.equals(mainField.type);
                final boolean profileWithTime = profile && includeTimeForProfile;
               /*
                * Compute procedure fields
                */
                List<Field> fields = fieldMap.get(procedure);
                if (fields == null) {
                    if (!currentFields.isEmpty()) {
                        fields = new ArrayList<>();
                        if (mainField != null) {
                            fields.add(mainField);
                        }

                        List<Field> phenFields = new ArrayList<>();
                        for (String f : currentFields) {
                            final Field field = getFieldForPhenomenon(procedure, f, c);
                            if (field != null && !fields.contains(field)) {
                                phenFields.add(field);
                            }
                        }

                        // add proper order to fields
                        List<Field> procedureFields = readFields(procedure, c);
                        phenFields = reOrderFields(procedureFields, phenFields);
                        fields.addAll(phenFields);

                        // special case for trajectory observation
                        // if lat/lon are available, include it anyway if they are not part of the phenomenon.
                        final List<Field> llFields = getPosFields(procedure, c);
                        for (Field llField : llFields) {
                            if (!fields.contains(llField)) {
                                fields.add(1, llField);
                            }
                        }

                    } else {
                        fields = readFields(procedure, c);
                    }

                    // add the time for profile in the dataBlock if requested
                    if (profileWithTime) {
                        fields.add(0, new Field(0, FieldType.TIME, "time_begin", "time", "time", null));
                    }
                    // add the result id in the dataBlock if requested
                    if (includeIDInDataBlock) {
                        fields.add(0, new Field(0, FieldType.TEXT, "id", "measure identifier", "measure identifier", null));
                    }
                    fieldMap.put(procedure, fields);
                }

               /*
                * Compute procedure measure request
                */
                int offset = getFieldsOffset(profile, profileWithTime, includeIDInDataBlock);
                FilterSQLRequest measureFilter = applyFilterOnMeasureRequest(offset, mainField, fields);
                
                final FilterSQLRequest measureRequest = new FilterSQLRequest("SELECT * FROM " + measureJoin + " WHERE m.\"id_observation\" = ");
                measureRequest.appendValue(-1).append(measureFilter, !profile).append("ORDER BY ").append("m.\"" + mainField.name + "\"");

                final String name = rs.getString("identifier");
                final FieldParser parser = new FieldParser(fields, values, profileWithTime, includeIDInDataBlock, includeQualityFields, name);

                if (observation == null) {
                    final String obsID            = "obs-" + oid;
                    final String timeID           = "time-" + oid;
                    final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                    final FeatureProperty prop    = buildFeatureProperty(version, feature);
                    final Phenomenon phen         = getGlobalCompositePhenomenon(version, c, procedure);

                    parser.firstTime = dateFromTS(rs.getTimestamp("time_begin"));
                    parser.lastTime = dateFromTS(rs.getTimestamp("time_end"));
                    final List<AnyScalar> scal = new ArrayList<>();
                    for (Field f : fields) {
                        scal.add(f.getScalar(version));
                    }

                    /*
                     *  BUILD RESULT
                     */
                    measureRequest.setParamValue(0, oid);
                    LOGGER.fine(measureRequest.getRequest());
                    try(final PreparedStatement stmt = measureRequest.fillParams(c.prepareStatement(measureRequest.getRequest()));
                        final ResultSet rs2 = stmt.executeQuery()) {
                        while (rs2.next()) {
                            parser.parseLine(rs2, offset);
                            nbValue = nbValue + parser.nbValue;

                            /**
                             * In "separated observation" mode we create an observation for each measure and don't merge it into a single obervation by procedure/foi.
                             */
                            if (separatedObs) {
                                final TemporalGeometricPrimitive time = buildTimeInstant(version, timeID, parser.lastTime != null ? parser.lastTime : parser.firstTime);
                                final Object result = buildComplexResult(version, scal, nbValue, encoding, values, observations.size());
                                final Process proc = processMap.computeIfAbsent(procedure, f -> {return getProcessSafe(version, procedure, c);});
                                final String measureID = rs2.getString("id");
                                final String singleObsID = "obs-" + oid + '-' + measureID;
                                final String singleName  = name + '-' + measureID;
                                observation = OMXmlFactory.buildObservation(version, singleObsID, singleName, null, prop, phen, proc, result, time, null);
                                observations.put(procedure + '-' + name + '-' + measureID, observation);
                                values.clear();
                                nbValue = 0;
                            }
                        }
                    } catch (SQLException ex) {
                        LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", measureRequest.getRequest());
                        throw new DataStoreException("the service has throw a SQL Exception.", ex);
                    }

                   /**
                    * we create an observation with all the measures and keep it so we can extend it if another observation for the same procedure/foi appears.
                    */
                    if (!separatedObs) {
                        final TemporalGeometricPrimitive time = buildTimePeriod(version, timeID, parser.firstTime, parser.lastTime);
                        final Object result = buildComplexResult(version, scal, nbValue, encoding, values, observations.size());
                        final Process proc = processMap.computeIfAbsent(procedure, f -> {return getProcessSafe(version, procedure, c);});
                        observation = OMXmlFactory.buildObservation(version, obsID, name, null, prop, phen, proc, result, time, null);
                        observations.put(procedure + '-' + featureID, observation);
                    }
                } else {
                    /**
                     * complete the previous observation with new measures.
                     */
                    measureRequest.setParamValue(0, oid);
                    LOGGER.fine(measureRequest.toString());
                    try(final PreparedStatement stmt = measureRequest.fillParams(c.prepareStatement(measureRequest.getRequest()));
                        final ResultSet rs2 = stmt.executeQuery()) {
                        while (rs2.next()) {
                            parser.parseLine(rs2, offset);
                            nbValue = nbValue + parser.nbValue;
                        }
                    }

                    // update observation result and sampling time
                    final DataArrayProperty result = (DataArrayProperty) (observation).getResult();
                    final DataArray array = result.getDataArray();
                    array.setElementCount(array.getElementCount().getCount().getValue() + nbValue);
                    switch (resultMode) {
                        case DATA_ARRAY: array.getDataValues().getAny().addAll(values.getDataArray()); break;
                        case CSV:     array.setValues(array.getValues() + values.getStringValues()); break;
                    }
                    ((AbstractObservation) observation).extendSamplingTime(parser.lastTime);
                }
                values.clear();
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        } catch (DataStoreException ex) {
            throw new DataStoreException("the service has throw a Datastore Exception:" + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new DataStoreException("the service has throw a Runtime Exception:" + ex.getMessage(), ex);
        }
        
        // TODO make a real pagination
        return applyPostPagination(new ArrayList<>(observations.values()));
    }

    private List<Observation> getMesurements() throws DataStoreException {
        // add orderby to the query
        sqlRequest.append(" ORDER BY o.\"id\"");
        if (firstFilter) {
            sqlRequest.replaceFirst("WHERE", "");
        }

        final List<Observation> observations = new ArrayList<>();
        final Map<String, Process> processMap = new HashMap<>();
        LOGGER.fine(sqlRequest.toString());
        try(final Connection c            = source.getConnection();
            final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
            final ResultSet rs            = pstmt.executeQuery()) {
            while (rs.next()) {
                final String procedure = rs.getString("procedure");
                final Date startTime = dateFromTS(rs.getTimestamp("time_begin"));
                final Date endTime = dateFromTS(rs.getTimestamp("time_end"));
                final int oid = rs.getInt("id");
                final String name = rs.getString("identifier");
                final String obsID = "obs-" + oid;
                final String timeID = "time-" + oid;
                final String featureID = rs.getString("foi");
                final String observedProperty = rs.getString("observed_property");
                final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                final Phenomenon phen = getPhenomenon(version, observedProperty, c);
                final String measureJoin   = getMeasureTableJoin(getPIDFromProcedure(procedure, c));
                final List<Field> fields = readFields(procedure, true, c);
                final List<FieldPhenomenon> fieldPhen = getPhenomenonFields(phen, fields, c);
                final Process proc = processMap.computeIfAbsent(procedure, f -> {return getProcessSafe(version, procedure, c);});
                final TemporalGeometricPrimitive time = buildTime(version, timeID, startTime, endTime);

                /*
                 *  BUILD RESULT
                 */
                final Field mainField = getMainField(procedure, c);
                boolean notProfile    = FieldType.TIME.equals(mainField.type);

                final FilterSQLRequest measureFilter = applyFilterOnMeasureRequest(0, mainField, fieldPhen.stream().map(f -> f.getField()).toList());

                final FilterSQLRequest measureRequest = new FilterSQLRequest("SELECT * FROM " + measureJoin + " WHERE m.\"id_observation\" = ");
                measureRequest.appendValue(oid).append(" ").append(measureFilter, notProfile);
                measureRequest.append(" ORDER BY ").append("m.\"" + mainField.name + "\"");

                /**
                 * coherence verification
                 */
                LOGGER.fine(measureRequest.toString());
                try(final PreparedStatement stmt = measureRequest.fillParams(c.prepareStatement(measureRequest.getRequest()));
                    final ResultSet rs2 = stmt.executeQuery()) {
                    while (rs2.next()) {
                        final Integer rid = rs2.getInt("id");
                        if (measureIdFilters.isEmpty() || measureIdFilters.contains(rid)) {
                            TemporalGeometricPrimitive measureTime;
                            if (notProfile) {
                                final Date mt = dateFromTS(rs2.getTimestamp(mainField.name));
                                measureTime = buildTimeInstant(version, "time-" + oid + '-' + rid, mt);
                            } else {
                                measureTime = time;
                            }

                            for (int i = 0; i < fieldPhen.size(); i++) {
                                FieldPhenomenon field = fieldPhen.get(i);
                                FieldType fType = field.getField().type;
                                String fName    = field.getField().name;
                                final String value = rs2.getString(fName);
                                if (value != null) {
                                    Object result = null;
                                    final String observationType;
                                    if (fType == FieldType.QUANTITY) {
                                        Double dValue = rs2.getDouble(fName);
                                        result = buildMeasure(version, "measure-00" + rid, field.getField().uom, dValue);
                                        observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement";
                                    } else if (fType == FieldType.BOOLEAN) {
                                        result = rs2.getBoolean(fName);
                                        observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TruthObservation";
                                    } else if (fType == FieldType.TIME) {
                                        Timestamp ts = rs2.getTimestamp(fName);
                                        GregorianCalendar cal = new GregorianCalendar();
                                        cal.setTimeInMillis(ts.getTime());
                                        result = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal) ;
                                        observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_TemporalObservation";
                                    } else {
                                        result = value;
                                        observationType = "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation";
                                    }
                                    
                                    final FeatureProperty foi = buildFeatureProperty(version, feature); // do not share the same object
                                    final String measId =  obsID + '-' + field.getField().index + '-' + rid;
                                    final String measName = name + '-' + field.getField().index + '-' + rid;
                                    List<Element> resultQuality = buildResultQuality(field.getField(), rs2);
                                    observations.add(OMXmlFactory.buildMeasurement(version, measId, measName, null, foi, field.getPhenomenon(), proc, result, measureTime, null, resultQuality, observationType));
                                }
                            }
                        }
                    }
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        } catch (DataStoreException | DatatypeConfigurationException ex) {
            throw new DataStoreException("the service has throw an Exception:" + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            throw new DataStoreException("the service has throw a Runtime Exception:" + ex.getMessage(), ex);
        }
        // TODO make a real pagination
        return applyPostPagination(observations);
    }

    /**
     * Return the index of the first measure fields.
     *
     * @param profile Set to {@code true} if the current observation is a Profile.
     * @param profileWithTime Set to {@code true} if the time has to be included in the result for a profile.
     * @param includeIDInDataBlock Set to {@code true} if the measure identifier has to be included in the result.
     * 
     * @return The index where starts the measure fields.
     */
    private  int getFieldsOffset(boolean profile, boolean profileWithTime, boolean includeIDInDataBlock) {
        int offset = profile ? 0 : 1; // for profile, the first phenomenon field is the main field
        if (profileWithTime) {
            offset++;
        }
        if (includeIDInDataBlock) {
            offset++;
        }
        return offset;
    }

    @Override
    public Object getResults() throws DataStoreException {
        if (ResponseModeType.OUT_OF_BAND.equals(responseMode)) {
            throw new ObservationStoreException("Out of band response mode has not been implemented yet", NO_APPLICABLE_CODE, RESPONSE_MODE);
        }
        final boolean countRequest         = "count".equals(responseFormat);
        boolean includeTimeForProfile      = !countRequest && this.includeTimeForProfile;
        final boolean profile              = "profile".equals(currentOMType);
        final boolean profileWithTime      = profile && includeTimeForProfile;
        if (decimationSize != null && !countRequest) {
            if (timescaleDB) {
                return getDecimatedResultsTimeScale();
            } else {
                return getDecimatedResults();
            }
        }
        try (final Connection c = source.getConnection()) {
            /**
             *  1) build field list.
             */
            final Field mainField = getMainField(currentProcedure);
            
            final List<Field> fields;
            if (!currentFields.isEmpty()) {
                fields = new ArrayList<>();
                if (mainField != null) {
                    fields.add(mainField);
                }
                List<Field> phenFields = new ArrayList<>();
                for (String f : currentFields) {
                    final Field field = getFieldForPhenomenon(currentProcedure, f, c);
                    if (field != null && !fields.contains(field)) {
                        phenFields.add(field);
                    }
                }
                // add proper order to fields
                List<Field> allfields = readFields(currentProcedure, c);
                phenFields = reOrderFields(allfields, phenFields);
                fields.addAll(phenFields);

            } else {
                fields = readFields(currentProcedure, c);
            }
            // add the time for profile in the dataBlock if requested
            if (profileWithTime) {
                fields.add(0, new Field(0, FieldType.TIME, "time_begin", "time", "time", null));
            }
            // add the result id in the dataBlock if requested
            if (includeIDInDataBlock) {
                fields.add(0, new Field(0, FieldType.TEXT, "id", "measure identifier", "measure identifier", null));
            }

            /**
             *  2) complete SQL request.
             */
            int offset = getFieldsOffset(profile, profileWithTime, includeIDInDataBlock);
            FilterSQLRequest measureFilter = applyFilterOnMeasureRequest(offset, mainField, fields);
            sqlRequest.append(measureFilter);
            
            final String fieldOrdering;
            if (mainField != null) {
                fieldOrdering = "m.\"" + mainField.name + "\"";
            } else {
                // we keep this fallback where no main field is found.
                // i'm not sure it will be possible to handle an observation with no main field (meaning its not a timeseries or a profile).
                fieldOrdering = "m.\"id\"";
            }
            StringBuilder select  = new StringBuilder("m.*");
            if (profile) {
                select.append(", o.\"id\" as oid ");
            }
            if (profileWithTime) {
                select.append(", o.\"time_begin\" ");
            }
            if (includeIDInDataBlock) {
                select.append(", o.\"identifier\" ");
            }
            sqlRequest.replaceFirst("m.*", select.toString());
            sqlRequest.append(" ORDER BY  o.\"id\", ").append(fieldOrdering);

            if (firstFilter) {
                return sqlRequest.replaceFirst("WHERE", "");
            }
            LOGGER.fine(sqlRequest.toString());

            /**
             * 3) Extract results.
             */
            ResultProcessor processor = new ResultProcessor(fields, profile, includeIDInDataBlock, includeQualityFields);
            ResultBuilder values = processor.initResultBuilder(responseFormat, countRequest);
            try (final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
                 final ResultSet rs = pstmt.executeQuery()) {
                processor.processResults(rs);
            }
            switch (values.getMode()) {
                case DATA_ARRAY:  return values.getDataArray();
                case CSV:         return values.getStringValues();
                case COUNT:       return values.getCount();
                default: throw new IllegalArgumentException("Unexpected result mode");
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        }
    }

    private Object getDecimatedResults() throws DataStoreException {
        final boolean profile = "profile".equals(currentOMType);
        final boolean profileWithTime = profile && includeTimeForProfile;
        try (final Connection c = source.getConnection()) {

            /**
             *  1) build field list.
             */
            final List<Field> fields = new ArrayList<>();
            final List<Field> allfields = readFields(currentProcedure, c);
            fields.add(allfields.get(0));
            for (int i = 1; i < allfields.size(); i++) {
                Field f = allfields.get(i);
                 if (isIncludedField(f.name, f.description, f.index)) {
                     fields.add(f);
                 }
            }

            // add the time for profile in the dataBlock if requested
            if (profileWithTime) {
                fields.add(0, new Field(0, FieldType.TIME, "time_begin", "time", "time", null));
            }
            // add the result id in the dataBlock if requested
            if (includeIDInDataBlock) {
                fields.add(0, new Field(0, FieldType.TEXT, "id", "measure identifier", "measure identifier", null));
            }
            final Field mainField = getMainField(currentProcedure, c);

            /**
             *  2) complete SQL request.
             */
            int offset = getFieldsOffset(profile, profileWithTime, includeIDInDataBlock);
            FilterSQLRequest measureFilter = applyFilterOnMeasureRequest(offset, mainField, fields);
            sqlRequest.append(measureFilter);
            
            final FilterSQLRequest fieldRequest = sqlRequest.clone();
            final String fieldOrdering;
            if (mainField != null) {
                fieldOrdering = "m.\"" + mainField.name + "\"";
            } else {
                // we keep this fallback where no main field is found.
                // i'm not sure it will be possible to handle an observation with no main field (meaning its not a timeseries or a profile).
                fieldOrdering = "m.\"id\"";
            }
            sqlRequest.append(" ORDER BY  o.\"id\", ").append(fieldOrdering);
            StringBuilder select  = new StringBuilder("m.*");
            if (profile) {
                select.append(", o.\"id\" as oid ");
            }
            if (profileWithTime) {
                select.append(", o.\"time_begin\" ");
            }
            if (includeIDInDataBlock) {
                select.append(", o.\"identifier\" ");
            }
            sqlRequest.replaceFirst("m.*", select.toString());
            LOGGER.fine(sqlRequest.toString());
            
            /**
             * 3) Extract results.
             */
            final Map<Object, long[]> times = getMainFieldStep(fieldRequest, mainField, c, decimationSize);
            final int mainFieldIndex = fields.indexOf(mainField);
            ResultProcessor processor = new DefaultResultDecimator(fields, profile, includeIDInDataBlock, decimationSize, fieldFilters, mainFieldIndex, currentProcedure, times);
            ResultBuilder values = processor.initResultBuilder(responseFormat, false);

            try (final PreparedStatement pstmt    = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
                 final ResultSet rs = pstmt.executeQuery()) {
                processor.processResults(rs);
            }
            switch (values.getMode()) {
                case DATA_ARRAY:  return values.getDataArray();
                case CSV:         return values.getStringValues();
                default: throw new IllegalArgumentException("Unexpected result mode");
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage(), ex);
        }
    }

    private Object getDecimatedResultsTimeScale() throws DataStoreException {
        final boolean profile = "profile".equals(currentOMType);
        final boolean profileWithTime = profile && includeTimeForProfile;
        try(final Connection c = source.getConnection()) {

            /**
             *  1) build field list.
             */
            final List<Field> fields = new ArrayList<>();
            final List<Field> allfields = readFields(currentProcedure, c);
            fields.add(allfields.get(0));
            for (int i = 1; i < allfields.size(); i++) {
                Field f = allfields.get(i);
                 if (isIncludedField(f.name, f.description, f.index)) {
                     fields.add(f);
                 }
            }
            final Field mainField = getMainField(currentProcedure, c);
            // add the time for profile in the dataBlock if requested
            if (profileWithTime) {
                fields.add(0, new Field(0, FieldType.TIME, "time_begin", "time", "time", null));
            }
            // add the result id in the dataBlock if requested
            if (includeIDInDataBlock) {
                fields.add(0, new Field(0, FieldType.TEXT, "id", "measure identifier", "measure identifier", null));
            }
            
            /**
             *  2) complete SQL request.
             */
            int offset = getFieldsOffset(profile, profileWithTime, includeIDInDataBlock);
            FilterSQLRequest measureFilter = applyFilterOnMeasureRequest(offset, mainField, fields);
            // TODO the measure filter is not used ? need testing

            // calculate step
            final Map<Object, long[]> times = getMainFieldStep(sqlRequest.clone(), fields.get(0), c, decimationSize);
            final long step;
            if (profile) {
                // choose the first step
                // (may be replaced by one request by observation, maybe by looking if the step is uniform)
                step = times.values().iterator().next()[1];
            } else {
                step = times.get(1)[1];
            }

            StringBuilder select  = new StringBuilder();
            select.append("time_bucket('").append(step);
            if (profile) {
                select.append("', \"");
            } else {
                select.append(" ms', \"");
            }
            select.append(mainField.name).append("\") AS step");
            for (int i = offset; i < fields.size(); i++) {
                 select.append(", avg(\"").append(fields.get(i).name).append("\") AS \"").append(fields.get(i).name).append("\"");
            }
            if (profile) {
                select.append(", o.\"id\" as \"oid\" ");
            }
            if (profileWithTime) {
                select.append(", o.\"time_begin\" ");
            }
            if (includeIDInDataBlock) {
                select.append(", o.\"identifier\" ");
            }
            sqlRequest.replaceFirst("m.*", select.toString());
            if (profile) {
                sqlRequest.append(" GROUP BY step, \"oid\" ORDER BY \"oid\", step");
            } else {
                sqlRequest.append(" GROUP BY step ORDER BY step");
            }
            LOGGER.fine(sqlRequest.toString());

            /**
             * 3) Extract results.
             */
            final int mainFieldIndex = fields.indexOf(mainField);
            ResultProcessor processor = new TimeScaleResultDecimator(fields, profile, includeIDInDataBlock, decimationSize, fieldFilters, mainFieldIndex, currentProcedure);
            ResultBuilder values = processor.initResultBuilder(responseFormat, false);
            try (final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
                 final ResultSet rs            = pstmt.executeQuery()) {
                processor.processResults(rs);
            }
            switch (values.getMode()) {
                case DATA_ARRAY:  return values.getDataArray();
                case CSV:         return values.getStringValues();
                default: throw new IllegalArgumentException("Unexpected result mode");
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception", ex);
        }
    }

    /**
     * extract the main field (time or other for profile observation) span and determine a step regarding the width parameter.
     *
     * return a Map with in the keys :
     *  - the procedure id for location retrieval
     *  - the observation id for profiles sensor.
     *  - a fixed value "1" for non profiles sensor (as in time series all observation are merged).
     *
     * and in the values, an array of a fixed size of 2 containing :
     *  - the mnimal value
     *  - the step value
     */
    private Map<Object, long[]> getMainFieldStep(FilterSQLRequest request, final Field mainField, final Connection c, final int width) throws SQLException {
        boolean profile = "profile".equals(currentOMType);
        boolean getLoc = OMEntity.HISTORICAL_LOCATION.equals(objectType);
        if (getLoc) {
            request.replaceFirst("SELECT hl.\"procedure\", hl.\"time\", st_asBinary(\"location\") as \"location\", hl.\"crs\" ",
                                 "SELECT MIN(\"" + mainField.name + "\") as tmin, MAX(\"" + mainField.name + "\") as tmax, hl.\"procedure\" ");
            request.append(" group by hl.\"procedure\" order by hl.\"procedure\"");

        } else {
            if (profile) {
                request.replaceSelect(" MIN(\"" + mainField.name + "\"), MAX(\"" + mainField.name + "\"), o.\"id\" ");
                request.append(" group by o.\"id\" order by o.\"id\"");
            } else {
                request.replaceSelect(" MIN(\"" + mainField.name + "\"), MAX(\"" + mainField.name + "\") ");
            }
        }
        LOGGER.fine(request.toString());
        try (final PreparedStatement pstmt = request.fillParams(c.prepareStatement(request.getRequest()));
             final ResultSet rs = pstmt.executeQuery()) {
            Map<Object, long[]> results = new LinkedHashMap<>();
            while (rs.next()) {
                final long[] result = {-1L, -1L};
                if (FieldType.TIME.equals(mainField.type)) {
                    final Timestamp minT = rs.getTimestamp(1);
                    final Timestamp maxT = rs.getTimestamp(2);
                    if (minT != null && maxT != null) {
                        final long min = minT.getTime();
                        final long max = maxT.getTime();
                        result[0] = min;
                        long step = (max - min) / width;
                        /* step should always be positive
                        if (step <= 0) {
                            step = 1;
                        }*/
                        result[1] = step;
                    }
                } else if (FieldType.QUANTITY.equals(mainField.type)) {
                    final Double minT = rs.getDouble(1);
                    final Double maxT = rs.getDouble(2);
                    final long min    = minT.longValue();
                    final long max    = maxT.longValue();
                    result[0] = min;
                    long step = (max - min) / width;
                    /* step should always be positive
                    if (step <= 0) {
                        step = 1;
                    }*/
                    result[1] = step;

                } else {
                    throw new SQLException("unable to extract bound from a " + mainField.type + " main field.");
                }
                final Object key;
                if (getLoc) {
                    key = rs.getString(3);
                } else {
                    if (profile) {
                        key = rs.getInt(3);
                    } else {
                        key = 1; // single in time series
                    }
                }
                results.put(key, result);
            }
            return results;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "SQLException while executing the query: {0}", request.toString());
            throw ex;
        }
    }

    @Override
    public List<SamplingFeature> getFeatureOfInterests() throws DataStoreException {
        if (obsJoin) {
            final String obsJoin = ", \"" + schemaPrefix + "om\".\"observations\" o WHERE o.\"foi\" = sf.\"id\" ";
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", obsJoin);
            } else {
                sqlRequest.replaceFirst("WHERE", obsJoin + "AND ");
            }
        } else if (offJoin) {
            final String offJoin = ", \"" + schemaPrefix + "om\".\"offering_foi\" off WHERE off.\"foi\" = sf.\"id\" ";
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", offJoin);
            } else {
                sqlRequest.replaceFirst("WHERE", offJoin + "AND ");
            }
        } else {
            sqlRequest.replaceFirst("\"foi\"='", "sf.\"id\"='");
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", "");
            }
        }
        sqlRequest = appendPaginationToRequest(sqlRequest);
        LOGGER.fine(sqlRequest.toString());
        final List<SamplingFeature> features = new ArrayList<>();
        try(final Connection c            = source.getConnection();
            final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
            final ResultSet rs            = pstmt.executeQuery()) {
            while (rs.next()) {
                final String id = rs.getString("id");
                final String name = rs.getString("name");
                final String desc = rs.getString("description");
                final String sf = rs.getString("sampledfeature");
                final int srid = rs.getInt("crs");
                final byte[] b = rs.getBytes("shape");
                final CoordinateReferenceSystem crs;
                if (srid != 0) {
                    crs = CRS.forCode(SRIDGenerator.toSRS(srid, SRIDGenerator.Version.V1));
                } else {
                    crs = defaultCRS;
                }
                final org.locationtech.jts.geom.Geometry geom;
                if (b != null) {
                    WKBReader reader = new WKBReader();
                    geom = reader.read(b);
                } else {
                    geom = null;
                }
                features.add(buildFoi(version, id, name, desc, sf, geom, crs));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        } catch (FactoryException ex) {
            LOGGER.log(Level.WARNING, "FactoryException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a Factory Exception:" + ex.getMessage(), ex);
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "ParseException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a Parse Exception:" + ex.getMessage(), ex);
        }
        return features;
    }

    @Override
    public List<Phenomenon> getPhenomenons() throws DataStoreException {
        if (obsJoin) {
            final String obsJoin = ", \"" + schemaPrefix + "om\".\"observations\" o WHERE o.\"observed_property\" = op.\"id\" ";
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", obsJoin);
            } else {
                sqlRequest.replaceFirst("WHERE", obsJoin + "AND ");
            }
        } else if (procDescJoin) {
            final String procDescJoin = ", \"" + schemaPrefix + "om\".\"procedure_descriptions\" pd WHERE pd.\"field_name\" = op.\"id\" ";
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", procDescJoin);
            } else {
                sqlRequest.replaceFirst("WHERE", procDescJoin + "AND ");
            }
        } else if (offJoin) {
            final String offJoin = ", \"" + schemaPrefix + "om\".\"offering_observed_properties\" off WHERE off.\"phenomenon\" = op.\"id\" ";
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", offJoin);
            } else {
                sqlRequest.replaceFirst("WHERE", offJoin + "AND ");
            }
        } else {
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", "");
            }
        }
        if (procDescJoin) {
            sqlRequest.append(" ORDER BY \"order\"");
        } else {
            sqlRequest.append(" ORDER BY \"id\"");
        }
        sqlRequest = appendPaginationToRequest(sqlRequest);
        final List<Phenomenon> phenomenons = new ArrayList<>();
        LOGGER.fine(sqlRequest.toString());
        try(final Connection c            = source.getConnection();
            final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
            final ResultSet rs            = pstmt.executeQuery()) {
            while (rs.next()) {
                Phenomenon phen = getPhenomenon(version, rs.getString(1), c);
                phenomenons.add(phen);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        }
        return phenomenons;
    }

    @Override
    public List<Process> getProcesses() throws DataStoreException {
        if (obsJoin) {
            final String obsJoin = ", \"" + schemaPrefix + "om\".\"observations\" o WHERE o.\"procedure\" = pr.\"id\" ";
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", obsJoin);
            } else {
                sqlRequest.replaceFirst("WHERE", obsJoin + "AND ");
            }
        } else if (offJoin) {
            final String offJoin = ", \"" + schemaPrefix + "om\".\"offerings\" off WHERE off.\"procedure\" = pr.\"id\" ";
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", offJoin);
            } else {
                sqlRequest.replaceFirst("WHERE", offJoin + "AND ");
            }
        } else {
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", "");
            }
        }
        sqlRequest = appendPaginationToRequest(sqlRequest);
        LOGGER.fine(sqlRequest.toString());
        final List<Process> processes = new ArrayList<>();
        try(final Connection c            = source.getConnection();
            final PreparedStatement pstmt =  sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
            final ResultSet rs            = pstmt.executeQuery()) {
            while (rs.next()) {
                processes.add(getProcess(version, rs.getString(1), c));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        }
        return processes;
    }

    @Override
    public Map<String, Geometry> getSensorLocations() throws DataStoreException {
        final String gmlVersion = getGMLVersion(version);
        if (obsJoin) {
            final String obsJoin = ", \"" + schemaPrefix + "om\".\"observations\" o WHERE o.\"procedure\" = pr.\"id\" ";
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", obsJoin);
            } else {
                sqlRequest.replaceFirst("WHERE", obsJoin + "AND ");
            }
        } else {
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", "");
            }
        }
         // will be removed when postgis filter will be set in request
        Polygon spaFilter = null;
        if (envelopeFilter != null) {
            spaFilter = JTS.toGeometry(envelopeFilter);
        }
        sqlRequest.append(" ORDER BY \"id\"");

        boolean applyPostPagination = true;
        if (spaFilter == null) {
            applyPostPagination = false;
            sqlRequest = appendPaginationToRequest(sqlRequest);
        }

        Map<String, Geometry> locations = new LinkedHashMap<>();
        LOGGER.fine(sqlRequest.toString());
        try(final Connection c            = source.getConnection();
            final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
            final ResultSet rs            = pstmt.executeQuery()) {
            while (rs.next()) {
                try {
                    final String procedure = rs.getString("id");
                    final byte[] b = rs.getBytes(2);
                    final int srid = rs.getInt(3);
                    final CoordinateReferenceSystem crs;
                    if (srid != 0) {
                        crs = CRS.forCode("urn:ogc:def:crs:EPSG::" + srid);
                    } else {
                        crs = defaultCRS;
                    }
                    final org.locationtech.jts.geom.Geometry geom;
                    if (b != null) {
                        WKBReader reader = new WKBReader();
                        geom             = reader.read(b);
                    } else {
                        continue;
                    }
                    // exclude from spatial filter (will be removed when postgis filter will be set in request)
                    if (spaFilter != null && !spaFilter.intersects(geom)) {
                        continue;
                    }
                    final AbstractGeometry gmlGeom = JTStoGeometry.toGML(gmlVersion, geom, crs);
                    if (gmlGeom instanceof Geometry g) {
                        locations.put(procedure, g);
                    } else {
                        throw new DataStoreException("GML geometry cannot be casted as an Opengis one");
                    }
                    
                } catch (FactoryException | ParseException ex) {
                    throw new DataStoreException(ex);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        }
        if (applyPostPagination) {
            locations = applyPostPagination(locations);
        }
        return locations;
    }

    @Override
    public Map<String, Map<Date, Geometry>> getSensorHistoricalLocations() throws DataStoreException {
        if (decimationSize != null) {
            return getDecimatedSensorLocationsV2(decimationSize);
        }
        final String gmlVersion = getGMLVersion(version);
        if (firstFilter) {
            sqlRequest.replaceFirst("WHERE", "");
        }
        if (obsJoin) {
            String obsJoin = ", \"" + schemaPrefix + "om\".\"observations\" o WHERE o.\"procedure\" = hl.\"procedure\" AND (";
            // profile / single date ts
            obsJoin = obsJoin + "(hl.\"time\" = o.\"time_begin\" AND o.\"time_end\" IS NULL)  OR ";
            // period observation
             obsJoin = obsJoin + "( o.\"time_end\" IS NOT NULL AND hl.\"time\" >= o.\"time_begin\" AND hl.\"time\" <= o.\"time_end\")) ";
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", obsJoin);
            } else {
                sqlRequest.replaceFirst("WHERE", obsJoin + "AND ");
            }
        } else {
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", "");
            }
        }
        sqlRequest.append(" ORDER BY \"procedure\", \"time\"");
        sqlRequest = appendPaginationToRequest(sqlRequest);

        LOGGER.fine(sqlRequest.toString());
        try(final Connection c            = source.getConnection();
            final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
            final ResultSet rs            = pstmt.executeQuery()) {

            SensorLocationProcessor processor = new SensorLocationProcessor(envelopeFilter, gmlVersion);
            return processor.processLocations(rs);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        }
    }

    /**
     * First version of decimation for Sensor Historical Location.
     *
     * We build a cube of decimSize X decimSize X decimSize  with the following dimension : Time, Latitude, longitude.
     * Each results maching the query will be put in the correspounding cell of the cube.
     *
     * Once the cube is complete, we build a entity for each non-empty cell.
     * if multiple entries are in the same cell, we build en entry by getting the centroid of the geometries union.
     *
     * This method is more efficient used with a bbox filter. if not, the whole world will be used for the cube LAT/LON boundaries.
     *
     * this method can return results for multiple procedure so, it will build one cube by procedure.
     *
     * @param hints
     * @param decimSize
     * @return
     * @throws DataStoreException
     */
    private Map<String, Map<Date, Geometry>> getDecimatedSensorLocations(int decimSize) throws DataStoreException {
        final String gmlVersion = getGMLVersion(version);
        FilterSQLRequest stepRequest = sqlRequest.clone();
        if (firstFilter) {
            sqlRequest.replaceFirst("WHERE", "");
        }
        sqlRequest.append(" ORDER BY \"procedure\", \"time\"");
        sqlRequest = appendPaginationToRequest(sqlRequest);

        int nbCell = decimSize;

        GeneralEnvelope env;
        if (envelopeFilter != null) {
            env = envelopeFilter;
        } else {
            env = new GeneralEnvelope(CRS.getDomainOfValidity(defaultCRS));
        }

        try (final Connection c = source.getConnection()) {

            // calculate the first date and the time step for each procedure.
            final Map<Object, long[]> times = getMainFieldStep(stepRequest, DEFAULT_TIME_FIELD, c, nbCell);

            LOGGER.fine(sqlRequest.toString());
            try(final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
                final ResultSet rs = pstmt.executeQuery()) {
                SensorLocationProcessor processor = new SensorLocationDecimator(envelopeFilter, gmlVersion, nbCell, times);
                return processor.processLocations(rs);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        }
    }

    /**
     * Second version of decimation for Sensor Historical Location.
     *
     * Much more simple version of  the decimation.
     * we split all the entries by time frame,
     * and we build an entry using centroid if multiple entries are in the same time slice.
     *
     * @param hints
     * @param decimSize
     * @return
     * @throws DataStoreException
     */
    private Map<String, Map<Date, Geometry>> getDecimatedSensorLocationsV2(int decimSize) throws DataStoreException {
        final String gmlVersion = getGMLVersion(version);
        FilterSQLRequest stepRequest = sqlRequest.clone();
        if (firstFilter) {
            sqlRequest.replaceFirst("WHERE", "");
        }
        sqlRequest.append(" ORDER BY \"procedure\", \"time\"");
        sqlRequest = appendPaginationToRequest(sqlRequest);

        // will be removed when postgis filter will be set in request
        Polygon spaFilter = null;
        if (envelopeFilter != null) {
            spaFilter = JTS.toGeometry(envelopeFilter);
        }

        int nbCell = decimationSize;

        try (final Connection c = source.getConnection()) {

            // calculate the first date and the time step for each procedure.
            final Map<Object, long[]> times = getMainFieldStep(stepRequest, DEFAULT_TIME_FIELD, c, nbCell);
            LOGGER.fine(sqlRequest.toString());
            try(final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
                final ResultSet rs = pstmt.executeQuery()) {
                final SensorLocationProcessor processor = new SensorLocationDecimatorV2(envelopeFilter, gmlVersion, nbCell, times);
                return processor.processLocations(rs);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        }
    }

    @Override
    public Map<String, List<Date>> getSensorTimes() throws DataStoreException {
        if (firstFilter) {
            sqlRequest.replaceFirst("WHERE", "");
        }
        if (obsJoin) {
            String obsJoin = ", \"" + schemaPrefix + "om\".\"observations\" o WHERE o.\"procedure\" = hl.\"procedure\" AND (";
            // profile / single date ts
            obsJoin = obsJoin + "(hl.\"time\" = o.\"time_begin\" AND o.\"time_end\" IS NULL)  OR ";
            // period observation
             obsJoin = obsJoin + "( o.\"time_end\" IS NOT NULL AND hl.\"time\" >= o.\"time_begin\" AND hl.\"time\" <= o.\"time_end\")) ";
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", obsJoin);
            } else {
                sqlRequest.replaceFirst("WHERE", obsJoin + "AND ");
            }
        } else {
            if (firstFilter) {
                sqlRequest.replaceFirst("WHERE", "");
            }
        }

        sqlRequest.append(" ORDER BY \"procedure\", \"time\"");
        sqlRequest = appendPaginationToRequest(sqlRequest);
        LOGGER.fine(sqlRequest.toString());
        Map<String, List<Date>> times = new LinkedHashMap<>();
        try(final Connection c            = source.getConnection();
            final PreparedStatement pstmt = sqlRequest.fillParams(c.prepareStatement(sqlRequest.getRequest()));
            final ResultSet rs            = pstmt.executeQuery()) {
            while (rs.next()) {
                final String procedure = rs.getString("procedure");
                final Date time = new Date(rs.getTimestamp("time").getTime());

                final List<Date> procedureTimes = times.computeIfAbsent(procedure, f -> {return new ArrayList<>();});
                procedureTimes.add(time);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception.", ex);
        } catch (RuntimeException ex) {
            throw new DataStoreException("the service has throw a Runtime Exception:" + ex.getMessage(), ex);
        }
        return times;
    }

    @Override
    public org.geotoolkit.gml.xml.Envelope getCollectionBoundingShape() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
