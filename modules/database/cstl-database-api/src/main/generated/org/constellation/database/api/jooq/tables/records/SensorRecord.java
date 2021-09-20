/**
 * This class is generated by jOOQ
 */
package org.constellation.database.api.jooq.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.5.3"
	},
	comments = "This class is generated by jOOQ"
)
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class SensorRecord extends org.jooq.impl.UpdatableRecordImpl<org.constellation.database.api.jooq.tables.records.SensorRecord> implements org.jooq.Record11<java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Long, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String> {

	private static final long serialVersionUID = 913752187;

	/**
	 * Setter for <code>admin.sensor.id</code>.
	 */
	public SensorRecord setId(java.lang.Integer value) {
		setValue(0, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.id</code>.
	 */
	@javax.validation.constraints.NotNull
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>admin.sensor.identifier</code>.
	 */
	public SensorRecord setIdentifier(java.lang.String value) {
		setValue(1, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.identifier</code>.
	 */
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 512)
	public java.lang.String getIdentifier() {
		return (java.lang.String) getValue(1);
	}

	/**
	 * Setter for <code>admin.sensor.type</code>.
	 */
	public SensorRecord setType(java.lang.String value) {
		setValue(2, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.type</code>.
	 */
	@javax.validation.constraints.NotNull
	@javax.validation.constraints.Size(max = 64)
	public java.lang.String getType() {
		return (java.lang.String) getValue(2);
	}

	/**
	 * Setter for <code>admin.sensor.parent</code>.
	 */
	public SensorRecord setParent(java.lang.String value) {
		setValue(3, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.parent</code>.
	 */
	@javax.validation.constraints.Size(max = 512)
	public java.lang.String getParent() {
		return (java.lang.String) getValue(3);
	}

	/**
	 * Setter for <code>admin.sensor.owner</code>.
	 */
	public SensorRecord setOwner(java.lang.Integer value) {
		setValue(4, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.owner</code>.
	 */
	public java.lang.Integer getOwner() {
		return (java.lang.Integer) getValue(4);
	}

	/**
	 * Setter for <code>admin.sensor.date</code>.
	 */
	public SensorRecord setDate(java.lang.Long value) {
		setValue(5, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.date</code>.
	 */
	public java.lang.Long getDate() {
		return (java.lang.Long) getValue(5);
	}

	/**
	 * Setter for <code>admin.sensor.provider_id</code>.
	 */
	public SensorRecord setProviderId(java.lang.Integer value) {
		setValue(6, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.provider_id</code>.
	 */
	public java.lang.Integer getProviderId() {
		return (java.lang.Integer) getValue(6);
	}

	/**
	 * Setter for <code>admin.sensor.profile</code>.
	 */
	public SensorRecord setProfile(java.lang.String value) {
		setValue(7, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.profile</code>.
	 */
	@javax.validation.constraints.Size(max = 255)
	public java.lang.String getProfile() {
		return (java.lang.String) getValue(7);
	}

	/**
	 * Setter for <code>admin.sensor.om_type</code>.
	 */
	public SensorRecord setOmType(java.lang.String value) {
		setValue(8, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.om_type</code>.
	 */
	@javax.validation.constraints.Size(max = 100)
	public java.lang.String getOmType() {
		return (java.lang.String) getValue(8);
	}

	/**
	 * Setter for <code>admin.sensor.name</code>.
	 */
	public SensorRecord setName(java.lang.String value) {
		setValue(9, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.name</code>.
	 */
	@javax.validation.constraints.Size(max = 1000)
	public java.lang.String getName() {
		return (java.lang.String) getValue(9);
	}

	/**
	 * Setter for <code>admin.sensor.description</code>.
	 */
	public SensorRecord setDescription(java.lang.String value) {
		setValue(10, value);
		return this;
	}

	/**
	 * Getter for <code>admin.sensor.description</code>.
	 */
	@javax.validation.constraints.Size(max = 5000)
	public java.lang.String getDescription() {
		return (java.lang.String) getValue(10);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Integer> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record11 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row11<java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Long, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String> fieldsRow() {
		return (org.jooq.Row11) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row11<java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Long, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String> valuesRow() {
		return (org.jooq.Row11) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field2() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.IDENTIFIER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field3() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field4() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.PARENT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field5() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.OWNER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Long> field6() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.DATE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field7() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.PROVIDER_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field8() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.PROFILE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field9() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.OM_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field10() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.NAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field11() {
		return org.constellation.database.api.jooq.tables.Sensor.SENSOR.DESCRIPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value2() {
		return getIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value3() {
		return getType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value4() {
		return getParent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value5() {
		return getOwner();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Long value6() {
		return getDate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value7() {
		return getProviderId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value8() {
		return getProfile();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value9() {
		return getOmType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value10() {
		return getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value11() {
		return getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value1(java.lang.Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value2(java.lang.String value) {
		setIdentifier(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value3(java.lang.String value) {
		setType(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value4(java.lang.String value) {
		setParent(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value5(java.lang.Integer value) {
		setOwner(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value6(java.lang.Long value) {
		setDate(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value7(java.lang.Integer value) {
		setProviderId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value8(java.lang.String value) {
		setProfile(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value9(java.lang.String value) {
		setOmType(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value10(java.lang.String value) {
		setName(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord value11(java.lang.String value) {
		setDescription(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SensorRecord values(java.lang.Integer value1, java.lang.String value2, java.lang.String value3, java.lang.String value4, java.lang.Integer value5, java.lang.Long value6, java.lang.Integer value7, java.lang.String value8, java.lang.String value9, java.lang.String value10, java.lang.String value11) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached SensorRecord
	 */
	public SensorRecord() {
		super(org.constellation.database.api.jooq.tables.Sensor.SENSOR);
	}

	/**
	 * Create a detached, initialised SensorRecord
	 */
	public SensorRecord(java.lang.Integer id, java.lang.String identifier, java.lang.String type, java.lang.String parent, java.lang.Integer owner, java.lang.Long date, java.lang.Integer providerId, java.lang.String profile, java.lang.String omType, java.lang.String name, java.lang.String description) {
		super(org.constellation.database.api.jooq.tables.Sensor.SENSOR);

		setValue(0, id);
		setValue(1, identifier);
		setValue(2, type);
		setValue(3, parent);
		setValue(4, owner);
		setValue(5, date);
		setValue(6, providerId);
		setValue(7, profile);
		setValue(8, omType);
		setValue(9, name);
		setValue(10, description);
	}
}
