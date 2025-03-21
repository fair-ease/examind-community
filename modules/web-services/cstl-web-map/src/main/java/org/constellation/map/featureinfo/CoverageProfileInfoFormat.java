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
package org.constellation.map.featureinfo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import org.apache.sis.coverage.SampleDimension;
import org.apache.sis.coverage.grid.DisjointExtentException;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.coverage.grid.GridCoverageProcessor;
import org.apache.sis.coverage.grid.GridDerivation;
import org.apache.sis.coverage.grid.GridExtent;
import org.apache.sis.coverage.grid.GridGeometry;
import org.apache.sis.coverage.grid.GridRoundingMode;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.image.Interpolation;
import org.apache.sis.math.Statistics;
import org.apache.sis.measure.Units;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.crs.DefaultCompoundCRS;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.GridCoverageResource;
import org.apache.sis.storage.NoSuchDataException;
import org.apache.sis.storage.Resource;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.apache.sis.map.MapLayer;
import org.geotoolkit.map.MapBuilder;
import org.geotoolkit.ows.xml.GetFeatureInfo;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.Metadata;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.apache.sis.coverage.grid.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.springframework.lang.Nullable;

/**
 * TODO: toSourceCorner/toSourceCenter
 * @author Johann Sorel (Geomatys)
 */
public class CoverageProfileInfoFormat extends AbstractFeatureInfoFormat {

    enum NaNPropagation {
        /**
         * A window whose values are <em>all</em> NaN values will result in a NaN value. Otherwise, NaN values are ignored.
         */
        ALL,
        /**
         * If a window of value contains <em>any</em> NaN value, it will be propagated.
         */
        ANY,
    }

    enum NaNCleanup {
        /**
         * No post-treatment is done to reduce the number of returned NaN values.
         */
        NONE,
        /**
         * When we find a series of more than two contiguous NaN value, remove all intermediate measures to keep only
         * the two extremum ones. It allow to keep the information that only a single value (NaN) is found in a
         * specified range, but allow to make room for more important information (measure variation) in the line
         * sampling.
         *
         * TODO: generalize that to any contiguous range of equivalent values: i.e make a proper signal filter.
         */
        CONTINUOUS
    }

    enum OutOfBounds {
        /**
         * Any measure outside data domain will be filtered out/removed from output. This means that potentially long
         * gaps can appear in data profile.
         */
        IGNORE,
        /**
         * Any measure outside data domain will be considered a NaN (fill-value). This is the default behaviour.
         */
        NAN
    }

    private static final String MIME = "application/json; subtype=profile";
    private static final String PARAM_PROFILE = "profile";
    private static final String PARAM_NBPOINT = "samplingCount";
    private static final String PARAM_ALTITUDE = "alt";
    private static final String PARAM_REDUCER = "reducer";
    private static final String PARAM_NAN_BEHAVIOR = "nanPropagation";
    private static final String PARAM_NAN_CLEANUP = "nanCleanup";
    private static final String PARAM_OUT_OF_BOUNDS = "outOfBounds";
    private static final String PARAM_INTERPOLATION = "interpolation";

    private static final Map<Unit,List<Unit>> UNIT_GROUPS = new HashMap<>();
    static {
        final List<Unit> tempUnits = new ArrayList<>();
        tempUnits.add(Units.CELSIUS);
        tempUnits.add(Units.FAHRENHEIT);
        tempUnits.add(Units.KELVIN);

        final List<Unit> pressUnits = new ArrayList<>();
        pressUnits.add(Units.BAR);
        pressUnits.add(Units.BAR.multiply(14.503773773));
        pressUnits.add(Units.PASCAL);

        final List<Unit> speedUnits = new ArrayList<>();
        speedUnits.add(Units.METRES_PER_SECOND);
        speedUnits.add(Units.METRES_PER_SECOND.divide(1000));

        for (Unit u : tempUnits) UNIT_GROUPS.put(u, tempUnits);
        for (Unit u : pressUnits) UNIT_GROUPS.put(u, pressUnits);
        for (Unit u : speedUnits) UNIT_GROUPS.put(u, speedUnits);
    }

