/*
 *    Examind - An open source and standard compliant SDI
 *    https://community.examind.com
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
package com.examind.store.observation.csv;

import java.nio.file.Path;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Set;
import org.geotoolkit.data.csv.CSVProvider;
import org.geotoolkit.observation.model.ComplexResult;
import org.geotoolkit.observation.model.CompositePhenomenon;
import org.geotoolkit.observation.model.Field;
import org.geotoolkit.observation.model.OMEntity;
import org.geotoolkit.observation.model.Observation;
import org.geotoolkit.observation.model.ObservationDataset;
import org.geotoolkit.observation.model.Phenomenon;
import org.geotoolkit.observation.model.Procedure;
import org.geotoolkit.observation.model.ProcedureDataset;
import org.geotoolkit.observation.query.DatasetQuery;
import org.geotoolkit.observation.query.IdentifierQuery;
import org.geotoolkit.observation.query.ObservedPropertyQuery;
import org.geotoolkit.observation.query.ProcedureQuery;
import org.geotoolkit.temporal.object.TemporalUtilities;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalPrimitive;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CsvObservationStoreTest extends AbstractCsvStoreTest {

    private static Path argoFile;
    private static Path fmlwFile;
    private static Path mooFile;
    private static Path boolProfFile;
    private static Path tsvFile;
    private static Path multiPlatFile;
    private static Path qualSpaceFile;
    private static Path inCompLineFile;
    private static Path propertiesFile;

    @BeforeClass
    public static void setUpClass() throws Exception {
        AbstractCsvStoreTest.setUpClass();
        
        argoFile       = writeResourceFileInDir("argo-profile", "argo-profiles-2902402-1.csv");
        fmlwFile       = writeResourceFileInDir("fmlw-traj", "tsg-FMLW-1.csv");
        mooFile        = writeResourceFileInDir("moo-ts", "mooring-buoys-time-series-62069.csv");
        boolProfFile   = writeResourceFileInDir("bool-prof", "boolean-profile.csv");
        tsvFile        = writeResourceFileInDir("tsv", "tabulation.tsv");
        multiPlatFile  = writeResourceFileInDir("multiPlat", "multiplatform-1.csv");
        qualSpaceFile  = writeResourceFileInDir("qual-space", "quality-space.csv");
        inCompLineFile = writeResourceFileInDir("incomplete-line", "incomplete-line.csv");
        propertiesFile = writeResourceFileInDir("properties", "properties.csv");
    }

    @Test
    public void csvStoreProfileTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();
        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(argoFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("DATE (YYYY-MM-DDTHH:MI:SSZ)");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("PRES (decibar)");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");
        params.parameter(CsvObservationStoreFactory.FOI_COLUMN.getName().getCode()).setValue("CONFIG_MISSION_NUMBER");
        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("LATITUDE (degree_north)");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("LONGITUDE (degree_east)");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("TEMP (degree_Celsius),PSAL (psu)");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Profile");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_ID.getName().getCode()).setValue("urn:sensor:1");
        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(','));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(1, procedureNames.size());

        String sensorId = procedureNames.iterator().next();
        Assert.assertEquals("urn:sensor:1", sensorId);

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("TEMP (degree_Celsius)"));
        Assert.assertTrue(phenomenonNames.contains("PSAL (psu)"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, sensorId);
        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        Period tp = (Period) time;
        Assert.assertEquals("2018-11-02T07:10:52" , format(tp.getBeginning()));
        Assert.assertEquals("2018-11-13T03:55:49" , format(tp.getEnding()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(1, results.procedures.size());
        Assert.assertEquals(4, results.procedures.get(0).spatialBound.getHistoricalLocations().size());

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());
        Assert.assertEquals(1, procedures.size());

        ProcedureDataset pt = procedures.get(0);
        Assert.assertEquals(4, pt.spatialBound.getHistoricalLocations().size());

        time = pt.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("2018-11-02T07:10:52" , format(tp.getBeginning()));
        Assert.assertEquals("2018-11-13T03:55:49" , format(tp.getEnding()));
    }

    @Test
    public void csvStoreTSTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();
        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(mooFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");
        params.parameter(CsvObservationStoreFactory.FOI_COLUMN.getName().getCode()).setValue("CONFIG_MISSION_NUMBER");
        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("LATITUDE (degree_north)");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("LONGITUDE (degree_east)");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("TEMP LEVEL0 (degree_Celsius),VEPK LEVEL0 (meter2 second)");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Timeserie");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_ID.getName().getCode()).setValue("urn:sensor:3");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_NAME_COLUMN.getName().getCode()).setValue("PLATFORM_NAME");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_DESC_COLUMN.getName().getCode()).setValue("PLATFORM_DESC");
        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CsvObservationStoreFactory.UOM_REGEX.getName().getCode()).setValue("\\(([^\\)]+)\\)?");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(','));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(1, procedureNames.size());

        String sensorId = procedureNames.iterator().next();
        Assert.assertEquals("urn:sensor:3", sensorId);

        List<Procedure> sensors = store.getProcedures(new ProcedureQuery());
        Assert.assertEquals(1, sensors.size());
        Procedure proc = (Procedure) sensors.get(0);

        Assert.assertEquals("urn:sensor:3", proc.getId());
        Assert.assertEquals("some name", proc.getName());
        Assert.assertEquals("some description", proc.getDescription());

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("TEMP LEVEL0 (degree_Celsius)"));
        Assert.assertTrue(phenomenonNames.contains("VEPK LEVEL0 (meter2 second)"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, sensorId);
        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        Period tp = (Period) time;
        Assert.assertEquals("2018-10-30T00:29:00" , format(tp.getBeginning()));
        Assert.assertEquals("2018-11-30T11:59:00" , format(tp.getEnding()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(1, results.procedures.size());
        Assert.assertEquals(1, results.procedures.get(0).spatialBound.getHistoricalLocations().size());

        Assert.assertEquals(1, results.observations.size());
        Observation obs = results.observations.get(0);
        Assert.assertTrue(obs.getResult() instanceof ComplexResult);
        ComplexResult cr = (ComplexResult) obs.getResult();

        Assert.assertEquals(3, cr.getFields().size());

        Field f = cr.getFields().get(1);
        Assert.assertEquals("degree_Celsius", f.uom);

        f = cr.getFields().get(2);
        Assert.assertEquals("meter2 second", f.uom);

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());
        Assert.assertEquals(1, procedures.size());

        ProcedureDataset pt = procedures.get(0);
        Assert.assertEquals(1, pt.spatialBound.getHistoricalLocations().size());

        time = pt.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("2018-10-30T00:29:00" , format(tp.getBeginning()));
        Assert.assertEquals("2018-11-30T11:59:00" , format(tp.getEnding()));
    }

    @Test
    public void csvStoreBooleanProfileTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();
        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(boolProfFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("TIME");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("DEPTH");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss.S");
        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("LAT");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("LON");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("STATUS");
        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN_TYPE.getName().getCode()).setValue("BOOLEAN");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Profile");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_ID.getName().getCode()).setValue("urn:sensor:bp");
        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(','));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(1, procedureNames.size());

        String sensorId = procedureNames.iterator().next();
        Assert.assertEquals("urn:sensor:bp", sensorId);

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("STATUS"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, sensorId);
        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Temporal tp = TemporalUtilities.toTemporal(time).orElseThrow();
        Assert.assertEquals("1980-03-01T21:52:00", format(tp));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(1, results.procedures.size());
        Assert.assertEquals(1, results.procedures.get(0).spatialBound.getHistoricalLocations().size());

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());
        Assert.assertEquals(1, procedures.size());

        ProcedureDataset pt = procedures.get(0);
        Assert.assertEquals(1, pt.spatialBound.getHistoricalLocations().size());

        time = pt.spatialBound.getTimeObject();

        tp = TemporalUtilities.toTemporal(time).orElseThrow();
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp));
    }

    @Test
    public void csvStoreTJTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();
        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(fmlwFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");

        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("LATITUDE (degree_north)");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("LONGITUDE (degree_east)");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("PSAL LEVEL1 (psu)");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Trajectory");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_ID.getName().getCode()).setValue("urn:sensor:4");
        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(','));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(1, procedureNames.size());

        String sensorId = procedureNames.iterator().next();
        Assert.assertEquals("urn:sensor:4", sensorId);

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("PSAL LEVEL1 (psu)"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, sensorId);
        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        Period tp = (Period) time;
        Assert.assertEquals("2018-10-30T00:00:00" , format(tp.getBeginning()));
        Assert.assertEquals("2018-10-30T06:45:00" , format(tp.getEnding()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(1, results.procedures.size());
        Assert.assertEquals(406, results.procedures.get(0).spatialBound.getHistoricalLocations().size());

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());
        Assert.assertEquals(1, procedures.size());

        ProcedureDataset pt = procedures.get(0);
        Assert.assertEquals(406, pt.spatialBound.getHistoricalLocations().size());

        time = pt.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("2018-10-30T00:00:00" , format(tp.getBeginning()));
        Assert.assertEquals("2018-10-30T06:45:00" , format(tp.getEnding()));
    }

    @Test
    public void csvStoreTSVTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();
        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(tsvFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("TIME");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("TIME");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss.S");

        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("LAT");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("LON");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("TEMPERATURE");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Timeserie");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_ID.getName().getCode()).setValue("urn:sensor:5");
        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("tsv");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf('\t'));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(1, procedureNames.size());

        String sensorId = procedureNames.iterator().next();
        Assert.assertEquals("urn:sensor:5", sensorId);

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("TEMPERATURE"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, sensorId);
        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        Period tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-02T21:52:00" , format(tp.getEnding()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(1, results.procedures.size());
        Assert.assertEquals(1, results.procedures.get(0).spatialBound.getHistoricalLocations().size());

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());
        Assert.assertEquals(1, procedures.size());

        ProcedureDataset pt = procedures.get(0);
        Assert.assertEquals(1, pt.spatialBound.getHistoricalLocations().size());

        time = pt.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-02T21:52:00" , format(tp.getEnding()));
    }

    @Test
    public void harvestCSVTSMultiPlatformTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();

        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(multiPlatFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("DATE");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("DATE");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");

        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("LATITUDE");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("LONGITUDE");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("TEMP (degree_Celsius),VEPK (meter2 second)");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Timeserie");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_COLUMN.getName().getCode()).setValue("PLATFORM");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_REGEX.getName().getCode()).setValue("(^[^/]*)");
        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(','));

         CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());

        // verify that the sensor has been created
        Assert.assertTrue(procedureNames.contains("p001"));
        Assert.assertTrue(procedureNames.contains("p002"));

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("TEMP (degree_Celsius)"));
        Assert.assertTrue(phenomenonNames.contains("VEPK (meter2 second)"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, "p001");
        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        Period tp = (Period) time;
        Assert.assertEquals("2000-07-28T00:30:00", format(tp.getBeginning()));
        Assert.assertEquals("2000-07-29T23:30:00",format(tp.getEnding()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(2, results.procedures.size());
        Assert.assertEquals(2, results.procedures.get(0).spatialBound.getHistoricalLocations().size());

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());
        Assert.assertEquals(2, procedures.size());

        ProcedureDataset pt = procedures.get(0);
        Assert.assertEquals(2, pt.spatialBound.getHistoricalLocations().size());

        time = pt.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("2000-07-28T00:30:00" , format(tp.getBeginning()));
        Assert.assertEquals("2000-07-29T23:30:00" , format(tp.getEnding()));
    }

    @Test
    public void csvStoreQualitySpaceTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();
        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(qualSpaceFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("TIME");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("TIME");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss.S");

        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("LAT");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("LON");

        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("TEMPERATURE");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Timeserie");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_ID.getName().getCode()).setValue("urn:space-qual:1");
        params.parameter(CsvObservationStoreFactory.QUALITY_COLUMN.getName().getCode()).setValue("QUA LITY FI");


        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(';'));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(1, procedureNames.size());

        String sensorId = "urn:space-qual:1";
        Assert.assertTrue(procedureNames.contains(sensorId));

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("TEMPERATURE"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, sensorId);
        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        Period tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-02T21:52:00" , format(tp.getEnding()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(1, results.procedures.size());
        ProcedureDataset proc = results.procedures.get(0);
        Assert.assertEquals(sensorId, proc.getId());
        Assert.assertEquals(1, proc.spatialBound.getHistoricalLocations().size());

        Assert.assertEquals(1, results.observations.size());
        Observation obs = results.observations.get(0);
        Assert.assertTrue(obs.getResult() instanceof ComplexResult);
        ComplexResult cr = (ComplexResult) obs.getResult();

        Assert.assertEquals(2, cr.getFields().size());

        Field f = cr.getFields().get(1);
        Assert.assertEquals(1, f.qualityFields.size());

        Field qualityField = f.qualityFields.get(0);
        Assert.assertEquals("qua_lity_fi", qualityField.name);

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());

        Assert.assertEquals(1, procedures.size());
        proc = procedures.get(0);
        Assert.assertEquals(sensorId, proc.getId());
        Assert.assertEquals(1, proc.spatialBound.getHistoricalLocations().size());

        time = proc.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-02T21:52:00" , format(tp.getEnding()));
    }
    
    @Test
    public void csvStorePropertiesTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();
        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(propertiesFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("TIME");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("TIME");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss.S");

        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("LAT");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("LON");

        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("TEMPERATURE");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Timeserie");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_ID.getName().getCode()).setValue("urn:properties:1");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_PROPERTIES_MAP_COLUMN.getName().getCode()).setValue("METADATA");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_PROPERTIES_COLUMN.getName().getCode()).setValue("PROP3");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(';'));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(1, procedureNames.size());

        String sensorId = "urn:properties:1";
        Assert.assertTrue(procedureNames.contains(sensorId));

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("TEMPERATURE"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, sensorId);
        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        Period tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-02T21:52:00" , format(tp.getEnding()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(1, results.procedures.size());
        ProcedureDataset proc = results.procedures.get(0);
        Assert.assertEquals(sensorId, proc.getId());
        Assert.assertEquals(1, proc.spatialBound.getHistoricalLocations().size());
        Assert.assertEquals(3, proc.getProperties().size());
        Assert.assertTrue(proc.getProperties().containsKey("PROP1"));
        Assert.assertEquals("p1", proc.getProperties().get("PROP1"));
        Assert.assertTrue(proc.getProperties().containsKey("PROP2"));
        Assert.assertTrue(proc.getProperties().get("PROP2") instanceof List);
        List prop2 = (List) proc.getProperties().get("PROP2");
        Assert.assertEquals(2, prop2.size());
        Assert.assertEquals("p2_1", prop2.get(0));
        Assert.assertEquals("p2_2", prop2.get(1));
        Assert.assertTrue(proc.getProperties().containsKey("PROP3"));
        Assert.assertEquals("p3", proc.getProperties().get("PROP3"));

        Assert.assertEquals(1, results.observations.size());
        Observation obs = results.observations.get(0);
        Assert.assertTrue(obs.getResult() instanceof ComplexResult);
        ComplexResult cr = (ComplexResult) obs.getResult();

        Assert.assertEquals(2, cr.getFields().size());

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());

        Assert.assertEquals(1, procedures.size());
        proc = procedures.get(0);
        Assert.assertEquals(sensorId, proc.getId());
        Assert.assertEquals(1, proc.spatialBound.getHistoricalLocations().size());
        Assert.assertEquals(3, proc.getProperties().size());
        Assert.assertTrue(proc.getProperties().containsKey("PROP1"));
        Assert.assertEquals("p1", proc.getProperties().get("PROP1"));
        Assert.assertTrue(proc.getProperties().containsKey("PROP2"));
        Assert.assertTrue(proc.getProperties().get("PROP2") instanceof List);
        prop2 = (List) proc.getProperties().get("PROP2");
        Assert.assertEquals(2, prop2.size());
        Assert.assertEquals("p2_1", prop2.get(0));
        Assert.assertEquals("p2_2", prop2.get(1));
        Assert.assertTrue(proc.getProperties().containsKey("PROP3"));
        Assert.assertEquals("p3", proc.getProperties().get("PROP3"));

        time = proc.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-02T21:52:00" , format(tp.getEnding()));
    }

    @Test
    public void csvStoreRenameQualityTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();
        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(qualSpaceFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("TIME");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("TIME");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss.S");

        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("LAT");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("LON");

        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("TEMPERATURE");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Timeserie");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_ID.getName().getCode()).setValue("urn:space-qual:1");
        params.parameter(CsvObservationStoreFactory.QUALITY_COLUMN.getName().getCode()).setValue("QUA LITY FI");
        params.parameter(CsvObservationStoreFactory.QUALITY_COLUMN_ID.getName().getCode()).setValue("new_quality_name");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(';'));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(1, procedureNames.size());

        String sensorId = "urn:space-qual:1";
        Assert.assertTrue(procedureNames.contains(sensorId));

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("TEMPERATURE"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, sensorId);
        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        Period tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-02T21:52:00" , format(tp.getEnding()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(1, results.procedures.size());
        ProcedureDataset proc = results.procedures.get(0);
        Assert.assertEquals(sensorId, proc.getId());
        Assert.assertEquals(1, proc.spatialBound.getHistoricalLocations().size());

        Assert.assertEquals(1, results.observations.size());
        Observation obs = results.observations.get(0);
        Assert.assertTrue(obs.getResult() instanceof ComplexResult);
        ComplexResult cr = (ComplexResult) obs.getResult();

        Assert.assertEquals(2, cr.getFields().size());

        Field f = cr.getFields().get(1);
        Assert.assertEquals(1, f.qualityFields.size());

        Field qualityField = f.qualityFields.get(0);
        Assert.assertEquals("new_quality_name", qualityField.name);

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());

        Assert.assertEquals(1, procedures.size());
        proc = procedures.get(0);
        Assert.assertEquals(sensorId, proc.getId());
        Assert.assertEquals(1, proc.spatialBound.getHistoricalLocations().size());

        time = proc.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-02T21:52:00" , format(tp.getEnding()));
    }

    @Test
    public void csvStoreFixedTSTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();
        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(mooFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");
        params.parameter(CsvObservationStoreFactory.FOI_COLUMN.getName().getCode()).setValue("CONFIG_MISSION_NUMBER");
        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("LATITUDE (degree_north)");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("LONGITUDE (degree_east)");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("TEMP LEVEL0 (degree_Celsius),VEPK LEVEL0 (meter2 second)");
        params.parameter(CsvObservationStoreFactory.OBS_PROP_ID.getName().getCode()).setValue("TEMP,VEPK");
        params.parameter(CsvObservationStoreFactory.OBS_PROP_NAME.getName().getCode()).setValue("Temperature,Velocity");
        params.parameter(CsvObservationStoreFactory.OBS_PROP_DESC.getName().getCode()).setValue("water temperature,velocity of a raptor");

        params.parameter(CsvObservationStoreFactory.UOM_ID.getName().getCode()).setValue("°C,m2/s");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Timeserie");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_ID.getName().getCode()).setValue("urn:sensor:fixed");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_NAME.getName().getCode()).setValue("fixed name");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_DESC.getName().getCode()).setValue("fixed platform");
        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(','));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(1, procedureNames.size());

        String sensorId = procedureNames.iterator().next();
        Assert.assertEquals("urn:sensor:fixed", sensorId);

        List<Procedure> sensors = store.getProcedures(new ProcedureQuery());
        Assert.assertEquals(1, sensors.size());
        Procedure proc = (Procedure) sensors.get(0);

        Assert.assertEquals("urn:sensor:fixed", proc.getId());
        Assert.assertEquals("fixed name", proc.getName());
        Assert.assertEquals("fixed platform", proc.getDescription());

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("TEMP"));
        Assert.assertTrue(phenomenonNames.contains("VEPK"));
        
        List<Phenomenon> phenomenons = store.getPhenomenons(new ObservedPropertyQuery(true));
        Assert.assertEquals(1, phenomenons.size());
        Assert.assertTrue(phenomenons.get(0) instanceof CompositePhenomenon);
        CompositePhenomenon composite = (CompositePhenomenon) phenomenons.get(0);
        Assert.assertEquals(2, composite.getComponent().size());
        Assert.assertEquals("TEMP", composite.getComponent().get(0).getId());
        Assert.assertEquals("Temperature", composite.getComponent().get(0).getName());
        Assert.assertEquals("water temperature", composite.getComponent().get(0).getDescription());
        
        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, sensorId);
        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        Period tp = (Period) time;
        Assert.assertEquals("2018-10-30T00:29:00" , format(tp.getBeginning()));
        Assert.assertEquals("2018-11-30T11:59:00" , format(tp.getEnding()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(1, results.procedures.size());
        Assert.assertEquals(1, results.procedures.get(0).spatialBound.getHistoricalLocations().size());

        Assert.assertEquals(1, results.observations.size());
        Observation obs = results.observations.get(0);
        Assert.assertTrue(obs.getResult() instanceof ComplexResult);
        ComplexResult cr = (ComplexResult) obs.getResult();

        Assert.assertEquals(3, cr.getFields().size());

        Field f = cr.getFields().get(1);
        Assert.assertEquals("°C", f.uom);

        f = cr.getFields().get(2);
        Assert.assertEquals("m2/s", f.uom);

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());
        Assert.assertEquals(1, procedures.size());

        ProcedureDataset pt = procedures.get(0);
        Assert.assertEquals(1, pt.spatialBound.getHistoricalLocations().size());

        time = pt.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("2018-10-30T00:29:00" , format(tp.getBeginning()));
        Assert.assertEquals("2018-11-30T11:59:00" , format(tp.getEnding()));

        Assert.assertEquals(3, pt.fields.size());
    }

    @Test
    public void csvStoreImcompleteLinesTest() throws Exception {

        CsvObservationStoreFactory factory = new CsvObservationStoreFactory();
        ParameterValueGroup params = factory.getOpenParameters().createValue();
        params.parameter(CsvObservationStoreFactory.LOCATION).setValue(inCompLineFile.toUri().toString());

        params.parameter(CsvObservationStoreFactory.DATE_COLUMN.getName().getCode()).setValue("time");
        params.parameter(CsvObservationStoreFactory.MAIN_COLUMN.getName().getCode()).setValue("time");

        params.parameter(CsvObservationStoreFactory.DATE_FORMAT.getName().getCode()).setValue("yyyy-MM-dd'T'HH:mm:ss.S");
        params.parameter(CsvObservationStoreFactory.LATITUDE_COLUMN.getName().getCode()).setValue("lat");
        params.parameter(CsvObservationStoreFactory.LONGITUDE_COLUMN.getName().getCode()).setValue("lon");

        params.parameter(CsvObservationStoreFactory.OBS_PROP_COLUMN.getName().getCode()).setValue("salinity,temperature");

        params.parameter(CsvObservationStoreFactory.OBSERVATION_TYPE.getName().getCode()).setValue("Timeserie");
        params.parameter(CsvObservationStoreFactory.PROCEDURE_COLUMN.getName().getCode()).setValue("plat");
        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(','));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(2, procedureNames.size());
        Assert.assertTrue(procedureNames.contains("P1"));
        Assert.assertTrue(procedureNames.contains("P2"));

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertEquals(2, phenomenonNames.size());
        Assert.assertTrue(phenomenonNames.contains("salinity"));
        Assert.assertTrue(phenomenonNames.contains("temperature"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, "P1");

        TemporalPrimitive time = store.getEntityTemporalBounds(timeQuery);
        Assert.assertTrue(time instanceof Period);

        Period tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-02T21:52:00" , format(tp.getEnding()));

        timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, "P2");

        time = store.getEntityTemporalBounds(timeQuery);
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("1980-03-03T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-04T21:52:00" , format(tp.getEnding()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(2, results.procedures.size());
        Assert.assertEquals(1, results.procedures.get(0).spatialBound.getHistoricalLocations().size());
        Assert.assertEquals(1, results.procedures.get(1).spatialBound.getHistoricalLocations().size());

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());
        Assert.assertEquals(2, procedures.size());

        ProcedureDataset pt = procedures.get(0);
        Assert.assertEquals(1, pt.spatialBound.getHistoricalLocations().size());

        time = pt.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-02T21:52:00" , format(tp.getEnding()));

        pt = procedures.get(1);
        Assert.assertEquals(1, pt.spatialBound.getHistoricalLocations().size());

        time = pt.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Period);

        tp = (Period) time;
        Assert.assertEquals("1980-03-03T21:52:00" , format(tp.getBeginning()));
        Assert.assertEquals("1980-03-04T21:52:00" , format(tp.getEnding()));
    }
}
