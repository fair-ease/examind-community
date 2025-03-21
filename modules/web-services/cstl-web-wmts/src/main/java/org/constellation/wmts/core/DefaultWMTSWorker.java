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
package org.constellation.wmts.core;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.Resource;
import org.apache.sis.util.Utilities;
import org.constellation.api.ServiceDef;
import org.constellation.dto.StyleReference;
import org.constellation.dto.contact.Details;
import org.constellation.exception.ConstellationStoreException;
import org.constellation.map.featureinfo.FeatureInfoFormat;
import org.constellation.provider.Data;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.LayerCache;
import org.constellation.ws.LayerWorker;
import org.constellation.ws.MimeType;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.apache.sis.map.MapLayers;
import org.apache.sis.metadata.iso.ISOMetadata;
import org.apache.sis.storage.tiling.Tile;
import org.apache.sis.storage.tiling.TileStatus;
import org.apache.sis.storage.tiling.TiledResource;
import org.apache.sis.style.Style;
import org.geotoolkit.internal.referencing.CRSUtilities;
import org.geotoolkit.ows.xml.AbstractCapabilitiesCore;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.ows.xml.v110.OperationsMetadata;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.ows.xml.v110.ServiceIdentification;
import org.geotoolkit.ows.xml.v110.ServiceProvider;
import org.geotoolkit.ows.xml.v110.WGS84BoundingBoxType;
import org.geotoolkit.referencing.ReferencingUtilities;
import org.geotoolkit.storage.coverage.finder.StrictlyCoverageFinder;
import org.geotoolkit.storage.multires.TileMatrices;
import org.geotoolkit.temporal.util.TimeParser;
import org.geotoolkit.wmts.WMTSUtilities;
import org.geotoolkit.wmts.xml.v100.Capabilities;
import org.geotoolkit.wmts.xml.v100.ContentsType;
import org.geotoolkit.wmts.xml.v100.Dimension;
import org.geotoolkit.wmts.xml.v100.DimensionNameValue;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.geotoolkit.wmts.xml.v100.GetFeatureInfo;
import org.geotoolkit.wmts.xml.v100.GetTile;
import org.geotoolkit.wmts.xml.v100.LayerType;
import org.geotoolkit.wmts.xml.v100.Themes;
import org.geotoolkit.wmts.xml.v100.TileMatrix;
import org.geotoolkit.wmts.xml.v100.TileMatrixSet;
import org.geotoolkit.wmts.xml.v100.TileMatrixSetLink;
import org.geotoolkit.wmts.xml.v100.URLTemplateType;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.Metadata;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.identification.Identification;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Working part of the WMTS service.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Guilhem Legal (Geomatys)
 * @since 0.3
 */