    @Override
    public Object getFeatureInfo(SceneDef sdef, CanvasDef cdef, Rectangle searchArea, GetFeatureInfo getFI) throws PortrayalException {

        //extract parameters : profile geometry and point count
        String geomStr = null;
        Integer samplingCount = null;
        if (getFI instanceof org.geotoolkit.wms.xml.GetFeatureInfo) {
            Object parameters = ((org.geotoolkit.wms.xml.GetFeatureInfo) getFI).getParameters();
            if (parameters instanceof Map) {
                Object cdt = ((Map) parameters).get(PARAM_PROFILE);
                geomStr = toStringValue(cdt);

                Object cdt2 = ((Map) parameters).get(PARAM_NBPOINT);
                if (cdt2 instanceof Number) samplingCount = ((Number) cdt2).intValue();
                else {
                    final String strNbPts = toStringValue(cdt2);
                    if (strNbPts != null) {
                        try {
                            samplingCount = Double.valueOf(strNbPts).intValue();
                        } catch (NumberFormatException ex) {
                            throw new PortrayalException(ex.getMessage(), ex);
                        }
                    }
                }
            }
        }

        if (geomStr == null) throw new PortrayalException("Missing PROFILE geometry parameter.");
        final WKTReader reader = new WKTReader();
        Geometry geom;
        try {
            geom = reader.read(geomStr);
        } catch (ParseException ex) {
            throw new PortrayalException(ex.getMessage(), ex);
        }
        if (!(geom instanceof LineString || geom instanceof Point)) {
            throw new PortrayalException("PROFILE geometry parameter must be a point or a LineString.");
        }

        //geometry is in view crs
        final CoordinateReferenceSystem geomCrs = CRS.getHorizontalComponent(cdef.getEnvelope().getCoordinateReferenceSystem());
        geom.setUserData(geomCrs);

        final Profile profil = new Profile();

        for (MapLayer layer : MapBuilder.getLayers(sdef.getContext())) {
            Resource resource = layer.getData();
            if (resource instanceof GridCoverageResource) {
                final GridCoverageResource ressource = (GridCoverageResource) resource;
                try {
                    final ProfilLayer l = extract(cdef, getFI, geom, ressource, samplingCount);
                    l.name = getNameForCoverageLayer(layer).getLocalPart();
                    if (l.name == null) {
                           l.name = ressource.getIdentifier()
                                   .orElseThrow(() -> new PortrayalException("resource identifier not present")).tip().toString();
                    }

                    final Object alias = layer.getUserProperties().get("alias");
                    if (alias instanceof CharSequence) l.alias = alias.toString();

                    profil.layers.add(l);
                } catch (DisjointExtentException | NoSuchDataException ex) {
                    LOGGER.log(Level.FINE, "Cannot extract profile for input geometry", ex);
                } catch (TransformException | DataStoreException | FactoryException ex) {
                    throw new PortrayalException(ex.getMessage(), ex);
                }
            }
        }
        return profil;
    }

    @Override
    public List<String> getSupportedMimeTypes() {
        return Collections.singletonList(MIME);
    }

