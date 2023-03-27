/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2020 Geomatys.
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
package org.constellation.ws;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.apache.sis.cql.CQL;
import org.apache.sis.cql.CQLException;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.measure.Units;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.crs.DefaultEngineeringCRS;
import org.apache.sis.referencing.cs.AbstractCS;
import org.apache.sis.referencing.cs.DefaultCoordinateSystemAxis;
import org.apache.sis.referencing.datum.DefaultEngineeringDatum;
import org.apache.sis.util.Utilities;
import org.constellation.admin.SpringHelper;
import org.constellation.api.DataType;
import org.constellation.api.ServiceDef;
import org.constellation.business.IDataBusiness;
import org.constellation.dto.DimensionRange;
import org.constellation.dto.FeatureDataDescription;
import org.constellation.dto.NameInProvider;
import org.constellation.dto.StyleReference;
import org.constellation.dto.service.config.wxs.DimensionDefinition;
import org.constellation.dto.service.config.wxs.LayerConfig;
import org.constellation.exception.ConstellationException;
import org.constellation.exception.ConstellationStoreException;
import org.constellation.map.util.DimensionDef;
import org.constellation.provider.Data;
import org.constellation.map.util.DtoToOGCFilterTransformer;
import org.constellation.provider.CoverageData;
import org.constellation.provider.FeatureData;
import org.geotoolkit.filter.FilterFactoryImpl;
import static org.geotoolkit.filter.FilterUtilities.FF;
import org.opengis.filter.Expression;
import org.opengis.filter.Filter;
import org.opengis.filter.ValueReference;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;
import org.opengis.util.GenericName;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class LayerCache {
    
    private static final Logger LOGGER = Logger.getLogger("org.constellation.ws");

    private final NameInProvider nip;
    private final GenericName name;
    private final List<StyleReference> styles;
    private final Data data;
    private final LayerConfig configuration;

    @Autowired
    private IDataBusiness dataBusiness;

    public LayerCache(final NameInProvider nip, GenericName name, Data d, List<StyleReference> styles, final LayerConfig configuration) {
        SpringHelper.injectDependencies(this);
        this.nip = nip;
        this.data = d;
        this.name = name;
        this.styles = styles;
        this.configuration = configuration;
    }

    public Integer getId() {
        return nip.layerId;
    }

    public Optional<String> getAlias() {
        if (nip.alias != null) {
            return Optional.of(nip.alias);
        }
        return Optional.empty();
    }

    public GenericName getName() {
        return name;
    }

    public List<StyleReference> getStyles() {
        return styles;
    }

    public Data getData() {
        return data;
    }

    public LayerConfig getConfiguration() {
        return configuration;
    }

    /**
     * lazy cached native data envelope.
     */
    private Envelope envelope;

    /**
     * @return The native envelope.
     * 
     * @throws ConstellationStoreException
     */
    public Envelope getEnvelope() throws ConstellationStoreException {
        if (envelope == null) {
            Optional<Envelope> env = dataBusiness.getEnvelope(nip.dataId);
            if (env.isPresent()) {
                envelope = env.get();
            } else {
                envelope = data.getEnvelope();
            }
        }
        return envelope;
    }

    /**
     * Return a reprojected data envelope.
     * 
     * @param crs A coordinate referenceSystem
     * @return
     * @throws ConstellationStoreException
     */
    public Envelope getEnvelope(CoordinateReferenceSystem crs) throws ConstellationStoreException {
        if (envelope != null && Utilities.equalsIgnoreMetadata(envelope.getCoordinateReferenceSystem(), crs)) {
            return envelope;
        } else {
            return data.getEnvelope(crs);
        }
    }
    
    public GeographicBoundingBox getGeographicBoundingBox() throws ConstellationStoreException {
        try {
            final Envelope env = getEnvelope();
            if (env != null) {
                final DefaultGeographicBoundingBox result = new DefaultGeographicBoundingBox();
                result.setBounds(env);
                return result;
            } else {
                LOGGER.warning("Null boundingBox for Layer:" + name + ". Returning World BBOX.");
                return new DefaultGeographicBoundingBox(-180, 180, -90, 90);
            }
        } catch (TransformException ex) {
            throw new ConstellationStoreException(ex);
        }
    }
    
    public CoordinateReferenceSystem getCoordinateReferenceSystem() throws ConstellationStoreException {
        CoordinateReferenceSystem dataCRS = null;
        Envelope env = getEnvelope();
        if (env != null) {
            dataCRS = env.getCoordinateReferenceSystem();
            // if the data has extra dimension, we need to add them in the crs.
            if (configuration != null && !configuration.getDimensions().isEmpty()) {
                try {
                    final int nbExtraDim = configuration.getDimensions().size();
                    CoordinateReferenceSystem[] crss = new CoordinateReferenceSystem[nbExtraDim + 1];
                    crss[0] = dataCRS;
                    for (int i = 0; i < nbExtraDim; i++) {
                        DimensionDef dd = getDimensionDef(configuration.getDimensions().get(i));
                        crss[i+1] = dd.crs;
                    }
                    dataCRS = CRS.compound(crss);
                } catch (CQLException | FactoryException ex) {
                    throw new ConstellationStoreException("Error while building a compound CRS with custom dimensions.", ex);
                }
            }
        }
        return dataCRS;
    }

    private org.constellation.dto.Data dbData;
    
    private org.constellation.dto.Data getDbData() throws ConstellationStoreException {
        if (dbData == null) {
            try {
                dbData = dataBusiness.getData(nip.dataId);
            } catch (ConstellationException ex) {
                throw new ConstellationStoreException(ex);
            }
        }
        return dbData;
    }

    public boolean isQueryable(ServiceDef.Query query) {
        return data.isQueryable(query);
    }
    
    public DataType getDataType() {
        return data.getDataType();
    }
    
    public SortedSet<Number> getAvailableElevations() throws ConstellationStoreException {
        SortedSet<Number> elevations;
        if (getDbData().getCachedInfo()) {
            if (getDbData().getHasElevation()) {
                elevations = dataBusiness.getDataElevations(nip.dataId);
            } else {
                elevations = new TreeSet<>();
            }
        } else {
            elevations = data.getAvailableElevations();
        }
        return elevations;
    }
    
    public Number getFirstElevation() throws ConstellationStoreException {
        // can be optimized like times behaviour
        final SortedSet<Number> elevations = getAvailableElevations();
        if (elevations != null && !elevations.isEmpty()) {
            return elevations.first();
        }
        return null;
    }
    
    public SortedSet<Date> getAvailableTimes() throws ConstellationStoreException {
        SortedSet<Date> dates;
        if (getDbData().getCachedInfo()) {
            if (getDbData().getHasTime()) {
                dates = dataBusiness.getDataTimes(nip.dataId, false);
            } else {
                dates = new TreeSet<>();
            }
        } else {
            dates = data.getAvailableTimes();
        }
        return dates;
    }
    
    public SortedSet<Date> getDateRange() throws ConstellationStoreException {
        SortedSet<Date> dates;
        if (getDbData().getCachedInfo()) {
            if (getDbData().getHasTime()) {
                dates = dataBusiness.getDataTimes(nip.dataId, true);
            } else {
                dates = new TreeSet<>();
            }
        } else {
            dates = data.getDateRange();
        }
        return dates;
    }
    
    public SortedSet<DimensionRange> getSampleValueRanges() throws ConstellationStoreException {
        final SortedSet<DimensionRange> dims;
        if (getDbData().getCachedInfo()) {
            if (getDbData().getHasDim()) {
                dims = dataBusiness.getDataDimensionRange(nip.dataId);
            } else {
                dims = new TreeSet<>();
            }
        } else {
            dims = data.getSampleValueRanges();
        }
        return dims;
    }

    public List<ValueReference> getTimeDimension() {
        if (data instanceof FeatureData featdata) {
            return featdata.getTimeDimension();
        }
        return new ArrayList<>();
    }

    public Double[] getResolution() throws ConstellationStoreException {
        Double[] results = new Double[2];
        if (data instanceof CoverageData covdata) {
            double[] nativeResolution = covdata.getGeometry().getResolution(true);
            results[0] = nativeResolution[0];
            results[1] = nativeResolution[1];
        }
        return results;
    }

    public boolean hasFilterAndDimension() {
        return configuration != null && (configuration.getFilter() != null || !configuration.getDimensions().isEmpty());
    }
    
    public Optional<Filter> getLayerFilter(Envelope env, Filter extraFilter) {
        final List<Filter> filters = new ArrayList<>();
        if (extraFilter != null) {
            filters.add(extraFilter);
        }
        if (configuration != null) {
            if (configuration.getFilter() != null) {
                try {
                    filters.add(new DtoToOGCFilterTransformer(new FilterFactoryImpl()).visitFilter(configuration.getFilter()));
                } catch (FactoryException e) {
                    LOGGER.log(Level.WARNING, "Error while transforming layer custom filter", e);
                }
            }
            if (!configuration.getDimensions().isEmpty() && env != null) {
                for (DimensionDefinition ddef : configuration.getDimensions()) {
                    try {
                        final DimensionDef def = getDimensionDef(ddef);
                        final Envelope dimEnv;
                        try {
                            dimEnv = Envelopes.transform(env, def.crs);
                        } catch (TransformException ex) {
                            LOGGER.log(Level.FINER, "Error while reprojecting the envelope to dimension CRS.", ex);
                            continue;
                        }

                        final Filter dimFilter = FF.and(
                                FF.lessOrEqual(FF.literal(dimEnv.getMinimum(0)), def.lower),
                                FF.greaterOrEqual(FF.literal(dimEnv.getMaximum(0)), def.upper));
                        filters.add(dimFilter);
                    } catch (CQLException ex) {
                        LOGGER.log(Level.WARNING, "Error while building a dimension filter.", ex);
                    }
                }
            }
        }
        return filters.stream().reduce(FF::and);
    }

    public List<String> getLayerProperties(List<String> propertyNames) throws ConstellationStoreException {
        if (propertyNames == null || propertyNames.isEmpty()) return Collections.EMPTY_LIST;

        List<String> results = new ArrayList<>();
        Set<String> inverted = new HashSet<>();

        for (String propertyName : propertyNames) {
            if (propertyName == null || propertyName.isEmpty()) continue;
            if (propertyName.startsWith("-") || propertyName.startsWith("!")) {
                inverted.add(propertyName.substring(1));
            } else {
                results.add(propertyName);
            }
        }
        if (results.isEmpty() && inverted.isEmpty()) {
            // Input contained only null or empty names
            LOGGER.fine("Invalid list of parameters: only null or empty values. Selection ignored.");
            return Collections.EMPTY_LIST;

         } else if (!results.isEmpty() && !inverted.isEmpty()) {
             throw new ConstellationStoreException("Mixed exclusive and inclusive property names");

         } else if (!inverted.isEmpty()) {
           
            if (data.getDataDescription(null, getEnvelope()) instanceof  FeatureDataDescription fd) {
                return fd.getProperties().stream()
                                  .map(p -> p.getName())
                                  .filter(p -> !inverted.contains(p))
                                  .toList();
            } else {
                LOGGER.warning("Layer property omission is only supported for Feature data");
                return Collections.EMPTY_LIST;
            }
        }
        
        return results;
    }

    public List<DimensionDef> getDimensiondefinition() {
        final List<DimensionDef> results = new ArrayList<>();
        if (configuration != null && !configuration.getDimensions().isEmpty()) {
            for (DimensionDefinition ddef : configuration.getDimensions()) {
                try {
                    results.add(getDimensionDef(ddef));
                } catch (CQLException ex) {
                    LOGGER.log(Level.WARNING, "Error while building a dimension filter.", ex);
                }
            }
        }
        return results;
    }

    private static DimensionDef getDimensionDef(DimensionDefinition ddef) throws CQLException {
        final String crsname = ddef.getCrs();
        final Expression lower = CQL.parseExpression(ddef.getLower());
        final Expression upper = CQL.parseExpression(ddef.getUpper());
        final CoordinateReferenceSystem dimCrs;

        if ("elevation".equalsIgnoreCase(crsname)) {
            dimCrs = CommonCRS.Vertical.ELLIPSOIDAL.crs();
        } else if ("temporal".equalsIgnoreCase(crsname)) {
            dimCrs = CommonCRS.Temporal.JAVA.crs();
        } else {
            final EngineeringDatum customDatum = new DefaultEngineeringDatum(Collections.singletonMap("name", crsname));
            final CoordinateSystemAxis csAxis = new DefaultCoordinateSystemAxis(Collections.singletonMap("name", crsname), "u", AxisDirection.valueOf(crsname), Units.UNITY);
            final AbstractCS customCs = new AbstractCS(Collections.singletonMap("name", crsname), csAxis);
            dimCrs = new DefaultEngineeringCRS(Collections.singletonMap("name", crsname), customDatum, customCs);
        }
        return new DimensionDef(dimCrs, lower, upper);
    }
}
