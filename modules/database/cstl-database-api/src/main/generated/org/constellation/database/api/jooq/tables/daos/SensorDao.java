/**
 * This class is generated by jOOQ
 */
package org.constellation.database.api.jooq.tables.daos;

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
public class SensorDao extends org.jooq.impl.DAOImpl<org.constellation.database.api.jooq.tables.records.SensorRecord, org.constellation.database.api.jooq.tables.pojos.Sensor, java.lang.Integer> {

	/**
	 * Create a new SensorDao without any configuration
	 */
	public SensorDao() {
		super(org.constellation.database.api.jooq.tables.Sensor.SENSOR, org.constellation.database.api.jooq.tables.pojos.Sensor.class);
	}

	/**
	 * Create a new SensorDao with an attached configuration
	 */
	public SensorDao(org.jooq.Configuration configuration) {
		super(org.constellation.database.api.jooq.tables.Sensor.SENSOR, org.constellation.database.api.jooq.tables.pojos.Sensor.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Integer getId(org.constellation.database.api.jooq.tables.pojos.Sensor object) {
		return object.getId();
	}

	/**
	 * Fetch records that have <code>id IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchById(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>id = value</code>
	 */
	public org.constellation.database.api.jooq.tables.pojos.Sensor fetchOneById(java.lang.Integer value) {
		return fetchOne(org.constellation.database.api.jooq.tables.Sensor.SENSOR.ID, value);
	}

	/**
	 * Fetch records that have <code>identifier IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchByIdentifier(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.IDENTIFIER, values);
	}

	/**
	 * Fetch a unique record that has <code>identifier = value</code>
	 */
	public org.constellation.database.api.jooq.tables.pojos.Sensor fetchOneByIdentifier(java.lang.String value) {
		return fetchOne(org.constellation.database.api.jooq.tables.Sensor.SENSOR.IDENTIFIER, value);
	}

	/**
	 * Fetch records that have <code>type IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchByType(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.TYPE, values);
	}

	/**
	 * Fetch records that have <code>parent IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchByParent(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.PARENT, values);
	}

	/**
	 * Fetch records that have <code>owner IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchByOwner(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.OWNER, values);
	}

	/**
	 * Fetch records that have <code>date IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchByDate(java.lang.Long... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.DATE, values);
	}

	/**
	 * Fetch records that have <code>provider_id IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchByProviderId(java.lang.Integer... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.PROVIDER_ID, values);
	}

	/**
	 * Fetch records that have <code>profile IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchByProfile(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.PROFILE, values);
	}

	/**
	 * Fetch records that have <code>om_type IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchByOmType(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.OM_TYPE, values);
	}

	/**
	 * Fetch records that have <code>name IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchByName(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.NAME, values);
	}

	/**
	 * Fetch records that have <code>description IN (values)</code>
	 */
	public java.util.List<org.constellation.database.api.jooq.tables.pojos.Sensor> fetchByDescription(java.lang.String... values) {
		return fetch(org.constellation.database.api.jooq.tables.Sensor.SENSOR.DESCRIPTION, values);
	}
}
