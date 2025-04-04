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

package org.constellation.map.core;

import org.apache.sis.util.iso.DefaultNameFactory;
import org.constellation.dto.contact.AccessConstraint;
import org.constellation.dto.contact.Contact;
import org.constellation.dto.contact.Details;
import org.geotoolkit.inspire.xml.vs.ExtendedCapabilitiesType;
import org.geotoolkit.service.ServiceType;
import org.geotoolkit.wms.xml.AbstractCapability;
import org.geotoolkit.wms.xml.AbstractContactAddress;
import org.geotoolkit.wms.xml.AbstractContactInformation;
import org.geotoolkit.wms.xml.AbstractContactPersonPrimary;
import org.geotoolkit.wms.xml.AbstractKeywordList;
import org.geotoolkit.wms.xml.AbstractOnlineResource;
import org.geotoolkit.wms.xml.AbstractService;
import org.geotoolkit.wms.xml.AbstractWMSCapabilities;
import org.geotoolkit.wms.xml.WmsXmlFactory;
import org.geotoolkit.wms.xml.v111.DescribeLayer;
import org.geotoolkit.wms.xml.v111.GetCapabilities;
import org.geotoolkit.wms.xml.v111.GetFeatureInfo;
import org.geotoolkit.wms.xml.v111.GetLegendGraphic;
import org.geotoolkit.wms.xml.v111.GetMap;
import org.geotoolkit.wms.xml.v130.DCPType;
import org.geotoolkit.wms.xml.v130.Get;
import org.geotoolkit.wms.xml.v130.HTTP;
import org.geotoolkit.wms.xml.v130.ObjectFactory;
import org.geotoolkit.wms.xml.v130.OnlineResource;
import org.geotoolkit.wms.xml.v130.OperationType;
import org.geotoolkit.wms.xml.v130.Post;
import org.geotoolkit.wms.xml.v130.Request;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.util.LocalName;
import org.opengis.util.NameFactory;

import jakarta.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.api.ServiceDef;
import org.constellation.ws.MimeType;