    private ProfilLayer extract(CanvasDef cdef, GetFeatureInfo getFI, Geometry geom, GridCoverageResource resource, Integer samplingCount) throws TransformException, FactoryException, DataStoreException {

        final ProfilLayer layer = new ProfilLayer();
        tryAddMetadata(layer, resource);

        final ProfilData baseData;
        try {
            //build temporal and vertical slice
            final JTSEnvelope2D geomEnv = JTS.toEnvelope(geom);
            final Envelope venv = cdef.getEnvelope();
            final CoordinateReferenceSystem vcrs = venv.getCoordinateReferenceSystem();
            final TemporalCRS vtcrs = CRS.getTemporalComponent(vcrs);
            final VerticalCRS vacrs = CRS.getVerticalComponent(vcrs, true);

            final GridGeometry gridGeometry = resource.getGridGeometry();
            final CoordinateReferenceSystem ccrs = gridGeometry.getCoordinateReferenceSystem();
            final TemporalCRS ctcrs = CRS.getTemporalComponent(ccrs);
            final VerticalCRS cacrs = CRS.getVerticalComponent(ccrs, true);

            Double time = null;
            Double alti = null;
            TemporalCRS ftcrs = null;
            VerticalCRS facrs = null;
            if (vtcrs == null) {
                //pick first coverage temporal slice
                if (ctcrs != null) {
                    ftcrs = ctcrs;
                    final Envelope tenv = Envelopes.transform(gridGeometry.getEnvelope(), ctcrs);
                    time = tenv.getMaximum(0);
                }
            } else {
                //extract user requested time
                ftcrs = vtcrs;
                final Envelope tenv = Envelopes.transform(venv, vtcrs);
                time = tenv.getMedian(0);
            }

            if (geom instanceof LineString) {
                //extract altitude parameter
                String altStr = null;
                if (getFI instanceof org.geotoolkit.wms.xml.GetFeatureInfo) {
                    Object parameters = ((org.geotoolkit.wms.xml.GetFeatureInfo) getFI).getParameters();
                    if (parameters instanceof Map) {
                        Object cdt = ((Map) parameters).get(PARAM_ALTITUDE);
                        altStr = toStringValue(cdt);
                    }
                }

                if (altStr != null) {
                    if (cacrs != null) {
                        facrs = cacrs;
                        alti = Double.parseDouble(altStr);
                    }
                } else if (vacrs == null) {
                    //pick first coverage altitude slice
                    if (altStr != null) {
                        facrs = CommonCRS.Vertical.ELLIPSOIDAL.crs();
                        alti = Double.parseDouble(altStr);
                    } else if (cacrs != null) {
                        facrs = cacrs;
                        final Envelope aenv = Envelopes.transform(gridGeometry.getEnvelope(), cacrs);
                        alti = aenv.getMaximum(0);
                    }
                } else {
                    //extract user requested altitude
                    facrs = vacrs;
                    final Envelope aenv = Envelopes.transform(venv, vacrs);
                    alti = aenv.getMedian(0);
                }
            }

            final GeneralEnvelope workEnv;
            if (time == null && alti == null) {
                workEnv = new GeneralEnvelope(geomEnv);
            } else if (alti == null) {
                final Map props = new HashMap();
                props.put("name", "2d+t");
                final CoordinateReferenceSystem crs = new DefaultCompoundCRS(props, geomEnv.getCoordinateReferenceSystem(), ftcrs);
                workEnv = new GeneralEnvelope(crs);
                workEnv.setRange(0, geomEnv.getMinimum(0), geomEnv.getMaximum(0));
                workEnv.setRange(1, geomEnv.getMinimum(1), geomEnv.getMaximum(1));
                workEnv.setRange(2, time, time);
            } else if (time == null) {
                final Map props = new HashMap();
                props.put("name", "2d+a");
                final CoordinateReferenceSystem crs = new DefaultCompoundCRS(props, geomEnv.getCoordinateReferenceSystem(), facrs);
                workEnv = new GeneralEnvelope(crs);
                workEnv.setRange(0, geomEnv.getMinimum(0), geomEnv.getMaximum(0));
                workEnv.setRange(1, geomEnv.getMinimum(1), geomEnv.getMaximum(1));
                workEnv.setRange(2, alti, alti);
            } else {
                final Map props = new HashMap();
                props.put("name", "2d+t+a");
                final CoordinateReferenceSystem crs = new DefaultCompoundCRS(props, geomEnv.getCoordinateReferenceSystem(), ftcrs, facrs);
                workEnv = new GeneralEnvelope(crs);
                workEnv.setRange(0, geomEnv.getMinimum(0), geomEnv.getMaximum(0));
                workEnv.setRange(1, geomEnv.getMinimum(1), geomEnv.getMaximum(1));
                workEnv.setRange(2, time, time);
                workEnv.setRange(3, alti, alti);
            }

            Object parameters = ((org.geotoolkit.wms.xml.GetFeatureInfo) getFI).getParameters();
            ReductionMethod reducer = ReductionMethod.AVG;
            NaNPropagation nanBehavior = NaNPropagation.ALL;
            OutOfBounds outOfBounds = OutOfBounds.NAN;
            NaNCleanup nanCleanup = NaNCleanup.CONTINUOUS;
            Interpolation interpolation = Interpolation.NEAREST;
            if (parameters instanceof Map) {
                final Map<?, ?> paramMap = (Map) parameters;
                ReductionMethod reduceParam = toEnumValue(ReductionMethod.class, paramMap.get(PARAM_REDUCER));
                if (reduceParam != null) reducer = reduceParam;

                NaNPropagation nanBehave = toEnumValue(NaNPropagation.class, paramMap.get(PARAM_NAN_BEHAVIOR));
                if (nanBehave != null) nanBehavior = nanBehave;

                OutOfBounds oob = toEnumValue(OutOfBounds.class, paramMap.get(PARAM_OUT_OF_BOUNDS));
                if (oob != null) outOfBounds = oob;

                NaNCleanup nc = toEnumValue(NaNCleanup.class, paramMap.get(PARAM_NAN_CLEANUP));
                if (nc != null) nanCleanup = nc;

                var tmpInterpol = toInterpolation(paramMap.get(PARAM_INTERPOLATION));
                if (tmpInterpol != null) interpolation = tmpInterpol;
            }

            final ProfileConfiguration conf = new ProfileConfiguration(nanBehavior, nanCleanup, outOfBounds, samplingCount, reducer, interpolation);

            final GridCoverage coverage = readCoverage(resource, workEnv, conf);

            baseData = extractData(coverage, geom, conf);

        } catch (DataStoreException ex) {
            layer.message = ex.getMessage();
            return layer;
        }

        //convert data in different units
        final List<Unit> group = UNIT_GROUPS.get(baseData.getUnit());
        if (group != null) {
            for (Unit u : group) {
                if (u.equals(baseData.getUnit())) {
                    layer.data.add(baseData);
                    continue;
                }
                //create converted datas
                final ProfilData data = new ProfilData();
                final Statistics stats = new Statistics("");
                final UnitConverter converter = baseData.getUnit().getConverterTo(u);

                for (XY xy : baseData.points) {
                    final XY c = new XY(xy.x, xy.y);
                    if (geom instanceof Point) {
                        c.x = converter.convert(c.x);
                        stats.accept(c.x);
                    } else {
                        c.y = converter.convert(c.y);
                        stats.accept(c.y);
                    }
                    data.points.add(c);
                }

                data.unit = u;
                data.min = stats.minimum();
                data.max = stats.maximum();
                layer.data.add(data);
            }
        } else {
            layer.data.add(baseData);
        }


        return layer;
    }

