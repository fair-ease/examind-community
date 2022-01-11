/*
 * This file is generated by jOOQ.
 */
package org.constellation.database.api.jooq.tables.records;


import javax.validation.constraints.NotNull;

import org.constellation.database.api.jooq.tables.MetadataBbox;
import org.jooq.Field;
import org.jooq.Record5;
import org.jooq.Row5;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class MetadataBboxRecord extends UpdatableRecordImpl<MetadataBboxRecord> implements Record5<Integer, Double, Double, Double, Double> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>admin.metadata_bbox.metadata_id</code>.
     */
    public MetadataBboxRecord setMetadataId(Integer value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>admin.metadata_bbox.metadata_id</code>.
     */
    @NotNull
    public Integer getMetadataId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>admin.metadata_bbox.east</code>.
     */
    public MetadataBboxRecord setEast(Double value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>admin.metadata_bbox.east</code>.
     */
    @NotNull
    public Double getEast() {
        return (Double) get(1);
    }

    /**
     * Setter for <code>admin.metadata_bbox.west</code>.
     */
    public MetadataBboxRecord setWest(Double value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>admin.metadata_bbox.west</code>.
     */
    @NotNull
    public Double getWest() {
        return (Double) get(2);
    }

    /**
     * Setter for <code>admin.metadata_bbox.north</code>.
     */
    public MetadataBboxRecord setNorth(Double value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>admin.metadata_bbox.north</code>.
     */
    @NotNull
    public Double getNorth() {
        return (Double) get(3);
    }

    /**
     * Setter for <code>admin.metadata_bbox.south</code>.
     */
    public MetadataBboxRecord setSouth(Double value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>admin.metadata_bbox.south</code>.
     */
    @NotNull
    public Double getSouth() {
        return (Double) get(4);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record5<Integer, Double, Double, Double, Double> key() {
        return (Record5) super.key();
    }

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row5<Integer, Double, Double, Double, Double> fieldsRow() {
        return (Row5) super.fieldsRow();
    }

    @Override
    public Row5<Integer, Double, Double, Double, Double> valuesRow() {
        return (Row5) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return MetadataBbox.METADATA_BBOX.METADATA_ID;
    }

    @Override
    public Field<Double> field2() {
        return MetadataBbox.METADATA_BBOX.EAST;
    }

    @Override
    public Field<Double> field3() {
        return MetadataBbox.METADATA_BBOX.WEST;
    }

    @Override
    public Field<Double> field4() {
        return MetadataBbox.METADATA_BBOX.NORTH;
    }

    @Override
    public Field<Double> field5() {
        return MetadataBbox.METADATA_BBOX.SOUTH;
    }

    @Override
    public Integer component1() {
        return getMetadataId();
    }

    @Override
    public Double component2() {
        return getEast();
    }

    @Override
    public Double component3() {
        return getWest();
    }

    @Override
    public Double component4() {
        return getNorth();
    }

    @Override
    public Double component5() {
        return getSouth();
    }

    @Override
    public Integer value1() {
        return getMetadataId();
    }

    @Override
    public Double value2() {
        return getEast();
    }

    @Override
    public Double value3() {
        return getWest();
    }

    @Override
    public Double value4() {
        return getNorth();
    }

    @Override
    public Double value5() {
        return getSouth();
    }

    @Override
    public MetadataBboxRecord value1(Integer value) {
        setMetadataId(value);
        return this;
    }

    @Override
    public MetadataBboxRecord value2(Double value) {
        setEast(value);
        return this;
    }

    @Override
    public MetadataBboxRecord value3(Double value) {
        setWest(value);
        return this;
    }

    @Override
    public MetadataBboxRecord value4(Double value) {
        setNorth(value);
        return this;
    }

    @Override
    public MetadataBboxRecord value5(Double value) {
        setSouth(value);
        return this;
    }

    @Override
    public MetadataBboxRecord values(Integer value1, Double value2, Double value3, Double value4, Double value5) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached MetadataBboxRecord
     */
    public MetadataBboxRecord() {
        super(MetadataBbox.METADATA_BBOX);
    }

    /**
     * Create a detached, initialised MetadataBboxRecord
     */
    public MetadataBboxRecord(Integer metadataId, Double east, Double west, Double north, Double south) {
        super(MetadataBbox.METADATA_BBOX);

        setMetadataId(metadataId);
        setEast(east);
        setWest(west);
        setNorth(north);
        setSouth(south);
    }
}
