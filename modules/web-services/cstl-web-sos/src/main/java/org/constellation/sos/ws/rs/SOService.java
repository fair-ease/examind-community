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
package org.constellation.sos.ws.rs;

import org.constellation.api.ServiceDef;
import org.constellation.api.ServiceDef.Specification;
import org.constellation.sos.core.SOSworker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.Worker;
import org.constellation.ws.rs.OGCWebService;
import org.geotoolkit.ows.xml.AcceptFormats;
import org.geotoolkit.ows.xml.AcceptVersions;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.sos.xml.GetCapabilities;
import org.geotoolkit.sos.xml.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.GetObservation;
import org.geotoolkit.sos.xml.GetObservationById;
import org.geotoolkit.sos.xml.GetResult;
import org.geotoolkit.sos.xml.GetResultTemplate;
import org.geotoolkit.sos.xml.InsertObservation;
import org.geotoolkit.sos.xml.InsertResult;
import org.geotoolkit.sos.xml.InsertResultTemplate;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sos.xml.SOSResponseWrapper;
import org.geotoolkit.sos.xml.GetFeatureOfInterestTime;
import org.geotoolkit.swes.xml.DeleteSensor;
import org.geotoolkit.swes.xml.DescribeSensor;
import org.geotoolkit.swes.xml.InsertSensor;
import org.geotoolkit.util.StringUtilities;
import org.opengis.filter.Filter;
import org.opengis.observation.ObservationCollection;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import static org.constellation.api.ServiceConstants.*;
import static org.constellation.api.QueryConstants.ACCEPT_FORMATS_PARAMETER;
import static org.constellation.api.QueryConstants.ACCEPT_VERSIONS_PARAMETER;
import static org.constellation.api.QueryConstants.REQUEST_PARAMETER;
import static org.constellation.api.QueryConstants.SECTIONS_PARAMETER;
import static org.constellation.api.QueryConstants.SERVICE_PARAMETER;
import static org.constellation.api.QueryConstants.UPDATESEQUENCE_PARAMETER;
import static org.constellation.api.QueryConstants.VERSION_PARAMETER;
import static org.constellation.sos.core.SOSConstants.ACCEPTED_OUTPUT_FORMATS;
import static org.constellation.api.CommonConstants.FEATURE_OF_INTEREST;
import static org.constellation.api.CommonConstants.OBSERVATION;
import static org.constellation.api.CommonConstants.OBSERVATION_ID;
import static org.constellation.api.CommonConstants.OBSERVED_PROPERTY;
import static org.constellation.api.CommonConstants.OFFERING;
import static org.constellation.api.CommonConstants.OM_NAMESPACE;
import static org.constellation.api.CommonConstants.OUTPUT_FORMAT;
import static org.constellation.api.CommonConstants.PROCEDURE;
import static org.constellation.api.CommonConstants.PROCEDURE_DESCRIPTION_FORMAT;
import static org.constellation.api.CommonConstants.RESPONSE_FORMAT;
import static org.constellation.api.CommonConstants.RESPONSE_MODE;
import static org.constellation.api.CommonConstants.RESULT_MODEL;
import static org.constellation.api.CommonConstants.SRS_NAME;
import org.constellation.ws.rs.ResponseObject;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildAcceptVersion;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildBBOX;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildDeleteSensor;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildDescribeSensor;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetCapabilities;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetFeatureOfInterest;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetObservation;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetObservationById;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetResult;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetResultTemplate;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildTimeDuring;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildTimeEquals;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildTimeInstant;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildTimePeriod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.constellation.api.CommonConstants.RESPONSE_FORMAT_V200_XML;
import org.geotoolkit.filter.FilterUtilities;
import org.geotoolkit.gml.xml.GMLInstant;
import org.geotoolkit.gml.xml.GMLPeriod;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.SpatialOperator;


/**
 *
 * @author Guilhem Legal
 * @author Benjamin Garcia (Geomatys)
 *
 * @version 0.9
 */
@Controller
@RequestMapping("sos/{serviceId:.+}")
public class SOService extends OGCWebService<SOSworker> {

    protected final FilterFactory<?,?,?> ff = FilterUtilities.FF;

