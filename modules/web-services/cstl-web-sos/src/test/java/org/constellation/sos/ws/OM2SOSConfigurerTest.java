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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import org.apache.sis.storage.DataStoreProvider;
import org.constellation.admin.SpringHelper;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.dto.service.config.sos.SOSConfiguration;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.internal.sql.DerbySqlScriptRunner;
import org.apache.sis.util.logging.Logging;
import org.constellation.business.IProviderBusiness;
import org.constellation.dto.Sensor;
import org.constellation.business.IProviderBusiness.SPI_NAMES;
import static org.constellation.test.utils.TestResourceUtils.writeResourceDataFile;
import org.geotoolkit.nio.IOUtilities;
import org.geotoolkit.storage.DataStores;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class OM2SOSConfigurerTest extends SOSConfigurerTest {

    private static final String CONFIG_DIR_NAME = "OM2SOSConfigurerTest" + UUID.randomUUID().toString();
    private static Path sensorDirectory;

    private static DefaultDataSource ds = null;

    private static String url;

    private static boolean initialized = false;

    @BeforeClass
    public static void setUpClass() throws Exception {
        url = "jdbc:derby:memory:OM2ConfigTest2;create=true";
        ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.setEncoding("UTF-8");
        String sql = IOUtilities.toString(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
        sql = sql.replace("$SCHEMA", "");
        sr.run(sql);
        sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));

        final Path configDir = ConfigDirectory.setupTestEnvironement(CONFIG_DIR_NAME);
        Path SOSDirectory    = configDir.resolve("SOS");
        Path instDirectory   = SOSDirectory.resolve("default");
        sensorDirectory      = instDirectory.resolve("sensors");
        Files.createDirectories(sensorDirectory);

        writeResourceDataFile(sensorDirectory, "org/constellation/xml/sml/system.xml",     "urn:ogc:object:sensor:GEOM:1.xml");
        writeResourceDataFile(sensorDirectory, "org/constellation/xml/sml/component.xml",  "urn:ogc:object:sensor:GEOM:2.xml");
        writeResourceDataFile(sensorDirectory, "org/constellation/xml/sml/system3.xml",    "urn:ogc:object:sensor:GEOM:5.xml");
        writeResourceDataFile(sensorDirectory, "org/constellation/xml/sml/system4.xml",    "urn:ogc:object:sensor:GEOM:8.xml");
    }

    @PostConstruct
    public void setUp() {
        SpringHelper.injectDependencies(configurer);
        try {

            if (!initialized) {
                // clean up
                serviceBusiness.deleteAll();
                providerBusiness.removeAll();

                final DataStoreProvider factory = DataStores.getProviderById("observationSOSDatabase");
                final ParameterValueGroup dbConfig = factory.getOpenParameters().createValue();
                dbConfig.parameter("sgbdtype").setValue("derby");
                dbConfig.parameter("derbyurl").setValue(url);
                dbConfig.parameter("phenomenon-id-base").setValue("urn:ogc:def:phenomenon:GEOM:");
                dbConfig.parameter("observation-template-id-base").setValue("urn:ogc:object:observation:template:GEOM:");
                dbConfig.parameter("observation-id-base").setValue("urn:ogc:object:observation:GEOM:");
                dbConfig.parameter("sensor-id-base").setValue("urn:ogc:object:sensor:GEOM:");
                Integer omPrId = providerBusiness.create("omSrc", SPI_NAMES.OBSERVATION_SPI_NAME, dbConfig);

                final DataStoreProvider factorySML = DataStores.getProviderById("filesensor");
                final ParameterValueGroup params = factorySML.getOpenParameters().createValue();
                params.parameter("data_directory").setValue(sensorDirectory);
                Integer senPrId = providerBusiness.create("sensorSrc", IProviderBusiness.SPI_NAMES.SENSOR_SPI_NAME, params);
                providerBusiness.createOrUpdateData(senPrId, null, false);

                //we write the configuration file
                final SOSConfiguration configuration = new SOSConfiguration();
                configuration.setProfile("transactional");
                configuration.getParameters().put("transactionSecurized", "false");

                int sid = serviceBusiness.create("sos", "default", configuration, null, null);
                serviceBusiness.linkServiceAndProvider(sid, omPrId);
                serviceBusiness.linkServiceAndProvider(sid, senPrId);

                List<Sensor> sensors = sensorBusiness.getByProviderId(senPrId);
                sensors.stream().forEach((sensor) -> {
                    sensorBusiness.addSensorToService(sid, sensor.getId());
                });

                initialized = true;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            File derbyLog = new File("derby.log");
            if (derbyLog.exists()) {
                derbyLog.delete();
            }
            File mappingFile = new File("mapping.properties");
            if (mappingFile.exists()) {
                mappingFile.delete();
            }
            if (ds != null) {
                ds.shutdown();
            }
            ConfigDirectory.shutdownTestEnvironement(CONFIG_DIR_NAME);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    @Test
    @Override
    @Order(order=1)
    public void getObservationsCsvTest() throws Exception {
        super.getObservationsCsvTest();
    }

    @Test
    @Override
    @Order(order=2)
    public void getObservationsCsvProfileTest() throws Exception {
        super.getObservationsCsvProfileTest();
    }

    @Test
    @Override
    @Order(order=3)
    public void getSensorIdTest() throws Exception {
        super.getSensorIdTest();
    }

    @Test
    @Override
    @Order(order=4)
    public void getSensorIdsForObservedPropertyTest() throws Exception {
        super.getSensorIdsForObservedPropertyTest();
    }

    @Test
    @Override
    @Order(order=5)
    public void getObservedPropertiesForSensorIdTest() throws Exception {
        super.getObservedPropertiesForSensorIdTest();
    }

    @Test
    @Override
    @Order(order=6)
    public void getTimeForSensorIdTest() throws Exception {
        super.getTimeForSensorIdTest();
    }

    @Test
    @Override
    @Order(order=7)
    public void getObservedPropertiesTest() throws Exception {
        super.getObservedPropertiesTest();
    }

    @Test
    @Override
    @Order(order=8)
    public void getDecimatedObservationsCsvTest() throws Exception {
        super.getDecimatedObservationsCsvTest();
    }

    @Test
    @Override
    @Order(order=8)
    public void getWKTSensorLocationTest() throws Exception {
        super.getWKTSensorLocationTest();
    }
}