    private void tryAddMetadata(ProfilLayer layer, GridCoverageResource resource) {
        try {
            final Metadata md = resource.getMetadata();
            final List<String> titles = md.getIdentificationInfo().stream()
                    .map(id -> id.getCitation())
                    .filter(Objects::nonNull)
                    .map(Citation::getTitle)
                    .filter(Objects::nonNull)
                    .map(Objects::toString)
                    .collect(Collectors.toList());
            if (!titles.isEmpty()) layer.setTitles(titles);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Cannot extract title from layer metadata", e);
        }
    }

    private ProfilData extractData(GridCoverage coverage, Geometry geom, ProfileConfiguration config) throws TransformException, FactoryException {

        final ProfilData pdata = new ProfilData();

        final CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem();
        final GridGeometry gridGeometry     = coverage.getGridGeometry();
        final GridEnvelope extent           = gridGeometry.getExtent();
        final MathTransform gridToCrs       = gridGeometry.getGridToCRS(PixelInCell.CELL_CENTER);
        final List<SampleDimension> samples = coverage.getSampleDimensions();

        //build axes informations
        final int dim = crs.getCoordinateSystem().getDimension();
        final long[] lowsI = extent.getLow().getCoordinateValues();
        final double[] lowsD = new double[dim];
        for (int i = 0;i < dim; i++) lowsD[i] = lowsI[i];
        final double[] gridPt = new double[extent.getDimension()];
        final double[] crsPt = new double[extent.getDimension()];
        final List<Axe> axes = new ArrayList<>();
        Integer altiIdx = null;
        for (int i = 2;i < dim; i++) {
            final CoordinateSystemAxis axis = crs.getCoordinateSystem().getAxis(i);
            System.arraycopy(lowsD, 0, gridPt, 0, dim);
            final double[] range = new double[(int) extent.getSize(i)];
            for (int k = 0, kg = (int) extent.getLow(k); k < range.length; k++, kg++) {
                gridPt[i] = kg;
                gridToCrs.transform(gridPt, 0, crsPt, 0, 1);
                range[k] = crsPt[i];
            }
            final Unit<?> unit = axis.getUnit();

            final Axe axe = new Axe();
            axe.name = axis.getName().toString();
            axe.direction = axis.getDirection().name();
            if (unit != null) axe.unit = unit;
            axe.range = range;
            axes.add(axe);

            if (axis.getDirection().equals(AxisDirection.UP) || axis.getDirection().equals(AxisDirection.DOWN)) {
                altiIdx = i-2;
            }
        }
        final int ALTIIDX = altiIdx == null ? -1 : altiIdx;

        //build sample dimension informations
        final Band[] bands = samples.stream()
                .map(Band::new)
            //limit to first band, request by david/mehdi
                .limit(1)
                .toArray(size -> new Band[size]);

        final boolean isPoint = geom instanceof Point;
        if (isPoint) {
            final Object userData = geom.getUserData();
            final Coordinate[] coords = geom.getCoordinates();
            geom = new GeometryFactory().createLineString(new Coordinate[]{coords[0], coords[0]});
            geom.setUserData(userData);
        }

        final DataProfile dp = new DataProfile(coverage, (LineString) geom, config.interpolation);

        final Statistics stats = new Statistics("");


        Stream<DataProfile.DataPoint> pointStream = StreamSupport.stream(dp, false);
        if (config.outOfBounds == OutOfBounds.IGNORE) {
            pointStream = pointStream.filter(point -> point.value != null);
        } else if (config.outOfBounds == OutOfBounds.NAN) {
            final UnaryOperator<DataProfile.DataPoint> replaceNullWithNaN = point -> {
                if (point.value != null) return point;
                final DataProfile.DataPoint newPoint = new DataProfile.DataPoint(point.geoLocation, point.gridLocation, point.distanceFromPrevious);
                newPoint.value = new double[] { Double.NaN };
                return newPoint;
            };
            pointStream = pointStream.map(replaceNullWithNaN);
        }

        if (isPoint) {
            pointStream.forEach(new Consumer<DataProfile.DataPoint>() {
                @Override
                public void accept(DataProfile.DataPoint t) {
                    Object value = t.value;

                    //unpack first band
                    value = Array.get(value, 0);

                    final List<Double> values = new ArrayList<>();
                    //unpack dimensions
                    final int as = axes.size();

                    //lazy, we expect only time and alti dimensions.
                    if (as == 0) {
                        extractValues(value, values);
                    } else if (as == 1) {
                        if (ALTIIDX == 0) {
                            //pick all
                            extractValues(value, values);
                        } else {
                            //pick first
                            value = Array.get(value, 0);
                            extractValues(value, values);
                        }
                    } else if (as == 2) {
                        if (ALTIIDX == 0) {
                            //pick first - alti
                            for (int i=0,n=Array.getLength(value);i<n;i++) {
                                Object sub = Array.get(value, i);
                                //pick first - not alti
                                sub = Array.get(sub, 0);
                                extractValues(sub, values);
                            }
                        } else if (ALTIIDX == 1) {
                            //pick first - not alti
                            value = Array.get(value, 0);
                            //pick all - alti
                            extractValues(value, values);
                        } else {
                            //pick first - not alti
                            value = Array.get(value, 0);
                            //pick first - not alti
                            value = Array.get(value, 0);
                            extractValues(value, values);
                        }
                    }

                    for (Double d : values) {
                        stats.accept(d);
                    }

                    if (ALTIIDX >= 0) {
                        final Axe axe = axes.get(ALTIIDX);
                        for (int i=0,n=values.size();i<n;i++) {
                            pdata.points.add(new XY(values.get(i), axe.range[i]));
                        }
                    } else {
                        pdata.points.add(new XY(0, values.get(0)));
                    }
                }
            });

        } else {
            double[] d = new double[1];
            pointStream.forEach(new Consumer<DataProfile.DataPoint>() {
                @Override
                public void accept(DataProfile.DataPoint t) {
                    Object value = t.value;
                    d[0] += t.distanceFromPrevious / 1000.0;
                    final double distancekm = d[0];

                    //unpack first band
                    value = Array.get(value, 0);

                    //pick first value
                    while (value.getClass().isArray()) {
                        value = Array.get(value, 0);
                    }

                    double num = ((Number)value).doubleValue();
                    stats.accept(num);
                    pdata.points.add(new XY(distancekm, num));
                }
            });
        }

        pdata.points = reduce(pdata.points, config.samplingCount == null ? pdata.points.size() : config.samplingCount, config.reductionMethod, config.nanPropagation);

        /* TODO: debug point order. Sorting operation should not be needed, as points should already be returned in
         * order of distance from trajectory start point. However, some reports have stated that in specific case, the
         * order is broken. By safety, we add a manual sort here as last operation, but it adds a performance drawback.
         * Further investigation should be done to determine if this is really necessary.
         */
        Collections.sort(pdata.points, Comparator.comparing(value -> value.x));

        // TODO: this would be more efficient before reduction, because it could potentially eliminate a lot of no-data
        // points before sampling, which means sampling points could be focused on areas filled with data. However, to
        // be properly done, we should propagate information of NaN ranges to reduction algorithm, so it would know that
        // 2 consecutive NaN values represent an irreductible gap that must not be reduced/sampled (otherwise it would
        // disappear, be melted in a reduction window).
        if (config.naNCleanup == NaNCleanup.CONTINUOUS && !pdata.points.isEmpty()) {
            pdata.points = cleanupNans(pdata.points);
        }

        pdata.setUnit(bands[0].getUnit());
        pdata.min = stats.minimum();
        pdata.max = stats.maximum();
        return pdata;
    }

