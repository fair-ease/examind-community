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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.constellation.test.utils.TestResourceUtils.writeResourceDataFile;
import org.geotoolkit.data.csv.CSVProvider;
import org.geotoolkit.nio.IOUtilities;
import org.geotoolkit.observation.model.OMEntity;
import org.geotoolkit.observation.model.ObservationDataset;
import org.geotoolkit.observation.model.ProcedureDataset;
import org.geotoolkit.observation.query.DatasetQuery;
import org.geotoolkit.observation.query.IdentifierQuery;
import org.geotoolkit.observation.query.ObservedPropertyQuery;
import org.geotoolkit.observation.query.ProcedureQuery;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalGeometricPrimitive;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CsvObservationStoreTest {

    private static Path DATA_DIRECTORY;
    private static Path argoFile;
    private static Path fmlwFile;
    private static Path mooFile;
    private static Path boolProfFile;
    private static Path tsvFile;

    @BeforeClass
    public static void setUpClass() throws Exception {

        final Path configDir = Paths.get("target");
        DATA_DIRECTORY       = configDir.resolve("data"  + UUID.randomUUID());
        Path argoDirectory       = DATA_DIRECTORY.resolve("argo-profile");
        Files.createDirectories(argoDirectory);
        Path fmlwDirectory       = DATA_DIRECTORY.resolve("fmlw-traj");
        Files.createDirectories(fmlwDirectory);
        Path mooDirectory       = DATA_DIRECTORY.resolve("moo-ts");
        Files.createDirectories(mooDirectory);
        Path bopDirectory       = DATA_DIRECTORY.resolve("bool-prof");
        Files.createDirectories(bopDirectory);
        Path tsvDirectory       = DATA_DIRECTORY.resolve("tsv");
        Files.createDirectories(tsvDirectory);

        writeResourceDataFile(argoDirectory, "com/examind/process/sos/argo-profiles-2902402-1.csv", "argo-profiles-2902402-1.csv");
        argoFile = argoDirectory.resolve("argo-profiles-2902402-1.csv");

        writeResourceDataFile(fmlwDirectory, "com/examind/process/sos/tsg-FMLW-1.csv", "tsg-FMLW-1.csv");
        fmlwFile = fmlwDirectory.resolve("tsg-FMLW-1.csv");

        writeResourceDataFile(mooDirectory,  "com/examind/process/sos/mooring-buoys-time-series-62069.csv", "mooring-buoys-time-series-62069.csv");
        mooFile = mooDirectory.resolve("mooring-buoys-time-series-62069.csv");

        writeResourceDataFile(mooDirectory,  "com/examind/process/sos/boolean-profile.csv", "boolean-profile.csv");
        boolProfFile = mooDirectory.resolve("boolean-profile.csv");
        writeResourceDataFile(tsvDirectory,  "com/examind/process/sos/tabulation.tsv", "tabulation.tsv");
        tsvFile = tsvDirectory.resolve("tabulation.tsv");
    }


    @AfterClass
    public static void tearDownClass() throws Exception {
        IOUtilities.deleteSilently(DATA_DIRECTORY);
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
        TemporalGeometricPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");

        Period tp = (Period) time;
        Assert.assertEquals("2018-11-02T07:10:52.000" , sdf.format(tp.getBeginning().getDate()));
        Assert.assertEquals("2018-11-13T03:55:49.000" , sdf.format(tp.getEnding().getDate()));

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
        Assert.assertEquals("2018-11-02T07:10:52.000" , sdf.format(tp.getBeginning().getDate()));
        Assert.assertEquals("2018-11-13T03:55:49.000" , sdf.format(tp.getEnding().getDate()));
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
        params.parameter(CsvObservationStoreFactory.PROCEDURE_DESC_COLUMN.getName().getCode()).setValue("PLATFORM_DESC");
        params.parameter(CsvObservationStoreFactory.FILE_MIME_TYPE.getName().getCode()).setValue("csv");

        params.parameter(CSVProvider.SEPARATOR.getName().getCode()).setValue(Character.valueOf(','));

        CsvObservationStore store = factory.open(params);

        Set<String> procedureNames = store.getEntityNames(new ProcedureQuery());
        Assert.assertEquals(1, procedureNames.size());

        String sensorId = procedureNames.iterator().next();
        Assert.assertEquals("urn:sensor:3", sensorId);

        Set<String> phenomenonNames = store.getEntityNames(new ObservedPropertyQuery());
        Assert.assertTrue(phenomenonNames.contains("TEMP LEVEL0 (degree_Celsius)"));
        Assert.assertTrue(phenomenonNames.contains("VEPK LEVEL0 (meter2 second)"));

        IdentifierQuery timeQuery = new IdentifierQuery(OMEntity.PROCEDURE, sensorId);
        TemporalGeometricPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        
        Period tp = (Period) time;
        Assert.assertEquals("2018-10-30T00:29:00.000" , sdf.format(tp.getBeginning().getDate()));
        Assert.assertEquals("2018-11-30T11:59:00.000" , sdf.format(tp.getEnding().getDate()));

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
        Assert.assertEquals("2018-10-30T00:29:00.000" , sdf.format(tp.getBeginning().getDate()));
        Assert.assertEquals("2018-11-30T11:59:00.000" , sdf.format(tp.getEnding().getDate()));
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
        TemporalGeometricPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Instant);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        Instant tp = (Instant) time;
        Assert.assertEquals("1980-03-01T21:52:00.000" , sdf.format(tp.getDate()));

        ObservationDataset results = store.getDataset(new DatasetQuery());
        Assert.assertEquals(1, results.procedures.size());
        Assert.assertEquals(1, results.procedures.get(0).spatialBound.getHistoricalLocations().size());

        List<ProcedureDataset> procedures = store.getProcedureDatasets(new DatasetQuery());
        Assert.assertEquals(1, procedures.size());

        ProcedureDataset pt = procedures.get(0);
        Assert.assertEquals(1, pt.spatialBound.getHistoricalLocations().size());

        time = pt.spatialBound.getTimeObject();
        Assert.assertTrue(time instanceof Instant);

        tp = (Instant) time;
        Assert.assertEquals("1980-03-01T21:52:00.000" , sdf.format(tp.getDate()));
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
        TemporalGeometricPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        Period tp = (Period) time;
        Assert.assertEquals("2018-10-30T00:00:00.000" , sdf.format(tp.getBeginning().getDate()));
        Assert.assertEquals("2018-10-30T06:45:00.000" , sdf.format(tp.getEnding().getDate()));

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
        Assert.assertEquals("2018-10-30T00:00:00.000" , sdf.format(tp.getBeginning().getDate()));
        Assert.assertEquals("2018-10-30T06:45:00.000" , sdf.format(tp.getEnding().getDate()));
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
        TemporalGeometricPrimitive time = store.getEntityTemporalBounds(timeQuery);

        Assert.assertTrue(time instanceof Period);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        Period tp = (Period) time;
        Assert.assertEquals("1980-03-01T21:52:00.000" , sdf.format(tp.getBeginning().getDate()));
        Assert.assertEquals("1980-03-02T21:52:00.000" , sdf.format(tp.getEnding().getDate()));

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
        Assert.assertEquals("1980-03-01T21:52:00.000" , sdf.format(tp.getBeginning().getDate()));
        Assert.assertEquals("1980-03-02T21:52:00.000" , sdf.format(tp.getEnding().getDate()));
    }
}
