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

import com.examind.sensor.component.SensorServiceBusiness;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.ISensorBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.test.SpringContextTest;
import org.geotoolkit.gml.xml.v321.TimePeriodType;
import org.junit.Assert;
import org.opengis.temporal.TemporalPrimitive;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public abstract class SOSConfigurerTest extends SpringContextTest {

    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos.ws");
    @Inject
    protected IServiceBusiness serviceBusiness;
    @Inject
    protected IProviderBusiness providerBusiness;
    @Inject
    protected ISensorBusiness sensorBusiness;

    @Autowired
    private SensorServiceBusiness sensorServBusiness;
    
    protected static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");

    public void getDecimatedObservationsCsvTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        String result = (String) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:3", Arrays.asList("depth"), new ArrayList<>(), null, null, 10, "text/csv");
        String expResult = "time,urn:ogc:def:phenomenon:GEOM:depth\n" +
                                "2007-05-01T03:56:00,6.56\n" +
                                "2007-05-01T05:50:00,6.56\n" +
                                "2007-05-01T07:44:00,6.56\n" +
                                "2007-05-01T09:38:00,6.56\n" +
                                "2007-05-01T11:32:00,6.56\n" +
                                "2007-05-01T17:59:00,6.56\n" +
                                "2007-05-01T19:08:00,6.55\n" +
                                "2007-05-01T21:02:00,6.55\n";
        Assert.assertEquals(expResult, result);

        result = (String) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:8", Arrays.asList("aggregatePhenomenon"), new ArrayList<>(), null, null, 10, "text/csv");
        expResult = "time,urn:ogc:def:phenomenon:GEOM:depth,urn:ogc:def:phenomenon:GEOM:temperature\n" +
                    "2007-05-01T12:59:00,6.56,12.0\n" +
                    "2007-05-01T13:59:00,6.56,13.0\n" +
                    "2007-05-01T14:59:00,6.56,14.0\n" +
                    "2007-05-01T15:59:00,6.56,15.0\n" +
                    "2007-05-01T16:59:00,6.56,16.0\n";

        Assert.assertEquals(expResult, result);

        result = (String) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:10", Arrays.asList("depth"), Arrays.asList("station-001"), null, null, 10, "text/csv");
        expResult = "time,urn:ogc:def:phenomenon:GEOM:depth\n" +
                    "2009-05-01T13:47:00,4.5\n" +
                    "2009-05-01T14:00:00,5.9\n";

        Assert.assertEquals(expResult, result);

        result = (String) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:10", Arrays.asList("depth"), Arrays.asList("station-002"), null, null, 10, "text/csv");
        expResult = "time,urn:ogc:def:phenomenon:GEOM:depth\n" +
                    "2009-05-01T14:01:00,8.9\n" +
                    "2009-05-01T14:02:00,7.8\n" +
                    "2009-05-01T14:03:00,9.9\n" +
                    "2009-05-01T14:04:00,9.1\n";

        Assert.assertEquals(expResult, result);
    }
    
    public void getDecimatedObservationsDataArrayTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        List result = (List) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:3", Arrays.asList("depth"), new ArrayList<>(), null, null, 10, "resultArray");
        List expResult = Arrays.asList(
                                Arrays.asList(format.parse("2007-05-01T03:56:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T05:50:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T07:44:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T09:38:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T11:32:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T17:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T19:08:00.0"),6.55),
                                Arrays.asList(format.parse("2007-05-01T21:02:00.0"),6.55));
        Assert.assertEquals(expResult, result);

        result = (List) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:8", Arrays.asList("aggregatePhenomenon"), new ArrayList<>(), null, null, 10, "resultArray");
        expResult = Arrays.asList(
                        Arrays.asList(format.parse("2007-05-01T12:59:00.0"),6.56,12.0),
                        Arrays.asList(format.parse("2007-05-01T13:59:00.0"),6.56,13.0),
                        Arrays.asList(format.parse("2007-05-01T14:59:00.0"),6.56,14.0),
                        Arrays.asList(format.parse("2007-05-01T15:59:00.0"),6.56,15.0),
                        Arrays.asList(format.parse("2007-05-01T16:59:00.0"),6.56,16.0));

        Assert.assertEquals(expResult, result);

        result = (List) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:10", Arrays.asList("depth"), Arrays.asList("station-001"), null, null, 10, "resultArray");
        expResult = Arrays.asList(
                    Arrays.asList(format.parse("2009-05-01T13:47:00.0"),4.5),
                    Arrays.asList(format.parse("2009-05-01T14:00:00.0"),5.9));

        Assert.assertEquals(expResult, result);

        result = (List) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:10", Arrays.asList("depth"), Arrays.asList("station-002"), null, null, 10, "resultArray");
        expResult = Arrays.asList(
                    Arrays.asList(format.parse("2009-05-01T14:01:00.0"),8.9),
                    Arrays.asList(format.parse("2009-05-01T14:02:00.0"),7.8),
                    Arrays.asList(format.parse("2009-05-01T14:03:00.0"),9.9),
                    Arrays.asList(format.parse("2009-05-01T14:04:00.0"),9.1));

        Assert.assertEquals(expResult, result);
    }

    public void getObservationsCsvTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        String result = (String) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:3", Arrays.asList("depth"), new ArrayList<>(), null, null, null, "text/csv");
        String expResult = "time,urn:ogc:def:phenomenon:GEOM:depth\n" +
                                "2007-05-01T02:59:00,6.56\n" +
                                "2007-05-01T03:59:00,6.56\n" +
                                "2007-05-01T04:59:00,6.56\n" +
                                "2007-05-01T05:59:00,6.56\n" +
                                "2007-05-01T06:59:00,6.56\n" +
                                "2007-05-01T07:59:00,6.56\n" +
                                "2007-05-01T08:59:00,6.56\n" +
                                "2007-05-01T09:59:00,6.56\n" +
                                "2007-05-01T10:59:00,6.56\n" +
                                "2007-05-01T11:59:00,6.56\n" +
                                "2007-05-01T17:59:00,6.56\n" +
                                "2007-05-01T18:59:00,6.55\n" +
                                "2007-05-01T19:59:00,6.55\n" +
                                "2007-05-01T20:59:00,6.55\n" +
                                "2007-05-01T21:59:00,6.55\n";
        Assert.assertEquals(expResult, result);

        result = (String) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:8", Arrays.asList("aggregatePhenomenon"), new ArrayList<>(), null, null, null, "text/csv");
        expResult = "time,urn:ogc:def:phenomenon:GEOM:depth,urn:ogc:def:phenomenon:GEOM:temperature\n" +
                    "2007-05-01T12:59:00,6.56,12.0\n" +
                    "2007-05-01T13:59:00,6.56,13.0\n" +
                    "2007-05-01T14:59:00,6.56,14.0\n" +
                    "2007-05-01T15:59:00,6.56,15.0\n" +
                    "2007-05-01T16:59:00,6.56,16.0\n";

        Assert.assertEquals(expResult, result);

        result = (String) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:10", Arrays.asList("depth"), Arrays.asList("station-001"), null, null, null, "text/csv");
        expResult = "time,urn:ogc:def:phenomenon:GEOM:depth\n" +
                    "2009-05-01T13:47:00,4.5\n" +
                    "2009-05-01T14:00:00,5.9\n";

        Assert.assertEquals(expResult, result);

        result = (String) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:10", Arrays.asList("depth"), Arrays.asList("station-002"), null, null, null, "text/csv");
        expResult = "time,urn:ogc:def:phenomenon:GEOM:depth\n" +
                    "2009-05-01T14:01:00,8.9\n" +
                    "2009-05-01T14:02:00,7.8\n" +
                    "2009-05-01T14:03:00,9.9\n" +
                    "2009-05-01T14:04:00,9.1\n";

        Assert.assertEquals(expResult, result);
    }
    
    public void getObservationsDataArrayTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        List result = (List) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:3", Arrays.asList("depth"), new ArrayList<>(), null, null, null, "resultArray");
        List expResult = Arrays.asList(
                                Arrays.asList(format.parse("2007-05-01T02:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T03:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T04:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T05:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T06:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T07:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T08:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T09:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T10:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T11:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T17:59:00.0"),6.56),
                                Arrays.asList(format.parse("2007-05-01T18:59:00.0"),6.55),
                                Arrays.asList(format.parse("2007-05-01T19:59:00.0"),6.55),
                                Arrays.asList(format.parse("2007-05-01T20:59:00.0"),6.55),
                                Arrays.asList(format.parse("2007-05-01T21:59:00.0"),6.55));
        Assert.assertEquals(expResult, result);

        result = (List) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:8", Arrays.asList("aggregatePhenomenon"), new ArrayList<>(), null, null, null, "resultArray");
        expResult = Arrays.asList(
                    Arrays.asList(format.parse("2007-05-01T12:59:00.0"),6.56,12.0),
                    Arrays.asList(format.parse("2007-05-01T13:59:00.0"),6.56,13.0),
                    Arrays.asList(format.parse("2007-05-01T14:59:00.0"),6.56,14.0),
                    Arrays.asList(format.parse("2007-05-01T15:59:00.0"),6.56,15.0),
                    Arrays.asList(format.parse("2007-05-01T16:59:00.0"),6.56,16.0));

        Assert.assertEquals(expResult, result);

        result = (List) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:10", Arrays.asList("depth"), Arrays.asList("station-001"), null, null, null, "resultArray");
        expResult = Arrays.asList(
                    Arrays.asList(format.parse("2009-05-01T13:47:00.0"),4.5),
                    Arrays.asList(format.parse("2009-05-01T14:00:00.0"),5.9));

        Assert.assertEquals(expResult, result);

        result = (List) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:10", Arrays.asList("depth"), Arrays.asList("station-002"), null, null, null, "resultArray");
        expResult = Arrays.asList(
                    Arrays.asList(format.parse("2009-05-01T14:01:00.0"),8.9),
                    Arrays.asList(format.parse("2009-05-01T14:02:00.0"),7.8),
                    Arrays.asList(format.parse("2009-05-01T14:03:00.0"),9.9),
                    Arrays.asList(format.parse("2009-05-01T14:04:00.0"),9.1));

        Assert.assertEquals(expResult, result);
    }

    public void getObservationsCsvProfileTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        String result = (String) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:2", Arrays.asList("aggregatePhenomenon"), new ArrayList<>(), null, null, 10, "text/csv");
        String expResult = "urn:ogc:def:phenomenon:GEOM:depth,urn:ogc:def:phenomenon:GEOM:temperature\n" +
                           "12,18.5\n" +
                           "87,21.2\n" +
                           "96,23.9\n" +
                           "192,26.2\n" +
                           "384,31.4\n" +
                           "768,35.1\n" +
                           "12,18.5\n" +
                           "12,18.5\n";
        Assert.assertEquals(expResult, result);
    }
    
    public void getObservationsDataArrayProfileTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        List result = (List) sensorServBusiness.getResultsCsv(sid, "urn:ogc:object:sensor:GEOM:2", Arrays.asList("aggregatePhenomenon"), new ArrayList<>(), null, null, 10, "resultArray");
        List expResult = Arrays.asList(
                            Arrays.asList(12L,18.5),
                            Arrays.asList(87L,21.2),
                            Arrays.asList(96L,23.9),
                            Arrays.asList(192L,26.2),
                            Arrays.asList(384L,31.4),
                            Arrays.asList(768L,35.1),
                            Arrays.asList(12L,18.5),
                            Arrays.asList(12L,18.5));
        Assert.assertEquals(expResult.size(), result.size());
        Assert.assertEquals(expResult.get(0), result.get(0));
        Assert.assertEquals(expResult, result);
    }

    public void getSensorIdTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        Collection<String> results = new HashSet(sensorServBusiness.getSensorIds(sid));
        Set<String> expResults = new HashSet<>();
        expResults.add("urn:ogc:object:sensor:GEOM:1");
        expResults.add("urn:ogc:object:sensor:GEOM:10");
        expResults.add("urn:ogc:object:sensor:GEOM:12");
        expResults.add("urn:ogc:object:sensor:GEOM:13");
        expResults.add("urn:ogc:object:sensor:GEOM:14");
        expResults.add("urn:ogc:object:sensor:GEOM:2");
        expResults.add("urn:ogc:object:sensor:GEOM:3");
        expResults.add("urn:ogc:object:sensor:GEOM:4");
        expResults.add("urn:ogc:object:sensor:GEOM:test-1");
        expResults.add("urn:ogc:object:sensor:GEOM:6");
        expResults.add("urn:ogc:object:sensor:GEOM:7");
        expResults.add("urn:ogc:object:sensor:GEOM:8");
        expResults.add("urn:ogc:object:sensor:GEOM:9");
        expResults.add("urn:ogc:object:sensor:GEOM:test-id");
        Assert.assertEquals(expResults, results);
    }

    public void getSensorIdsForObservedPropertyTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        Collection<String> results = sensorServBusiness.getSensorIdsForObservedProperty(sid, "temperature");
        List<String> expResults = Arrays.asList("urn:ogc:object:sensor:GEOM:12",
                                                "urn:ogc:object:sensor:GEOM:13",
                                                "urn:ogc:object:sensor:GEOM:14",
                                                "urn:ogc:object:sensor:GEOM:2",
                                                "urn:ogc:object:sensor:GEOM:7",
                                                "urn:ogc:object:sensor:GEOM:8",
                                                "urn:ogc:object:sensor:GEOM:test-1");
        Assert.assertEquals(expResults, results);
    }

    public void getObservedPropertiesForSensorIdTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        Collection<String> results = sensorServBusiness.getObservedPropertiesForSensorId(sid, "urn:ogc:object:sensor:GEOM:3", true);
        Set<String> expResults = Collections.singleton("urn:ogc:def:phenomenon:GEOM:depth");
        Assert.assertEquals(expResults, results);

        results = sensorServBusiness.getObservedPropertiesForSensorId(sid, "urn:ogc:object:sensor:GEOM:test-1", false);
        expResults = Collections.singleton("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon");
        Assert.assertEquals(expResults, results);

        results = sensorServBusiness.getObservedPropertiesForSensorId(sid, "urn:ogc:object:sensor:GEOM:test-1", true);
        expResults = new HashSet();
        expResults.add("urn:ogc:def:phenomenon:GEOM:temperature");
        expResults.add("urn:ogc:def:phenomenon:GEOM:depth");
        Assert.assertEquals(expResults, results);
    }

    public void getTimeForSensorIdTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        TemporalPrimitive results = sensorServBusiness.getTimeForSensorId(sid, "urn:ogc:object:sensor:GEOM:3");
        TemporalPrimitive expResults = new TimePeriodType(null, "2007-05-01 02:59:00.0", "2007-05-01 21:59:00.0");
        Assert.assertEquals(expResults, results);
    }


    public void getObservedPropertiesTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        Collection<String> results = sensorServBusiness.getObservedPropertiesIds(sid);
        Set<String> expResults = new HashSet<>();
        expResults.add("aggregatePhenomenon");
        expResults.add("aggregatePhenomenon-2");
        expResults.add("depth");
        expResults.add("temperature");
        expResults.add("salinity");
        Assert.assertEquals(expResults, results);
    }

    public void getWKTSensorLocationTest() throws Exception {
        final Integer sid = serviceBusiness.getServiceIdByIdentifierAndType("SOS", "default");
        String result = sensorServBusiness.getWKTSensorLocation(sid, "urn:ogc:object:sensor:GEOM:1");

        String expResult = "POINT (-4.144984627896042 42.38798858151254)";
        double expX = -4.144984627896042;
        double expY = 42.38798858151254;

        Assert.assertTrue(result.startsWith("POINT ("));
        Assert.assertTrue(result.endsWith(")"));

        String s = result.substring(7, result.length() - 1);
        String[] coords = s.split(" ");
        Assert.assertEquals(2, coords.length);

        Assert.assertEquals(expX, Double.parseDouble(coords[0]), 0.00000000000001);
        Assert.assertEquals(expY, Double.parseDouble(coords[1]), 0.00000000000001);

        //Assert.assertEquals(expResult, result);
    }
}