    /**
     * Filter NaN values from input list as specified by {@link NaNCleanup#CONTINUOUS}.
     * @param points Point list to filter out. It will not be modified, a copy will be done.
     */
    static List<XY> cleanupNans(List<XY> points) {
        final int nbPts = points.size();
        if (nbPts < 3) return points;
        final List<XY> cleanedPoints = new ArrayList<>();
        int nanSeriesStart = -1;
        for (int i = 0; i < nbPts; i++) {
            final XY point = points.get(i);
            final boolean isNaN = Double.isNaN(point.y);
            if (isNaN && nanSeriesStart < 0) nanSeriesStart = i;
            else if (!isNaN) {
                if (nanSeriesStart >= 0) {
                    cleanedPoints.add(points.get(nanSeriesStart));
                    if (i - 1 > nanSeriesStart) cleanedPoints.add(points.get(i-1));
                    nanSeriesStart = -1;
                }
                cleanedPoints.add(point);
            }
        }

        if (nanSeriesStart >= 0) {
            cleanedPoints.add(points.get(nanSeriesStart));
            final int lastPtIdx = nbPts - 1;
            if (nanSeriesStart < lastPtIdx) cleanedPoints.add(points.get(lastPtIdx));
        }

        return cleanedPoints;
    }

    /**
     * Reduce list to number of requested points.
     * At least first and last points will be preserved.
     * Decimate points trying to preserve a regular distance
     *
     * @param lst list to decimate
     * @param samplingCount
     * @return
     */
    static List<XY> reduce(List<XY> lst, int samplingCount) {
        return reduce(lst, samplingCount, ReductionMethod.NEAREST, NaNPropagation.ALL);
    }

