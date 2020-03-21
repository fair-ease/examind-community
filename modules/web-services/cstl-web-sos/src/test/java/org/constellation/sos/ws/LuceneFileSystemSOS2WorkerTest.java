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

package org.constellation.sos.ws;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.xml.bind.Marshaller;
import org.apache.sis.storage.DataStoreProvider;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.business.IProviderBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.dto.service.config.sos.SOSConfiguration;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.sos.core.SOSworker;
import org.constellation.sos.io.lucene.LuceneObservationIndexer;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import static org.constellation.test.utils.TestEnvironment.EPSG_VERSION;
import org.geotoolkit.index.tree.manager.SQLRtreeManager;
import org.geotoolkit.storage.DataStores;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import static org.constellation.test.utils.TestResourceUtils.writeDataFileEPSG;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class LuceneFileSystemSOS2WorkerTest extends SOS2WorkerTest {

    private static boolean initialized = false;
    private static String configDirName = "LUCSOS2WorkerTest" + UUID.randomUUID().toString();

    private static Path instDirectory;
    private static Path configDir;


    @BeforeClass
    public static void setUpClass() throws Exception {
        MarshallerPool pool   = GenericDatabaseMarshallerPool.getInstance();
        Marshaller marshaller =  pool.acquireMarshaller();

        configDir          = ConfigDirectory.setupTestEnvironement(configDirName);
        Path SOSDirectory  = configDir.resolve("SOS");
        instDirectory      = SOSDirectory.resolve("default");
        Files.createDirectories(instDirectory);

        //we write the data files
        Path offeringDirectory = instDirectory.resolve("offerings");
        Files.createDirectories(offeringDirectory);

        Path offeringV100Directory = offeringDirectory.resolve("1.0.0");
        Files.createDirectories(offeringV100Directory);
        writeDataFileEPSG(offeringV100Directory, "org/constellation/sos/v100/offering-1.xml", "offering-allSensor.xml", EPSG_VERSION);

        Path offeringV200Directory = offeringDirectory.resolve("2.0.0");
        Files.createDirectories(offeringV200Directory);
        writeDataFileEPSG(offeringV200Directory, "org/constellation/sos/v200/offering-1.xml", "offering-1.xml", EPSG_VERSION);
        writeDataFileEPSG(offeringV200Directory, "org/constellation/sos/v200/offering-2.xml", "offering-4.xml", EPSG_VERSION);
        writeDataFileEPSG(offeringV200Directory, "org/constellation/sos/v200/offering-3.xml", "offering-3.xml", EPSG_VERSION);
        writeDataFileEPSG(offeringV200Directory, "org/constellation/sos/v200/offering-4.xml", "offering-5.xml", EPSG_VERSION);
        writeDataFileEPSG(offeringV200Directory, "org/constellation/sos/v200/offering-5.xml", "offering-2.xml", EPSG_VERSION);
        writeDataFileEPSG(offeringV200Directory, "org/constellation/sos/v200/offering-6.xml", "offering-6.xml", EPSG_VERSION);
        writeDataFileEPSG(offeringV200Directory, "org/constellation/sos/v200/offering-7.xml", "offering-7.xml", EPSG_VERSION);
        writeDataFileEPSG(offeringV200Directory, "org/constellation/sos/v200/offering-8.xml", "offering-8.xml", EPSG_VERSION);
        writeDataFileEPSG(offeringV200Directory, "org/constellation/sos/v200/offering-9.xml", "offering-9.xml", EPSG_VERSION);
        writeDataFileEPSG(offeringV200Directory, "org/constellation/sos/v200/offering-10.xml", "offering-10.xml", EPSG_VERSION);

        Path phenomenonDirectory = instDirectory.resolve("phenomenons");
        Files.createDirectories(phenomenonDirectory);
        writeDataFileEPSG(phenomenonDirectory, "org/constellation/sos/phenomenon-depth.xml", "depth.xml", EPSG_VERSION);
        writeDataFileEPSG(phenomenonDirectory, "org/constellation/sos/phenomenon-temp.xml",  "temperature.xml", EPSG_VERSION);
        writeDataFileEPSG(phenomenonDirectory, "org/constellation/sos/phenomenon-depth-temp.xml",  "aggregatePhenomenon.xml", EPSG_VERSION);

        Path featureDirectory = instDirectory.resolve("features");
        Files.createDirectories(featureDirectory);
        writeDataFileEPSG(featureDirectory, "org/constellation/sos/v200/feature1.xml", "station-001.xml", EPSG_VERSION);
        writeDataFileEPSG(featureDirectory, "org/constellation/sos/v200/feature2.xml", "station-002.xml", EPSG_VERSION);
        writeDataFileEPSG(featureDirectory, "org/constellation/sos/v200/feature3.xml", "station-006.xml", EPSG_VERSION);

        Path observationsDirectory = instDirectory.resolve("observations");
        Files.createDirectories(observationsDirectory);
        writeDataFileEPSG(observationsDirectory, "org/constellation/sos/v200/observation1.xml", "urn:ogc:object:observation:GEOM:304.xml", EPSG_VERSION);
        writeDataFileEPSG(observationsDirectory, "org/constellation/sos/v200/observation2.xml", "urn:ogc:object:observation:GEOM:305.xml", EPSG_VERSION);
        writeDataFileEPSG(observationsDirectory, "org/constellation/sos/v200/observation3.xml", "urn:ogc:object:observation:GEOM:406.xml", EPSG_VERSION);
        writeDataFileEPSG(observationsDirectory, "org/constellation/sos/v200/observation4.xml", "urn:ogc:object:observation:GEOM:307.xml", EPSG_VERSION);
        writeDataFileEPSG(observationsDirectory, "org/constellation/sos/v200/observation5.xml", "urn:ogc:object:observation:GEOM:507.xml", EPSG_VERSION);
        writeDataFileEPSG(observationsDirectory, "org/constellation/sos/v200/observation6.xml", "urn:ogc:object:observation:GEOM:801.xml", EPSG_VERSION);
        writeDataFileEPSG(observationsDirectory, "org/constellation/sos/v200/observation7.xml", "urn:ogc:object:observation:GEOM:901.xml", EPSG_VERSION);

        Path observationTemplatesDirectory = instDirectory.resolve("observationTemplates");
        Files.createDirectories(observationTemplatesDirectory);
        writeDataFileEPSG(observationTemplatesDirectory, "org/constellation/sos/v200/observationTemplate-3.xml", "urn:ogc:object:observation:template:GEOM:3.xml", EPSG_VERSION);
        writeDataFileEPSG(observationTemplatesDirectory, "org/constellation/sos/v200/observationTemplate-4.xml", "urn:ogc:object:observation:template:GEOM:4.xml", EPSG_VERSION);
        writeDataFileEPSG(observationTemplatesDirectory, "org/constellation/sos/observationTemplate-5.xml", "urn:ogc:object:observation:template:GEOM:5.xml", EPSG_VERSION);
        writeDataFileEPSG(observationTemplatesDirectory, "org/constellation/sos/observationTemplate-7.xml", "urn:ogc:object:observation:template:GEOM:7.xml", EPSG_VERSION);
        writeDataFileEPSG(observationTemplatesDirectory, "org/constellation/sos/observationTemplate-8.xml", "urn:ogc:object:observation:template:GEOM:8.xml", EPSG_VERSION);

        Path sensorDirectory = instDirectory.resolve("sensors");
        Files.createDirectories(sensorDirectory);
        Path sensor1         = sensorDirectory.resolve("urnµogcµobjectµsensorµGEOMµ1.xml");
        Files.createFile(sensor1);
        Path sensor2         = sensorDirectory.resolve("urnµogcµobjectµsensorµGEOMµ2.xml");
        Files.createFile(sensor2);
        Path sensor3         = sensorDirectory.resolve("urnµogcµobjectµsensorµGEOMµ3.xml");
        Files.createFile(sensor3);
        Path sensor4         = sensorDirectory.resolve("urnµogcµobjectµsensorµGEOMµ4.xml");
        Files.createFile(sensor4);
        Path sensor5         = sensorDirectory.resolve("urnµogcµobjectµsensorµGEOMµ5.xml");
        Files.createFile(sensor5);
        Path sensor6         = sensorDirectory.resolve("urnµogcµobjectµsensorµGEOMµ6.xml");
        Files.createFile(sensor6);
        Path sensor7         = sensorDirectory.resolve("urnµogcµobjectµsensorµGEOMµ7.xml");
        Files.createFile(sensor7);
        Path sensor8         = sensorDirectory.resolve("urnµogcµobjectµsensorµGEOMµ8.xml");
        Files.createFile(sensor8);
        Path sensor9         = sensorDirectory.resolve("urnµogcµobjectµsensorµGEOMµ9.xml");
        Files.createFile(sensor9);
        Path sensor10        = sensorDirectory.resolve("urnµogcµobjectµsensorµGEOMµ10.xml");
        Files.createFile(sensor10);
        pool.recycle(marshaller);
    }

    @PostConstruct
    public void setUp() {
        try {

            if (!initialized) {
                // clean up
                serviceBusiness.deleteAll();
                providerBusiness.removeAll();

                final DataStoreProvider omfactory = DataStores.getProviderById("observationSOSLucene");
                final ParameterValueGroup dbConfig = omfactory.getOpenParameters().createValue();
                dbConfig.parameter("data-directory").setValue(instDirectory);
                dbConfig.parameter("config-directory").setValue(configDir);
                dbConfig.parameter("phenomenon-id-base").setValue("urn:ogc:def:phenomenon:GEOM:");
                dbConfig.parameter("observation-template-id-base").setValue("urn:ogc:object:observation:template:GEOM:");
                dbConfig.parameter("observation-id-base").setValue("urn:ogc:object:observation:GEOM:");
                dbConfig.parameter("sensor-id-base").setValue("urn:ogc:object:sensor:GEOM:");
                Integer pid = providerBusiness.create("omSrc", IProviderBusiness.SPI_NAMES.OBSERVATION_SPI_NAME, dbConfig);

                //we write the configuration file
                final SOSConfiguration configuration = new SOSConfiguration();
                configuration.setProfile("transactional");
                configuration.getParameters().put("transactionSecurized", "false");

                Integer sid = serviceBusiness.create("sos", "default", configuration, null, null);
                serviceBusiness.linkServiceAndProvider(sid, pid);

                init();
                worker = new SOSworker("default");
                worker.setServiceUrl(URL);
                initialized = true;
            }
        } catch (Exception ex) {
            Logging.getLogger("org.constellation.sos.ws").log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initWorker() {
        worker = new SOSworker("default");
        worker.setServiceUrl(URL);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            if (worker != null) {
                worker.destroy();
            }
            LuceneObservationIndexer indexer = new LuceneObservationIndexer(instDirectory, configDir, "", true);
            SQLRtreeManager.removeTree(indexer.getFileDirectory());
            indexer.destroy();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        try {
            ConfigDirectory.shutdownTestEnvironement(configDirName);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=1)
    public void getCapabilitiesErrorTest() throws Exception {
        super.getCapabilitiesErrorTest();

    }

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=2)
    public void getCapabilitiesTest() throws Exception {
        super.getCapabilitiesTest();

    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=3)
    public void GetObservationErrorTest() throws Exception {
        super.GetObservationErrorTest();
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=4)
    public void GetObservationTest() throws Exception {
        super.GetObservationTest();
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=5)
    public void GetObservationSamplingCurveTest() throws Exception {
        super.GetObservationSamplingCurveTest();
    }

    /**
     * Tests the GetObservationById method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=6)
    public void GetObservationByIdTest() throws Exception {
        super.GetObservationByIdTest();
    }

    /**
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=7)
    public void GetFeatureOfInterestErrorTest() throws Exception {
        super.GetFeatureOfInterestErrorTest();
    }

    /**
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=8)
    public void GetFeatureOfInterestTest() throws Exception {
        super.GetFeatureOfInterestTest();
    }

    /**
     * Tests the GetResultTemplate method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=9)
    public void GetResultTemplateTest() throws Exception {
        super.GetResultTemplateTest();
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=10)
    public void GetResultErrorTest() throws Exception {
        super.GetResultErrorTest();
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=11)
    public void GetResultTest() throws Exception {
        super.GetResultTest();
    }

    /**
     * Tests the InsertObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=12)
    public void insertObservationTest() throws Exception {
        super.insertObservationTest();
    }

    @Test
    @Override
    @Order(order=13)
    public void insertResultTest() throws Exception {
        super.insertResultTest();
    }

    /**
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=14)
    public void destroyTest() throws Exception {
        super.destroyTest();
    }

}
