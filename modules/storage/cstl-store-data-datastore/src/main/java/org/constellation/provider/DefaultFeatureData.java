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
package org.constellation.provider;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.opengis.feature.AttributeType;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureAssociationRole;
import org.opengis.feature.FeatureType;
import org.opengis.feature.IdentifiedType;
import org.opengis.feature.Operation;
import org.opengis.feature.PropertyNotFoundException;
import org.opengis.feature.PropertyType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.ValueReference;
import org.opengis.geometry.Envelope;
import org.opengis.util.GenericName;

import org.apache.sis.internal.feature.AttributeConvention;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.FeatureSet;

import org.geotoolkit.storage.feature.FeatureStoreUtilities;
import org.geotoolkit.storage.feature.query.Query;
import org.geotoolkit.style.RandomStyleBuilder;
import org.geotoolkit.util.NamesExt;

import org.constellation.api.DataType;
import org.constellation.dto.FeatureDataDescription;
import org.constellation.dto.PropertyDescription;
import org.constellation.dto.StatInfo;
import org.constellation.exception.ConstellationStoreException;
import org.locationtech.jts.geom.Geometry;

import org.geotoolkit.filter.FilterUtilities;
import org.opengis.style.Style;

/**
 * Default layer details for a datastore type.
 *
 * @author Johann Sorel (Geomatys)
 */
public class DefaultFeatureData extends DefaultGeoData<FeatureSet> implements FeatureData {

    /**
     * Defines the number of pixels we want to add to the specified coordinates given by
     * the GetFeatureInfo request.
     */
    protected static final int MARGIN = 4;

    protected final ValueReference dateStartField;
    protected final ValueReference dateEndField;
    protected final ValueReference elevationStartField;
    protected final ValueReference elevationEndField;

    /**
     * Data version date. Use to query Features is input FeatureStore is versioned.
     */
    protected final Date versionDate;

    /**
     * Build a FeatureData with layer name, store, favorite style names, temporal/elevation filters and
     * data version date.
     *
     * @param name layer name
     * @param store FeatureStore
     * @param origin Wrapped feature set.
     * @param dateStart temporal filter start
     * @param dateEnd temporal filter end
     * @param elevationStart elevation filter start
     * @param elevationEnd elevation filter end
     * @param versionDate data version date of the layer (can be null)
     */
    public DefaultFeatureData(GenericName name, DataStore store, FeatureSet origin,
                                        String dateStart, String dateEnd, String elevationStart, String elevationEnd, Date versionDate){
        super(name, origin, store);
        this.versionDate = versionDate;

        final FilterFactory ff = FilterUtilities.FF;

        this.dateStartField = dateStart != null ? ff.property(dateStart) : null;
        this.dateEndField   = dateEnd   != null ? ff.property(dateEnd)   : null;

        this.elevationStartField = elevationStart != null ? ff.property(elevationStart) : null;
        this.elevationEndField   = elevationEnd   != null ? ff.property(elevationEnd)   : null;
    }

    @Override
    public List<ValueReference> getTimeDimension() {
        List<ValueReference> results = new ArrayList<>();
        if (dateStartField != null) results.add(dateStartField);
        if (dateEndField != null) results.add(dateEndField);
        return results;
    }