    static List<XY> reduce(List<XY> lst, int samplingCount, ReductionMethod reductionStrategy, NaNPropagation nanBehavior) {
        // Reduction cannot be done with less than 4 points, so in this case, we directly return the input point list.
        if (lst.size() < 4) return lst;

        if (reductionStrategy == null) reductionStrategy = ReductionMethod.AVG;
        switch (reductionStrategy) {
            case NEAREST: return reduceNearest(lst, samplingCount);
            case MIN    : return reduce(lst, samplingCount, values -> reduce(values, Math::min, (value, count) -> value, nanBehavior));
            case MAX    : return reduce(lst, samplingCount, values -> reduce(values, Math::max, (value, count) -> value, nanBehavior));
            default     : return reduce(lst, samplingCount, values -> reduce(values, Double::sum, (value, count) -> value / count, nanBehavior));
        }
    }

    /**
     *
     * @param values Values to reduce
     * @param valueAccumulator The operation that merge two independent values
     * @param finalizer A final operation that produces final result from merged values (first argument) and number of
     *                  non NaN merged points.
     * @param nanBehavior How to handle NaN values in input dataset.
     * @return Result of reduction: X will be the mean of distances between given values. Y will be the result of value
     * reduction.
     */
    private static XY reduce(List<XY> values, DoubleBinaryOperator valueAccumulator, Finalizer finalizer, NaNPropagation nanBehavior) {
        if (nanBehavior == NaNPropagation.ALL) {
            valueAccumulator = nanOp(valueAccumulator);
        }
        XY reduction = new XY(values.get(0));

        int nbNaN = Double.isNaN(reduction.y) ? 1 : 0;
        final int nbPts = values.size();
        for (int i = 1 ; i < nbPts ; i++) {
            final XY value = values.get(i);
            reduction.x += value.x;
            reduction.y = valueAccumulator.applyAsDouble(reduction.y, value.y);
            if (Double.isNaN(value.y)) nbNaN++;
        }

        reduction.x /= nbPts;
        if (nanBehavior == NaNPropagation.ALL && nbNaN >= nbPts) {
            reduction.y = Double.NaN;
        } else {
            reduction.y = finalizer.apply(reduction.y, nbPts - nbNaN);
        }

        return reduction;
    }

    @FunctionalInterface
    private interface Finalizer {
        double apply(double accumulation, int count);
    }

    /**
     * Decorate given double operation, but also ignore any NaN value present.
     */
    private static DoubleBinaryOperator nanOp(DoubleBinaryOperator base) {
        return (d1, d2) -> Double.isNaN(d1) ? d2 : Double.isNaN(d2) ? d1 : base.applyAsDouble(d1, d2);
    }

    /**
     * HACK : quick fix to mimic advanced reduction algorithms. Little things to know:
     * <ul>
     *     <li>First and last points are preserved</li>
     *     <li>Overlap behavior : to simplify algorithm, window management is approximative</li>
     * </ul>
     */
    static List<XY> reduce(List<XY> datasource, int samplingCount, Function<List<XY>, XY> reducer) {
        final int sourceSize = datasource.size();
        // HACK: for reduction to make sense, we want at least a central point with 2 edges
        final int ptsPerWindow = Math.max(3, Math.round(sourceSize / (float) samplingCount));
        final int outSamplingCount = sourceSize / ptsPerWindow;
        final List<XY> reduced = IntStream.rangeClosed(1, outSamplingCount)
                .mapToObj(ptIdx -> datasource.subList(ptsPerWindow * ptIdx - ptsPerWindow, ptsPerWindow * ptIdx))
                .map(reducer)
                .collect(Collectors.toList());
        reduced.add(0, datasource.get(0));
        reduced.add(datasource.get(sourceSize - 1));
        return reduced;
    }

