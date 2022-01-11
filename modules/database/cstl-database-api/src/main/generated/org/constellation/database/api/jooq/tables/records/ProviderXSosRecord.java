/*
 * This file is generated by jOOQ.
 */
package org.constellation.database.api.jooq.tables.records;


import javax.validation.constraints.NotNull;

import org.constellation.database.api.jooq.tables.ProviderXSos;
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.TableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProviderXSosRecord extends TableRecordImpl<ProviderXSosRecord> implements Record3<Integer, Integer, Boolean> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>admin.provider_x_sos.sos_id</code>.
     */
    public ProviderXSosRecord setSosId(Integer value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>admin.provider_x_sos.sos_id</code>.
     */
    @NotNull
    public Integer getSosId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>admin.provider_x_sos.provider_id</code>.
     */
    public ProviderXSosRecord setProviderId(Integer value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>admin.provider_x_sos.provider_id</code>.
     */
    @NotNull
    public Integer getProviderId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>admin.provider_x_sos.all_sensor</code>.
     */
    public ProviderXSosRecord setAllSensor(Boolean value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>admin.provider_x_sos.all_sensor</code>.
     */
    public Boolean getAllSensor() {
        return (Boolean) get(2);
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row3<Integer, Integer, Boolean> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    @Override
    public Row3<Integer, Integer, Boolean> valuesRow() {
        return (Row3) super.valuesRow();
    }

    @Override
    public Field<Integer> field1() {
        return ProviderXSos.PROVIDER_X_SOS.SOS_ID;
    }

    @Override
    public Field<Integer> field2() {
        return ProviderXSos.PROVIDER_X_SOS.PROVIDER_ID;
    }

    @Override
    public Field<Boolean> field3() {
        return ProviderXSos.PROVIDER_X_SOS.ALL_SENSOR;
    }

    @Override
    public Integer component1() {
        return getSosId();
    }

    @Override
    public Integer component2() {
        return getProviderId();
    }

    @Override
    public Boolean component3() {
        return getAllSensor();
    }

    @Override
    public Integer value1() {
        return getSosId();
    }

    @Override
    public Integer value2() {
        return getProviderId();
    }

    @Override
    public Boolean value3() {
        return getAllSensor();
    }

    @Override
    public ProviderXSosRecord value1(Integer value) {
        setSosId(value);
        return this;
    }

    @Override
    public ProviderXSosRecord value2(Integer value) {
        setProviderId(value);
        return this;
    }

    @Override
    public ProviderXSosRecord value3(Boolean value) {
        setAllSensor(value);
        return this;
    }

    @Override
    public ProviderXSosRecord values(Integer value1, Integer value2, Boolean value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ProviderXSosRecord
     */
    public ProviderXSosRecord() {
        super(ProviderXSos.PROVIDER_X_SOS);
    }

    /**
     * Create a detached, initialised ProviderXSosRecord
     */
    public ProviderXSosRecord(Integer sosId, Integer providerId, Boolean allSensor) {
        super(ProviderXSos.PROVIDER_X_SOS);

        setSosId(sosId);
        setProviderId(providerId);
        setAllSensor(allSensor);
    }
}
