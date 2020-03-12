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

package org.constellation.sos.io.generic;


import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.dto.service.config.generic.Automatic;
import org.constellation.dto.service.config.generic.BDD;
import org.constellation.dto.service.config.generic.Query;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.observation.ObservationFilterReader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.api.CommonConstants;
import org.constellation.generic.BDDUtils;

import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;

/**
 *
 * @author Guilhem Legal
 */
public abstract class AbstractGenericObservationFilter implements ObservationFilterReader {

    /**
     * The base whole configuration query extract from the file Affinage.xml
     */
    protected final Query configurationQuery;

    /**
     * A map of static variable to replace in the statements.
     */
    protected HashMap<String, Object> staticParameters = new HashMap<>();

    /**
     *  The current query built by the sos worker in the scope of a getObservation/getResult request.
     */
    protected Query currentQuery;

     /**
     * The base for observation id.
     */
    protected final String observationIdBase;

    /**
     * The base for observation id.
     */
    protected final String observationTemplateIdBase;

    protected final String phenomenonIdBase;

    /**
     * The O&amp;M data source
     */
    protected DataSource dataSource;

    /**
     * A flag indicating that the service is trying to reconnect the database.
     */
    private boolean isReconnecting = false;

     /**
     * The database informations.
     */
    private Automatic configuration;


    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos.io.generic");

    public AbstractGenericObservationFilter(final Automatic configuration, final Map<String, Object> properties) throws DataStoreException {
        this.observationIdBase         = (String) properties.get(CommonConstants.OBSERVATION_ID_BASE);
        this.observationTemplateIdBase = (String) properties.get(CommonConstants.OBSERVATION_TEMPLATE_ID_BASE);
        this.phenomenonIdBase          = (String) properties.get(CommonConstants.PHENOMENON_ID_BASE);
        if (configuration == null) {
            throw new DataStoreException("The configuration object is null");
        }
        this.configuration = configuration;

        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new DataStoreException("The configuration file does not contains a BDD object");
        }
        this.configurationQuery = configuration.getFilterQueries();
        if (configurationQuery == null) {
            throw new DataStoreException("Unable to find the filter queries part");
        }
        try {
            this.dataSource = BDDUtils.getDataSource(db.getClassName(), db.getConnectURL(), db.getUser(), db.getPassword());
            if (configurationQuery.getStatique() != null) {
                for (Query query : configurationQuery.getStatique().getQuery()) {
                    processStatiqueQuery(query);
                }
            }
        } catch (SQLException ex) {
            throw new DataStoreException("SQLException while initializing the observation filter:\ncause:" + ex.getMessage());
        }
    }

    public AbstractGenericObservationFilter(final AbstractGenericObservationFilter that) {
        this.observationIdBase         = that.observationIdBase;
        this.observationTemplateIdBase = that.observationTemplateIdBase;
        this.configurationQuery        = that.configurationQuery;
        this.dataSource                = that.dataSource;
        this.staticParameters          = that.staticParameters;
        this.phenomenonIdBase          = that.phenomenonIdBase;
    }

    private void processStatiqueQuery(final Query query) throws SQLException {
        final List<String> varNames = query.getVarNames();
        final String textQuery      = query.buildSQLQuery(staticParameters);
        final Map<String, StringBuilder> parameterValue = new HashMap<>();
        for (String varName : varNames) {
            parameterValue.put(varName, new StringBuilder());
        }

        try (final Connection connection = acquireConnection();
             final Statement stmt        = connection.createStatement();
             final ResultSet res         = stmt.executeQuery(textQuery)) {
            while (res.next()) {
                for (String varName : varNames) {
                    final StringBuilder builder = parameterValue.get(varName);
                    builder.append("'").append(res.getString(varName)).append("',");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "SQL exception while executing static query :{0}", textQuery);
            throw ex;
        }
        //we remove the last ','
        for (String varName : varNames) {
            final StringBuilder builder = parameterValue.get(varName);
            final String pValue;
            if (builder.length() > 0) {
                pValue = builder.substring(0, builder.length() - 1);
            } else {
                pValue = "";
            }
            staticParameters.put(varName, pValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> supportedQueryableResultProperties() {
        return new ArrayList<>();
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public void setResultEquals(final String propertyName, final String value) throws DataStoreException {
        throw new DataStoreException("setResultEquals is not supported by this ObservationFilter implementation.");
    }

    /**
     * Try to reconnect to the database if the connection have been lost.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    protected void reloadConnection() throws CstlServiceException {
        if (!isReconnecting) {
            try {
               LOGGER.info("refreshing the connection");
               BDD db          = configuration.getBdd();
               this.dataSource = BDDUtils.getDataSource(db.getClassName(), db.getConnectURL(), db.getUser(), db.getPassword());
               isReconnecting  = false;

            } catch(SQLException ex) {
                LOGGER.log(Level.SEVERE, "SQLException while restarting the connection:{0}", ex.getMessage());
                isReconnecting = false;
            }
        }
        throw new CstlServiceException("The database connection has been lost, the service is trying to reconnect", NO_APPLICABLE_CODE);
    }

    protected Connection acquireConnection() throws SQLException {
        @SuppressWarnings("squid:S2095")
        final Connection c = dataSource.getConnection();
        c.setReadOnly(true);
        c.setAutoCommit(false);
        return c;
    }

    @Override
    public boolean isDefaultTemplateTime() {
        return true;
    }
}