    static List<XY> reduceNearest(List<XY> lst, int samplingCount) {
        final int sourceSize = lst.size();
        if (sourceSize <= samplingCount) return lst;
        final List<BiSegment> prepared = new ArrayList<>();

        final BiSegment first = new BiSegment(lst.get(0));
        final BiSegment last = new BiSegment(lst.get(sourceSize -1));
        BiSegment previous = null;
        for (int i = 1, n = sourceSize -1; i < n ; i++) {
            final BiSegment bis = new BiSegment(lst.get(i));
            if (previous != null) {
                bis.updatePrevious(previous);
            }
            previous = bis;
            prepared.add(bis);
        }
        prepared.get(0).updatePrevious(first);
        prepared.get(prepared.size()-1).updateNext(last);

        final TreeSet<BiSegment> distanceSort = new TreeSet<>(prepared);

        //remove segments until we have the wanted count
        samplingCount -= 2; //reduce by two we are working with segments, first and last points are not in the Set.
        while (distanceSort.size() > samplingCount && !distanceSort.isEmpty()) {
            final BiSegment bis = distanceSort.pollFirst();
            final BiSegment nextb = bis.next;
            final BiSegment prevb = bis.prev;
            distanceSort.remove(nextb);
            distanceSort.remove(prevb);

            //update next segment
            nextb.updatePrevious(prevb);
            if (!nextb.isEdge()) {
                distanceSort.add(nextb);
            }

            //update previous segment
            prevb.updateNext(nextb);
            if (!prevb.isEdge()) {
                distanceSort.add(prevb);
            }
        }

        //rebuild list
        BiSegment seg = first;
        lst = new ArrayList<>();
        while (seg != null) {
            lst.add(seg.xy);
            seg = seg.next;
        }

        return lst;
    }

    private static void extractValues(Object value, List<Double> values) {
        if (value instanceof Number) {
            values.add( ((Number) value).doubleValue() );
        } else {
            for (int i = 0, n = Array.getLength(value); i < n; i++) {
                Object sub = Array.get(value, i);
                extractValues(sub, values);
            }
        }
    }

    private static GridCoverage readCoverage(GridCoverageResource resource, Envelope work, ProfileConfiguration conf)
            throws TransformException, DataStoreException {

        //ensure envelope is no flat
        final GeneralEnvelope workEnv = new GeneralEnvelope(work);
        if (workEnv.isEmpty()) {
            if (workEnv.getSpan(0) <= 0.0) {
                double buffer = workEnv.getSpan(1) / 100.0;
                if (buffer <= 0.0) buffer = 0.00001;
                workEnv.setRange(0, workEnv.getLower(0)-buffer, workEnv.getLower(0)+buffer);
            }
            if (workEnv.getSpan(1) <= 0.0) {
                double buffer = workEnv.getSpan(0) / 100.0;
                if (buffer <= 0.0) buffer = 0.00001;
                workEnv.setRange(1, workEnv.getLower(1)-buffer, workEnv.getLower(1)+buffer);
            }
        }

        final GridGeometry baseGrid = resource.getGridGeometry();
        GridGeometry selection = baseGrid.derive()
                .rounding(GridRoundingMode.ENCLOSING)
                .subgrid(workEnv)
                .build();
        final GridExtent intersection = selection.getExtent();
        final int[] subspace2d = intersection.getSubspaceDimensions(2);

        final Dimension margin2d = conf.interpolation.getSupportSize();
        if (Math.max(margin2d.width, margin2d.height) > 1) {
            final int[] margin = new int[intersection.getDimension()];
            margin[subspace2d[0]] = margin2d.width;
            margin[subspace2d[1]] = margin2d.height;
            selection = baseGrid.derive()
                    .margin(margin)
                    .rounding(GridRoundingMode.ENCLOSING)
                    .subgrid(workEnv).build();
        }

        GridCoverage data = resource.read(selection).forConvertedValues(true);
        final GridGeometry grid = data.getGridGeometry();
        // HACK: force 2D representation
        if (grid.getDimension() > 2) {
            final GridGeometry subGrid2d = grid.selectDimensions(subspace2d);
            return new GridCoverageProcessor().resample(data, subGrid2d);
        } else {
            return data;
        }
    }

    private static @Nullable String toStringValue(final Object value) {
        if (value instanceof String) return (String) value;
        else if (value instanceof String[]) {
            String[] valueArray = (String[]) value;
            if (valueArray.length > 1) throw new IllegalArgumentException("Expect a single value, but found "+valueArray.length);
            if (valueArray.length < 1) return null;
            else return valueArray[0];
        }

        return null;
    }

    private static <T extends Enum> T toEnumValue(Class<T> enumClass, Object value) {
        if (enumClass.isInstance(value)) return enumClass.cast(value);
        final String strValue = toStringValue(value);
        if (strValue != null) return (T) Enum.valueOf(enumClass, strValue.toUpperCase(Locale.ROOT));
        return null;
    }

    private static @Nullable Interpolation toInterpolation(Object value) {
        if (value instanceof Interpolation i) return i;
        var interpolName = toStringValue(value);
        if (interpolName == null) return null;
        if (interpolName.equalsIgnoreCase("NEAREST")) return Interpolation.NEAREST;
        if (interpolName.equalsIgnoreCase("BILINEAR")) return Interpolation.BILINEAR;
        if (interpolName.equalsIgnoreCase("LANCZOS")) return Interpolation.LANCZOS;
        return null;
    }

    private class ProfileConfiguration {
        final NaNPropagation nanPropagation;
        final NaNCleanup naNCleanup;
        final OutOfBounds outOfBounds;
        final Integer samplingCount;
        final ReductionMethod reductionMethod;
        final Interpolation interpolation;