@Component("WMTSWorker")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DefaultWMTSWorker extends LayerWorker implements WMTSWorker {

    public static final String TIME_NAME = "time";
    public static final String TIME_UNIT = "ISO-8601";

    public static final String ELEVATION_NAME = "elevation";
    public static final String CURRENT_VALUE = "current";

    public static final double RESOLUTION_EPSILON = 1E-9;

    /** Default temporal CRS, used for comparison purposes. */
    public static final TemporalCRS JAVA_TIME = CommonCRS.Temporal.JAVA.crs();

    public final SimpleDateFormat ISO_8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
     {
        ISO_8601_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * A list of supported MIME type
     */
    private static final List<String> ACCEPTED_OUTPUT_FORMATS;
    static {
        ACCEPTED_OUTPUT_FORMATS = Arrays.asList(MimeType.TEXT_XML,
                                                MimeType.APP_XML,
                                                MimeType.TEXT_PLAIN);
    }

    /**
     * A map which contains the binding between capabilities tile matrix set identifiers and input
     * {@link TileMatrixSet} ids. It's used only if we've got multiple pyramids with the same ID but
     * different matrix structure. Otherwise, we directly use pyramid ids as tile matrix set name.
     */
    private final HashMap<String, HashSet<String>> tmsIdBinding = new HashMap<>();
    private final ReentrantReadWriteLock tmsBindingLock = new ReentrantReadWriteLock();

    public DefaultWMTSWorker(final String id) {
        super(id, ServiceDef.Specification.WMTS);
        started();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities getCapabilities(GetCapabilities requestCapabilities) throws CstlServiceException {
        LOGGER.log(Level.FINER, "getCapabilities request processing\n");
        final long start = System.currentTimeMillis();
        final String userLogin  = getUserLogin();

        //we verify the base request attribute
        if (requestCapabilities.getService() != null) {
            if (!requestCapabilities.getService().equalsIgnoreCase("WMTS")) {
                throw new CstlServiceException("service must be \"WMTS\"!",
                                                 INVALID_PARAMETER_VALUE, "service");
            }
        } else {
            throw new CstlServiceException("Service must be specified!",
                                             MISSING_PARAMETER_VALUE, "service");
        }
        final AcceptVersionsType versions = requestCapabilities.getAcceptVersions();
        if (versions != null) {
            if (!versions.getVersion().contains("1.0.0")){
                 throw new CstlServiceException("version available : 1.0.0",
                                             VERSION_NEGOTIATION_FAILED, "acceptVersion");
            }
        }

        final AcceptFormatsType formats = requestCapabilities.getAcceptFormats();
        if (formats != null && !formats.getOutputFormat().isEmpty() ) {
            boolean found = false;
            for (String form: formats.getOutputFormat()) {
                if (ACCEPTED_OUTPUT_FORMATS.contains(form)) {
                    found = true;
                }
            }
            if (!found) {
                throw new CstlServiceException("accepted format : text/xml, application/xml",
                                                 INVALID_PARAMETER_VALUE, "acceptFormats");
            }
        }

        SectionsType sections = requestCapabilities.getSections();
        if (sections == null) {
            sections = new SectionsType(SectionsType.getExistingSections("1.1.1"));
        }

        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(requestCapabilities.getUpdateSequence());
        if (returnUS) {
            return new Capabilities("1.0.0", getCurrentUpdateSequence());
        }

        // If the getCapabilities response is in cache, we just return it.
        AbstractCapabilitiesCore cachedCapabilities = getCapabilitiesFromCache("1.0.0", null);
        if (cachedCapabilities != null) {
            return (Capabilities) cachedCapabilities.applySections(sections);
        }

        /* We synchronize Computing, because every thread will compute the same thing, its useless to waste CPU. One will
         * do the job, the others will wait for it. More over, it's EXTREMELY important for the integrity of the binding
         * between pyramids and Tile matrix set ids that we synchronize the get Capa using our binding lock.
         */
        tmsBindingLock.writeLock().lock();
        try {

            cachedCapabilities = getCapabilitiesFromCache("1.0.0", null);
            if (cachedCapabilities != null) {
                return (Capabilities) cachedCapabilities.applySections(sections);
            }
            /*
             * BUILD NEW CAPABILITIES DOCUMENT
             */
            tmsIdBinding.clear();

            // we load the skeleton capabilities
            final Details skeleton = getStaticCapabilitiesObject("wmts", null);
            final Capabilities skeletonCapabilities = (Capabilities) WMTSConstant.createCapabilities("1.0.0", skeleton, getCurrentUpdateSequence());

            //we prepare the response document
            final ServiceIdentification si = skeletonCapabilities.getServiceIdentification();
            final ServiceProvider sp = skeletonCapabilities.getServiceProvider();
            final OperationsMetadata om = (OperationsMetadata) WMTSConstant.OPERATIONS_METADATA.clone();
            // TODO
            final List<Themes> themes = new ArrayList<>();

            //we update the URL
            om.updateURL(getServiceUrl());

            // Build the list of layers
            final List<LayerType> outputLayers = new ArrayList<>();
            // and the list of matrix set
            final HashMap<String, TileMatrixSet> tileSets = new HashMap<>();

            final List<LayerCache> layers = getLayerCaches(userLogin, true);

            for (final LayerCache layer : layers) {
                final String name = identifier(layer);
                try {
                    final Data data = layer.getData();
                    if (data == null) {
                        LOGGER.log(Level.WARNING, "No data can be found for name : "+layer.getName());
                        continue;
                    }
                    final Object origin = data.getOrigin();
                    if (!(origin instanceof TiledResource)) {
                        LOGGER.log(Level.WARNING, "Layer {0} is not tiled. It will not be included in capabilities", name);
                        continue;
                    }
                    final TiledResource pmodel = (TiledResource) origin;
                    final List<org.apache.sis.storage.tiling.TileMatrixSet> pyramids = new ArrayList<>(pmodel.getTileMatrixSets());
                    if (pyramids.isEmpty()) {
                        throw new CstlServiceException("No valid extent for layer " + name);
                    }

                    final org.apache.sis.storage.tiling.TileMatrixSet firstPyramid = pyramids.get(0);
                    final Envelope pyramidSetEnv = firstPyramid.getEnvelope().orElse(null);
                    if (pyramidSetEnv == null) {
                        throw new CstlServiceException("No valid extent for layer " + name);
                    }

                    //extract tile format
                    Format tileFormat = null;
                    if (firstPyramid instanceof org.geotoolkit.storage.multires.TileMatrixSet tms) {
                        final Metadata metaData = tms.getMetaData();
                        for (Identification id : metaData.getIdentificationInfo()) {
                            for (Format format : id.getResourceFormats()) {
                                tileFormat = format;
                                break;
                            }
                        }
                    }
                    if (tileFormat == null) {
                        //fallback consider it as PNG, which is the most common case
                        tileFormat = org.geotoolkit.storage.multires.TileMatrixSet.createFormat("png", "png", "image/png", "png");
                    }

                    final CoordinateReferenceSystem pyramidSetEnvCRS = pyramidSetEnv.getCoordinateReferenceSystem();
                    final int xAxis = Math.max(0, CRSUtilities.firstHorizontalAxis(pyramidSetEnvCRS));
                    final int yAxis = xAxis + 1;

                    /* We get pyramid set CRS components to identify additional dimensions. We remove horizontal component
                     * from the list to ease further operations, and prepare WMTS dimension descriptors. Dimension allowed
                     * values will be filled when we'll browse mosaics to build tile matrix capabilities.
                     */
                    final HashMap<Integer, Dimension> dims = new HashMap<>();
                    final Map<Integer, CoordinateReferenceSystem> splittedCRS = ReferencingUtilities.indexedDecompose(pyramidSetEnvCRS);
                    final Iterator<Map.Entry<Integer, CoordinateReferenceSystem>> iterator = splittedCRS.entrySet().iterator();
                    while (iterator.hasNext()) {
                        final Map.Entry<Integer, CoordinateReferenceSystem> entry = iterator.next();
                        final CoordinateReferenceSystem tmpCRS = entry.getValue();
                        // If it's not a single dimension, It's not an additional dimension.
                        if (tmpCRS.getCoordinateSystem().getDimension() > 1) {
                            iterator.remove();
                        } else {
                            // TODO : we have no check for multiple temporal dimensions (is it possible to have more than one ?)
                            final Dimension dimension;
                            if (tmpCRS instanceof TemporalCRS) {
                                // current value is a special wmts case.
                                dimension = new Dimension(TIME_NAME, TIME_UNIT, "current");
                            } else {
                                final String dimName;
                                final CoordinateSystemAxis axis = tmpCRS.getCoordinateSystem().getAxis(0);
                                // vertical dimension name is fixed by 1.0.0 standard.
                                if (tmpCRS instanceof VerticalCRS) {
                                    dimName = ELEVATION_NAME;
                                } else {
                                    dimName = axis.getName().getCode();
                                }
                                dimension = new Dimension(dimName, axis.getUnit().toString(), "current");
                            }
                            dims.put(entry.getKey(), dimension);
                        }
                    }

                    final List<BoundingBoxType> bboxList = new ArrayList<>();
                    for (org.apache.sis.storage.tiling.TileMatrixSet pyramid : pyramids) {
                        final Envelope pyramidEnv = pyramid.getEnvelope().get();
                        final int envXAxis = Math.max(0, CRSUtilities.firstHorizontalAxis(pyramid.getCoordinateReferenceSystem()));
                        final int envYAxis = xAxis + 1;
                        final BoundingBoxType bbox = new BoundingBoxType(
                                getCRSCode(pyramid.getCoordinateReferenceSystem()),
                                pyramidEnv.getMinimum(envXAxis),
                                pyramidEnv.getMinimum(envYAxis),
                                pyramidEnv.getMaximum(envXAxis),
                                pyramidEnv.getMaximum(envYAxis));
                        bboxList.add(bbox);
                    }

                    final LayerType outputLayer = new LayerType(
                            name,
                            name,
                            name,
                            bboxList,
                            WMTSConstant.DEFAULT_STYLES,
                            new ArrayList<>(dims.values()));

                    try {
                        final Envelope crs84Env = Envelopes.transform(pyramidSetEnv, CommonCRS.defaultGeographic());
                        outputLayer.getWGS84BoundingBox().add(new WGS84BoundingBoxType("urn:ogc:def:crs:OGC:2:84",
                                crs84Env.getMinimum(xAxis),
                                crs84Env.getMinimum(yAxis),
                                crs84Env.getMaximum(xAxis),
                                crs84Env.getMaximum(yAxis)));
                    } catch (Exception e) {
                        // Optional parameter, we don't let exception make capabilities fail.
                        LOGGER.log(Level.FINE, "Input envelope cannot be reprojected in CRS:84.");
                    }

                    final ISOMetadata formatMeta = (ISOMetadata) tileFormat;
                    String format    = "image/png"; 
                    String extension = "png";
                    for (Identifier fid : formatMeta.getIdentifiers()) {
                        if (org.geotoolkit.storage.multires.TileMatrixSet.AUTHORITY_EXTENSION.equals(fid.getAuthority().getTitle().toString())) {
                            extension = fid.getCode();
                        } else if (org.geotoolkit.storage.multires.TileMatrixSet.AUTHORITY_MIME.equals(fid.getAuthority().getTitle().toString())) {
                            format = fid.getCode();
                        }
                    }

                    outputLayer.setFormat(List.of(format));
                    
                    final List<URLTemplateType> resources = new ArrayList<>();
                    String url = getServiceUrl();
                    url = url.substring(0, url.length() - 1) + "/" + name + "/{tileMatrixSet}/{tileMatrix}/{tileRow}/{tileCol}." + extension;
                    final URLTemplateType tileURL = new URLTemplateType(format, "tile", url);
                    resources.add(tileURL);
                    
                    
                    outputLayer.setResourceURL(resources);

                    for (org.apache.sis.storage.tiling.TileMatrixSet pr : pyramids) {
                        final TileMatrixSet tms = new TileMatrixSet();
                        tms.setIdentifier(new CodeType(pr.getIdentifier().toString()));
                        tms.setSupportedCRS(getCRSCode(pr.getCoordinateReferenceSystem()));

                        final List<TileMatrix> tm = new ArrayList<>();
                        final double[] scales = TileMatrices.getScales(pr);
                        for (int i = 0; i < scales.length; i++) {
                            final Iterator<? extends org.apache.sis.storage.tiling.TileMatrix> mosaicIt = TileMatrices.getTileMatrices(pr, scales[i]).iterator();
                            if (!mosaicIt.hasNext()) {
                                continue;
                            }
                            final org.apache.sis.storage.tiling.TileMatrix mosaic = mosaicIt.next();
                            DirectPosition upperLeft = TileMatrices.getUpperLeftCorner(mosaic);
                            double scale = TileMatrices.getScale(mosaic);
                            //convert scale in the strange WMTS scale denominator
                            scale = WMTSUtilities.toScaleDenominator(pr.getCoordinateReferenceSystem(), scale);
                            final TileMatrix matrix = new TileMatrix();
                            matrix.setIdentifier(new CodeType(mosaic.getIdentifier().toString()));
                            matrix.setScaleDenominator(scale);
                            matrix.setMatrixDimension(TileMatrices.getGridSize(mosaic));
                            int[] tileSize = TileMatrices.getTileSize(mosaic);
                            matrix.setTileWidth(tileSize[0]);
                            matrix.setTileHeight(tileSize[1]);
                            matrix.getTopLeftCorner().add(upperLeft.getCoordinate(xAxis));
                            matrix.getTopLeftCorner().add(upperLeft.getCoordinate(yAxis));
                            tm.add(matrix);

                            // Fill dimensions. We iterate over all mosaics of the current scale to find all slices.
                            int timeIndex = -1;
                            MathTransform toJavaTime = null;
                            final SimpleDateFormat dateFormatter = (SimpleDateFormat) ISO_8601_FORMATTER.clone();
                            for (Map.Entry<Integer, CoordinateReferenceSystem> entry : splittedCRS.entrySet()) {
                                String strValue;
                                // For temporal values, we convert it into timestamp, then to an ISO 8601 date.
                                final List<String> currentDimValues = dims.get(entry.getKey()).getValue();
                                if (entry.getValue() instanceof TemporalCRS) {
                                    timeIndex = entry.getKey();
                                    double value = upperLeft.getCoordinate(entry.getKey());
                                    if (!Utilities.equalsApproximately(JAVA_TIME, entry.getValue())) {
                                        final double[] tmpArray = new double[]{value};
                                        toJavaTime = CRS.findOperation(entry.getValue(), JAVA_TIME, null).getMathTransform();
                                        toJavaTime.transform(tmpArray, 0, tmpArray, 0, 1);
                                        value = tmpArray[0];
                                    }

                                    strValue = dateFormatter.format(new Date((long) value));

                                } else {
                                    strValue = String.valueOf(upperLeft.getCoordinate(entry.getKey()));
                                }

                                if (strValue != null && !currentDimValues.contains(strValue)) {
                                    currentDimValues.add(strValue);
                                }
                            }

                            while (mosaicIt.hasNext()) {
                                upperLeft = TileMatrices.getUpperLeftCorner(mosaicIt.next());
                                for (Map.Entry<Integer, CoordinateReferenceSystem> entry : splittedCRS.entrySet()) {
                                    String strValue = null;
                                    // For temporal values, we convert it into timestamp, then to an ISO 8601 date.
                                    final List<String> currentDimValues = dims.get(entry.getKey()).getValue();
                                    if (timeIndex == entry.getKey()) {
                                        double value = upperLeft.getCoordinate(entry.getKey());
                                        if (toJavaTime != null) {
                                            final double[] tmpArray = new double[]{value};
                                            toJavaTime.transform(tmpArray, 0, tmpArray, 0, 1);
                                            value = tmpArray[0];
                                        }

                                        strValue = dateFormatter.format(new Date((long) value));

                                    } else {
                                        strValue = String.valueOf(upperLeft.getCoordinate(entry.getKey()));
                                    }

                                    if (strValue != null && !currentDimValues.contains(strValue)) {
                                        currentDimValues.add(strValue);
                                    }
                                }
                            }
                        }
                        tms.setTileMatrix(tm);

                    /*
                     * Once our tile matrix set is defined, we must check if we've got one which is equal to the newly
                     * computed set.
                     * - If no matrix set is equal to the new one, and no matrix set with the same name exists, we add our
                     * matrix set to the service capabilities.
                     * - If we already have a tile matrix set equal to the current one, we just make a link to the old
                     * one.
                     * - If we've got two sets with the same name, but they're different, we rename the new matrix set
                     * to avoid mistakes.
                     *
                     * In all cases, we store a binding between the TMS identifier and the pyramid one to be able to
                     * retrieve pyramid at getTile request.
                     */
                        TileMatrixSet previousDefined = tileSets.get(pr.getIdentifier());
                        boolean equalSets = false;
                        if (previousDefined == null || !(equalSets = areEqual(tms, previousDefined))) {
                            for (final TileMatrixSet tmpSet : tileSets.values()) {
                                if (areEqual(tms, tmpSet)) {
                                    equalSets = true;
                                    previousDefined = tmpSet;
                                    break;
                                }
                            }
                        }

                        if (previousDefined == null) {
                            tileSets.put(pr.getIdentifier().toString(), tms);

                        } else if (equalSets) {
                            tms.setIdentifier(previousDefined.getIdentifier());

                        } else {
                            // Two different matrix sets with same identifier. We'll change the name of the new one.
                            final String tmsUUID = UUID.randomUUID().toString();
                            tms.setIdentifier(new CodeType(tmsUUID));
                            tileSets.put(tmsUUID, tms);
                        }

                        addTileMatrixSetBinding(tms.getIdentifier().getValue(), pr.getIdentifier().toString());


                        final TileMatrixSetLink tmsl = new TileMatrixSetLink(tms.getIdentifier().getValue());
                        outputLayer.addTileMatrixSetLink(tmsl);
                    }

                    outputLayers.add(outputLayer);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Cannot build matrix list of the following layer : " + name, ex);
                }
            }
            final ContentsType cont = new ContentsType(outputLayers, new ArrayList<>(tileSets.values()));

            // put full capabilities in cache
            final Capabilities c = new Capabilities(si, sp, om, "1.0.0", getCurrentUpdateSequence(), cont, themes);
            putCapabilitiesInCache("1.0.0", null, c);
            LOGGER.log(Level.FINER, "getCapabilities processed in {0}ms.\n", (System.currentTimeMillis() - start));
            return (Capabilities) c.applySections(sections);

        } finally {
            tmsBindingLock.writeLock().unlock();
        }
    }

    /**
     * Add a binding between a tile matrix set defined in service GetCapabilities,
     * and a pyramid set Id. The aim is to factorize the possible tile matrixes.
     * @param tmsId The ID of the {@link TileMatrixSet} to expose via GetCapabilities.
     * @param pyramidId The pyramid ID to add as valid pyramid for input TMS.
     * @return True if we successfully added the binding, false otherwise (in could already exist).
     */
    private boolean addTileMatrixSetBinding(final String tmsId, final String pyramidId) {
        tmsBindingLock.writeLock().lock();
        try {
            HashSet<String> bindings = tmsIdBinding.get(tmsId);
            if (bindings == null) {
                bindings = new HashSet<>();
                tmsIdBinding.put(tmsId, bindings);
            }
            return bindings.add(pyramidId);
        } finally {
            tmsBindingLock.writeLock().unlock();
        }
    }

    /**
     * Return CRS code name. As WMTS define only 2D CRS (additional dimensions are stored beside), we will extract
     * horizontal CRS, and search for a standard EPSG identifier. If we cannot find it, we will just keep CRS initial
     * code.
     * @param candidate The system to analyse.
     * @return An identifier for the horizontal part of input crs.
     */
    private String getCRSCode(CoordinateReferenceSystem candidate) {
        final SingleCRS horizontal = CRS.getHorizontalComponent(candidate);
        // Workaround to normalize WGS84 that return "EPSG:WGS 84"
        // for IdentifiedObjects.getIdentifierOrName() call
        if (Utilities.equalsIgnoreMetadata(CommonCRS.WGS84.normalizedGeographic(), horizontal)) {
            return "urn:ogc:def:crs:OGC:2:84";
        } else {
            try {
                final Integer identifier = IdentifiedObjects.lookupEPSG(horizontal);
                if (identifier != null) {
                    return "EPSG:"+identifier;
                }
            } catch (FactoryException e) {
                LOGGER.log(Level.INFO, e.getLocalizedMessage(), e);
            }
        }
        return IdentifiedObjects.getIdentifierOrName(candidate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map.Entry<String, Object> getFeatureInfo(GetFeatureInfo request) throws CstlServiceException {

        //       -- get the List of layer references
        final GetTile getTile       = request.getGetTile();
        final String userLogin      = getUserLogin();
        final QName layerName       = Util.parseQName(getTile.getLayer());
        final LayerCache layer      = getLayerCache(userLogin, layerName);

        // build an equivalent style List
        final String styleName        = getTile.getStyle();
        final StyleReference styleRef = Util.findStyleReference(styleName, layer.getStyles());
        final Style style      = getStyle(styleRef);


        GridCoverage c = null;
        Double elevation =  null;
        Date time        = null;
        final List<DimensionNameValue> dimensions = getTile.getDimensionNameValue();
        for (DimensionNameValue dimension : dimensions) {
            if (dimension.getName().equalsIgnoreCase("elevation")) {
                try {
                    elevation = Double.parseDouble(dimension.getValue());
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("Unable to parse the elevation value", INVALID_PARAMETER_VALUE, "elevation");
                }
            }
            if (dimension.getName().equalsIgnoreCase("time")) {
                try {
                    time = TimeParser.toDate(dimension.getValue());
                } catch (ParseException ex) {
                    throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, "time");
                }
            }
        }

        // TODO elevation and time are not taken in count yet
        final Map<String, Object> params = new HashMap<>();
        params.put("ELEVATION", elevation);
        params.put("TIME", time);

        final SceneDef sdef = new SceneDef();

        try {
            final MapLayers context = mapBusiness.createContext(layer, style);
            sdef.setContext(context);
        } catch (ConstellationStoreException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }

        // 3. CANVAS
        // this part does not work and throw a null pointer exception TODO
        final JTSEnvelope2D refEnv = new JTSEnvelope2D(c.getGridGeometry().getEnvelope());
        final double azimuth       = 0;//request.getAzimuth();
        final java.awt.Dimension canvasDimension = null;//request.getSize();
        final Color background = null;
        final CanvasDef cdef = new CanvasDef(canvasDimension,refEnv);
        cdef.setBackground(background);
        cdef.setAzimuth(azimuth);

        // 4. SHAPE
        //     a
        final int pixelTolerance = 3;
        final int i = request.getI();
        final int j = request.getJ();
        if (i < 0 || i > canvasDimension.width) {
            throw new CstlServiceException("The requested point has an invalid X coordinate.", INVALID_POINT);
        }
        if (j < 0 || j > canvasDimension.height) {
            throw new CstlServiceException("The requested point has an invalid Y coordinate.", INVALID_POINT);
        }
        final Rectangle selectionArea = new Rectangle( request.getI()-pixelTolerance,
        		                               request.getJ()-pixelTolerance,
        		                               pixelTolerance*2,
        		                               pixelTolerance*2);

        // 5. VISITOR
        String infoFormat = request.getInfoFormat();
        if (infoFormat == null) {
            //Should not happen since the info format parameter is mandatory for the GetFeatureInfo request.
            infoFormat = MimeType.TEXT_PLAIN;
        }

        final FeatureInfoFormat featureInfo =  getFeatureInfo(layer.getConfiguration(), infoFormat);
        try {
            final Object result = featureInfo.getFeatureInfo(sdef, cdef, selectionArea, request);
            return new AbstractMap.SimpleEntry<>(infoFormat, result);
        } catch (PortrayalException ex) {
            throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tile getTile(final GetTile request) throws CstlServiceException {

        //1 LAYER NOT USED FOR NOW
        final QName layerName = Util.parseQName(request.getLayer());
        final String userLogin  = getUserLogin();
//        final Layer configLayer = getConfigurationLayer(layerName, userLogin);

        // 2. STYLE NOT USED FOR NOW
//        final String styleName    = request.getStyle();
//        final DataReference styleRef = configLayer.getStyle(styleName);
//        final Style style  = getStyle(styleRef);

        // 3. Get and check parameters
        final int columnIndex         = request.getTileCol();
        final int rowIndex            = request.getTileRow();
        final String level            = request.getTileMatrix();

        String matrixSetName   = request.getTileMatrixSet();
        final HashSet<String> validPyramidNames;
        tmsBindingLock.readLock().lock();
        try {
            final HashSet<String> idBinding = tmsIdBinding.get(matrixSetName);
            if (idBinding != null && !idBinding.isEmpty()) {
                validPyramidNames = idBinding;
            } else {
                validPyramidNames = new HashSet<>(1);
                validPyramidNames.add(matrixSetName);
            }
        } finally {
            tmsBindingLock.readLock().unlock();
        }

        if (columnIndex < 0 || rowIndex < 0) {
            throw new CstlServiceException("Operation request contains an invalid parameter value, " +
                    "TileCol and TileRow must be positive integers. Received position : " +
                    new Point(columnIndex, rowIndex), INVALID_PARAMETER_VALUE, "TileCol or TileRow");
        }

        try {
            final Data data = getLayerCache(userLogin, layerName).getData();
            if (data == null) {
                throw new CstlServiceException("Operation request contains an invalid parameter value, "
                        + "No layer for name : " + layerName,
                        INVALID_PARAMETER_VALUE, "layerName");
            }

            final Resource origin = data.getOrigin();
            if (origin == null) throw new CstlServiceException("Invalid layer: no resource associated", INVALID_PARAMETER_VALUE, "layerName");
            else if (!(origin instanceof TiledResource)) throw new CstlServiceException("Invalid layer: not a tiled resource", INVALID_PARAMETER_VALUE, "layerName");
            org.apache.sis.storage.tiling.TileMatrixSet pyramid = null;
            for (org.apache.sis.storage.tiling.TileMatrixSet pr : ((TiledResource) origin).getTileMatrixSets()) {
                if (validPyramidNames.contains(pr.getIdentifier().toString())) {
                    pyramid = pr;
                    break;
                }
            }
            if (pyramid == null) {
                throw new CstlServiceException("Operation request contains an invalid parameter value,"
                        + " undefined matrixSet: " + matrixSetName + " for layer: " + layerName,
                        INVALID_PARAMETER_VALUE, "tilematrixset");
            }

            org.apache.sis.storage.tiling.TileMatrix mosaic = null;
            for (org.apache.sis.storage.tiling.TileMatrix gm : pyramid.getTileMatrices().values()) {
                if (gm.getIdentifier().toString().equals(level)) {
                    mosaic = gm;
                    break;
                }
            }

            // 4. If we found a base mosaic and user specified additional dimensions, we try to switch on the right slice.
            final List<DimensionNameValue> dimensions = request.getDimensionNameValue();
            if (mosaic != null && dimensions != null && !dimensions.isEmpty()) {
                final GeneralEnvelope envelope = envelopeFromDimensions(mosaic.getTilingScheme().getEnvelope(), dimensions);
                // We use a strict finder, because default one (as methods in coverage utilities) return arbitrary data
                // if it don't find any fitting mosaic...
                StrictlyCoverageFinder finder = new StrictlyCoverageFinder();
                mosaic = finder.findMosaic(pyramid, TileMatrices.getScale(mosaic), RESOLUTION_EPSILON, envelope, -1);
            }

            if (mosaic == null) {
                throw new CstlServiceException("Operation request contains an invalid parameter value," +
                        " undefined matrix: " + level + " for matrixSet: " + matrixSetName,
                        INVALID_PARAMETER_VALUE, "tilematrix");
            }
            java.awt.Dimension gridSize = TileMatrices.getGridSize(mosaic);
            if (columnIndex >= gridSize.width) {
                throw new CstlServiceException("TileCol out of range, expected value < " + gridSize.width + " but got " + columnIndex,
                        TILE_OUT_OF_RANGE, "tilecol");
            }
            if (rowIndex >= gridSize.height) {
                throw new CstlServiceException("TileRow out of range, expected value < " + gridSize.height + " but got "+rowIndex,
                        TILE_OUT_OF_RANGE, "tilerow");
            }

            if (mosaic.getTileStatus(columnIndex, rowIndex) == TileStatus.MISSING) {
                return emptyTile(columnIndex, rowIndex);
            } else {
                final Tile tile = mosaic.getTile(columnIndex, rowIndex).orElse(null);
                if (tile == null) {
                    return emptyTile(columnIndex, rowIndex);
                } else {
                    return tile;
                }
            }

        } catch(CstlServiceException ex) {
            throw ex;
        } catch(Exception ex) {
            throw new CstlServiceException("Unexpected error for operation GetTile  : "+ layerName, ex , NO_APPLICABLE_CODE);
        }
    }

    /**
     * Create empty TileReference with black image as input.
     * @param mosaic
     * @param columnIndex
     * @param rowIndex
     * @return TileReference
     */
    private Tile emptyTile(final int columnIndex, final int rowIndex) {
        return new Tile() {

            @Override
            public long[] getIndices() {
                return new long[] { columnIndex, rowIndex };
            }

            @Override
            public TileStatus getStatus() {
                return TileStatus.MISSING;
            }

            @Override
            public Resource getResource() throws DataStoreException {
                throw new UnsupportedOperationException("Not supported yet");
            }
        };
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Capabilities getCapabilities(String version) throws CstlServiceException {
       return getCapabilities(new GetCapabilities("WMTS"));
    }

    /**
     * Change range values of input envelope for all dimensions specified in given dimension list.
     * @param envelope The envelope containing base values for dimensions to change. Not modified, a copy is performed.
     * @param dimensions Dimensions to override.
     * @return An envelope containing same values as input, except for ranges specified in dimensions parameter.
     * @throws ParseException
     * @throws FactoryException
     * @throws TransformException
     */
    private static GeneralEnvelope envelopeFromDimensions(final Envelope envelope, List<DimensionNameValue> dimensions) throws ParseException, FactoryException, TransformException {
        final GeneralEnvelope result = new GeneralEnvelope(envelope);
        final CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();
        final Map<Integer, CoordinateReferenceSystem> systems = ReferencingUtilities.indexedDecompose(crs);
        CoordinateReferenceSystem currentCRS;
        for (final Map.Entry<Integer, CoordinateReferenceSystem> entry : systems.entrySet()) {
            currentCRS = entry.getValue();
            // Not an additional dimension, it must be horizontal CRS.
            if (currentCRS.getCoordinateSystem().getDimension() > 1) {
                continue;
            }

            if (currentCRS instanceof TemporalCRS) {
                for (final DimensionNameValue dim : dimensions) {
                    if (dim.getName().equalsIgnoreCase(TIME_NAME)) {
                        if (dim.getValue().equalsIgnoreCase(CURRENT_VALUE)) break;
                        final long timestamp = TimeParser.toDate(dim.getValue()).getTime();
                        // We don't know what is the CRS of our envelope, but WMTS exposes times as ISO 8601, so a
                        // conversion may be needed.
                        if (Utilities.equalsApproximately(currentCRS, JAVA_TIME)) {
                            // put a minimal epsilon.
                            result.setRange(entry.getKey(), timestamp - 1, timestamp + 1);
                        } else {
                            final double[] time = new double[1];
                            CRS.findOperation(JAVA_TIME, currentCRS, null).getMathTransform().transform(time, 0, time, 0, 1);
                            result.setRange(entry.getKey(), time[0], time[0]);
                        }
                        break;
                    }
                }

            } else {
                final String axisName;
                if (currentCRS instanceof VerticalCRS) {
                    axisName = ELEVATION_NAME; // Fixed in WMTS standard.
                } else {
                    axisName = currentCRS.getCoordinateSystem().getAxis(0).getName().getCode();
                }
                for (final DimensionNameValue dim : dimensions) {
                    if (dim.getName().equalsIgnoreCase(axisName)) {
                        if (dim.getValue().equalsIgnoreCase(CURRENT_VALUE)) break;
                        final double value = Double.parseDouble(dim.getValue());
                        // put a minimal epsilon.
                        result.setRange(entry.getKey(), value, value);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Test the equality of 2 {@link org.geotoolkit.wmts.xml.v100.TileMatrixSet}, ignoring their name.
     *
     * We check their bounding box, CRS and list of tile matrixes.
     * @param tms1
     * @param tms2
     * @return
     */
    private static boolean areEqual(final TileMatrixSet tms1, TileMatrixSet tms2) {
        if (!tms1.getSupportedCRS().equals(tms2.getSupportedCRS())) return false;

        final BoundingBoxType bbox1 = (tms1.getBoundingBox() == null)? null : tms1.getBoundingBox().getValue();
        final BoundingBoxType bbox2 = (tms2.getBoundingBox() == null)? null : tms2.getBoundingBox().getValue();
        if (bbox1 != null? !bbox1.equals(bbox2) : bbox2 != null) return false;

        final List<TileMatrix> sourceMatrixes = tms1.getTileMatrix();
        final List<TileMatrix> targetMatrixes = tms2.getTileMatrix();
        return (targetMatrixes == null ? sourceMatrixes == null : targetMatrixes.equals(sourceMatrixes));
    }
}
