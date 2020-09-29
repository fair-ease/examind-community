/*
 *     Examind Community - An open source and standard compliant SDI
 *     https://community.examind.com/
 * 
 *  Copyright 2022 Geomatys.
 * 
 *  Licensed under the Apache License, Version 2.0 (    the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/
package com.examind.database.api.jooq;


import com.examind.database.api.jooq.tables.Attachment;
import com.examind.database.api.jooq.tables.ChainProcess;
import com.examind.database.api.jooq.tables.Crs;
import com.examind.database.api.jooq.tables.CstlUser;
import com.examind.database.api.jooq.tables.Data;
import com.examind.database.api.jooq.tables.DataDimRange;
import com.examind.database.api.jooq.tables.DataElevations;
import com.examind.database.api.jooq.tables.DataEnvelope;
import com.examind.database.api.jooq.tables.DataTimes;
import com.examind.database.api.jooq.tables.DataXData;
import com.examind.database.api.jooq.tables.Dataset;
import com.examind.database.api.jooq.tables.Datasource;
import com.examind.database.api.jooq.tables.DatasourcePath;
import com.examind.database.api.jooq.tables.DatasourcePathStore;
import com.examind.database.api.jooq.tables.DatasourceSelectedPath;
import com.examind.database.api.jooq.tables.DatasourceStore;
import com.examind.database.api.jooq.tables.InternalMetadata;
import com.examind.database.api.jooq.tables.InternalSensor;
import com.examind.database.api.jooq.tables.Layer;
import com.examind.database.api.jooq.tables.Mapcontext;
import com.examind.database.api.jooq.tables.MapcontextStyledLayer;
import com.examind.database.api.jooq.tables.Metadata;
import com.examind.database.api.jooq.tables.MetadataBbox;
import com.examind.database.api.jooq.tables.MetadataXAttachment;
import com.examind.database.api.jooq.tables.MetadataXCsw;
import com.examind.database.api.jooq.tables.Permission;
import com.examind.database.api.jooq.tables.Property;
import com.examind.database.api.jooq.tables.Provider;
import com.examind.database.api.jooq.tables.ProviderXCsw;
import com.examind.database.api.jooq.tables.ProviderXSos;
import com.examind.database.api.jooq.tables.Role;
import com.examind.database.api.jooq.tables.Sensor;
import com.examind.database.api.jooq.tables.SensorXSos;
import com.examind.database.api.jooq.tables.SensoredData;
import com.examind.database.api.jooq.tables.Service;
import com.examind.database.api.jooq.tables.ServiceDetails;
import com.examind.database.api.jooq.tables.ServiceExtraConfig;
import com.examind.database.api.jooq.tables.Style;
import com.examind.database.api.jooq.tables.StyledData;
import com.examind.database.api.jooq.tables.StyledLayer;
import com.examind.database.api.jooq.tables.Task;
import com.examind.database.api.jooq.tables.TaskParameter;
import com.examind.database.api.jooq.tables.Thesaurus;
import com.examind.database.api.jooq.tables.ThesaurusLanguage;
import com.examind.database.api.jooq.tables.ThesaurusXService;
import com.examind.database.api.jooq.tables.UserXRole;

import java.util.Arrays;
import java.util.List;

import org.jooq.Catalog;
import org.jooq.Sequence;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Admin extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>admin</code>
     */
    public static final Admin ADMIN = new Admin();

    /**
     * The table <code>admin.attachment</code>.
     */
    public final Attachment ATTACHMENT = Attachment.ATTACHMENT;

    /**
     * The table <code>admin.chain_process</code>.
     */
    public final ChainProcess CHAIN_PROCESS = ChainProcess.CHAIN_PROCESS;

    /**
     * The table <code>admin.crs</code>.
     */
    public final Crs CRS = Crs.CRS;

    /**
     * The table <code>admin.cstl_user</code>.
     */
    public final CstlUser CSTL_USER = CstlUser.CSTL_USER;

    /**
     * The table <code>admin.data</code>.
     */
    public final Data DATA = Data.DATA;

    /**
     * The table <code>admin.data_dim_range</code>.
     */
    public final DataDimRange DATA_DIM_RANGE = DataDimRange.DATA_DIM_RANGE;

    /**
     * The table <code>admin.data_elevations</code>.
     */
    public final DataElevations DATA_ELEVATIONS = DataElevations.DATA_ELEVATIONS;

    /**
     * The table <code>admin.data_envelope</code>.
     */
    public final DataEnvelope DATA_ENVELOPE = DataEnvelope.DATA_ENVELOPE;

    /**
     * The table <code>admin.data_times</code>.
     */
    public final DataTimes DATA_TIMES = DataTimes.DATA_TIMES;

    /**
     * The table <code>admin.data_x_data</code>.
     */
    public final DataXData DATA_X_DATA = DataXData.DATA_X_DATA;

    /**
     * The table <code>admin.dataset</code>.
     */
    public final Dataset DATASET = Dataset.DATASET;

    /**
     * The table <code>admin.datasource</code>.
     */
    public final Datasource DATASOURCE = Datasource.DATASOURCE;

    /**
     * The table <code>admin.datasource_path</code>.
     */
    public final DatasourcePath DATASOURCE_PATH = DatasourcePath.DATASOURCE_PATH;

    /**
     * The table <code>admin.datasource_path_store</code>.
     */
    public final DatasourcePathStore DATASOURCE_PATH_STORE = DatasourcePathStore.DATASOURCE_PATH_STORE;

    /**
     * The table <code>admin.datasource_selected_path</code>.
     */
    public final DatasourceSelectedPath DATASOURCE_SELECTED_PATH = DatasourceSelectedPath.DATASOURCE_SELECTED_PATH;

    /**
     * The table <code>admin.datasource_store</code>.
     */
    public final DatasourceStore DATASOURCE_STORE = DatasourceStore.DATASOURCE_STORE;

    /**
     * The table <code>admin.internal_metadata</code>.
     */
    public final InternalMetadata INTERNAL_METADATA = InternalMetadata.INTERNAL_METADATA;

    /**
     * The table <code>admin.internal_sensor</code>.
     */
    public final InternalSensor INTERNAL_SENSOR = InternalSensor.INTERNAL_SENSOR;

    /**
     * The table <code>admin.layer</code>.
     */
    public final Layer LAYER = Layer.LAYER;

    /**
     * The table <code>admin.mapcontext</code>.
     */
    public final Mapcontext MAPCONTEXT = Mapcontext.MAPCONTEXT;

    /**
     * The table <code>admin.mapcontext_styled_layer</code>.
     */
    public final MapcontextStyledLayer MAPCONTEXT_STYLED_LAYER = MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER;

    /**
     * The table <code>admin.metadata</code>.
     */
    public final Metadata METADATA = Metadata.METADATA;

    /**
     * The table <code>admin.metadata_bbox</code>.
     */
    public final MetadataBbox METADATA_BBOX = MetadataBbox.METADATA_BBOX;

    /**
     * The table <code>admin.metadata_x_attachment</code>.
     */
    public final MetadataXAttachment METADATA_X_ATTACHMENT = MetadataXAttachment.METADATA_X_ATTACHMENT;

    /**
     * The table <code>admin.metadata_x_csw</code>.
     */
    public final MetadataXCsw METADATA_X_CSW = MetadataXCsw.METADATA_X_CSW;

    /**
     * The table <code>admin.permission</code>.
     */
    public final Permission PERMISSION = Permission.PERMISSION;

    /**
     * The table <code>admin.property</code>.
     */
    public final Property PROPERTY = Property.PROPERTY;

    /**
     * The table <code>admin.provider</code>.
     */
    public final Provider PROVIDER = Provider.PROVIDER;

    /**
     * The table <code>admin.provider_x_csw</code>.
     */
    public final ProviderXCsw PROVIDER_X_CSW = ProviderXCsw.PROVIDER_X_CSW;

    /**
     * The table <code>admin.provider_x_sos</code>.
     */
    public final ProviderXSos PROVIDER_X_SOS = ProviderXSos.PROVIDER_X_SOS;

    /**
     * The table <code>admin.role</code>.
     */
    public final Role ROLE = Role.ROLE;

    /**
     * The table <code>admin.sensor</code>.
     */
    public final Sensor SENSOR = Sensor.SENSOR;

    /**
     * The table <code>admin.sensor_x_sos</code>.
     */
    public final SensorXSos SENSOR_X_SOS = SensorXSos.SENSOR_X_SOS;

    /**
     * The table <code>admin.sensored_data</code>.
     */
    public final SensoredData SENSORED_DATA = SensoredData.SENSORED_DATA;

    /**
     * The table <code>admin.service</code>.
     */
    public final Service SERVICE = Service.SERVICE;

    /**
     * The table <code>admin.service_details</code>.
     */
    public final ServiceDetails SERVICE_DETAILS = ServiceDetails.SERVICE_DETAILS;

    /**
     * The table <code>admin.service_extra_config</code>.
     */
    public final ServiceExtraConfig SERVICE_EXTRA_CONFIG = ServiceExtraConfig.SERVICE_EXTRA_CONFIG;

    /**
     * The table <code>admin.style</code>.
     */
    public final Style STYLE = Style.STYLE;

    /**
     * The table <code>admin.styled_data</code>.
     */
    public final StyledData STYLED_DATA = StyledData.STYLED_DATA;

    /**
     * The table <code>admin.styled_layer</code>.
     */
    public final StyledLayer STYLED_LAYER = StyledLayer.STYLED_LAYER;

    /**
     * The table <code>admin.task</code>.
     */
    public final Task TASK = Task.TASK;

    /**
     * The table <code>admin.task_parameter</code>.
     */
    public final TaskParameter TASK_PARAMETER = TaskParameter.TASK_PARAMETER;

    /**
     * The table <code>admin.thesaurus</code>.
     */
    public final Thesaurus THESAURUS = Thesaurus.THESAURUS;

    /**
     * The table <code>admin.thesaurus_language</code>.
     */
    public final ThesaurusLanguage THESAURUS_LANGUAGE = ThesaurusLanguage.THESAURUS_LANGUAGE;

    /**
     * The table <code>admin.thesaurus_x_service</code>.
     */
    public final ThesaurusXService THESAURUS_X_SERVICE = ThesaurusXService.THESAURUS_X_SERVICE;

    /**
     * The table <code>admin.user_x_role</code>.
     */
    public final UserXRole USER_X_ROLE = UserXRole.USER_X_ROLE;

    /**
     * No further instances allowed
     */
    private Admin() {
        super("admin", null);
    }


    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Sequence<?>> getSequences() {
        return Arrays.<Sequence<?>>asList(
            Sequences.ATTACHMENT_ID_SEQ,
            Sequences.CHAIN_PROCESS_ID_SEQ,
            Sequences.CSTL_USER_ID_SEQ,
            Sequences.DATA_ID_SEQ,
            Sequences.DATASET_ID_SEQ,
            Sequences.DATASOURCE_ID_SEQ,
            Sequences.INTERNAL_METADATA_ID_SEQ,
            Sequences.INTERNAL_SENSOR_ID_SEQ,
            Sequences.LAYER_ID_SEQ,
            Sequences.MAPCONTEXT_ID_SEQ,
            Sequences.MAPCONTEXT_STYLED_LAYER_ID_SEQ,
            Sequences.METADATA_ID_SEQ,
            Sequences.PERMISSION_ID_SEQ,
            Sequences.PROVIDER_ID_SEQ,
            Sequences.SENSOR_ID_SEQ,
            Sequences.SERVICE_ID_SEQ,
            Sequences.STYLE_ID_SEQ,
            Sequences.TASK_PARAMETER_ID_SEQ,
            Sequences.THESAURUS_ID_SEQ);
    }

    @Override
    public final List<Table<?>> getTables() {
        return Arrays.<Table<?>>asList(
            Attachment.ATTACHMENT,
            ChainProcess.CHAIN_PROCESS,
            Crs.CRS,
            CstlUser.CSTL_USER,
            Data.DATA,
            DataDimRange.DATA_DIM_RANGE,
            DataElevations.DATA_ELEVATIONS,
            DataEnvelope.DATA_ENVELOPE,
            DataTimes.DATA_TIMES,
            DataXData.DATA_X_DATA,
            Dataset.DATASET,
            Datasource.DATASOURCE,
            DatasourcePath.DATASOURCE_PATH,
            DatasourcePathStore.DATASOURCE_PATH_STORE,
            DatasourceSelectedPath.DATASOURCE_SELECTED_PATH,
            DatasourceStore.DATASOURCE_STORE,
            InternalMetadata.INTERNAL_METADATA,
            InternalSensor.INTERNAL_SENSOR,
            Layer.LAYER,
            Mapcontext.MAPCONTEXT,
            MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER,
            Metadata.METADATA,
            MetadataBbox.METADATA_BBOX,
            MetadataXAttachment.METADATA_X_ATTACHMENT,
            MetadataXCsw.METADATA_X_CSW,
            Permission.PERMISSION,
            Property.PROPERTY,
            Provider.PROVIDER,
            ProviderXCsw.PROVIDER_X_CSW,
            ProviderXSos.PROVIDER_X_SOS,
            Role.ROLE,
            Sensor.SENSOR,
            SensorXSos.SENSOR_X_SOS,
            SensoredData.SENSORED_DATA,
            Service.SERVICE,
            ServiceDetails.SERVICE_DETAILS,
            ServiceExtraConfig.SERVICE_EXTRA_CONFIG,
            Style.STYLE,
            StyledData.STYLED_DATA,
            StyledLayer.STYLED_LAYER,
            Task.TASK,
            TaskParameter.TASK_PARAMETER,
            Thesaurus.THESAURUS,
            ThesaurusLanguage.THESAURUS_LANGUAGE,
            ThesaurusXService.THESAURUS_X_SERVICE,
            UserXRole.USER_X_ROLE);
    }
}