        public ProfileConfiguration(NaNPropagation nanPropagation, NaNCleanup naNCleanup, OutOfBounds outOfBounds, Integer samplingCount, ReductionMethod reductionMethod, Interpolation interpolation) {
            this.nanPropagation = nanPropagation;
            this.naNCleanup = naNCleanup;
            this.outOfBounds = outOfBounds;
            this.samplingCount = samplingCount;
            this.reductionMethod = reductionMethod;
            this.interpolation = interpolation;
        }
    }

    public static class Band {

        public String name;
        public Unit unit;

        Band() {}

        Band(SampleDimension source) {
            name = source.getName().toString();
            unit= source.getUnits().orElse(null);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JsonSerialize(converter = UnitSerializer.class)
        public Unit getUnit() {
            return unit;
        }

        @JsonDeserialize(converter = UnitDeSerializer.class)
        public void setUnit(Unit unit) {
            this.unit = unit;
        }
    }

    public static class Axe {
        public String name;
        public String direction;
        public Unit unit;
        public double[] range;

        @JsonSerialize(converter = UnitSerializer.class)
        public Unit getUnit() {
            return unit;
        }

        @JsonDeserialize(converter = UnitDeSerializer.class)
        public void setUnit(Unit unit) {
            this.unit = unit;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        public double[] getRange() {
            return range;
        }

        public void setRange(double[] range) {
            this.range = range;
        }
    }

    public static class Profile {
        public List<ProfilLayer> layers = new ArrayList<>();
    }

    public static class ProfilLayer {

        public String name;
        public String alias;
        private List<String> titles;
        public List<ProfilData> data = new ArrayList<>();
        public String message;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getTitles() {
            return titles;
        }

        public void setTitles(List<String> titles) {
            this.titles = titles;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<ProfilData> getData() {
            return data;
        }

        public void setData(List<ProfilData> data) {
            this.data = data;
        }
    }

    public static class ProfilData {

        private Unit unit;
        private double min;
        private double max;
        public List<XY> points = new ArrayList<>();

        @JsonSerialize(converter = UnitSerializer.class)
        public Unit getUnit() {
            return unit;
        }

        @JsonDeserialize(converter = UnitDeSerializer.class)
        public void setUnit(Unit unit) {
            this.unit = unit;
        }

        public double getMin() {
            return min;
        }

        public void setMin(double min) {
            this.min = min;
        }

        public double getMax() {
            return max;
        }

        public void setMax(double max) {
            this.max = max;
        }
    }

    public static class XY {
        public double x;
        public double y;

        public XY() {}

        public XY(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public XY(XY toCopy) {
            this.x = toCopy.x;
            this.y = toCopy.y;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return "[" + x +" " + y + "]";
        }

    }

    /**
     * Used for point decimation only.
     */
    private static class BiSegment implements Comparable<BiSegment> {
        XY xy;
        double distanceIfRemoved;
        BiSegment prev;
        BiSegment next;

        public BiSegment(XY current) {
            this.xy = current;
        }

        public boolean isEdge() {
            return prev == null || next == null;
        }

        public void updatePrevious(BiSegment previousSegment) {
            if (this.prev == previousSegment) return;
            this.prev = previousSegment;
            updateDistance();
            previousSegment.updateNext(this);
        }

        public void updateNext(BiSegment nextSegment) {
            if (this.next == nextSegment) return;
            this.next = nextSegment;
            updateDistance();
            nextSegment.updatePrevious(this);
        }

        private void updateDistance() {
            if (prev != null && next != null) {
                this.distanceIfRemoved = next.xy.x - prev.xy.x;
            } else {
                this.distanceIfRemoved = Double.POSITIVE_INFINITY;
            }
        }

        @Override
        public int compareTo(BiSegment o) {
            int cmp = Double.compare(distanceIfRemoved, o.distanceIfRemoved);
            if (cmp == 0) {
                //we need to have all segment different otherwise the TreeMap will remove entries
                //use distance, it is always increasing
                return Double.compare(xy.x, o.xy.x);
            }
            return cmp;
        }

        @Override
        public String toString() {
            return ((prev == null) ? "null" : prev.xy) + " -> " + xy + " -> " + ((next == null) ? "null" : next.xy) + " d:" + distanceIfRemoved;
        }

    }

    public enum ReductionMethod {
        NEAREST, MIN, MAX, AVG
    }

    private static class UnitSerializer extends StdConverter<Unit, String> {

        @Override
        public String convert(Unit unit) {
            return unit == null? null : unit.getSymbol();
        }
    }

    private static class UnitDeSerializer extends StdConverter<String, Unit> {

        @Override
        public Unit convert(String symbol) {
            return symbol == null ? null : Units.valueOf(symbol);
        }
    }
}