    /**
     * Build a new Restfull SOS service.
     */
    public SOService() throws CstlServiceException {
        super(Specification.SOS);
        setXMLContext(SOSMarshallerPool.getInstance());
        LOGGER.log(Level.INFO, "SOS REST service running");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseObject treatIncomingRequest(final Object objectRequest, final SOSworker worker) {
        ServiceDef serviceDef = null;
        try {
            final RequestBase request;
            if (objectRequest == null) {
                request = adaptQuery(getParameter(REQUEST_PARAMETER, true), worker);
            } else if (objectRequest instanceof RequestBase) {
                request = (RequestBase) objectRequest;
            } else {
                throw new CstlServiceException("The operation " + objectRequest.getClass().getName() + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
            }

            serviceDef = worker.getVersionFromNumber(request.getVersion());
            final String currentVersion;
            if (request.getVersion() != null) {
                currentVersion = request.getVersion().toString();
            } else {
                currentVersion = null;
            }

             if (request instanceof GetObservation) {
                final GetObservation go = (GetObservation) request;
                final Object response   = worker.getObservation(go);

                String outputFormat = go.getResponseFormat();
                if (outputFormat != null) {
                    if (outputFormat.startsWith(MimeType.TEXT_XML) || outputFormat.equals(RESPONSE_FORMAT_V200_XML)) {
                        outputFormat = MimeType.TEXT_XML;
                    } else if (outputFormat.startsWith(MimeType.TEXT_PLAIN)) {
                        outputFormat = MimeType.TEXT_PLAIN;
                    } else if (outputFormat.startsWith(MimeType.APP_JSON)) {
                        outputFormat = MimeType.APP_JSON;
                    }
                }
                final Object marshalled;
                if (response instanceof ObservationCollection) {
                    marshalled = new SOSResponseWrapper(response, currentVersion);
                } else {
                    marshalled = response;
                 }
                return new ResponseObject(marshalled, outputFormat);
             }

             if (request instanceof GetObservationById) {
                final GetObservationById go = (GetObservationById)request;
                String outputFormat = go.getResponseFormat();
                if (outputFormat != null) {
                    if (outputFormat.startsWith(MimeType.TEXT_XML) || outputFormat.equals(RESPONSE_FORMAT_V200_XML)) {
                        outputFormat = MimeType.TEXT_XML;
                    } else if (outputFormat.startsWith(MimeType.TEXT_PLAIN)) {
                        outputFormat = MimeType.TEXT_PLAIN;
                    } else if (outputFormat.startsWith(MimeType.APP_JSON)) {
                        outputFormat = MimeType.APP_JSON;
                    }
                } else {
                    outputFormat = MimeType.TEXT_XML;
                }
                return new ResponseObject(worker.getObservationById(go), outputFormat);
             }

             if (request instanceof DescribeSensor) {
                final DescribeSensor ds       = (DescribeSensor)request;
                return new ResponseObject(worker.describeSensor(ds), MediaType.TEXT_XML);
             }

             if (request instanceof GetFeatureOfInterest) {
                final GetFeatureOfInterest gf     = (GetFeatureOfInterest)request;
                final SOSResponseWrapper response = new SOSResponseWrapper(worker.getFeatureOfInterest(gf), currentVersion);
                final String outputFormat = gf.getResponseFormat();
                return new ResponseObject(response, outputFormat);
             }

             if (request instanceof InsertObservation) {
                final InsertObservation is = (InsertObservation)request;
                final String outputFormat = is.getResponseFormat();
                return new ResponseObject(worker.insertObservation(is), outputFormat);
             }

             if (request instanceof GetResult) {
                final GetResult gr = (GetResult)request;
                final String outputFormat = gr.getResponseFormat();
                return new ResponseObject(worker.getResult(gr), outputFormat);
             }

             if (request instanceof InsertSensor) {
                final InsertSensor rs = (InsertSensor)request;
                return new ResponseObject(worker.registerSensor(rs), MediaType.TEXT_XML);
             }

             if (request instanceof DeleteSensor) {
                final DeleteSensor rs = (DeleteSensor)request;
                return new ResponseObject(worker.deleteSensor(rs), MediaType.TEXT_XML);
             }

             if (request instanceof InsertResult) {
                final InsertResult rs = (InsertResult)request;
                final String outputFormat = rs.getResponseFormat();
                return new ResponseObject(worker.insertResult(rs), outputFormat);
             }

             if (request instanceof InsertResultTemplate) {
                final InsertResultTemplate rs = (InsertResultTemplate)request;
                final String outputFormat = rs.getResponseFormat();
                return new ResponseObject(worker.insertResultTemplate(rs), outputFormat);
             }

             if (request instanceof GetResultTemplate) {
                final GetResultTemplate rs = (GetResultTemplate)request;
                final String outputFormat = rs.getResponseFormat();
                return new ResponseObject(worker.getResultTemplate(rs), outputFormat);
             }

             if (request instanceof GetFeatureOfInterestTime) {
                final GetFeatureOfInterestTime gft = (GetFeatureOfInterestTime)request;
                final SOSResponseWrapper response = new SOSResponseWrapper(worker.getFeatureOfInterestTime(gft), currentVersion);
                return new ResponseObject(response, MediaType.TEXT_XML);
             }

             if (request instanceof GetCapabilities) {
                final GetCapabilities gc = (GetCapabilities)request;
                return new ResponseObject(worker.getCapabilities(gc), getCapabilitiesOutputFormat(gc));
             }

             throw new CstlServiceException("The operation " + request + " is not supported by the service",
                     INVALID_PARAMETER_VALUE, "request");

        } catch (Exception ex) {
            return processExceptionResponse(ex, serviceDef, worker);
        }
    }

    /**
     * Throw an CstlServiceException when a request is not available in GET.
     *
     * @param operationName The name of the request. (example getCapabilities)
     *
     * @throws CstlServiceException every time.
     */
    private void throwUnsupportedGetMethod(String operationName) throws CstlServiceException {
        throw new CstlServiceException("The operation " + operationName + " is only requestable in XML via POST method",
                                                  OPERATION_NOT_SUPPORTED, operationName);
    }

    /**
     * Build request object from KVP parameters.
     */
    private RequestBase adaptQuery(String request, final Worker w) throws CstlServiceException {
         if (INSERT_OBSERVATION .equalsIgnoreCase(request) ||
             REGISTER_SENSOR    .equalsIgnoreCase(request)
         ){
             throwUnsupportedGetMethod(request);

         } else if (GET_FEATURE_OF_INTEREST.equalsIgnoreCase(request)) {
             return createGetFeatureOfInterest(w);
         } else if (GET_FEATURE_OF_INTEREST_TIME.equalsIgnoreCase(request)) {
             return createGetFeatureOfInterestTime(w);
         } else if (GET_OBSERVATION.equalsIgnoreCase(request)) {
             return createGetObservation(w);
         } else if (GET_RESULT.equalsIgnoreCase(request)) {
             return createGetResult(w);
         } else if (DESCRIBE_SENSOR.equalsIgnoreCase(request)) {
             return createDescribeSensor(w);
         } else if (DELETE_SENSOR.equalsIgnoreCase(request)) {
             return createDeleteSensor(w);
         } else if (GET_RESULT_TEMPLATE.equalsIgnoreCase(request)) {
             return createGetResultTemplate(w);
         } else if (GET_OBSERVATION_BY_ID.equalsIgnoreCase(request)) {
             return createGetObservationById(w);
         } else if (GET_CAPABILITIES.equalsIgnoreCase(request)) {
             return createNewGetCapabilities(w);
         }
         throw new CstlServiceException("The operation " + request + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
    }

    /**
     * Build a new getCapabilities request from kvp encoding
     */
    private GetCapabilities createNewGetCapabilities(final Worker worker) throws CstlServiceException {

        String version        = getParameter(ACCEPT_VERSIONS_PARAMETER, false);
        String currentVersion = getParameter(VERSION_PARAMETER, false);

        if (currentVersion == null) {
            currentVersion = worker.getBestVersion(null).version.toString();
        }
        worker.checkVersionSupported(currentVersion, true);

        final List<String> versions = new ArrayList<>();
        if (version != null) {
            String[] vArray = version.split(",");
            versions.addAll(Arrays.asList(vArray));
        } else {
            versions.add(currentVersion);
        }
        final AcceptVersions acceptVersions = buildAcceptVersion(currentVersion, versions);

        final String format = getParameter(ACCEPT_FORMATS_PARAMETER, false);
        final AcceptFormats formats;
        if (format != null) {
            formats = OWSXmlFactory.buildAcceptFormat("1.1.0", Arrays.asList(format));
        } else {
            formats = null;
        }

        final String updateSequence = getParameter(UPDATESEQUENCE_PARAMETER, false);

        //We transform the String of sections in a list.
        //In the same time we verify that the requested sections are valid.
        final String section = getParameter(SECTIONS_PARAMETER, false);
        List<String> requestedSections = new ArrayList<>();
        if (section != null && !section.equalsIgnoreCase("All")) {
            final StringTokenizer tokens = new StringTokenizer(section, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (SectionsType.getExistingSections("1.1.1").contains(token)){
                    requestedSections.add(token);
                } else {
                    throw new CstlServiceException("The section " + token + " does not exist",
                                                  INVALID_PARAMETER_VALUE, "section");
                }
            }
        } else {
            //if there is no requested Sections we add all the sections
            requestedSections = SectionsType.getExistingSections("1.1.1");
        }
        final Sections sections = OWSXmlFactory.buildSections("1.1.0", requestedSections);
        return buildGetCapabilities(currentVersion,
                                   acceptVersions,
                                   sections,
                                   formats,
                                   updateSequence,
                                   getParameter(SERVICE_PARAMETER, true));

    }

    private String getCapabilitiesOutputFormat(final GetCapabilities request) {
        final AcceptFormats formats = request.getAcceptFormats();
        if (formats != null && formats.getOutputFormat().size() > 0 ) {
            for (String form: formats.getOutputFormat()) {
                if (ACCEPTED_OUTPUT_FORMATS.contains(form)) {
                    return form;
                }
            }
        }
        return MimeType.APPLICATION_XML;
    }

    /**
     * Build a new getCapabilities request from kvp encoding
     */
    private DescribeSensor createDescribeSensor(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        final String procedure = getParameter(PROCEDURE, true);
        if (procedure.isEmpty()) {
            throw new CstlServiceException("The parameter procedure must be specified", MISSING_PARAMETER_VALUE, "procedure");
        }
        final String varName;
        if (currentVersion.equals("1.0.0")) {
            varName = OUTPUT_FORMAT;
        } else {
            varName = PROCEDURE_DESCRIPTION_FORMAT;
        }
        final String outputFormat = getParameter(varName, true);
        if (outputFormat.isEmpty()) {
            throw new CstlServiceException("The parameter " + varName + " must be specified", MISSING_PARAMETER_VALUE, varName);
        }
        return buildDescribeSensor(currentVersion,
                                   service,
                                   procedure,
                                   outputFormat);
    }

    private GetFeatureOfInterest createGetFeatureOfInterest(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        if (currentVersion.equals("1.0.0")) {
            final String featureID = getParameter("FeatureOfInterestId", true);
            final List<String> fidList = StringUtilities.toStringList(featureID);
            return buildGetFeatureOfInterest(getParameter(VERSION_PARAMETER, true),getParameter(SERVICE_PARAMETER, true), fidList);
        } else {
            final String obpList = getParameter(OBSERVED_PROPERTY, false);
            final List<String> observedProperties;
            if (obpList != null) {
                observedProperties = StringUtilities.toStringList(obpList);
            } else {
                observedProperties = new ArrayList<>();
            }
            final String prList = getParameter(PROCEDURE, false);
            final List<String> procedures;
            if (prList != null) {
                procedures = StringUtilities.toStringList(prList);
            } else {
                procedures = new ArrayList<>();
            }
            final String foList = getParameter(FEATURE_OF_INTEREST, false);
            final List<String> foids;
            if (foList != null) {
                foids = StringUtilities.toStringList(foList);
            } else {
                foids = new ArrayList<>();
            }
            final String bboxStr = getParameter("spatialFilter", false);
            final Filter spatialFilter;
            if (bboxStr != null) {
                spatialFilter = parseBBoxFilter(bboxStr);
            } else {
                spatialFilter = null;
            }
            final Object extension = getParameter("extension", false);
            final List<Object> extensions = new ArrayList<>();
            if (extension != null) {
                extensions.add(extension);
            }
            return buildGetFeatureOfInterest(currentVersion, service, foids, observedProperties, procedures, spatialFilter, extensions);
        }
    }

    private GetFeatureOfInterestTime createGetFeatureOfInterestTime(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        if (currentVersion.equals("1.0.0")) {
            return new org.geotoolkit.sos.xml.v100.GetFeatureOfInterestTime(getParameter(VERSION_PARAMETER, true), getParameter("FeatureOfInterestId", true));
        } else {
            throw new CstlServiceException("The operation GetFeatureOfInterestTime is only requestable in 1.0.0 version",
                                                  OPERATION_NOT_SUPPORTED, GET_FEATURE_OF_INTEREST_TIME);
        }
    }

    private DeleteSensor createDeleteSensor(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        final String procedure = getParameter(PROCEDURE, true);
        if (procedure.isEmpty()) {
            throw new CstlServiceException("The parameter procedure must be specified", MISSING_PARAMETER_VALUE, "procedure");
        }
        return buildDeleteSensor(currentVersion,
                                 service,
                                 procedure);
    }

    private GetResult createGetResult(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        if (currentVersion.equals("2.0.0")) {
            final String offering = getParameter(OFFERING, true);
            if (offering.isEmpty()) {
                throw new CstlServiceException("The parameter offering must be specified", MISSING_PARAMETER_VALUE, OFFERING);
            }
            final String observedProperty = getParameter(OBSERVED_PROPERTY, true);
            if (observedProperty.isEmpty()) {
                throw new CstlServiceException("The parameter observedProperty must be specified", MISSING_PARAMETER_VALUE, OBSERVED_PROPERTY);
            }
            final String foList = getParameter(FEATURE_OF_INTEREST, false);
            final List<String> foids;
            if (foList != null) {
                foids = StringUtilities.toStringList(foList);
            } else {
                foids = new ArrayList<>();
            }
            final String bboxStr = getParameter("spatialFilter", false);
            final Filter spatialFilter;
            if (bboxStr != null) {
                spatialFilter = parseBBoxFilter(bboxStr);
            } else {
                spatialFilter = null;
            }
            final String tempStr = getParameter("temporalFilter", false);
            final List<Filter> temporalFilters = new ArrayList<>();
            if (tempStr != null) {
                temporalFilters.add(parseTemporalFilter(tempStr));
            }
            final Object extension = getParameter("extension", false);
            final List<Object> extensions = new ArrayList<>();
            if (extension != null) {
                extensions.add(extension);
            }
            return buildGetResult(currentVersion, service, offering, observedProperty, foids, spatialFilter, temporalFilters, extensions);
        } else {
            throwUnsupportedGetMethod(GET_RESULT);
            return null;
        }
    }

    private GetResultTemplate createGetResultTemplate(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        final String offering = getParameter(OFFERING, true);
        if (offering.isEmpty()) {
            throw new CstlServiceException("The parameter offering must be specified", MISSING_PARAMETER_VALUE, OFFERING);
        }
        final String observedProperty = getParameter(OBSERVED_PROPERTY, true);
        if (observedProperty.isEmpty()) {
            throw new CstlServiceException("The parameter observedProperty must be specified", MISSING_PARAMETER_VALUE, OBSERVED_PROPERTY);
        }
        final Object extension = getParameter("extension", false);
        final List<Object> extensions = new ArrayList<>();
        if (extension != null) {
            extensions.add(extension);
        }
        return buildGetResultTemplate(currentVersion, service, offering, observedProperty, extensions);
    }

    private GetObservationById createGetObservationById(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        final List<String> observations;
        final String srsName;
        final QName resultModel;
        final ResponseModeType responseMode;
        final String responseFormat;
        final List<Object> extensions = new ArrayList<>();
        if (currentVersion.equals("2.0.0")) {
            final String observationList = getParameter(OBSERVATION, true);
            if (observationList.isEmpty()) {
                throw new CstlServiceException("The parameter observation must be specified", MISSING_PARAMETER_VALUE, OBSERVATION);
            } else {
                observations = StringUtilities.toStringList(observationList);
            }
            srsName        = null;
            resultModel    = null;
            responseMode   = null;
            responseFormat = null;
            final Object extension = getParameter("extension", false);
            if (extension != null) {
                extensions.add(extension);
            }
        } else {
            final String observationList = getParameter(OBSERVATION_ID, true);
            if (observationList.isEmpty()) {
                throw new CstlServiceException("The parameter observationID must be specified", MISSING_PARAMETER_VALUE, OBSERVATION_ID);
            } else {
                observations = StringUtilities.toStringList(observationList);
            }
            srsName = getParameter(SRS_NAME, false);
            responseFormat = getParameter(RESPONSE_FORMAT, true);
            final String rm = getParameter(RESULT_MODEL, false);
            if (rm != null && rm.indexOf(':') != -1) {
                resultModel = new QName(OM_NAMESPACE, rm.substring(rm.indexOf(':')));
            } else if (rm != null){
                resultModel = new QName(rm);
            } else {
                resultModel = null;
            }
            final String rmd = getParameter(RESPONSE_MODE, false);
            if (rmd != null) {
                responseMode = ResponseModeType.fromValue(rm);
            } else {
                responseMode = null;
            }
        }
        return buildGetObservationById(currentVersion, service, observations, resultModel, responseMode, srsName, responseFormat, extensions);
    }

    private GetObservation createGetObservation(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);

        if (currentVersion.equals("2.0.0")) {
            final String offList = getParameter(OFFERING, false);
            final List<String> offering;
            if (offList != null) {
                offering = StringUtilities.toStringList(offList);
            } else {
                offering = new ArrayList<>();
            }
            final String obpList = getParameter(OBSERVED_PROPERTY, false);
            final List<String> observedProperties;
            if (obpList != null) {
                observedProperties = StringUtilities.toStringList(obpList);
            } else {
                observedProperties = new ArrayList<>();
            }
            final String prList = getParameter(PROCEDURE, false);
            final List<String> procedures;
            if (prList != null) {
                procedures = StringUtilities.toStringList(prList);
            } else {
                procedures = new ArrayList<>();
            }
            final String foList = getParameter(FEATURE_OF_INTEREST, false);
            final List<String> foids;
            if (foList != null) {
                foids = StringUtilities.toStringList(foList);
            } else {
                foids = new ArrayList<>();
            }
            final String responseFormat = getParameter(RESPONSE_FORMAT, false);
            final String bboxStr = getParameter("spatialFilter", false);
            final Filter spatialFilter;
            if (bboxStr != null) {
                spatialFilter = parseBBoxFilter(bboxStr);
            } else {
                spatialFilter = null;
            }
            final String tempStr = getParameter("temporalFilter", false);
            final List<Filter> temporalFilters = new ArrayList<>();
            if (tempStr != null) {
                temporalFilters.add(parseTemporalFilter(tempStr));
            }
            return buildGetObservation(currentVersion, service, offering, observedProperties, procedures, foids, responseFormat, temporalFilters, spatialFilter, null, null, null, null);
        } else {
            throw new CstlServiceException("The operation GetObservation is only requestable in XML via POST method for 1.0.0 version",
                                                  OPERATION_NOT_SUPPORTED, GET_OBSERVATION);
        }
    }

    private SpatialOperator parseBBoxFilter(final String bboxStr) {
        final String[] part = bboxStr.split(",");
        final String valueReference = part[0];
        final double[] coord = new double[4];
        int j = 0;
        for (int i=1; i < 5; i++) {
            coord[j] = Double.parseDouble(part[i]);
            j++;
        }
        final String srsName;
        if (part.length > 5) {
            srsName = part[5];
        } else {
            srsName = "urn:ogc:def:crs:EPSG::4326";
        }
        return buildBBOX("2.0.0", valueReference, coord[0], coord[1], coord[2], coord[3], srsName);
    }

    private Filter parseTemporalFilter(final String tempStr) {
        final String[] part = tempStr.split(",");
        final String valueReference = part[0];
        final int slash = part[1].indexOf('/');
        if (slash != -1) {
            final String dateBegin = part[1].substring(0, slash);
            final String dateEnd   = part[1].substring(slash + 1);
            final GMLPeriod period = buildTimePeriod("2.0.0", null, dateBegin, dateEnd);
            return buildTimeDuring("2.0.0", valueReference, ff.literal(period));
        } else {
            final GMLInstant instant = buildTimeInstant("2.0.0", null, part[1]);
            return buildTimeEquals("2.0.0", valueReference, ff.literal(instant));
        }
    }
}