/**
 *  WMS Constants
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class WMSConstant {

    /**
     * Request parameters.
     */
    public static final String GETMAP           = "GetMap";
    public static final String MAP              = "Map";
    public static final String GETFEATUREINFO   = "GetFeatureInfo";
    public static final String DESCRIBELAYER    = "DescribeLayer";
    public static final String GETLEGENDGRAPHIC = "GetLegendGraphic";
    public static final String GETORIGFILE      = "GetOrigFile";

    /**
     * WMS Query service
     */
    public static final String WMS_SERVICE = "WMS";

    /**
     * For backward compatibility with WMS 1.0.0, the request can be done with
     * a value {@code capabilities}.
     */
    public static final String CAPABILITIES     = "Capabilities";

    /** Parameter used in getMap, getLegendGraphic, getCapabilities */
    public static final String KEY_FORMAT = "FORMAT";
    /** Parameter used in getMap, describeLayer */
    public static final String KEY_LAYERS = "LAYERS";
    /** Parameter used in getOrigFile, getLegendGraphic */
    public static final String KEY_LAYER = "LAYER";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_QUERY_LAYERS = "QUERY_LAYERS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_CRS_V111 = "SRS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_CRS_V130 = "CRS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_BBOX = "BBOX";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_ELEVATION = "ELEVATION";
    /** Parameter used in getMap, getOrigFile, getFeatureInfo */
    public static final String KEY_TIME = "TIME";
    /** Parameter used in getMap, getFeatureInfo, getLegendGraphic */
    public static final String KEY_WIDTH = "WIDTH";
    /** Parameter used in getMap, getFeatureInfo, getLegendGraphic */
    public static final String KEY_HEIGHT = "HEIGHT";
    /** Parameter used in getMap */
    public static final String KEY_BGCOLOR = "BGCOLOR";
    /** Parameter used in getMap */
    public static final String KEY_TRANSPARENT = "TRANSPARENT";
    /** Parameter used in getMap */
    public static final String KEY_STYLES = "STYLES";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_STYLE = "STYLE";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_SLD = "SLD";
    /** Parameter used in getMap, getLegendGraphic */
    public static final String KEY_SLD_VERSION = "SLD_VERSION";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_FEATURETYPE = "FEATURETYPE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_COVERAGE = "COVERAGE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_RULE = "RULE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_SCALE = "SCALE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_SLD_BODY = "SLD_BODY";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_REMOTE_OWS_TYPE = "REMOTE_OWS_TYPE";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_REMOTE_OWS_URL = "REMOTE_OWS_URL";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_I_V130 = "I";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_J_V130 = "J";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_I_V111 = "X";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_J_V111 = "Y";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_INFO_FORMAT= "INFO_FORMAT";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_FEATURE_COUNT = "FEATURE_COUNT";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_GETMETADATA = "GetMetadata";
    /** Parameter used in getMap */
    public static final String KEY_AZIMUTH = "AZIMUTH";
    /** Parameter used in GetCapabilities, for backward compatibility with WMS 1.0.0 */
    public static final String KEY_WMTVER = "WMTVER";
    /** Parameter used to store additional parameters from the query, the value object is a MultiValueMap */
    public static final String KEY_EXTRA_PARAMETERS = "EXTRA";
    /** Parameter INSPIRE used to choose the language of the capabilities document */
    public static final String KEY_LANGUAGE = "LANGUAGE";
     /** Parameter used in getFeatureInfo */
    public static final String KEY_FILTER = "FILTER";
     /** Parameter used in getFeatureInfo */
    public static final String KEY_CQL_FILTER = "CQL_FILTER";
     /** Parameter used in getFeatureInfo */
    public static final String KEY_PROPERTYNAME = "PROPERTYNAME";

    public static final String KEY_EXCEPTIONS = "EXCEPTIONS";

    public static final String EXCEPTIONS_INIMAGE = "INIMAGE";

    public static final List<String> SUPPORTED_IMAGE_FORMAT = Collections.unmodifiableList(Arrays.asList(MimeType.IMAGE_GIF,
                                                                                                         MimeType.IMAGE_PNG,
                                                                                                         MimeType.IMAGE_JPEG,
                                                                                                         MimeType.IMAGE_BMP,
                                                                                                         MimeType.IMAGE_TIFF,
                                                                                                         MimeType.IMAGE_PPM));

    public static final List<String> SUPPORTED_LEGEND_IMAGE_FORMAT = Collections.unmodifiableList(Arrays.asList(MimeType.IMAGE_GIF,
                                                                                                         MimeType.IMAGE_PNG,
                                                                                                         MimeType.IMAGE_JPEG,
                                                                                                         MimeType.IMAGE_BMP,
                                                                                                         MimeType.IMAGE_TIFF));

    public static final List<String> NO_TRANSPARENT_FORMAT = Collections.unmodifiableList(Arrays.asList(MimeType.IMAGE_JPEG,
                                                                                                         MimeType.IMAGE_BMP,
                                                                                                         MimeType.IMAGE_PPM));

    public static final List<String> CRS_KEYS = Collections.unmodifiableList(Arrays.asList(KEY_CRS_V111,
                                                                                           KEY_CRS_V130));

    public static final List<String> X_KEYS = Collections.unmodifiableList(Arrays.asList(KEY_I_V111,
                                                                                         KEY_I_V130));

    public static final List<String> Y_KEYS = Collections.unmodifiableList(Arrays.asList(KEY_J_V111,
                                                                                         KEY_J_V130));

    private WMSConstant() {}

    public static Request createRequest130(final List<String> gfi_mimetypes){

        final DCPType dcp = new DCPType(new HTTP(new Get(new OnlineResource("someurl")), new Post(new OnlineResource("someurl"))));

        final OperationType getCapabilities = new OperationType(Arrays.asList(MimeType.TEXT_XML, 
                                                                              MimeType.APP_WMS_XML), dcp);
        final OperationType getMap          = new OperationType(SUPPORTED_IMAGE_FORMAT, dcp);
        final OperationType getFeatureInfo  = new OperationType(gfi_mimetypes, dcp);

        final Request REQUEST_130 = new Request(getCapabilities, getMap, getFeatureInfo);

        /*
         * Extended Operation
         */
        ObjectFactory factory = new ObjectFactory();

        final OperationType describeLayer    = new OperationType(Arrays.asList(MimeType.TEXT_XML), dcp);
        final OperationType getLegendGraphic = new OperationType(SUPPORTED_IMAGE_FORMAT, dcp);

        REQUEST_130.getExtendedOperation().add(factory.createDescribeLayer(describeLayer));

        REQUEST_130.getExtendedOperation().add(factory.createGetLegendGraphic(getLegendGraphic));
        return REQUEST_130;
    }

    public static org.geotoolkit.wms.xml.v111.Request createRequest111(final List<String> gfi_mimetypes){
        final org.geotoolkit.wms.xml.v111.Post post   = new org.geotoolkit.wms.xml.v111.Post(new org.geotoolkit.wms.xml.v111.OnlineResource("someurl"));
        final org.geotoolkit.wms.xml.v111.Get get     = new org.geotoolkit.wms.xml.v111.Get(new org.geotoolkit.wms.xml.v111.OnlineResource("someurl"));
        final org.geotoolkit.wms.xml.v111.HTTP http   = new org.geotoolkit.wms.xml.v111.HTTP(get, post);
        final org.geotoolkit.wms.xml.v111.DCPType dcp = new org.geotoolkit.wms.xml.v111.DCPType(http);

        final GetCapabilities getCapabilities = new GetCapabilities(Arrays.asList(MimeType.TEXT_XML,
                                                                                  MimeType.APP_WMS_XML), dcp);
        final GetMap getMap                   = new GetMap(SUPPORTED_IMAGE_FORMAT, dcp);
        final GetFeatureInfo getFeatureInfo   = new GetFeatureInfo(gfi_mimetypes, dcp);

         /*
         * Extended Operation
         */
        final DescribeLayer describeLayer       = new DescribeLayer(Arrays.asList(MimeType.TEXT_XML), dcp);
        final GetLegendGraphic getLegendGraphic = new GetLegendGraphic(SUPPORTED_LEGEND_IMAGE_FORMAT, dcp);

        org.geotoolkit.wms.xml.v111.Request REQUEST_111 = new org.geotoolkit.wms.xml.v111.Request(getCapabilities, getMap, getFeatureInfo, describeLayer, getLegendGraphic, null, null);
        return REQUEST_111;
    }

    public static final String EXCEPTION_111_XML        = "application/vnd.ogc.se_xml";
    public static final String EXCEPTION_111_INIMAGE    = "application/vnd.ogc.se_inimage";
    public static final String EXCEPTION_111_BLANK      = "application/vnd.ogc.se_blank";
    public static final List<String> EXCEPTION_111 = new ArrayList<>();
    static {
        EXCEPTION_111.add(EXCEPTION_111_XML);
        EXCEPTION_111.add(EXCEPTION_111_INIMAGE);
        EXCEPTION_111.add(EXCEPTION_111_BLANK);
    }


    public static final String EXCEPTION_130_XML        = "XML";
    public static final String EXCEPTION_130_INIMAGE    = "INIMAGE";
    public static final String EXCEPTION_130_BLANK      = "BLANK";
    public static final List<String> EXCEPTION_130 = new ArrayList<>();
    static {
        EXCEPTION_130.add(EXCEPTION_130_XML);
        EXCEPTION_130.add(EXCEPTION_130_INIMAGE);
        EXCEPTION_130.add(EXCEPTION_130_BLANK);
    }

    public static boolean isErrorInImage(String exceptionFormat, String queryVersion) {
        if (queryVersion.equals(ServiceDef.WMS_1_3_0.version.toString())) {
           return EXCEPTION_130_INIMAGE.equalsIgnoreCase(exceptionFormat);
        }
        return EXCEPTION_111_INIMAGE.equalsIgnoreCase(exceptionFormat);
    }

    public static boolean isErrorBlank(String exceptionFormat, String queryVersion) {
        if (queryVersion.equals(ServiceDef.WMS_1_3_0.version.toString())) {
           return EXCEPTION_130_BLANK.equalsIgnoreCase(exceptionFormat);
        }
        return EXCEPTION_111_BLANK.equalsIgnoreCase(exceptionFormat);
    }

    /**
     * Generates the base capabilities for a WMS from the service metadata.
     *
     * @param version the version of the returned object.
     * @param metadata the service metadata
     * @param updateSequence current update sequence.
     * @return the service base capabilities
     */
    public static AbstractWMSCapabilities createCapabilities(final String version, final Details metadata, String updateSequence) {
        ensureNonNull("metadata", metadata);
        ensureNonNull("version",  version);

        final Contact currentContact = metadata.getServiceContact();

        // Create keywords part.
        AbstractKeywordList keywordList = null;
        if (metadata.getKeywords() != null) {
            keywordList = WmsXmlFactory.createKeyword(version, metadata.getKeywords());
        }

        // Create address part.
        AbstractOnlineResource orgUrl = null;
        AbstractContactInformation contact = null;
        if (currentContact != null) {
            currentContact.setFullname();

            final AbstractContactAddress address = WmsXmlFactory.createContactAddress(version,"POSTAL",
                    currentContact.getAddress(), currentContact.getCity(), currentContact.getState(),
                    currentContact.getZipCode(), currentContact.getCountry());

            // Create contact part.
            final AbstractContactPersonPrimary personPrimary = WmsXmlFactory.createContactPersonPrimary(version,
                    currentContact.getFullname(), currentContact.getOrganisation());
            contact = WmsXmlFactory.createContactInformation(version,
                    personPrimary, currentContact.getPosition(), address, currentContact.getPhone(), currentContact.getFax(),
                    currentContact.getEmail());

            // url
            if (currentContact.getUrl() != null) {
                orgUrl = WmsXmlFactory.createOnlineResource(version, currentContact.getUrl());
            }
        }

        // Create service part.
        AccessConstraint serviceConstraints = metadata.getServiceConstraints();
        if (serviceConstraints == null) {
            serviceConstraints = new AccessConstraint();
        }
        final String name;
        if ("1.1.1".equals(version)) {
            name = "OGC:WMS";
        } else {
            name = metadata.getName();
        }
        final AbstractService newService = WmsXmlFactory.createService(version, name,
                metadata.getName(), metadata.getDescription(), keywordList, orgUrl, contact,
                serviceConstraints.getFees(), serviceConstraints.getAccessConstraint(),
                serviceConstraints.getLayerLimit(), serviceConstraints.getMaxWidth(),
                serviceConstraints.getMaxHeight());

        // extension
        final NameFactory nf = new DefaultNameFactory();
        final LocalName servType = nf.createLocalName(null, "view");
        final ExtendedCapabilitiesType ext = new ExtendedCapabilitiesType(ScopeCode.SERVICE, new ServiceType(servType));
        final org.geotoolkit.inspire.xml.vs.ObjectFactory factory = new org.geotoolkit.inspire.xml.vs.ObjectFactory();
        final JAXBElement<?> extension = factory.createExtendedCapabilities(ext);
        // Create capabilities base.
        final AbstractCapability capability =  WmsXmlFactory.createCapability(version, extension);
        return WmsXmlFactory.createCapabilities(version, newService, capability, updateSequence);
    }
}
