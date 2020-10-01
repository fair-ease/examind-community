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
package com.examind.process.sos;

import com.examind.sts.core.STSWorker;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.SpringHelper;
import org.constellation.admin.WSEngine;
import org.constellation.business.IDatasetBusiness;
import org.constellation.business.IDatasourceBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.ISensorBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.dto.Sensor;
import org.constellation.dto.process.ServiceProcessReference;
import org.constellation.dto.service.ServiceComplete;
import org.constellation.dto.service.config.sos.SOSConfiguration;
import org.constellation.process.ExamindProcessFactory;
import org.constellation.sos.core.SOSworker;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.test.utils.TestEnvironment.TestResource;
import org.constellation.test.utils.TestEnvironment.TestResources;
import static org.constellation.test.utils.TestEnvironment.initDataDirectory;
import static org.constellation.test.utils.TestResourceUtils.getResourceAsString;
import static org.constellation.test.utils.TestResourceUtils.writeResourceDataFile;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.internal.geojson.binding.GeoJSONFeature;
import org.geotoolkit.internal.geojson.binding.GeoJSONGeometry.GeoJSONPoint;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.sos.xml.Capabilities;
import org.geotoolkit.sos.xml.Contents;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.v200.GetCapabilitiesType;
import org.geotoolkit.sos.xml.v200.GetObservationResponseType;
import org.geotoolkit.sos.xml.v200.GetObservationType;
import org.geotoolkit.sos.xml.v200.GetResultResponseType;
import org.geotoolkit.sos.xml.v200.GetResultType;
import org.geotoolkit.sts.GetHistoricalLocations;
import org.geotoolkit.sts.json.HistoricalLocation;
import org.geotoolkit.sts.json.HistoricalLocationsResponse;
import org.geotoolkit.swe.xml.v200.DataArrayPropertyType;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,DirtiesContextTestExecutionListener.class})
@DirtiesContext(hierarchyMode = DirtiesContext.HierarchyMode.EXHAUSTIVE,classMode=DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(inheritInitializers = false, locations={"classpath:/cstl/spring/test-context.xml"})
@RunWith(SpringTestRunner.class)
public class SosHarvesterProcessTest {

    private static final Logger LOGGER = Logging.getLogger("com.examind.process.sos");

    private static boolean initialized = false;

    // CSV dir
    private static Path argoDirectory;
    private static Path fmlwDirectory;
    private static Path mooDirectory;
    private static Path multiPlatDirectory;
    private static Path bigdataDirectory;

    // DBF dir
    private static Path ltDirectory;
    private static Path rtDirectory;

    @Inject
    protected IServiceBusiness serviceBusiness;
    @Inject
    protected IDatasourceBusiness datasourceBusiness;
    @Inject
    protected IProviderBusiness providerBusiness;
    @Inject
    protected ISensorBusiness sensorBusiness;
    @Inject
    protected IDatasetBusiness datasetBusiness;

    @Inject
    protected WSEngine wsEngine;

    private static final String CONFIG_DIR_NAME = "SosHarvesterProcessTest" + UUID.randomUUID().toString();

    @BeforeClass
    public static void setUpClass() throws Exception {

        final Path configDir = ConfigDirectory.setupTestEnvironement(CONFIG_DIR_NAME);
        Path dataDirectory  = configDir.resolve("data");
        argoDirectory       = dataDirectory.resolve("argo-profile");
        Files.createDirectories(argoDirectory);
        fmlwDirectory       = dataDirectory.resolve("fmlw-traj");
        Files.createDirectories(fmlwDirectory);
        mooDirectory       = dataDirectory.resolve("moo-ts");
        Files.createDirectories(mooDirectory);
        ltDirectory       = dataDirectory.resolve("lt-ts");
        Files.createDirectories(ltDirectory);
        rtDirectory       = dataDirectory.resolve("rt-ts");
        Files.createDirectories(rtDirectory);
        multiPlatDirectory = dataDirectory.resolve("multi-plat");
        Files.createDirectories(multiPlatDirectory);
        bigdataDirectory = dataDirectory.resolve("bigdata-profile");
        Files.createDirectories(bigdataDirectory);

        writeResourceDataFile(argoDirectory, "com/examind/process/sos/argo-profiles-2902402-1.csv", "argo-profiles-2902402-1.csv");

        writeResourceDataFile(fmlwDirectory, "com/examind/process/sos/tsg-FMLW-1.csv", "tsg-FMLW-1.csv");
        writeResourceDataFile(fmlwDirectory, "com/examind/process/sos/tsg-FMLW-2.csv", "tsg-FMLW-2.csv");
        writeResourceDataFile(fmlwDirectory, "com/examind/process/sos/tsg-FMLW-3.csv", "tsg-FMLW-3.csv");

        writeResourceDataFile(mooDirectory,  "com/examind/process/sos/mooring-buoys-time-series-62069.csv", "mooring-buoys-time-series-62069.csv");

        writeResourceDataFile(ltDirectory,   "com/examind/process/sos/LakeTile_001.dbf", "LakeTile_001.dbf");
        writeResourceDataFile(ltDirectory,   "com/examind/process/sos/LakeTile_002.dbf", "LakeTile_002.dbf");
        
        writeResourceDataFile(rtDirectory,   "com/examind/process/sos/rivertile_001.dbf", "rivertile_001.dbf");
        writeResourceDataFile(rtDirectory,   "com/examind/process/sos/rivertile_002.dbf", "rivertile_002.dbf");

        writeResourceDataFile(multiPlatDirectory,   "com/examind/process/sos/multiplatform-1.csv", "multiplatform-1.csv");
        writeResourceDataFile(multiPlatDirectory,   "com/examind/process/sos/multiplatform-2.csv", "multiplatform-2.csv");

        writeResourceDataFile(bigdataDirectory, "com/examind/process/sos/bigdata-1.csv", "bigdata-1.csv");
    }

    @PostConstruct
    public void setUp() {
        try {

            if (!initialized) {
                // clean up
                serviceBusiness.deleteAll();
                providerBusiness.removeAll();
                datasourceBusiness.deleteAll();

                final TestResources testResource = initDataDirectory();

                Integer pid = testResource.createProvider(TestResource.OM2_DB, providerBusiness);

                //we write the configuration file
                final SOSConfiguration configuration = new SOSConfiguration();
                configuration.setProfile("transactional");
                configuration.getParameters().put("transactionSecurized", "false");

                Integer sid = serviceBusiness.create("sos", "default", configuration, null, null);
                serviceBusiness.linkServiceAndProvider(sid, pid);
                serviceBusiness.start(sid);

                sid = serviceBusiness.create("sts", "default", configuration, null, null);
                serviceBusiness.linkServiceAndProvider(sid, pid);
                serviceBusiness.start(sid);

                initialized = true;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            IServiceBusiness sb = SpringHelper.getBean(IServiceBusiness.class);
            if (sb != null) {
                sb.deleteAll();
            }
            IProviderBusiness pb = SpringHelper.getBean(IProviderBusiness.class);
            if (pb != null) {
                pb.removeAll();
            }
            IDatasourceBusiness dsb = SpringHelper.getBean(IDatasourceBusiness.class);
            if (dsb != null) {
                dsb.deleteAll();
            }
            File derbyLog = new File("derby.log");
            if (derbyLog.exists()) {
                derbyLog.delete();
            }
            File mappingFile = new File("mapping.properties");
            if (mappingFile.exists()) {
                mappingFile.delete();
            }
            ConfigDirectory.shutdownTestEnvironement(CONFIG_DIR_NAME);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    @Test
    @Order(order = 1)
    public void harvestCSVProfileTest() throws Exception {

        ServiceComplete sc = serviceBusiness.getServiceByIdentifierAndType("sos", "default");
        Assert.assertNotNull(sc);

        String sensorId = "urn:sensor:1";

        String datasetId = "SOS_DATA";

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ExamindProcessFactory.NAME, SosHarvesterProcessDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SosHarvesterProcessDescriptor.DATASET_IDENTIFIER_NAME).setValue(datasetId);
        in.parameter(SosHarvesterProcessDescriptor.DATA_FOLDER_NAME).setValue(argoDirectory.toUri().toString());

        in.parameter(SosHarvesterProcessDescriptor.DATE_COLUMN_NAME).setValue("DATE (YYYY-MM-DDTHH:MI:SSZ)");
        in.parameter(SosHarvesterProcessDescriptor.MAIN_COLUMN_NAME).setValue("PRES (decibar)");

        in.parameter(SosHarvesterProcessDescriptor.DATE_FORMAT_NAME).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");
        in.parameter(SosHarvesterProcessDescriptor.FOI_COLUMN_NAME).setValue("CONFIG_MISSION_NUMBER");
        in.parameter(SosHarvesterProcessDescriptor.LATITUDE_COLUMN_NAME).setValue("LATITUDE (degree_north)");
        in.parameter(SosHarvesterProcessDescriptor.LONGITUDE_COLUMN_NAME).setValue("LONGITUDE (degree_east)");

        in.parameter(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).setValue("TEMP (degree_Celsius)");
        in.parameter(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).setValue("PSAL (psu)");

        in.parameter(SosHarvesterProcessDescriptor.OBS_TYPE_NAME).setValue("Profile");
        in.parameter(SosHarvesterProcessDescriptor.PROCEDURE_ID_NAME).setValue(sensorId);
        in.parameter(SosHarvesterProcessDescriptor.REMOVE_PREVIOUS_NAME).setValue(false);
        in.parameter(SosHarvesterProcessDescriptor.SERVICE_ID_NAME).setValue(new ServiceProcessReference(sc));

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        // verify that the dataset has been created
        Assert.assertNotNull(datasetBusiness.getDatasetId(datasetId));

        // verify that the sensor has been created
        Assert.assertNotNull(sensorBusiness.getSensor(sensorId));

        SOSworker worker = (SOSworker) wsEngine.buildWorker("sos", "default");
        worker.setServiceUrl("http://localhost/examind/");

        STSWorker stsWorker = (STSWorker) wsEngine.buildWorker("sts", "default");
        stsWorker.setServiceUrl("http://localhost/examind/");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        ObservationOffering offp = getOffering(worker, sensorId);
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        TimePeriodType time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2018-11-02T07:10:52.000", time.getBeginPosition().getValue());
        Assert.assertEquals("2018-11-13T03:55:49.000", time.getEndPosition().getValue());

        Assert.assertEquals(4, offp.getFeatureOfInterestIds().size());

        GetHistoricalLocations hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", sensorId);
        hl.getExpand().add("Locations");
        HistoricalLocationsResponse response = stsWorker.getHistoricalLocations(hl);

        Assert.assertEquals(4, response.getValue().size());

        HistoricalLocation loc1 = response.getValue().get(0);
        Assert.assertEquals(loc1.getTime().getTime(), sdf.parse("2018-11-02T07:10:52Z").getTime());
        Assert.assertEquals(1, loc1.getLocations().size());
        Assert.assertTrue(loc1.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        GeoJSONFeature feat1 = (GeoJSONFeature) loc1.getLocations().get(0).getLocation();
        Assert.assertTrue(feat1.getGeometry() instanceof GeoJSONPoint);
        GeoJSONPoint pt1 = (GeoJSONPoint) feat1.getGeometry();
        Assert.assertEquals(44.06, pt1.getCoordinates()[1], 0);
        Assert.assertEquals(-6.81, pt1.getCoordinates()[0], 0);

        /*
         * add a new file to integrate and call again the process
         */
        writeResourceDataFile(argoDirectory, "com/examind/process/sos/argo-profiles-2902402-2.csv", "argo-profiles-2902402-2.csv");
        proc.call();

        offp = getOffering(worker, sensorId);
        Assert.assertNotNull(offp);
        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2018-11-02T07:10:52.000", time.getBeginPosition().getValue());
        Assert.assertEquals("2018-11-27T15:09:17.000", time.getEndPosition().getValue());

        Assert.assertEquals(8, offp.getFeatureOfInterestIds().size());
        Assert.assertEquals(1, offp.getObservedProperties().size());

        String observedProperty = offp.getObservedProperties().get(0);
        String foi = "251";


        /*
        * Verify an inserted profile
        */
        GetResultResponseType gr = (GetResultResponseType) worker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        String expectedResult = getResourceAsString("com/examind/process/sos/argo-datablock-values.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", sensorId);
        hl.getExpand().add("Locations");
        response = stsWorker.getHistoricalLocations(hl);

        Assert.assertEquals(8, response.getValue().size());

        loc1 = response.getValue().get(0);
        Assert.assertEquals(loc1.getTime().getTime(), sdf.parse("2018-11-02T07:10:52Z").getTime());
        Assert.assertEquals(1, loc1.getLocations().size());
        Assert.assertTrue(loc1.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        feat1 = (GeoJSONFeature) loc1.getLocations().get(0).getLocation();
        Assert.assertTrue(feat1.getGeometry() instanceof GeoJSONPoint);
        pt1 = (GeoJSONPoint) feat1.getGeometry();
        Assert.assertEquals(44.06, pt1.getCoordinates()[1], 0);
        Assert.assertEquals(-6.81, pt1.getCoordinates()[0], 0);

        HistoricalLocation loc2 = response.getValue().get(1);
        Assert.assertEquals(loc2.getTime().getTime(), sdf.parse("2018-11-05T22:03:31Z").getTime());
        Assert.assertEquals(1, loc2.getLocations().size());
        Assert.assertTrue(loc2.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        GeoJSONFeature feat2 = (GeoJSONFeature) loc2.getLocations().get(0).getLocation();
        Assert.assertTrue(feat2.getGeometry() instanceof GeoJSONPoint);
        GeoJSONPoint pt2 = (GeoJSONPoint) feat2.getGeometry();
        Assert.assertEquals(44.01, pt2.getCoordinates()[1], 0);
        Assert.assertEquals(-6.581, pt2.getCoordinates()[0], 0);

        HistoricalLocation loc8 = response.getValue().get(7);
        Assert.assertEquals(loc8.getTime().getTime(), sdf.parse("2018-11-27T15:09:17Z").getTime());
        Assert.assertEquals(1, loc8.getLocations().size());
        Assert.assertTrue(loc8.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        GeoJSONFeature feat8 = (GeoJSONFeature) loc8.getLocations().get(0).getLocation();
        Assert.assertTrue(feat8.getGeometry() instanceof GeoJSONPoint);
        GeoJSONPoint pt8 = (GeoJSONPoint) feat8.getGeometry();
        Assert.assertEquals(44.154, pt8.getCoordinates()[1], 0);
        Assert.assertEquals(-5.04, pt8.getCoordinates()[0], 0);

        /*
         * remove the new file and reinsert with REMOVE PREVIOUS
         */
        Path p = argoDirectory.resolve("argo-profiles-2902402-2.csv");
        Files.delete(p);
        in.parameter(SosHarvesterProcessDescriptor.REMOVE_PREVIOUS_NAME).setValue(true);

        proc = desc.createProcess(in);
        proc.call();

        Assert.assertNotNull(sensorBusiness.getSensor(sensorId));

        offp = getOffering(worker, sensorId);
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2018-11-02T07:10:52.000", time.getBeginPosition().getValue());
        Assert.assertEquals("2018-11-13T03:55:49.000", time.getEndPosition().getValue());

        Assert.assertEquals(4, offp.getFeatureOfInterestIds().size());

        hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", sensorId);
        hl.getExpand().add("Locations");
        response = stsWorker.getHistoricalLocations(hl);

        Assert.assertEquals(4, response.getValue().size());

        loc1 = response.getValue().get(0);
        Assert.assertEquals(loc1.getTime().getTime(), sdf.parse("2018-11-02T07:10:52Z").getTime());
        Assert.assertEquals(1, loc1.getLocations().size());
        Assert.assertTrue(loc1.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        feat1 = (GeoJSONFeature) loc1.getLocations().get(0).getLocation();
        Assert.assertTrue(feat1.getGeometry() instanceof GeoJSONPoint);
        pt1 = (GeoJSONPoint) feat1.getGeometry();
        Assert.assertEquals(44.06, pt1.getCoordinates()[1], 0);
        Assert.assertEquals(-6.81, pt1.getCoordinates()[0], 0);
    }

    @Test
    @Order(order = 2)
    public void harvestCSVTrajTest() throws Exception {

        ServiceComplete sc = serviceBusiness.getServiceByIdentifierAndType("sos", "default");
        Assert.assertNotNull(sc);

        String sensorId = "urn:sensor:2";

        String datasetId = "SOS_DATA";

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ExamindProcessFactory.NAME, SosHarvesterProcessDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SosHarvesterProcessDescriptor.DATASET_IDENTIFIER_NAME).setValue(datasetId);
        in.parameter(SosHarvesterProcessDescriptor.DATA_FOLDER_NAME).setValue(fmlwDirectory.toUri().toString());

        in.parameter(SosHarvesterProcessDescriptor.DATE_COLUMN_NAME).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");
        in.parameter(SosHarvesterProcessDescriptor.MAIN_COLUMN_NAME).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");

        in.parameter(SosHarvesterProcessDescriptor.DATE_FORMAT_NAME).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");

        in.parameter(SosHarvesterProcessDescriptor.LATITUDE_COLUMN_NAME).setValue("LATITUDE (degree_north)");
        in.parameter(SosHarvesterProcessDescriptor.LONGITUDE_COLUMN_NAME).setValue("LONGITUDE (degree_east)");

        in.parameter(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).setValue("TEMP LEVEL1 (degree_Celsius)");
        in.parameter(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).setValue("PSAL LEVEL1 (psu)");

        in.parameter(SosHarvesterProcessDescriptor.OBS_TYPE_NAME).setValue("Trajectory");
        in.parameter(SosHarvesterProcessDescriptor.PROCEDURE_ID_NAME).setValue(sensorId);
        in.parameter(SosHarvesterProcessDescriptor.REMOVE_PREVIOUS_NAME).setValue(false);
        in.parameter(SosHarvesterProcessDescriptor.SERVICE_ID_NAME).setValue(new ServiceProcessReference(sc));

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        // verify that the dataset has been created
        Assert.assertNotNull(datasetBusiness.getDatasetId(datasetId));

        // verify that the sensor has been created
        Assert.assertNotNull(sensorBusiness.getSensor(sensorId));

        SOSworker worker = (SOSworker) wsEngine.buildWorker("sos", "default");
        worker.setServiceUrl("http://localhost/examind/");

        STSWorker stsWorker = (STSWorker) wsEngine.buildWorker("sts", "default");
        stsWorker.setServiceUrl("http://localhost/examind/");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        ObservationOffering offp = getOffering(worker, sensorId);
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        TimePeriodType time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2018-10-30", time.getBeginPosition().getValue());
        Assert.assertEquals("2018-10-31T06:42:00.000", time.getEndPosition().getValue());

        Assert.assertEquals(3, offp.getFeatureOfInterestIds().size());

        String observedProperty = offp.getObservedProperties().get(0);
        String foi = "foi-tsg-FMLW-1.csv-0";


        /*
        * Verify an inserted profile
        */
        GetResultResponseType gr = (GetResultResponseType) worker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        String expectedResult =  getResourceAsString("com/examind/process/sos/tsg-FMLW-datablock-values.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        GetHistoricalLocations hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", sensorId);
        hl.getExpand().add("Locations");
        HistoricalLocationsResponse response = stsWorker.getHistoricalLocations(hl);

        Assert.assertEquals(1236, response.getValue().size());
    }

    @Test
    @Order(order = 3)
    public void harvestCSVTSTest() throws Exception {

        ServiceComplete sc = serviceBusiness.getServiceByIdentifierAndType("sos", "default");
        Assert.assertNotNull(sc);

        String sensorId = "urn:sensor:3";

        String datasetId = "SOS_DATA";

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ExamindProcessFactory.NAME, SosHarvesterProcessDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SosHarvesterProcessDescriptor.DATASET_IDENTIFIER_NAME).setValue(datasetId);
        in.parameter(SosHarvesterProcessDescriptor.DATA_FOLDER_NAME).setValue(mooDirectory.toUri().toString());

        in.parameter(SosHarvesterProcessDescriptor.DATE_COLUMN_NAME).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");
        in.parameter(SosHarvesterProcessDescriptor.MAIN_COLUMN_NAME).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");

        in.parameter(SosHarvesterProcessDescriptor.DATE_FORMAT_NAME).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");

        in.parameter(SosHarvesterProcessDescriptor.LATITUDE_COLUMN_NAME).setValue("LATITUDE (degree_north)");
        in.parameter(SosHarvesterProcessDescriptor.LONGITUDE_COLUMN_NAME).setValue("LONGITUDE (degree_east)");

        ParameterValue val1 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val1.setValue("TEMP LEVEL0 (degree_Celsius)");
        in.values().add(val1);
        ParameterValue val2 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val2.setValue("VEPK LEVEL0 (meter2 second)");
        in.values().add(val2);


        in.parameter(SosHarvesterProcessDescriptor.OBS_TYPE_NAME).setValue("Timeserie");
        in.parameter(SosHarvesterProcessDescriptor.PROCEDURE_ID_NAME).setValue(sensorId);
        in.parameter(SosHarvesterProcessDescriptor.REMOVE_PREVIOUS_NAME).setValue(false);
        in.parameter(SosHarvesterProcessDescriptor.SERVICE_ID_NAME).setValue(new ServiceProcessReference(sc));

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        // verify that the dataset has been created
        Assert.assertNotNull(datasetBusiness.getDatasetId(datasetId));

        // verify that the sensor has been created
        Assert.assertNotNull(sensorBusiness.getSensor(sensorId));

        SOSworker worker = (SOSworker) wsEngine.buildWorker("sos", "default");
        worker.setServiceUrl("http://localhost/examind/");

        STSWorker stsWorker = (STSWorker) wsEngine.buildWorker("sts", "default");
        stsWorker.setServiceUrl("http://localhost/examind/");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        ObservationOffering offp = getOffering(worker, sensorId);
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        TimePeriodType time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2018-10-30T00:29:00.000", time.getBeginPosition().getValue());
        Assert.assertEquals("2018-11-30T11:59:00.000", time.getEndPosition().getValue());

        Assert.assertEquals(1, offp.getFeatureOfInterestIds().size());

        String observedProperty = offp.getObservedProperties().get(0);
        String foi = offp.getFeatureOfInterestIds().get(0);


        /*
        * Verify an inserted profile
        */
        GetResultResponseType gr = (GetResultResponseType) worker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        String expectedResult = getResourceAsString("com/examind/process/sos/mooring-datablock-values.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        GetHistoricalLocations hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", sensorId);
        hl.getExpand().add("Locations");
        HistoricalLocationsResponse response = stsWorker.getHistoricalLocations(hl);

        Assert.assertEquals(1, response.getValue().size());
    }

    @Test
    @Order(order = 4)
    public void harvestTS2Test() throws Exception {

        ServiceComplete sc = serviceBusiness.getServiceByIdentifierAndType("sos", "default");
        Assert.assertNotNull(sc);

        String sensorId = "urn:sensor:5";

        String datasetId = "SOS_DATA";

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ExamindProcessFactory.NAME, SosHarvesterProcessDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SosHarvesterProcessDescriptor.DATASET_IDENTIFIER_NAME).setValue(datasetId);
        in.parameter(SosHarvesterProcessDescriptor.DATA_FOLDER_NAME).setValue(mooDirectory.toUri().toString());

        in.parameter(SosHarvesterProcessDescriptor.DATE_COLUMN_NAME).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");
        in.parameter(SosHarvesterProcessDescriptor.MAIN_COLUMN_NAME).setValue("DATE (yyyy-mm-ddThh:mi:ssZ)");

        in.parameter(SosHarvesterProcessDescriptor.DATE_FORMAT_NAME).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");

        in.parameter(SosHarvesterProcessDescriptor.LATITUDE_COLUMN_NAME).setValue("LATITUDE (degree_north)");
        in.parameter(SosHarvesterProcessDescriptor.LONGITUDE_COLUMN_NAME).setValue("LONGITUDE (degree_east)");

        ParameterValue val1 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val1.setValue("TEMP LEVEL0 (degree_Celsius)");
        in.values().add(val1);
        ParameterValue val2 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val2.setValue("VZMX LEVEL0 (meter)");
        in.values().add(val2);


        in.parameter(SosHarvesterProcessDescriptor.OBS_TYPE_NAME).setValue("Timeserie");
        in.parameter(SosHarvesterProcessDescriptor.PROCEDURE_ID_NAME).setValue(sensorId);
        in.parameter(SosHarvesterProcessDescriptor.REMOVE_PREVIOUS_NAME).setValue(true);
        in.parameter(SosHarvesterProcessDescriptor.SERVICE_ID_NAME).setValue(new ServiceProcessReference(sc));

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        // verify that the dataset has been created
        Assert.assertNotNull(datasetBusiness.getDatasetId(datasetId));

        // verify that the sensor has been created
        Assert.assertNotNull(sensorBusiness.getSensor(sensorId));

        SOSworker worker = (SOSworker) wsEngine.buildWorker("sos", "default");
        worker.setServiceUrl("http://localhost/examind/");

        ObservationOffering offp = getOffering(worker, sensorId);
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        TimePeriodType time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2018-10-30", time.getBeginPosition().getValue());
        Assert.assertEquals("2018-11-30T12:30:00.000", time.getEndPosition().getValue());

        Assert.assertEquals(1, offp.getFeatureOfInterestIds().size());
        Assert.assertEquals(1, offp.getObservedProperties().size());

        String observedProperty = offp.getObservedProperties().get(0);
        String foi = offp.getFeatureOfInterestIds().get(0);


        /*
        * Verify an inserted timeSeries
        */
        GetResultResponseType gr = (GetResultResponseType) worker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        String expectedResult = getResourceAsString("com/examind/process/sos/mooring-datablock-values-2.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        /*
        * Verify an inserted timeSeries
        */
        observedProperty = "VZMX LEVEL0 (meter)";
        gr = (GetResultResponseType) worker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        expectedResult = getResourceAsString("com/examind/process/sos/mooring-datablock-values-3.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        Object obj = worker.getObservation(new GetObservationType("2.0.0", offp.getId(), null, Arrays.asList(sensorId), Arrays.asList(observedProperty), new ArrayList<>(), null));

        Assert.assertTrue(obj instanceof GetObservationResponseType);

        GetObservationResponseType result = (GetObservationResponseType) obj;

        Assert.assertTrue(result.getObservationData().get(0).getOMObservation().getResult() instanceof DataArrayPropertyType);

        DataArrayPropertyType daResult = (DataArrayPropertyType) result.getObservationData().get(0).getOMObservation().getResult();

        String value = daResult.getDataArray().getValues();
        Assert.assertEquals(expectedResult, value + '\n');
    }

    @Test
    @Order(order = 4)
    public void harvestDBFTSTest() throws Exception {
        ServiceComplete sc = serviceBusiness.getServiceByIdentifierAndType("sos", "default");
        Assert.assertNotNull(sc);

        String sensorId = "urn:sensor:dbf:1";

        String datasetId = "SOS_DATA";

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ExamindProcessFactory.NAME, SosHarvesterProcessDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SosHarvesterProcessDescriptor.DATASET_IDENTIFIER_NAME).setValue(datasetId);
        in.parameter(SosHarvesterProcessDescriptor.DATA_FOLDER_NAME).setValue(ltDirectory.toUri().toString());

        in.parameter(SosHarvesterProcessDescriptor.STORE_ID_NAME).setValue("observationDbfFile");
        in.parameter(SosHarvesterProcessDescriptor.FORMAT_NAME).setValue("application/dbase; subtype=\"om\"");

        in.parameter(SosHarvesterProcessDescriptor.DATE_COLUMN_NAME).setValue("time_str");
        in.parameter(SosHarvesterProcessDescriptor.MAIN_COLUMN_NAME).setValue("time_str");

        in.parameter(SosHarvesterProcessDescriptor.DATE_FORMAT_NAME).setValue("yyyy-MM-dd' 'HH:mm:ss");
        in.parameter(SosHarvesterProcessDescriptor.LATITUDE_COLUMN_NAME).setValue("LATITUDE (degree_north)");
        in.parameter(SosHarvesterProcessDescriptor.LONGITUDE_COLUMN_NAME).setValue("LONGITUDE (degree_east)");

        in.parameter(SosHarvesterProcessDescriptor.FOI_COLUMN_NAME).setValue("prior_id");

        in.parameter(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).setValue("height");

        in.parameter(SosHarvesterProcessDescriptor.OBS_TYPE_NAME).setValue("Timeserie");
        in.parameter(SosHarvesterProcessDescriptor.PROCEDURE_ID_NAME).setValue(sensorId);
        in.parameter(SosHarvesterProcessDescriptor.REMOVE_PREVIOUS_NAME).setValue(false);
        in.parameter(SosHarvesterProcessDescriptor.SERVICE_ID_NAME).setValue(new ServiceProcessReference(sc));

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        // verify that the dataset has been created
        Assert.assertNotNull(datasetBusiness.getDatasetId(datasetId));

        // verify that the sensor has been created
        Assert.assertNotNull(sensorBusiness.getSensor(sensorId));

        SOSworker worker = (SOSworker) wsEngine.buildWorker("sos", "default");
        worker.setServiceUrl("http://localhost/examind/");

        ObservationOffering offp = getOffering(worker, sensorId);
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        TimePeriodType time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2022-08-20T01:55:11.000", time.getBeginPosition().getValue());
        Assert.assertEquals("2022-08-28T10:58:39.000", time.getEndPosition().getValue());

        Assert.assertEquals(4, offp.getFeatureOfInterestIds().size());
        
        Assert.assertEquals(1, offp.getObservedProperties().size());

        String observedProperty = offp.getObservedProperties().get(0);

        /*
        * Verify an inserted time serie
        */
        String foi = "54008001708";
        GetResultResponseType gr = (GetResultResponseType) worker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        String expectedResult = getResourceAsString("com/examind/process/sos/LakeTile_foi-1.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        foi = "54008001586";
        gr = (GetResultResponseType) worker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        expectedResult = getResourceAsString("com/examind/process/sos/LakeTile_foi-2.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

    }
    
    @Test
    @Order(order = 5)
    public void harvestDBFTS2Test() throws Exception {
        ServiceComplete sc = serviceBusiness.getServiceByIdentifierAndType("sos", "default");
        Assert.assertNotNull(sc);

        String sensorId = "urn:sensor:dbf:2";

        String datasetId = "SOS_DATA";

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ExamindProcessFactory.NAME, SosHarvesterProcessDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SosHarvesterProcessDescriptor.DATASET_IDENTIFIER_NAME).setValue(datasetId);
        in.parameter(SosHarvesterProcessDescriptor.DATA_FOLDER_NAME).setValue(rtDirectory.toUri().toString());

        in.parameter(SosHarvesterProcessDescriptor.STORE_ID_NAME).setValue("observationDbfFile");
        in.parameter(SosHarvesterProcessDescriptor.FORMAT_NAME).setValue("application/dbase; subtype=\"om\"");

        in.parameter(SosHarvesterProcessDescriptor.DATE_COLUMN_NAME).setValue("time");
        in.parameter(SosHarvesterProcessDescriptor.MAIN_COLUMN_NAME).setValue("time");

        in.parameter(SosHarvesterProcessDescriptor.DATE_FORMAT_NAME).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");
        in.parameter(SosHarvesterProcessDescriptor.LATITUDE_COLUMN_NAME).setValue("latitude");
        in.parameter(SosHarvesterProcessDescriptor.LONGITUDE_COLUMN_NAME).setValue("longitude");

        in.parameter(SosHarvesterProcessDescriptor.FOI_COLUMN_NAME).setValue("node_id");

        in.parameter(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).setValue("height");
        
        ParameterValue val1 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val1.setValue("height");
        in.values().add(val1);
        ParameterValue val2 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val2.setValue("width");
        in.values().add(val2);

        in.parameter(SosHarvesterProcessDescriptor.OBS_TYPE_NAME).setValue("Timeserie");
        in.parameter(SosHarvesterProcessDescriptor.PROCEDURE_ID_NAME).setValue(sensorId);
        in.parameter(SosHarvesterProcessDescriptor.REMOVE_PREVIOUS_NAME).setValue(false);
        in.parameter(SosHarvesterProcessDescriptor.SERVICE_ID_NAME).setValue(new ServiceProcessReference(sc));

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        // verify that the dataset has been created
        Assert.assertNotNull(datasetBusiness.getDatasetId(datasetId));

        // verify that the sensor has been created
        Assert.assertNotNull(sensorBusiness.getSensor(sensorId));

        SOSworker worker = (SOSworker) wsEngine.buildWorker("sos", "default");
        worker.setServiceUrl("http://localhost/examind/");

        ObservationOffering offp = getOffering(worker, sensorId);
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        TimePeriodType time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2022-06-06T00:58:50.921", time.getBeginPosition().getValue());
        Assert.assertEquals("2022-06-15T23:21:00.641", time.getEndPosition().getValue());

        Assert.assertEquals(64, offp.getFeatureOfInterestIds().size());
        
        Assert.assertEquals(1, offp.getObservedProperties().size());

        String observedProperty = offp.getObservedProperties().get(0);

        /*
        * Verify an inserted time serie
        */
        String foi = "8403780.0";
        GetResultResponseType gr = (GetResultResponseType) worker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        String expectedResult = getResourceAsString("com/examind/process/sos/rivertile_foi-1.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        foi = "8403781.0";
        gr = (GetResultResponseType) worker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        expectedResult = getResourceAsString("com/examind/process/sos/rivertile_foi-2.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

    }

    @Test
    @Order(order = 6)
    public void harvestCSVTSMultiPlatformTest() throws Exception {

        ServiceComplete sc = serviceBusiness.getServiceByIdentifierAndType("sos", "default");
        Assert.assertNotNull(sc);

        String datasetId = "SOS_DATA";

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ExamindProcessFactory.NAME, SosHarvesterProcessDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SosHarvesterProcessDescriptor.DATASET_IDENTIFIER_NAME).setValue(datasetId);
        in.parameter(SosHarvesterProcessDescriptor.DATA_FOLDER_NAME).setValue(multiPlatDirectory.toUri().toString());

        in.parameter(SosHarvesterProcessDescriptor.DATE_COLUMN_NAME).setValue("DATE");
        in.parameter(SosHarvesterProcessDescriptor.MAIN_COLUMN_NAME).setValue("DATE");

        in.parameter(SosHarvesterProcessDescriptor.DATE_FORMAT_NAME).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");

        in.parameter(SosHarvesterProcessDescriptor.LATITUDE_COLUMN_NAME).setValue("LATITUDE");
        in.parameter(SosHarvesterProcessDescriptor.LONGITUDE_COLUMN_NAME).setValue("LONGITUDE");

        ParameterValue val1 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val1.setValue("TEMP (degree_Celsius)");
        in.values().add(val1);
        ParameterValue val2 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val2.setValue("VEPK (meter2 second)");
        in.values().add(val2);


        in.parameter(SosHarvesterProcessDescriptor.OBS_TYPE_NAME).setValue("Timeserie");
        in.parameter(SosHarvesterProcessDescriptor.PROCEDURE_COLUMN_NAME).setValue("PLATFORM");
        in.parameter(SosHarvesterProcessDescriptor.REMOVE_PREVIOUS_NAME).setValue(false);
        in.parameter(SosHarvesterProcessDescriptor.SERVICE_ID_NAME).setValue(new ServiceProcessReference(sc));

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        // verify that the dataset has been created
        Assert.assertNotNull(datasetBusiness.getDatasetId(datasetId));

        // verify that the sensor has been created
        Assert.assertNotNull(sensorBusiness.getSensor("p001"));
        Assert.assertNotNull(sensorBusiness.getSensor("p002"));
        Assert.assertNotNull(sensorBusiness.getSensor("p003"));

        SOSworker sosWorker = (SOSworker) wsEngine.buildWorker("sos", "default");
        sosWorker.setServiceUrl("http://localhost/examind/");

        STSWorker stsWorker = (STSWorker) wsEngine.buildWorker("sts", "default");
        stsWorker.setServiceUrl("http://localhost/examind/");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        /*
        * first extracted procedure
        */

        ObservationOffering offp = getOffering(sosWorker, "p001");
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        TimePeriodType time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2000-07-28T00:30:00.000", time.getBeginPosition().getValue());
        Assert.assertEquals("2000-07-29T23:30:00.000", time.getEndPosition().getValue());

        Assert.assertEquals(1, offp.getFeatureOfInterestIds().size());

        String observedProperty = offp.getObservedProperties().get(0);
        String foi = offp.getFeatureOfInterestIds().get(0);

        /*
        * Verify an inserted data
        */
        GetResultResponseType gr = (GetResultResponseType) sosWorker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        String expectedResult = getResourceAsString("com/examind/process/sos/multi-platform-values-p001.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        GetHistoricalLocations hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", "p001");
        hl.getExpand().add("Locations");
        HistoricalLocationsResponse response = stsWorker.getHistoricalLocations(hl);

        Assert.assertEquals(2, response.getValue().size());

        HistoricalLocation loc1 = response.getValue().get(0);
        Assert.assertEquals(loc1.getTime().getTime(), sdf.parse("2000-07-28T00:30:00Z").getTime());
        Assert.assertEquals(1, loc1.getLocations().size());
        Assert.assertTrue(loc1.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        GeoJSONFeature feat1 = (GeoJSONFeature) loc1.getLocations().get(0).getLocation();
        Assert.assertTrue(feat1.getGeometry() instanceof GeoJSONPoint);
        GeoJSONPoint pt1 = (GeoJSONPoint) feat1.getGeometry();
        Assert.assertEquals(49.4, pt1.getCoordinates()[1], 0);
        Assert.assertEquals(-6.9, pt1.getCoordinates()[0], 0);

        HistoricalLocation loc2 = response.getValue().get(1);
        Assert.assertEquals(loc2.getTime().getTime(), sdf.parse("2000-07-29T23:00:00Z").getTime());
        Assert.assertEquals(1, loc2.getLocations().size());
        Assert.assertTrue(loc2.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        GeoJSONFeature feat2 = (GeoJSONFeature) loc2.getLocations().get(0).getLocation();
        Assert.assertTrue(feat2.getGeometry() instanceof GeoJSONPoint);
        GeoJSONPoint pt2 = (GeoJSONPoint) feat2.getGeometry();
        Assert.assertEquals(49.5, pt2.getCoordinates()[1], 0);
        Assert.assertEquals(-6.8, pt2.getCoordinates()[0], 0);


        /*
        * second extracted procedure
        */

        offp = getOffering(sosWorker, "p002");
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2000-07-30", time.getBeginPosition().getValue());
        Assert.assertEquals("2000-07-31T10:30:00.000", time.getEndPosition().getValue());

        Assert.assertEquals(1, offp.getFeatureOfInterestIds().size());

        observedProperty = offp.getObservedProperties().get(0);
        foi = offp.getFeatureOfInterestIds().get(0);

        /*
        * Verify an inserted data
        */
        gr = (GetResultResponseType) sosWorker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        expectedResult = getResourceAsString("com/examind/process/sos/multi-platform-values-p002.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", "p002");
        hl.getExpand().add("Locations");
        response = stsWorker.getHistoricalLocations(hl);

        Assert.assertEquals(2, response.getValue().size());

        loc1 = response.getValue().get(0);
        Assert.assertEquals(loc1.getTime().getTime(), sdf.parse("2000-07-30T00:00:00Z").getTime());
        Assert.assertEquals(1, loc1.getLocations().size());
        Assert.assertTrue(loc1.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        feat1 = (GeoJSONFeature) loc1.getLocations().get(0).getLocation();
        Assert.assertTrue(feat1.getGeometry() instanceof GeoJSONPoint);
        pt1 = (GeoJSONPoint) feat1.getGeometry();
        Assert.assertEquals(49.4, pt1.getCoordinates()[1], 0);
        Assert.assertEquals(-6.9, pt1.getCoordinates()[0], 0);

        loc2 = response.getValue().get(1);
        Assert.assertEquals(loc2.getTime().getTime(), sdf.parse("2000-07-30T02:30:00Z").getTime());
        Assert.assertEquals(1, loc2.getLocations().size());
        Assert.assertTrue(loc2.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        feat2 = (GeoJSONFeature) loc2.getLocations().get(0).getLocation();
        Assert.assertTrue(feat2.getGeometry() instanceof GeoJSONPoint);
        pt2 = (GeoJSONPoint) feat2.getGeometry();
        Assert.assertEquals(49.4, pt2.getCoordinates()[1], 0);
        Assert.assertEquals(-6.9, pt2.getCoordinates()[0], 0);


       /*
        * third extracted procedure
        */

        offp = getOffering(sosWorker, "p003");
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2000-07-31T11:00:00.000", time.getBeginPosition().getValue());
        Assert.assertEquals("2000-08-01T04:00:00.000", time.getEndPosition().getValue());

        Assert.assertEquals(1, offp.getFeatureOfInterestIds().size());

        observedProperty = offp.getObservedProperties().get(0);
        foi = offp.getFeatureOfInterestIds().get(0);

        /*
        * Verify an inserted data
        */
        gr = (GetResultResponseType) sosWorker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        expectedResult = getResourceAsString("com/examind/process/sos/multi-platform-values-p003.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", "p003");
        hl.getExpand().add("Locations");
        response = stsWorker.getHistoricalLocations(hl);

        Assert.assertEquals(1, response.getValue().size());

        loc1 = response.getValue().get(0);
        Assert.assertEquals(loc1.getTime().getTime(), sdf.parse("2000-07-31T11:00:00Z").getTime());
        Assert.assertEquals(1, loc1.getLocations().size());
        Assert.assertTrue(loc1.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        feat1 = (GeoJSONFeature) loc1.getLocations().get(0).getLocation();
        Assert.assertTrue(feat1.getGeometry() instanceof GeoJSONPoint);
        pt1 = (GeoJSONPoint) feat1.getGeometry();
        Assert.assertEquals(51.2, pt1.getCoordinates()[1], 0);
        Assert.assertEquals(-5.3, pt1.getCoordinates()[0], 0);


    }

    @Test
    @Order(order = 6)
    public void harvesterCSVCoriolisProfileTest() throws Exception {

        ServiceComplete sc = serviceBusiness.getServiceByIdentifierAndType("sos", "default");
        Assert.assertNotNull(sc);

        // ???
        String sensorId = "urn:sensor:bgdata";

        String datasetId = "SOS_DATA";

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ExamindProcessFactory.NAME, SosHarvesterProcessDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SosHarvesterProcessDescriptor.DATASET_IDENTIFIER_NAME).setValue(datasetId);
        in.parameter(SosHarvesterProcessDescriptor.DATA_FOLDER_NAME).setValue(bigdataDirectory.toUri().toString());

        in.parameter(SosHarvesterProcessDescriptor.STORE_ID_NAME).setValue("observationCsvCoriolisFile");

        in.parameter(SosHarvesterProcessDescriptor.DATE_COLUMN_NAME).setValue("station_date");
        in.parameter(SosHarvesterProcessDescriptor.MAIN_COLUMN_NAME).setValue("z_value");

        in.parameter(SosHarvesterProcessDescriptor.DATE_FORMAT_NAME).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");
        in.parameter(SosHarvesterProcessDescriptor.LATITUDE_COLUMN_NAME).setValue("latitude");
        in.parameter(SosHarvesterProcessDescriptor.LONGITUDE_COLUMN_NAME).setValue("longitude");

        ParameterValue val1 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val1.setValue("measure1");
        in.values().add(val1);
        ParameterValue val2 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val2.setValue("measure2");
        in.values().add(val2);
        ParameterValue val3 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val3.setValue("measure3");
        in.values().add(val3);

        in.parameter(SosHarvesterProcessDescriptor.VALUE_COLUMN_NAME).setValue("parameter_value");
        in.parameter(SosHarvesterProcessDescriptor.CODE_COLUMN_NAME).setValue("parameter_code");
        in.parameter(SosHarvesterProcessDescriptor.TYPE_COLUMN_NAME).setValue("file_type");

        in.parameter(SosHarvesterProcessDescriptor.OBS_TYPE_NAME).setValue("Profile");
        in.parameter(SosHarvesterProcessDescriptor.PROCEDURE_ID_NAME).setValue(sensorId);
        in.parameter(SosHarvesterProcessDescriptor.REMOVE_PREVIOUS_NAME).setValue(true);
        in.parameter(SosHarvesterProcessDescriptor.SERVICE_ID_NAME).setValue(new ServiceProcessReference(sc));

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        // verify that the dataset has been created
        Assert.assertNotNull(datasetBusiness.getDatasetId(datasetId));

        // verify that the sensor has been created
        Assert.assertNotNull(sensorBusiness.getSensor(sensorId));

        SOSworker worker = (SOSworker) wsEngine.buildWorker("sos", "default");
        worker.setServiceUrl("http://localhost/examind/");

        STSWorker stsWorker = (STSWorker) wsEngine.buildWorker("sts", "default");
        stsWorker.setServiceUrl("http://localhost/examind/");

        ObservationOffering offp = getOffering(worker, sensorId);
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        TimePeriodType time = (TimePeriodType) offp.getTime();
        
        String bpgv = time.getBeginPosition().getValue();
        String epgv = time.getEndPosition().getValue();

        // ???
        Assert.assertEquals("2020-03-24T00:25:47.000", time.getBeginPosition().getValue());
        Assert.assertEquals("2020-03-24T08:48:00.000", time.getEndPosition().getValue());

        Assert.assertEquals(11, offp.getFeatureOfInterestIds().size());

        String observedProperty = offp.getObservedProperties().get(0);
        System.out.println(offp.getFeatureOfInterestIds());
        System.out.println(offp.getFeatureOfInterestIds().get(0));
        String foi = "foi-bigdata-1.csv-0";


        /*
         * Verify an inserted profile
         */
        GetResultResponseType gr = (GetResultResponseType) worker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        String expectedResult = getResourceAsString("com/examind/process/sos/bigdata-datablock-values.txt");
        System.out.println(gr.getResultValues().toString());
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        GetHistoricalLocations hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", sensorId);
        hl.getExpand().add("Locations");
        HistoricalLocationsResponse response = stsWorker.getHistoricalLocations(hl);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        Assert.assertEquals(11, response.getValue().size());
        
        HistoricalLocation loc1 = response.getValue().get(0);
        Assert.assertEquals(loc1.getTime().getTime(), sdf.parse("2020-03-24T00:25:47Z").getTime());
        Assert.assertEquals(1, loc1.getLocations().size());
        Assert.assertTrue(loc1.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        GeoJSONFeature feat1 = (GeoJSONFeature) loc1.getLocations().get(0).getLocation();
        Assert.assertTrue(feat1.getGeometry() instanceof GeoJSONPoint);
        GeoJSONPoint pt1 = (GeoJSONPoint) feat1.getGeometry();
        Assert.assertEquals(-3.61021, pt1.getCoordinates()[1], 0);
        Assert.assertEquals(-35.27835, pt1.getCoordinates()[0], 0);
    }
    
    @Test
    @Order(order = 7)
    public void harvesterCSVCoriolisTSTest() throws Exception {

        ServiceComplete sc = serviceBusiness.getServiceByIdentifierAndType("sos", "default");
        Assert.assertNotNull(sc);

        String sensorId = "urn:sensor:bgdata2";

        String datasetId = "SOS_DATA_2";

        final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ExamindProcessFactory.NAME, SosHarvesterProcessDescriptor.NAME);

        final ParameterValueGroup in = desc.getInputDescriptor().createValue();
        in.parameter(SosHarvesterProcessDescriptor.DATASET_IDENTIFIER_NAME).setValue(datasetId);
        in.parameter(SosHarvesterProcessDescriptor.DATA_FOLDER_NAME).setValue(bigdataDirectory.toUri().toString());

        in.parameter(SosHarvesterProcessDescriptor.STORE_ID_NAME).setValue("observationCsvCoriolisFile");

        in.parameter(SosHarvesterProcessDescriptor.DATE_COLUMN_NAME).setValue("station_date");
        in.parameter(SosHarvesterProcessDescriptor.MAIN_COLUMN_NAME).setValue("station_date");

        in.parameter(SosHarvesterProcessDescriptor.DATE_FORMAT_NAME).setValue("yyyy-MM-dd'T'HH:mm:ss'Z'");
        in.parameter(SosHarvesterProcessDescriptor.LATITUDE_COLUMN_NAME).setValue("latitude");
        in.parameter(SosHarvesterProcessDescriptor.LONGITUDE_COLUMN_NAME).setValue("longitude");

        ParameterValue val1 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val1.setValue("measure1");
        in.values().add(val1);
        ParameterValue val2 = (ParameterValue) desc.getInputDescriptor().descriptor(SosHarvesterProcessDescriptor.MEASURE_COLUMNS_NAME).createValue();
        val2.setValue("measure2");
        in.values().add(val2);

        in.parameter(SosHarvesterProcessDescriptor.VALUE_COLUMN_NAME).setValue("parameter_value");
        in.parameter(SosHarvesterProcessDescriptor.CODE_COLUMN_NAME).setValue("parameter_code");
        in.parameter(SosHarvesterProcessDescriptor.TYPE_COLUMN_NAME).setValue("file_type");

        in.parameter(SosHarvesterProcessDescriptor.OBS_TYPE_NAME).setValue("Timeserie");
        in.parameter(SosHarvesterProcessDescriptor.PROCEDURE_COLUMN_NAME).setValue("platform_code");
        in.parameter(SosHarvesterProcessDescriptor.PROCEDURE_ID_NAME).setValue(sensorId);
        in.parameter(SosHarvesterProcessDescriptor.REMOVE_PREVIOUS_NAME).setValue(true);
        in.parameter(SosHarvesterProcessDescriptor.SERVICE_ID_NAME).setValue(new ServiceProcessReference(sc));

        org.geotoolkit.process.Process proc = desc.createProcess(in);
        proc.call();

        // verify that the dataset has been created
        Assert.assertNotNull(datasetBusiness.getDatasetId(datasetId));

        // verify that the sensor has been created
        Assert.assertNotNull(sensorBusiness.getSensor("1501563"));
        Assert.assertNotNull(sensorBusiness.getSensor("1501564"));
        
        SOSworker sosWorker = (SOSworker) wsEngine.buildWorker("sos", "default");
        sosWorker.setServiceUrl("http://localhost/examind/");

        STSWorker stsWorker = (STSWorker) wsEngine.buildWorker("sts", "default");
        stsWorker.setServiceUrl("http://localhost/examind/");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        /*
        * first extracted procedure
        */

        ObservationOffering offp = getOffering(sosWorker, "1501563");
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        TimePeriodType time = (TimePeriodType) offp.getTime();
        
        Assert.assertEquals("2020-03-24", time.getBeginPosition().getValue());
        Assert.assertEquals("2020-03-24T10:00:00.000", time.getEndPosition().getValue());

        Assert.assertEquals(1, offp.getFeatureOfInterestIds().size());

        String observedProperty = offp.getObservedProperties().get(0);
        String foi = offp.getFeatureOfInterestIds().get(0);

        /*
        * Verify an inserted data
        */
        GetResultResponseType gr = (GetResultResponseType) sosWorker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        String expectedResult = getResourceAsString("com/examind/process/sos/bigdata-datablock-values-2.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        GetHistoricalLocations hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", "1501563");
        hl.getExpand().add("Locations");
        HistoricalLocationsResponse response = stsWorker.getHistoricalLocations(hl);

        Assert.assertEquals(21, response.getValue().size());

        HistoricalLocation loc1 = response.getValue().get(0);
        Assert.assertEquals(loc1.getTime().getTime(), sdf.parse("2020-03-24T00:00:00Z").getTime());
        Assert.assertEquals(1, loc1.getLocations().size());
        Assert.assertTrue(loc1.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        GeoJSONFeature feat1 = (GeoJSONFeature) loc1.getLocations().get(0).getLocation();
        Assert.assertTrue(feat1.getGeometry() instanceof GeoJSONPoint);
        GeoJSONPoint pt1 = (GeoJSONPoint) feat1.getGeometry();
        Assert.assertEquals(-29.9916, pt1.getCoordinates()[1], 0);
        Assert.assertEquals(-20.539, pt1.getCoordinates()[0], 0);

        HistoricalLocation loc2 = response.getValue().get(1);
        Assert.assertEquals(loc2.getTime().getTime(), sdf.parse("2020-03-24T00:30:00Z").getTime());
        Assert.assertEquals(1, loc2.getLocations().size());
        Assert.assertTrue(loc2.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        GeoJSONFeature feat2 = (GeoJSONFeature) loc2.getLocations().get(0).getLocation();
        Assert.assertTrue(feat2.getGeometry() instanceof GeoJSONPoint);
        GeoJSONPoint pt2 = (GeoJSONPoint) feat2.getGeometry();
        Assert.assertEquals(-29.995, pt2.getCoordinates()[1], 0);
        Assert.assertEquals(-20.5456, pt2.getCoordinates()[0], 0);


        /*
        * second extracted procedure
        */

        offp = getOffering(sosWorker, "1501564");
        Assert.assertNotNull(offp);

        Assert.assertTrue(offp.getTime() instanceof TimePeriodType);
        time = (TimePeriodType) offp.getTime();

        Assert.assertEquals("2020-03-24", time.getBeginPosition().getValue());
        Assert.assertEquals("2020-03-24T10:00:00.000", time.getEndPosition().getValue());

        Assert.assertEquals(1, offp.getFeatureOfInterestIds().size());

        observedProperty = offp.getObservedProperties().get(0);
        foi = offp.getFeatureOfInterestIds().get(0);

        /*
        * Verify an inserted data
        */
        gr = (GetResultResponseType) sosWorker.getResult(new GetResultType("2.0.0", "SOS", offp.getId(), observedProperty, null, null, Arrays.asList(foi)));
        expectedResult = getResourceAsString("com/examind/process/sos/bigdata-datablock-values-3.txt");
        Assert.assertEquals(expectedResult, gr.getResultValues().toString() + '\n');

        hl = new GetHistoricalLocations();
        hl.getExtraFilter().put("procedure", "1501564");
        hl.getExpand().add("Locations");
        response = stsWorker.getHistoricalLocations(hl);

        Assert.assertEquals(21, response.getValue().size());

        loc1 = response.getValue().get(0);
        Assert.assertEquals(loc1.getTime().getTime(), sdf.parse("2020-03-24T00:00:00Z").getTime());
        Assert.assertEquals(1, loc1.getLocations().size());
        Assert.assertTrue(loc1.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        feat1 = (GeoJSONFeature) loc1.getLocations().get(0).getLocation();
        Assert.assertTrue(feat1.getGeometry() instanceof GeoJSONPoint);
        pt1 = (GeoJSONPoint) feat1.getGeometry();
        Assert.assertEquals(-30.3464, pt1.getCoordinates()[1], 0);
        Assert.assertEquals(-23.209, pt1.getCoordinates()[0], 0);

        loc2 = response.getValue().get(1);
        Assert.assertEquals(loc2.getTime().getTime(), sdf.parse("2020-03-24T00:30:00Z").getTime());
        Assert.assertEquals(1, loc2.getLocations().size());
        Assert.assertTrue(loc2.getLocations().get(0).getLocation() instanceof GeoJSONFeature);
        feat2 = (GeoJSONFeature) loc2.getLocations().get(0).getLocation();
        Assert.assertTrue(feat2.getGeometry() instanceof GeoJSONPoint);
        pt2 = (GeoJSONPoint) feat2.getGeometry();
        Assert.assertEquals(-30.3484, pt2.getCoordinates()[1], 0);
        Assert.assertEquals(-23.2064, pt2.getCoordinates()[0], 0);
    }

    private static ObservationOffering getOffering(SOSworker worker, String sensorId) throws CstlServiceException {
        Capabilities capa        = worker.getCapabilities(new GetCapabilitiesType());
        Contents ct              = capa.getContents();
        ObservationOffering offp = null;
        for (ObservationOffering off : ct.getOfferings()) {
            if (off.getId().equals("offering-" + sensorId.replace(':', '-'))) {
                offp = off;
            }
        }
        return offp;
    }
}
