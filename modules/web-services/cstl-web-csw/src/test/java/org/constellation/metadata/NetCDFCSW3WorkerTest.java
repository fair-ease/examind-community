/*
 *    Examind - An open source and standard compliant SDI
 *    https://community.examind.com/
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
package org.constellation.metadata;

import org.constellation.metadata.core.CSWworker;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.ComparisonMode;
import org.constellation.dto.service.config.generic.Automatic;
import org.constellation.test.utils.Order;
import org.constellation.util.Util;
import org.constellation.ws.MimeType;
import org.geotoolkit.csw.xml.ElementSetType;
import org.geotoolkit.csw.xml.v202.ElementSetNameType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdResponseType;
import org.geotoolkit.csw.xml.v202.GetRecordByIdType;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.geotoolkit.xml.AnchoredMarshallerPool;
import org.junit.Assume;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.Unmarshaller;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import org.geotoolkit.test.xml.DocumentComparator;

import static org.constellation.api.CommonConstants.TRANSACTION_SECURIZED;
import org.constellation.dto.contact.AccessConstraint;
import org.constellation.dto.contact.Contact;
import org.constellation.dto.contact.Details;
import static org.constellation.test.utils.MetadataUtilities.metadataEquals;
import org.constellation.test.utils.TestEnvironment.TestResource;
import org.constellation.test.utils.TestEnvironment.TestResources;
import static org.constellation.test.utils.TestEnvironment.initDataDirectory;
import static org.constellation.test.utils.TestResourceUtils.writeResourceDataFile;
import org.geotoolkit.nio.IOUtilities;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Ignore;


/**
 * Test of the NetCDF Metadata provider
 *
 *  @author Guilhem Legal (Geomatys)
 */
public class NetCDFCSW3WorkerTest extends CSW3WorkerTest {

    private static Path DATA_DIRECTORY;

    private static boolean initialized = false;

    @BeforeClass
    public static void setUpClass() throws Exception {
        final Path configDir = Paths.get("target");
        DATA_DIRECTORY = configDir.resolve("NCCSWWorker3Test" + UUID.randomUUID());
        Files.createDirectories(DATA_DIRECTORY);

        //we write the data files
        writeResourceDataFile(DATA_DIRECTORY, "org/constellation/netcdf/2005092200_sst_21-24.en.nc", "2005092200_sst_21-24.en.nc");

    }