    @Override
    public List<ValueReference> getElevationDimension() {
        List<ValueReference> results = new ArrayList<>();
        if (dateStartField != null) results.add(dateStartField);
        if (dateEndField != null) results.add(dateEndField);
        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Style getDefaultStyle() throws ConstellationStoreException {
        return RandomStyleBuilder.createDefaultVectorStyle(getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Envelope getEnvelope() throws ConstellationStoreException {
        try {
            FeatureSet subfs;
            if (versionDate != null) {
                final Query query = new Query();
                query.setVersionDate(versionDate);
                query.setTypeName(name);
                subfs = origin.subset(query);
            } else {
                subfs = origin;
            }
            return FeatureStoreUtilities.getEnvelope(subfs);
        } catch (Exception ex) {
            throw new ConstellationStoreException(ex);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Date> getAvailableTimes() throws ConstellationStoreException {
        final SortedSet<Date> dates = new TreeSet<>();
        if (dateStartField != null) {
            try {
                final AttributeType desc = (AttributeType) dateStartField.apply(origin.getType());

                if(desc == null){
                    LOGGER.log(Level.WARNING , "Invalide field : "+ dateStartField + " Doesnt exists in layer :" + name);
                    return dates;
                }

                final Class type = desc.getValueClass();
                if( !(Date.class.isAssignableFrom(type)) ){
                    LOGGER.log(Level.WARNING , "Invalide field type for dates, layer " + name +", must be a Date, found a " + type);
                    return dates;
                }

                final Query query = new Query();
                query.setTypeName(name);
                query.setProperties(new String[]{dateStartField.getXPath()});
                query.setVersionDate(versionDate);

                try (Stream<Feature> stream = origin.subset(query).features(false)) {
                    Iterator<Feature> features = stream.iterator();
                    while(features.hasNext()){
                        final Feature sf = features.next();
                        final Date date = (Date) dateStartField.apply(sf);
                        if(date != null){
                            dates.add(date);
                        }
                    }
                }
            } catch(DataStoreException ex) {
                LOGGER.log(Level.WARNING , "Could not evaluate dates",ex);
            }

        }

        return dates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Number> getAvailableElevations() throws ConstellationStoreException {
        final SortedSet<Number> elevations = new TreeSet<>();
        if (elevationStartField != null) {

            try {
                final AttributeType desc = (AttributeType) elevationStartField.apply(origin.getType());
                if(desc == null){
                    LOGGER.log(Level.WARNING , "Invalid field : "+ elevationStartField + " Does not exist in layer :" + name);
                    return elevations;
                }

                final Class type = desc.getValueClass();
                if (!(Number.class.isAssignableFrom(type)) ){
                    LOGGER.log(Level.WARNING , "Invalid field type for elevations, layer " + name +", must be a Number, found a " + type);
                    return elevations;
                }

                final Query query = new Query();
                query.setTypeName(name);
                query.setProperties(new String[]{elevationStartField.getXPath()});
                query.setVersionDate(versionDate);

                try (Stream<Feature> stream = origin.subset(query).features(false)) {
                    Iterator<Feature> features = stream.iterator();
                    while (features.hasNext()) {
                        final Feature sf = features.next();
                        final Number ele = (Number) elevationStartField.apply(sf);
                        if(ele != null){
                            elevations.add(ele);
                        }
                    }
                }

            } catch(DataStoreException ex) {
                LOGGER.log(Level.WARNING , "Could not evaluate elevationss",ex);
            }
        }
        return elevations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FeatureType getType() throws ConstellationStoreException {
        try {
            return origin.getType();
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }


    /**
     * Gives a {@link FeatureDataDescription} instance describing the feature layer
     * data source.
     */
    @Override
    public FeatureDataDescription getDataDescription(StatInfo statInfo, Envelope env) throws ConstellationStoreException {
        final FeatureDataDescription description = new FeatureDataDescription();
        try {

            // Acquire data feature type.
            final FeatureType featureType = origin.getType();

            // Feature attributes description.
            for (PropertyType pt : featureType.getProperties(true)) {
                if (pt instanceof AttributeType && !AttributeConvention.contains(pt.getName())) {
                    final AttributeType attType = (AttributeType) pt;
                    description.getProperties().add(new PropertyDescription(
                        NamesExt.getNamespace(pt.getName()),
                        pt.getName().tip().toString(),
                        attType.getValueClass()));
                }
            }

            // Geographic extent description.
            if (env == null) {
                env = getEnvelope();
            }
            DataProviders.fillGeographicDescription(env, description);

        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
        return description;
    }

    @Override
    public Object[] getPropertyValues(String property) throws ConstellationStoreException {
        try {
            // Visit collection.
            final Query query = new Query();
            query.setProperties(new String[]{property});
            query.setTypeName(getName());
            try (Stream<Feature> stream = origin.subset(query).features(false)) {
                return stream
                        .map(f -> f.getPropertyValue(property))
                        .toArray();
            }

        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    @Override
    public DataType getDataType() {
        return DataType.VECTOR;
    }

    @Override
    public String getSubType() throws ConstellationStoreException {
        try {
            FeatureType fType = origin.getType();
            return findGeometryType(fType, null);
        } catch (DataStoreException ex) {
            throw new ConstellationStoreException(ex);
        }
    }

    private static String findGeometryType(FeatureType ft, Set<GenericName> visited){
        if(ft==null) return null;

        if(visited==null) visited = new HashSet<>();
        if(visited.contains(ft.getName())) return null;
        visited.add(ft.getName());

        if(ft instanceof FeatureType){
            try {
                IdentifiedType property = ((FeatureType)ft).getProperty(AttributeConvention.GEOMETRY_PROPERTY.toString());
                if (property instanceof Operation) {
                   property = ((Operation)property).getResult();
                }
                if(property instanceof AttributeType) {
                    return ((AttributeType)property).getValueClass().getSimpleName();
                }
            } catch (PropertyNotFoundException ex){
                //continue
            }
        }

        for (PropertyType type : ft.getProperties(true)) {

            if (type instanceof FeatureAssociationRole) {
                try {
                    FeatureType propFt = ((FeatureAssociationRole) type).getValueType();
                    String subType = findGeometryType(propFt, visited);
                    if (subType != null) {
                        return subType;
                    }
                } catch (IllegalStateException ex) {
                    LOGGER.log(Level.WARNING, "Unable to resolver feature type attribute:" + ex.getMessage());
                }
            } else if (type instanceof AttributeType) {
                final Class<?> valueClass = ((AttributeType) type).getValueClass();
                if (Geometry.class.isAssignableFrom(valueClass)) {
                    return valueClass.getSimpleName();
                }
            }
        }
        return null;
    }
}
