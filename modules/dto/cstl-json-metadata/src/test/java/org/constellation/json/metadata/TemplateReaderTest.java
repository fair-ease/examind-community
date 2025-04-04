/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014-2017 Geomatys.
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

package org.constellation.json.metadata;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import org.apache.sis.xml.bind.metadata.replace.ReferenceSystemMetadata;
import org.apache.sis.metadata.MetadataStandard;
import org.apache.sis.metadata.iso.DefaultIdentifier;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.metadata.iso.citation.DefaultCitation;
import org.apache.sis.metadata.iso.citation.DefaultCitationDate;
import org.apache.sis.metadata.iso.constraint.DefaultLegalConstraints;
import org.apache.sis.metadata.iso.constraint.DefaultSecurityConstraints;
import org.apache.sis.metadata.iso.extent.DefaultExtent;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.metadata.iso.extent.DefaultGeographicDescription;
import org.apache.sis.metadata.iso.extent.DefaultTemporalExtent;
import org.apache.sis.metadata.iso.identification.DefaultDataIdentification;
import org.apache.sis.metadata.iso.identification.DefaultKeywords;
import org.apache.sis.metadata.iso.maintenance.DefaultScope;
import org.apache.sis.metadata.iso.quality.DefaultConformanceResult;
import org.apache.sis.metadata.iso.quality.DefaultDataQuality;
import org.apache.sis.metadata.iso.quality.DefaultDomainConsistency;
import org.apache.sis.util.SimpleInternationalString;
import org.constellation.dto.metadata.RootObj;
import org.constellation.test.utils.MetadataUtilities;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.junit.Test;
import org.opengis.metadata.citation.DateType;
import org.opengis.metadata.constraint.Classification;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.FactoryException;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class TemplateReaderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testReadFromFilledTemplate() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);


        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);

        Object result = reader.readTemplate(root, new DefaultMetadata());


        final DefaultMetadata expResult = new DefaultMetadata();

        expResult.setFileIdentifier("metadata-id-0007");
        expResult.setLanguage(Locale.FRENCH);
        expResult.setCharacterSet(CharacterSet.UTF_8);
        expResult.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));
        expResult.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster");
        expResult.setMetadataStandardVersion("2011.03");

        final DefaultDataQuality quality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency report = new DefaultDomainConsistency();
        final DefaultCitation cit = new DefaultCitation("some title");
        final DefaultCitationDate date = new DefaultCitationDate(Instant.ofEpochMilli(11145603000L), DateType.CREATION);
        cit.setDates(Arrays.asList(date));
        final DefaultConformanceResult confResult = new DefaultConformanceResult(cit, "some explanation", true);
        report.setResults(Arrays.asList(confResult));
        quality.setReports(Arrays.asList(report));
        expResult.setDataQualityInfo(Arrays.asList(quality));

        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));

        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("this");
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw21, kw22));

        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));

        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);

    }

    @Test
    public void testReadFromFilledTemplate2() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result2.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);


        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);

        Object result = reader.readTemplate(root, new DefaultMetadata());


        final DefaultMetadata expResult = new DefaultMetadata();

        expResult.setFileIdentifier("metadata-id-0007");
        expResult.setLanguage(Locale.FRENCH);
        expResult.setCharacterSet(CharacterSet.UTF_8);
        expResult.setHierarchyLevels(Arrays.asList(ScopeCode.DATASET));
        expResult.setMetadataStandardName("x-urn:schema:ISO19115:INSPIRE:dataset:geo-raster");
        expResult.setMetadataStandardVersion("2011.03");

        final DefaultDataQuality quality = new DefaultDataQuality(new DefaultScope(ScopeCode.DATASET));
        final DefaultDomainConsistency report = new DefaultDomainConsistency();
        final DefaultCitation cit = new DefaultCitation("some title");
        final DefaultCitationDate date = new DefaultCitationDate(Instant.ofEpochMilli(11145600000L), DateType.CREATION);
        cit.setDates(Arrays.asList(date));
        final DefaultConformanceResult confResult = new DefaultConformanceResult(cit, "some explanation", true);
        report.setResults(Arrays.asList(confResult));
        quality.setReports(Arrays.asList(report));
        expResult.setDataQualityInfo(Arrays.asList(quality));

        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));

        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("this");
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw21, kw22));

        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));

        final DefaultLegalConstraints constraint1 = new DefaultLegalConstraints();
        constraint1.setAccessConstraints(Arrays.asList(Restriction.LICENCE));

        final DefaultSecurityConstraints constraint2 = new DefaultSecurityConstraints();
        constraint2.setUseLimitations(Arrays.asList(new SimpleInternationalString("some limitations")));
        constraint2.setClassification(Classification.UNCLASSIFIED);

        dataIdent.setResourceConstraints(Arrays.asList(constraint1,constraint2));

        expResult.setIdentificationInfo(Arrays.asList(dataIdent));


        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);

    }

    @Test
    public void testReadFromFilledTemplateKeywords() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_keywords.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);


        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);

        Object result = reader.readTemplate(root, new DefaultMetadata());


        final DefaultMetadata expResult = new DefaultMetadata();

        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));

        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("this");
        final InternationalString kw22 = new SimpleInternationalString("is");
        keywords2.setKeywords(Arrays.asList(kw21, kw22));

        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));

        expResult.setIdentificationInfo(Arrays.asList(dataIdent));

        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }

    @Test
    public void testReadFromFilledTemplateKeywords3() throws IOException, FactoryException {
        final InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_keywords8.json");
        final RootObj root       =  objectMapper.readValue(stream, RootObj.class);


        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);

        Object result = reader.readTemplate(root, new DefaultMetadata());


        final DefaultMetadata expResult = new DefaultMetadata();

        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("value1");
        final InternationalString kw2 = new SimpleInternationalString("value2");
        final InternationalString kw3 = new SimpleInternationalString("hello");
        final InternationalString kw4 = new SimpleInternationalString("world");

        keywords.setKeywords(Arrays.asList(kw1, kw2, kw3, kw4));

        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords));

        expResult.setIdentificationInfo(Arrays.asList(dataIdent));

        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }

    @Test
    public void testReadFromFilledTemplateMultipleBlock() throws IOException, FactoryException {
        InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_multiple_block.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);

        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);

        Object result = reader.readTemplate(root, new DefaultMetadata());


        final DefaultMetadata expResult = new DefaultMetadata();

        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("hello");
        final InternationalString kw2 = new SimpleInternationalString("world");
        keywords.setKeywords(Arrays.asList(kw1, kw2));
        final DefaultCitation gemet = new DefaultCitation("GEMET");
        gemet.setDates(Arrays.asList(new DefaultCitationDate(Instant.ofEpochMilli(1325376000000L), DateType.PUBLICATION)));
        keywords.setThesaurusName(gemet);

        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords));
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));

        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);

        /*
        * TEST 2 : one keyword with two thesaurus date
        */
        gemet.setDates(Arrays.asList(new DefaultCitationDate(Instant.ofEpochMilli(11145600000L), DateType.CREATION),
                                     new DefaultCitationDate(Instant.ofEpochMilli(1325376000000L), DateType.PUBLICATION)));

        stream = TemplateReaderTest.class.getResourceAsStream("result_multiple_block2.json");
        root   =  objectMapper.readValue(stream, RootObj.class);

        result = reader.readTemplate(root, new DefaultMetadata());

        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }

    @Test
    public void testReadFromFilledTemplateSpecialType() throws Exception {
        InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_special_type.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);

        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);

        Object result = reader.readTemplate(root, new DefaultMetadata());

        final DefaultMetadata expResult = new DefaultMetadata();

        final ReferenceSystemMetadata rs = new ReferenceSystemMetadata(new DefaultIdentifier("EPSG:4326"));
        expResult.setReferenceSystemInfo(Arrays.asList(rs));


        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();

        final DefaultExtent ex = new DefaultExtent();
        final DefaultTemporalExtent tex = new DefaultTemporalExtent();

        tex.setExtent(new TimePeriodType(null, Instant.parse("1970-05-10T00:00:00Z"), Instant.parse("2012-01-01T00:00:00Z")));
        ex.setTemporalElements(Arrays.asList(tex));
        dataIdent.setExtents(Arrays.asList(ex));
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));

        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }

    @Test
    public void testReadFromFilledTemplateExtent() throws IOException, FactoryException {
        InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_extent.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);

        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);

        Object result = reader.readTemplate(root, new DefaultMetadata());

        final DefaultMetadata expResult = new DefaultMetadata();

        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();

        final DefaultExtent ex = new DefaultExtent();
        final DefaultGeographicBoundingBox bbox = new DefaultGeographicBoundingBox(-10, 10, -10, 10);
        bbox.setInclusion(null);
        ex.setGeographicElements(Arrays.asList(bbox));

        dataIdent.setExtents(Arrays.asList(ex));
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));

        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }

    @Test
    public void testReadFromFilledTemplateExtent2() throws IOException, FactoryException {
        InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_extent2.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);

        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);

        Object result = reader.readTemplate(root, new DefaultMetadata());

        final DefaultMetadata expResult = new DefaultMetadata();

        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();

        final DefaultExtent ex = new DefaultExtent();
        final DefaultGeographicBoundingBox bbox = new DefaultGeographicBoundingBox(-10, 10, -10, 10);
        bbox.setInclusion(null);

        final DefaultGeographicDescription desc = new DefaultGeographicDescription();
        final DefaultIdentifier id = new DefaultIdentifier("Gard");
        id.setCodeSpace("departement");
        desc.setGeographicIdentifier(id);

        ex.setGeographicElements(Arrays.asList(bbox, desc));

        dataIdent.setExtents(Arrays.asList(ex));
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));

        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }

    @Test
    public void testReadFromFilledTemplateInspireKeywordsTopicCategory() throws IOException, FactoryException {
        InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_inspire_keywords_topic.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);

        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);

        Object result = reader.readTemplate(root, new DefaultMetadata());

        Instant d = Instant.parse("2008-06-01T00:00:00Z");
        final DefaultMetadata expResult = new DefaultMetadata();

        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();
        final DefaultKeywords keywords = new DefaultKeywords();
        final InternationalString kw1 = new SimpleInternationalString("Elevation");
        keywords.setKeywords(Arrays.asList(kw1));
        final DefaultCitation gemet = new DefaultCitation("GEMET - INSPIRE themes, version 1.0");
        gemet.setDates(Arrays.asList(new DefaultCitationDate(d, DateType.PUBLICATION)));
        keywords.setThesaurusName(gemet);

        final DefaultKeywords keywords2 = new DefaultKeywords();
        final InternationalString kw21 = new SimpleInternationalString("keyword1");
        keywords2.setKeywords(Arrays.asList(kw21));
        final DefaultCitation th2 = new DefaultCitation("thesaurus1");
        th2.setDates(Arrays.asList(new DefaultCitationDate(d, DateType.IN_FORCE)));
        keywords2.setThesaurusName(th2);

        dataIdent.setDescriptiveKeywords(Arrays.asList(keywords, keywords2));

        dataIdent.setTopicCategories(Arrays.asList(TopicCategory.BIOTA));

        expResult.setIdentificationInfo(Arrays.asList(dataIdent));

        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
    
    @Test
    public void testReadFromFilledTemplateTemporalExtent() throws IOException, FactoryException {
        InputStream stream = TemplateReaderTest.class.getResourceAsStream("result_temporal_extent.json");
        RootObj root       =  objectMapper.readValue(stream, RootObj.class);

        TemplateReader reader = new TemplateReader(MetadataStandard.ISO_19115);

        Object result = reader.readTemplate(root, new DefaultMetadata());

        final DefaultMetadata expResult = new DefaultMetadata();

        final DefaultDataIdentification dataIdent = new DefaultDataIdentification();

        final DefaultExtent ex = new DefaultExtent();
        final DefaultTemporalExtent tex = new DefaultTemporalExtent();
        final TimePeriodType period = new TimePeriodType(null, Instant.parse("2020-01-01T00:00:00Z"), 
                                                               Instant.parse("2021-01-01T00:00:00Z"));
        tex.setExtent(period);

        ex.setTemporalElements(Arrays.asList(tex));
        dataIdent.setExtents(Arrays.asList(ex));
        expResult.setIdentificationInfo(Arrays.asList(dataIdent));


        MetadataUtilities.metadataEquals(expResult, (DefaultMetadata) result);
    }
}