    @PostConstruct
    public void setUp() {
        try {
            if (!initialized) {
                serviceBusiness.deleteAll();
                providerBusiness.removeAll();

                final TestResources testResource = initDataDirectory();

                Integer pr = testResource.createProviderWithPath(TestResource.METADATA_NETCDF, DATA_DIRECTORY, providerBusiness, null).id;

                //we write the configuration file
                Automatic configuration = new Automatic();
                configuration.putParameter(TRANSACTION_SECURIZED, "false");
                configuration.putParameter("locale", "en");

                Details d = new Details("Constellation CSW Server", "default", Arrays.asList("CS-W"),
                                        "CS-W 2.0.2/AP ISO19115/19139 for service, datasets and applications",
                                        Arrays.asList("2.0.0", "2.0.2", "3.0.0"),
                                        new Contact(), new AccessConstraint(),
                                        true, "eng");

                Integer sid = serviceBusiness.create("csw", "default", configuration, d, null);
                serviceBusiness.linkCSWAndProvider(sid, pr, true);

                pool = EBRIMMarshallerPool.getInstance();
                fillPoolAnchor((AnchoredMarshallerPool) pool);

                Unmarshaller u = pool.acquireUnmarshaller();
                pool.recycle(u);

                worker = new CSWworker("default");
                initialized = true;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        IOUtilities.deleteSilently(DATA_DIRECTORY);
    }

    /**
     * Tests the getcapabilities method
     */
    @Test
    @Override
    @Order(order=1)
    public void getRecordByIdTest() throws Exception {
        Assume.assumeTrue(System.getProperty("os.name").toLowerCase().contains("linux"));
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        /*
         *  TEST 1 : getRecordById with the first metadata in ISO mode.
         */
        GetRecordByIdType request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.isotc211.org/2005/gmd", Arrays.asList("2005092200_sst_21-24.en"));
        GetRecordByIdResponseType result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAny().size() == 1);
        Object obj = result.getAny().get(0);

        if (obj instanceof DefaultMetadata) {
            DefaultMetadata isoResult = (DefaultMetadata) obj;
            DefaultMetadata ExpResult1 = (DefaultMetadata) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/2005092200_sst_21-24.en.xml"));
            metadataEquals(ExpResult1, isoResult, ComparisonMode.APPROXIMATE);
        } else if (obj instanceof Node) {
            Node resultNode = (Node) obj;
            Node expResultNode = getOriginalMetadata("org/constellation/xml/metadata/2005092200_sst_21-24.en.xml");
            DocumentComparator comparator = new DocumentComparator(expResultNode, resultNode);
            comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
            comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
            comparator.ignoredNodes.add("http://www.isotc211.org/2005/gco:DateTime");       // Because the date type is not the same.
            comparator.ignoredNodes.add("http://www.opengis.net/gml/3.2:timePosition");     // Because the date type is not the same.
            comparator.compare();
        } else {
            fail("unexpected record type:" + obj);
        }



/*        Marshaller marshaller = pool.acquireMarshaller();
        marshaller.marshal(obj, new File("test.xml"));*/



        /*
         *  TEST 2 : getRecordById with the first metadata in DC mode (BRIEF).

        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.BRIEF),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof BriefRecordType);

        BriefRecordType briefResult =  (BriefRecordType) obj;

        BriefRecordType expBriefResult1 =  ((JAXBElement<BriefRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1BDC.xml"))).getValue();

        assertEquals(expBriefResult1, briefResult);

        /*
         *  TEST 3 : getRecordById with the first metadata in DC mode (SUMMARY).

        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.SUMMARY),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof SummaryRecordType);

        SummaryRecordType sumResult =  (SummaryRecordType) obj;

        SummaryRecordType expSumResult1 =  ((JAXBElement<SummaryRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1SDC.xml"))).getValue();

        assertEquals(expSumResult1.getFormat(), sumResult.getFormat());
        assertEquals(expSumResult1, sumResult);

        /*
         *  TEST 4 : getRecordById with the first metadata in DC mode (FULL).

        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof RecordType);

        RecordType recordResult = (RecordType) obj;

        RecordType expRecordResult1 =  ((JAXBElement<RecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1FDC.xml"))).getValue();

        assertEquals(expRecordResult1.getFormat(), recordResult.getFormat());
        assertEquals(expRecordResult1, recordResult);

        /*
         *  TEST 5 : getRecordById with two metadata in DC mode (FULL).

        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.FULL),
                MimeType.APPLICATION_XML, "http://www.opengis.net/cat/csw/2.0.2", Arrays.asList("42292_5p_19900609195600","42292_9s_19900610041000"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 2);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof RecordType);
        RecordType recordResult1 = (RecordType) obj;

        obj = result.getAbstractRecord().get(1);
        assertTrue(obj instanceof RecordType);
        RecordType recordResult2 = (RecordType) obj;

        RecordType expRecordResult2 =  ((JAXBElement<RecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta2FDC.xml"))).getValue();

        assertEquals(expRecordResult1, recordResult1);
        assertEquals(expRecordResult2, recordResult2);

        /*
         *  TEST 6 : getRecordById with the first metadata with no outputSchema.

        request = new GetRecordByIdType("CSW", "2.0.2", new ElementSetNameType(ElementSetType.SUMMARY),
                MimeType.APPLICATION_XML, null, Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof SummaryRecordType);

        sumResult =  (SummaryRecordType) obj;

        expSumResult1 =  ((JAXBElement<SummaryRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1SDC.xml"))).getValue();

        assertEquals(expSumResult1.getFormat(), sumResult.getFormat());
        assertEquals(expSumResult1, sumResult);

        /*
         *  TEST 7 : getRecordById with the first metadata with no outputSchema and no ElementSetName.

        request = new GetRecordByIdType("CSW", "2.0.2", null,
                MimeType.APPLICATION_XML, null, Arrays.asList("42292_5p_19900609195600"));
        result = (GetRecordByIdResponseType) worker.getRecordById(request);

        assertTrue(result != null);
        assertTrue(result.getAbstractRecord().size() == 1);
        assertTrue(result.getAny().isEmpty());

        obj = result.getAbstractRecord().get(0);
        assertTrue(obj instanceof SummaryRecordType);

        sumResult =  (SummaryRecordType) obj;

        expSumResult1 =  ((JAXBElement<SummaryRecordType>) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/xml/metadata/meta1SDC.xml"))).getValue();

        assertEquals(expSumResult1.getFormat(), sumResult.getFormat());
        assertEquals(expSumResult1, sumResult);

       */
        pool.recycle(unmarshaller);
    }


    /**
     * Tests the getRecords method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Ignore
    @Override
    @Order(order=2)
    public void getRecordsTest() throws Exception {
        //
    }



    /**
     * Tests the getDomain method
     */
    @Test
    @Ignore
    @Override
    @Order(order=3)
    public void getDomainTest() throws Exception {
        //
    }
}
