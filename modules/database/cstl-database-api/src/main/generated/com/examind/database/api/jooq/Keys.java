/*
 * This file is generated by jOOQ.
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
import com.examind.database.api.jooq.tables.records.AttachmentRecord;
import com.examind.database.api.jooq.tables.records.ChainProcessRecord;
import com.examind.database.api.jooq.tables.records.CrsRecord;
import com.examind.database.api.jooq.tables.records.CstlUserRecord;
import com.examind.database.api.jooq.tables.records.DataDimRangeRecord;
import com.examind.database.api.jooq.tables.records.DataElevationsRecord;
import com.examind.database.api.jooq.tables.records.DataEnvelopeRecord;
import com.examind.database.api.jooq.tables.records.DataRecord;
import com.examind.database.api.jooq.tables.records.DataTimesRecord;
import com.examind.database.api.jooq.tables.records.DataXDataRecord;
import com.examind.database.api.jooq.tables.records.DatasetRecord;
import com.examind.database.api.jooq.tables.records.DatasourcePathRecord;
import com.examind.database.api.jooq.tables.records.DatasourcePathStoreRecord;
import com.examind.database.api.jooq.tables.records.DatasourceRecord;
import com.examind.database.api.jooq.tables.records.DatasourceSelectedPathRecord;
import com.examind.database.api.jooq.tables.records.DatasourceStoreRecord;
import com.examind.database.api.jooq.tables.records.InternalMetadataRecord;
import com.examind.database.api.jooq.tables.records.InternalSensorRecord;
import com.examind.database.api.jooq.tables.records.LayerRecord;
import com.examind.database.api.jooq.tables.records.MapcontextRecord;
import com.examind.database.api.jooq.tables.records.MapcontextStyledLayerRecord;
import com.examind.database.api.jooq.tables.records.MetadataBboxRecord;
import com.examind.database.api.jooq.tables.records.MetadataRecord;
import com.examind.database.api.jooq.tables.records.MetadataXAttachmentRecord;
import com.examind.database.api.jooq.tables.records.MetadataXCswRecord;
import com.examind.database.api.jooq.tables.records.PermissionRecord;
import com.examind.database.api.jooq.tables.records.PropertyRecord;
import com.examind.database.api.jooq.tables.records.ProviderRecord;
import com.examind.database.api.jooq.tables.records.ProviderXCswRecord;
import com.examind.database.api.jooq.tables.records.ProviderXSosRecord;
import com.examind.database.api.jooq.tables.records.RoleRecord;
import com.examind.database.api.jooq.tables.records.SensorRecord;
import com.examind.database.api.jooq.tables.records.SensorXSosRecord;
import com.examind.database.api.jooq.tables.records.SensoredDataRecord;
import com.examind.database.api.jooq.tables.records.ServiceDetailsRecord;
import com.examind.database.api.jooq.tables.records.ServiceExtraConfigRecord;
import com.examind.database.api.jooq.tables.records.ServiceRecord;
import com.examind.database.api.jooq.tables.records.StyleRecord;
import com.examind.database.api.jooq.tables.records.StyledDataRecord;
import com.examind.database.api.jooq.tables.records.StyledLayerRecord;
import com.examind.database.api.jooq.tables.records.TaskParameterRecord;
import com.examind.database.api.jooq.tables.records.TaskRecord;
import com.examind.database.api.jooq.tables.records.ThesaurusLanguageRecord;
import com.examind.database.api.jooq.tables.records.ThesaurusRecord;
import com.examind.database.api.jooq.tables.records.ThesaurusXServiceRecord;
import com.examind.database.api.jooq.tables.records.UserXRoleRecord;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in 
 * admin.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<AttachmentRecord> ATTACHMENT_PK = Internal.createUniqueKey(Attachment.ATTACHMENT, DSL.name("attachment_pk"), new TableField[] { Attachment.ATTACHMENT.ID }, true);
    public static final UniqueKey<ChainProcessRecord> CHAIN_PROCESS_PK = Internal.createUniqueKey(ChainProcess.CHAIN_PROCESS, DSL.name("chain_process_pk"), new TableField[] { ChainProcess.CHAIN_PROCESS.ID }, true);
    public static final UniqueKey<CrsRecord> CRS_PK = Internal.createUniqueKey(Crs.CRS, DSL.name("crs_pk"), new TableField[] { Crs.CRS.DATAID, Crs.CRS.CRSCODE }, true);
    public static final UniqueKey<CstlUserRecord> CSTL_USER_EMAIL_KEY = Internal.createUniqueKey(CstlUser.CSTL_USER, DSL.name("cstl_user_email_key"), new TableField[] { CstlUser.CSTL_USER.EMAIL }, true);
    public static final UniqueKey<CstlUserRecord> CSTL_USER_FORGOT_PASSWORD_UUID_KEY = Internal.createUniqueKey(CstlUser.CSTL_USER, DSL.name("cstl_user_forgot_password_uuid_key"), new TableField[] { CstlUser.CSTL_USER.FORGOT_PASSWORD_UUID }, true);
    public static final UniqueKey<CstlUserRecord> CSTL_USER_LOGIN_KEY = Internal.createUniqueKey(CstlUser.CSTL_USER, DSL.name("cstl_user_login_key"), new TableField[] { CstlUser.CSTL_USER.LOGIN }, true);
    public static final UniqueKey<CstlUserRecord> USER_PK = Internal.createUniqueKey(CstlUser.CSTL_USER, DSL.name("user_pk"), new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final UniqueKey<DataRecord> DATA_PK = Internal.createUniqueKey(Data.DATA, DSL.name("data_pk"), new TableField[] { Data.DATA.ID }, true);
    public static final UniqueKey<DataDimRangeRecord> DATA_DIM_RANGE_PK = Internal.createUniqueKey(DataDimRange.DATA_DIM_RANGE, DSL.name("data_dim_range_pk"), new TableField[] { DataDimRange.DATA_DIM_RANGE.DATA_ID, DataDimRange.DATA_DIM_RANGE.DIMENSION }, true);
    public static final UniqueKey<DataElevationsRecord> DATA_ELEVATIONS_PK = Internal.createUniqueKey(DataElevations.DATA_ELEVATIONS, DSL.name("data_elevations_pk"), new TableField[] { DataElevations.DATA_ELEVATIONS.DATA_ID }, true);
    public static final UniqueKey<DataEnvelopeRecord> DATA_ENVELOPE_PK = Internal.createUniqueKey(DataEnvelope.DATA_ENVELOPE, DSL.name("data_envelope_pk"), new TableField[] { DataEnvelope.DATA_ENVELOPE.DATA_ID, DataEnvelope.DATA_ENVELOPE.DIMENSION }, true);
    public static final UniqueKey<DataTimesRecord> DATA_TIMES_PK = Internal.createUniqueKey(DataTimes.DATA_TIMES, DSL.name("data_times_pk"), new TableField[] { DataTimes.DATA_TIMES.DATA_ID }, true);
    public static final UniqueKey<DataXDataRecord> DATA_X_DATA_PK = Internal.createUniqueKey(DataXData.DATA_X_DATA, DSL.name("data_x_data_pk"), new TableField[] { DataXData.DATA_X_DATA.DATA_ID, DataXData.DATA_X_DATA.CHILD_ID }, true);
    public static final UniqueKey<DatasetRecord> DATASET_PK = Internal.createUniqueKey(Dataset.DATASET, DSL.name("dataset_pk"), new TableField[] { Dataset.DATASET.ID }, true);
    public static final UniqueKey<DatasourceRecord> DATASOURCE_PK = Internal.createUniqueKey(Datasource.DATASOURCE, DSL.name("datasource_pk"), new TableField[] { Datasource.DATASOURCE.ID }, true);
    public static final UniqueKey<DatasourcePathRecord> DATASOURCE_PATH_PK = Internal.createUniqueKey(DatasourcePath.DATASOURCE_PATH, DSL.name("datasource_path_pk"), new TableField[] { DatasourcePath.DATASOURCE_PATH.DATASOURCE_ID, DatasourcePath.DATASOURCE_PATH.PATH }, true);
    public static final UniqueKey<DatasourcePathStoreRecord> DATASOURCE_PATH_STORE_PK = Internal.createUniqueKey(DatasourcePathStore.DATASOURCE_PATH_STORE, DSL.name("datasource_path_store_pk"), new TableField[] { DatasourcePathStore.DATASOURCE_PATH_STORE.DATASOURCE_ID, DatasourcePathStore.DATASOURCE_PATH_STORE.PATH, DatasourcePathStore.DATASOURCE_PATH_STORE.STORE, DatasourcePathStore.DATASOURCE_PATH_STORE.TYPE }, true);
    public static final UniqueKey<DatasourceSelectedPathRecord> DATASOURCE_SELECTED_PATH_PK = Internal.createUniqueKey(DatasourceSelectedPath.DATASOURCE_SELECTED_PATH, DSL.name("datasource_selected_path_pk"), new TableField[] { DatasourceSelectedPath.DATASOURCE_SELECTED_PATH.DATASOURCE_ID, DatasourceSelectedPath.DATASOURCE_SELECTED_PATH.PATH }, true);
    public static final UniqueKey<DatasourceStoreRecord> DATASOURCE_STORE_PK = Internal.createUniqueKey(DatasourceStore.DATASOURCE_STORE, DSL.name("datasource_store_pk"), new TableField[] { DatasourceStore.DATASOURCE_STORE.DATASOURCE_ID, DatasourceStore.DATASOURCE_STORE.STORE, DatasourceStore.DATASOURCE_STORE.TYPE }, true);
    public static final UniqueKey<InternalMetadataRecord> INTERNAL_METADATA_PK = Internal.createUniqueKey(InternalMetadata.INTERNAL_METADATA, DSL.name("internal_metadata_pk"), new TableField[] { InternalMetadata.INTERNAL_METADATA.ID }, true);
    public static final UniqueKey<InternalSensorRecord> INTERNAL_SENSOR_PK = Internal.createUniqueKey(InternalSensor.INTERNAL_SENSOR, DSL.name("internal_sensor_pk"), new TableField[] { InternalSensor.INTERNAL_SENSOR.ID }, true);
    public static final UniqueKey<LayerRecord> LAYER_NAME_UQ = Internal.createUniqueKey(Layer.LAYER, DSL.name("layer_name_uq"), new TableField[] { Layer.LAYER.NAME, Layer.LAYER.NAMESPACE, Layer.LAYER.SERVICE, Layer.LAYER.ALIAS }, true);
    public static final UniqueKey<LayerRecord> LAYER_PK = Internal.createUniqueKey(Layer.LAYER, DSL.name("layer_pk"), new TableField[] { Layer.LAYER.ID }, true);
    public static final UniqueKey<LayerRecord> LAYER_UNIQUE_ALIAS = Internal.createUniqueKey(Layer.LAYER, DSL.name("layer_unique_alias"), new TableField[] { Layer.LAYER.SERVICE, Layer.LAYER.ALIAS }, true);
    public static final UniqueKey<MapcontextRecord> MAPCONTEXT_PK = Internal.createUniqueKey(Mapcontext.MAPCONTEXT, DSL.name("mapcontext_pk"), new TableField[] { Mapcontext.MAPCONTEXT.ID }, true);
    public static final UniqueKey<MapcontextStyledLayerRecord> MAPCONTEXT_STYLED_LAYER_PK = Internal.createUniqueKey(MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER, DSL.name("mapcontext_styled_layer_pk"), new TableField[] { MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER.ID }, true);
    public static final UniqueKey<MetadataRecord> METADATA_PK = Internal.createUniqueKey(Metadata.METADATA, DSL.name("metadata_pk"), new TableField[] { Metadata.METADATA.ID }, true);
    public static final UniqueKey<MetadataBboxRecord> METADATA_BBOX_PK = Internal.createUniqueKey(MetadataBbox.METADATA_BBOX, DSL.name("metadata_bbox_pk"), new TableField[] { MetadataBbox.METADATA_BBOX.METADATA_ID, MetadataBbox.METADATA_BBOX.EAST, MetadataBbox.METADATA_BBOX.WEST, MetadataBbox.METADATA_BBOX.NORTH, MetadataBbox.METADATA_BBOX.SOUTH }, true);
    public static final UniqueKey<MetadataXAttachmentRecord> METADATA_X_ATTACHMENT_PK = Internal.createUniqueKey(MetadataXAttachment.METADATA_X_ATTACHMENT, DSL.name("metadata_x_attachment_pk"), new TableField[] { MetadataXAttachment.METADATA_X_ATTACHMENT.ATTACHEMENT_ID, MetadataXAttachment.METADATA_X_ATTACHMENT.METADATA_ID }, true);
    public static final UniqueKey<MetadataXCswRecord> METADATA_X_CSW_PK = Internal.createUniqueKey(MetadataXCsw.METADATA_X_CSW, DSL.name("metadata_x_csw_pk"), new TableField[] { MetadataXCsw.METADATA_X_CSW.METADATA_ID, MetadataXCsw.METADATA_X_CSW.CSW_ID }, true);
    public static final UniqueKey<PermissionRecord> PERMISSION_PK = Internal.createUniqueKey(Permission.PERMISSION, DSL.name("permission_pk"), new TableField[] { Permission.PERMISSION.ID }, true);
    public static final UniqueKey<PropertyRecord> PROPERTY_PK = Internal.createUniqueKey(Property.PROPERTY, DSL.name("property_pk"), new TableField[] { Property.PROPERTY.NAME }, true);
    public static final UniqueKey<ProviderRecord> PROVIDER_PK = Internal.createUniqueKey(Provider.PROVIDER, DSL.name("provider_pk"), new TableField[] { Provider.PROVIDER.ID }, true);
    public static final UniqueKey<ProviderRecord> SQL140711122144190 = Internal.createUniqueKey(Provider.PROVIDER, DSL.name("sql140711122144190"), new TableField[] { Provider.PROVIDER.IDENTIFIER }, true);
    public static final UniqueKey<ProviderXCswRecord> PROVIDER_CSW_CROSS_ID_PK = Internal.createUniqueKey(ProviderXCsw.PROVIDER_X_CSW, DSL.name("provider_csw_cross_id_pk"), new TableField[] { ProviderXCsw.PROVIDER_X_CSW.CSW_ID, ProviderXCsw.PROVIDER_X_CSW.PROVIDER_ID }, true);
    public static final UniqueKey<RoleRecord> ROLE_PK = Internal.createUniqueKey(Role.ROLE, DSL.name("role_pk"), new TableField[] { Role.ROLE.NAME }, true);
    public static final UniqueKey<SensorRecord> SENSOR_ID_UQ = Internal.createUniqueKey(Sensor.SENSOR, DSL.name("sensor_id_uq"), new TableField[] { Sensor.SENSOR.IDENTIFIER }, true);
    public static final UniqueKey<SensorRecord> SENSOR_PK = Internal.createUniqueKey(Sensor.SENSOR, DSL.name("sensor_pk"), new TableField[] { Sensor.SENSOR.ID }, true);
    public static final UniqueKey<SensorXSosRecord> SENSOR_X_SOS_PK = Internal.createUniqueKey(SensorXSos.SENSOR_X_SOS, DSL.name("sensor_x_sos_pk"), new TableField[] { SensorXSos.SENSOR_X_SOS.SENSOR_ID, SensorXSos.SENSOR_X_SOS.SOS_ID }, true);
    public static final UniqueKey<SensoredDataRecord> SENSOR_DATA_PK = Internal.createUniqueKey(SensoredData.SENSORED_DATA, DSL.name("sensor_data_pk"), new TableField[] { SensoredData.SENSORED_DATA.SENSOR, SensoredData.SENSORED_DATA.DATA }, true);
    public static final UniqueKey<ServiceRecord> SERVICE_PK = Internal.createUniqueKey(Service.SERVICE, DSL.name("service_pk"), new TableField[] { Service.SERVICE.ID }, true);
    public static final UniqueKey<ServiceRecord> SERVICE_UQ = Internal.createUniqueKey(Service.SERVICE, DSL.name("service_uq"), new TableField[] { Service.SERVICE.IDENTIFIER, Service.SERVICE.TYPE }, true);
    public static final UniqueKey<ServiceDetailsRecord> SERVICE_DETAILS_PK = Internal.createUniqueKey(ServiceDetails.SERVICE_DETAILS, DSL.name("service_details_pk"), new TableField[] { ServiceDetails.SERVICE_DETAILS.ID, ServiceDetails.SERVICE_DETAILS.LANG }, true);
    public static final UniqueKey<ServiceExtraConfigRecord> SERVICE_EXTRA_CONFIG_PK = Internal.createUniqueKey(ServiceExtraConfig.SERVICE_EXTRA_CONFIG, DSL.name("service_extra_config_pk"), new TableField[] { ServiceExtraConfig.SERVICE_EXTRA_CONFIG.ID, ServiceExtraConfig.SERVICE_EXTRA_CONFIG.FILENAME }, true);
    public static final UniqueKey<StyleRecord> STYLE_NAME_PROVIDER_UQ = Internal.createUniqueKey(Style.STYLE, DSL.name("style_name_provider_uq"), new TableField[] { Style.STYLE.NAME, Style.STYLE.PROVIDER }, true);
    public static final UniqueKey<StyleRecord> STYLE_PK = Internal.createUniqueKey(Style.STYLE, DSL.name("style_pk"), new TableField[] { Style.STYLE.ID }, true);
    public static final UniqueKey<StyledDataRecord> STYLED_DATA_PK = Internal.createUniqueKey(StyledData.STYLED_DATA, DSL.name("styled_data_pk"), new TableField[] { StyledData.STYLED_DATA.STYLE, StyledData.STYLED_DATA.DATA }, true);
    public static final UniqueKey<StyledLayerRecord> STYLED_LAYER_PK = Internal.createUniqueKey(StyledLayer.STYLED_LAYER, DSL.name("styled_layer_pk"), new TableField[] { StyledLayer.STYLED_LAYER.STYLE, StyledLayer.STYLED_LAYER.LAYER }, true);
    public static final UniqueKey<TaskRecord> TASK_PK = Internal.createUniqueKey(Task.TASK, DSL.name("task_pk"), new TableField[] { Task.TASK.IDENTIFIER }, true);
    public static final UniqueKey<TaskParameterRecord> TASK_PARAMETER_PK = Internal.createUniqueKey(TaskParameter.TASK_PARAMETER, DSL.name("task_parameter_pk"), new TableField[] { TaskParameter.TASK_PARAMETER.ID }, true);
    public static final UniqueKey<ThesaurusRecord> THESAURUS_PK = Internal.createUniqueKey(Thesaurus.THESAURUS, DSL.name("thesaurus_pk"), new TableField[] { Thesaurus.THESAURUS.ID }, true);
    public static final UniqueKey<ThesaurusLanguageRecord> THESAURUS_LANGUAGE_PK = Internal.createUniqueKey(ThesaurusLanguage.THESAURUS_LANGUAGE, DSL.name("thesaurus_language_pk"), new TableField[] { ThesaurusLanguage.THESAURUS_LANGUAGE.THESAURUS_ID, ThesaurusLanguage.THESAURUS_LANGUAGE.LANGUAGE }, true);
    public static final UniqueKey<UserXRoleRecord> USER_X_ROLE_PK = Internal.createUniqueKey(UserXRole.USER_X_ROLE, DSL.name("user_x_role_pk"), new TableField[] { UserXRole.USER_X_ROLE.USER_ID, UserXRole.USER_X_ROLE.ROLE }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<CrsRecord, DataRecord> CRS__CRS_DATAID_FK = Internal.createForeignKey(Crs.CRS, DSL.name("crs_dataid_fk"), new TableField[] { Crs.CRS.DATAID }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<DataRecord, DatasetRecord> DATA__DATA_DATASET_ID_FK = Internal.createForeignKey(Data.DATA, DSL.name("data_dataset_id_fk"), new TableField[] { Data.DATA.DATASET_ID }, Keys.DATASET_PK, new TableField[] { Dataset.DATASET.ID }, true);
    public static final ForeignKey<DataRecord, CstlUserRecord> DATA__DATA_OWNER_FK = Internal.createForeignKey(Data.DATA, DSL.name("data_owner_fk"), new TableField[] { Data.DATA.OWNER }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final ForeignKey<DataRecord, ProviderRecord> DATA__DATA_PROVIDER_FK = Internal.createForeignKey(Data.DATA, DSL.name("data_provider_fk"), new TableField[] { Data.DATA.PROVIDER }, Keys.PROVIDER_PK, new TableField[] { Provider.PROVIDER.ID }, true);
    public static final ForeignKey<DataDimRangeRecord, DataRecord> DATA_DIM_RANGE__DATA_DIM_RANGE_DATA_FK = Internal.createForeignKey(DataDimRange.DATA_DIM_RANGE, DSL.name("data_dim_range_data_fk"), new TableField[] { DataDimRange.DATA_DIM_RANGE.DATA_ID }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<DataElevationsRecord, DataRecord> DATA_ELEVATIONS__DATA_ELEVATIONS_DATA_FK = Internal.createForeignKey(DataElevations.DATA_ELEVATIONS, DSL.name("data_elevations_data_fk"), new TableField[] { DataElevations.DATA_ELEVATIONS.DATA_ID }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<DataEnvelopeRecord, DataRecord> DATA_ENVELOPE__DATA_ENVELOPE_DATA_FK = Internal.createForeignKey(DataEnvelope.DATA_ENVELOPE, DSL.name("data_envelope_data_fk"), new TableField[] { DataEnvelope.DATA_ENVELOPE.DATA_ID }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<DataTimesRecord, DataRecord> DATA_TIMES__DATA_TIMES_DATA_FK = Internal.createForeignKey(DataTimes.DATA_TIMES, DSL.name("data_times_data_fk"), new TableField[] { DataTimes.DATA_TIMES.DATA_ID }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<DataXDataRecord, DataRecord> DATA_X_DATA__DATA_X_DATA_CROSS_ID_FK = Internal.createForeignKey(DataXData.DATA_X_DATA, DSL.name("data_x_data_cross_id_fk"), new TableField[] { DataXData.DATA_X_DATA.DATA_ID }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<DataXDataRecord, DataRecord> DATA_X_DATA__DATA_X_DATA_CROSS_ID_FK2 = Internal.createForeignKey(DataXData.DATA_X_DATA, DSL.name("data_x_data_cross_id_fk2"), new TableField[] { DataXData.DATA_X_DATA.CHILD_ID }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<DatasetRecord, CstlUserRecord> DATASET__DATASET_OWNER_FK = Internal.createForeignKey(Dataset.DATASET, DSL.name("dataset_owner_fk"), new TableField[] { Dataset.DATASET.OWNER }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final ForeignKey<DatasourcePathRecord, DatasourceRecord> DATASOURCE_PATH__DATASOURCE_PATH_DATASOURCE_ID_FK = Internal.createForeignKey(DatasourcePath.DATASOURCE_PATH, DSL.name("datasource_path_datasource_id_fk"), new TableField[] { DatasourcePath.DATASOURCE_PATH.DATASOURCE_ID }, Keys.DATASOURCE_PK, new TableField[] { Datasource.DATASOURCE.ID }, true);
    public static final ForeignKey<DatasourcePathStoreRecord, DatasourcePathRecord> DATASOURCE_PATH_STORE__DATASOURCE_PATH_STORE_PATH_FK = Internal.createForeignKey(DatasourcePathStore.DATASOURCE_PATH_STORE, DSL.name("datasource_path_store_path_fk"), new TableField[] { DatasourcePathStore.DATASOURCE_PATH_STORE.DATASOURCE_ID, DatasourcePathStore.DATASOURCE_PATH_STORE.PATH }, Keys.DATASOURCE_PATH_PK, new TableField[] { DatasourcePath.DATASOURCE_PATH.DATASOURCE_ID, DatasourcePath.DATASOURCE_PATH.PATH }, true);
    public static final ForeignKey<DatasourceSelectedPathRecord, DatasourceRecord> DATASOURCE_SELECTED_PATH__DATASOURCE_SELECTED_PATH_DATASOURCE_ID_FK = Internal.createForeignKey(DatasourceSelectedPath.DATASOURCE_SELECTED_PATH, DSL.name("datasource_selected_path_datasource_id_fk"), new TableField[] { DatasourceSelectedPath.DATASOURCE_SELECTED_PATH.DATASOURCE_ID }, Keys.DATASOURCE_PK, new TableField[] { Datasource.DATASOURCE.ID }, true);
    public static final ForeignKey<DatasourceStoreRecord, DatasourceRecord> DATASOURCE_STORE__DATASOURCE_STORE_DATASOURCE_ID_FK = Internal.createForeignKey(DatasourceStore.DATASOURCE_STORE, DSL.name("datasource_store_datasource_id_fk"), new TableField[] { DatasourceStore.DATASOURCE_STORE.DATASOURCE_ID }, Keys.DATASOURCE_PK, new TableField[] { Datasource.DATASOURCE.ID }, true);
    public static final ForeignKey<InternalMetadataRecord, MetadataRecord> INTERNAL_METADATA__INTERNAL_METADATA_ID_FK = Internal.createForeignKey(InternalMetadata.INTERNAL_METADATA, DSL.name("internal_metadata_id_fk"), new TableField[] { InternalMetadata.INTERNAL_METADATA.ID }, Keys.METADATA_PK, new TableField[] { Metadata.METADATA.ID }, true);
    public static final ForeignKey<InternalSensorRecord, SensorRecord> INTERNAL_SENSOR__INTERNAL_SENSOR_ID_FK = Internal.createForeignKey(InternalSensor.INTERNAL_SENSOR, DSL.name("internal_sensor_id_fk"), new TableField[] { InternalSensor.INTERNAL_SENSOR.ID }, Keys.SENSOR_PK, new TableField[] { Sensor.SENSOR.ID }, true);
    public static final ForeignKey<LayerRecord, DataRecord> LAYER__LAYER_DATA_FK = Internal.createForeignKey(Layer.LAYER, DSL.name("layer_data_fk"), new TableField[] { Layer.LAYER.DATA }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<LayerRecord, CstlUserRecord> LAYER__LAYER_OWNER_FK = Internal.createForeignKey(Layer.LAYER, DSL.name("layer_owner_fk"), new TableField[] { Layer.LAYER.OWNER }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final ForeignKey<LayerRecord, ServiceRecord> LAYER__LAYER_SERVICE_FK = Internal.createForeignKey(Layer.LAYER, DSL.name("layer_service_fk"), new TableField[] { Layer.LAYER.SERVICE }, Keys.SERVICE_PK, new TableField[] { Service.SERVICE.ID }, true);
    public static final ForeignKey<MapcontextRecord, CstlUserRecord> MAPCONTEXT__MAPCONTEXT_OWNER_FK = Internal.createForeignKey(Mapcontext.MAPCONTEXT, DSL.name("mapcontext_owner_fk"), new TableField[] { Mapcontext.MAPCONTEXT.OWNER }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final ForeignKey<MapcontextStyledLayerRecord, DataRecord> MAPCONTEXT_STYLED_LAYER__MAPCONTEXT_STYLED_LAYER_DATA_ID_FK = Internal.createForeignKey(MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER, DSL.name("mapcontext_styled_layer_data_id_fk"), new TableField[] { MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER.DATA_ID }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<MapcontextStyledLayerRecord, LayerRecord> MAPCONTEXT_STYLED_LAYER__MAPCONTEXT_STYLED_LAYER_LAYER_ID_FK = Internal.createForeignKey(MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER, DSL.name("mapcontext_styled_layer_layer_id_fk"), new TableField[] { MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER.LAYER_ID }, Keys.LAYER_PK, new TableField[] { Layer.LAYER.ID }, true);
    public static final ForeignKey<MapcontextStyledLayerRecord, MapcontextRecord> MAPCONTEXT_STYLED_LAYER__MAPCONTEXT_STYLED_LAYER_MAPCONTEXT_ID_FK = Internal.createForeignKey(MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER, DSL.name("mapcontext_styled_layer_mapcontext_id_fk"), new TableField[] { MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER.MAPCONTEXT_ID }, Keys.MAPCONTEXT_PK, new TableField[] { Mapcontext.MAPCONTEXT.ID }, true);
    public static final ForeignKey<MapcontextStyledLayerRecord, StyleRecord> MAPCONTEXT_STYLED_LAYER__MAPCONTEXT_STYLED_LAYER_STYLE_ID_FK = Internal.createForeignKey(MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER, DSL.name("mapcontext_styled_layer_style_id_fk"), new TableField[] { MapcontextStyledLayer.MAPCONTEXT_STYLED_LAYER.STYLE_ID }, Keys.STYLE_PK, new TableField[] { Style.STYLE.ID }, true);
    public static final ForeignKey<MetadataRecord, MapcontextRecord> METADATA__MAP_CONTEXT_ID_FK = Internal.createForeignKey(Metadata.METADATA, DSL.name("map_context_id_fk"), new TableField[] { Metadata.METADATA.MAP_CONTEXT_ID }, Keys.MAPCONTEXT_PK, new TableField[] { Mapcontext.MAPCONTEXT.ID }, true);
    public static final ForeignKey<MetadataRecord, DataRecord> METADATA__METADATA_DATA_FK = Internal.createForeignKey(Metadata.METADATA, DSL.name("metadata_data_fk"), new TableField[] { Metadata.METADATA.DATA_ID }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<MetadataRecord, DatasetRecord> METADATA__METADATA_DATASET_FK = Internal.createForeignKey(Metadata.METADATA, DSL.name("metadata_dataset_fk"), new TableField[] { Metadata.METADATA.DATASET_ID }, Keys.DATASET_PK, new TableField[] { Dataset.DATASET.ID }, true);
    public static final ForeignKey<MetadataRecord, CstlUserRecord> METADATA__METADATA_OWNER_FK = Internal.createForeignKey(Metadata.METADATA, DSL.name("metadata_owner_fk"), new TableField[] { Metadata.METADATA.OWNER }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final ForeignKey<MetadataRecord, ProviderRecord> METADATA__METADATA_PROVIDER_ID_FK = Internal.createForeignKey(Metadata.METADATA, DSL.name("metadata_provider_id_fk"), new TableField[] { Metadata.METADATA.PROVIDER_ID }, Keys.PROVIDER_PK, new TableField[] { Provider.PROVIDER.ID }, true);
    public static final ForeignKey<MetadataRecord, ServiceRecord> METADATA__METADATA_SERVICE_FK = Internal.createForeignKey(Metadata.METADATA, DSL.name("metadata_service_fk"), new TableField[] { Metadata.METADATA.SERVICE_ID }, Keys.SERVICE_PK, new TableField[] { Service.SERVICE.ID }, true);
    public static final ForeignKey<MetadataBboxRecord, MetadataRecord> METADATA_BBOX__BBOX_METADATA_FK = Internal.createForeignKey(MetadataBbox.METADATA_BBOX, DSL.name("bbox_metadata_fk"), new TableField[] { MetadataBbox.METADATA_BBOX.METADATA_ID }, Keys.METADATA_PK, new TableField[] { Metadata.METADATA.ID }, true);
    public static final ForeignKey<MetadataXAttachmentRecord, MetadataRecord> METADATA_X_ATTACHMENT__ATTACHMENT_METADATA_CROSS_ID_FK = Internal.createForeignKey(MetadataXAttachment.METADATA_X_ATTACHMENT, DSL.name("attachment_metadata_cross_id_fk"), new TableField[] { MetadataXAttachment.METADATA_X_ATTACHMENT.METADATA_ID }, Keys.METADATA_PK, new TableField[] { Metadata.METADATA.ID }, true);
    public static final ForeignKey<MetadataXAttachmentRecord, AttachmentRecord> METADATA_X_ATTACHMENT__METADATA_ATTACHMENT_CROSS_ID_FK = Internal.createForeignKey(MetadataXAttachment.METADATA_X_ATTACHMENT, DSL.name("metadata_attachment_cross_id_fk"), new TableField[] { MetadataXAttachment.METADATA_X_ATTACHMENT.ATTACHEMENT_ID }, Keys.ATTACHMENT_PK, new TableField[] { Attachment.ATTACHMENT.ID }, true);
    public static final ForeignKey<MetadataXCswRecord, ServiceRecord> METADATA_X_CSW__CSW_METADATA_CROSS_ID_FK = Internal.createForeignKey(MetadataXCsw.METADATA_X_CSW, DSL.name("csw_metadata_cross_id_fk"), new TableField[] { MetadataXCsw.METADATA_X_CSW.CSW_ID }, Keys.SERVICE_PK, new TableField[] { Service.SERVICE.ID }, true);
    public static final ForeignKey<MetadataXCswRecord, MetadataRecord> METADATA_X_CSW__METADATA_CSW_CROSS_ID_FK = Internal.createForeignKey(MetadataXCsw.METADATA_X_CSW, DSL.name("metadata_csw_cross_id_fk"), new TableField[] { MetadataXCsw.METADATA_X_CSW.METADATA_ID }, Keys.METADATA_PK, new TableField[] { Metadata.METADATA.ID }, true);
    public static final ForeignKey<ProviderRecord, CstlUserRecord> PROVIDER__PROVIDER_OWNER_FK = Internal.createForeignKey(Provider.PROVIDER, DSL.name("provider_owner_fk"), new TableField[] { Provider.PROVIDER.OWNER }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final ForeignKey<ProviderXCswRecord, ServiceRecord> PROVIDER_X_CSW__CSW_PROVIDER_CROSS_ID_FK = Internal.createForeignKey(ProviderXCsw.PROVIDER_X_CSW, DSL.name("csw_provider_cross_id_fk"), new TableField[] { ProviderXCsw.PROVIDER_X_CSW.CSW_ID }, Keys.SERVICE_PK, new TableField[] { Service.SERVICE.ID }, true);
    public static final ForeignKey<ProviderXCswRecord, ProviderRecord> PROVIDER_X_CSW__PROVIDER_CSW_CROSS_ID_FK = Internal.createForeignKey(ProviderXCsw.PROVIDER_X_CSW, DSL.name("provider_csw_cross_id_fk"), new TableField[] { ProviderXCsw.PROVIDER_X_CSW.PROVIDER_ID }, Keys.PROVIDER_PK, new TableField[] { Provider.PROVIDER.ID }, true);
    public static final ForeignKey<ProviderXSosRecord, ProviderRecord> PROVIDER_X_SOS__PROVIDER_SOS_CROSS_ID_FK = Internal.createForeignKey(ProviderXSos.PROVIDER_X_SOS, DSL.name("provider_sos_cross_id_fk"), new TableField[] { ProviderXSos.PROVIDER_X_SOS.PROVIDER_ID }, Keys.PROVIDER_PK, new TableField[] { Provider.PROVIDER.ID }, true);
    public static final ForeignKey<ProviderXSosRecord, ServiceRecord> PROVIDER_X_SOS__SOS_PROVIDER_CROSS_ID_FK = Internal.createForeignKey(ProviderXSos.PROVIDER_X_SOS, DSL.name("sos_provider_cross_id_fk"), new TableField[] { ProviderXSos.PROVIDER_X_SOS.SOS_ID }, Keys.SERVICE_PK, new TableField[] { Service.SERVICE.ID }, true);
    public static final ForeignKey<SensorRecord, CstlUserRecord> SENSOR__SENSOR_OWNER_FK = Internal.createForeignKey(Sensor.SENSOR, DSL.name("sensor_owner_fk"), new TableField[] { Sensor.SENSOR.OWNER }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final ForeignKey<SensorRecord, SensorRecord> SENSOR__SENSOR_PARENT_FK = Internal.createForeignKey(Sensor.SENSOR, DSL.name("sensor_parent_fk"), new TableField[] { Sensor.SENSOR.PARENT }, Keys.SENSOR_ID_UQ, new TableField[] { Sensor.SENSOR.IDENTIFIER }, true);
    public static final ForeignKey<SensorRecord, ProviderRecord> SENSOR__SENSOR_PROVIDER_ID_FK = Internal.createForeignKey(Sensor.SENSOR, DSL.name("sensor_provider_id_fk"), new TableField[] { Sensor.SENSOR.PROVIDER_ID }, Keys.PROVIDER_PK, new TableField[] { Provider.PROVIDER.ID }, true);
    public static final ForeignKey<SensorXSosRecord, SensorRecord> SENSOR_X_SOS__SENSOR_SOS_CROSS_ID_FK = Internal.createForeignKey(SensorXSos.SENSOR_X_SOS, DSL.name("sensor_sos_cross_id_fk"), new TableField[] { SensorXSos.SENSOR_X_SOS.SENSOR_ID }, Keys.SENSOR_PK, new TableField[] { Sensor.SENSOR.ID }, true);
    public static final ForeignKey<SensorXSosRecord, ServiceRecord> SENSOR_X_SOS__SOS_SENSOR_CROSS_ID_FK = Internal.createForeignKey(SensorXSos.SENSOR_X_SOS, DSL.name("sos_sensor_cross_id_fk"), new TableField[] { SensorXSos.SENSOR_X_SOS.SOS_ID }, Keys.SERVICE_PK, new TableField[] { Service.SERVICE.ID }, true);
    public static final ForeignKey<SensoredDataRecord, DataRecord> SENSORED_DATA__SENSORED_DATA_DATA_FK = Internal.createForeignKey(SensoredData.SENSORED_DATA, DSL.name("sensored_data_data_fk"), new TableField[] { SensoredData.SENSORED_DATA.DATA }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<SensoredDataRecord, SensorRecord> SENSORED_DATA__SENSORED_DATA_SENSOR_FK = Internal.createForeignKey(SensoredData.SENSORED_DATA, DSL.name("sensored_data_sensor_fk"), new TableField[] { SensoredData.SENSORED_DATA.SENSOR }, Keys.SENSOR_PK, new TableField[] { Sensor.SENSOR.ID }, true);
    public static final ForeignKey<ServiceRecord, CstlUserRecord> SERVICE__SERVICE_OWNER_FK = Internal.createForeignKey(Service.SERVICE, DSL.name("service_owner_fk"), new TableField[] { Service.SERVICE.OWNER }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final ForeignKey<ServiceDetailsRecord, ServiceRecord> SERVICE_DETAILS__SERVICE_DETAILS_SERVICE_ID_FK = Internal.createForeignKey(ServiceDetails.SERVICE_DETAILS, DSL.name("service_details_service_id_fk"), new TableField[] { ServiceDetails.SERVICE_DETAILS.ID }, Keys.SERVICE_PK, new TableField[] { Service.SERVICE.ID }, true);
    public static final ForeignKey<ServiceExtraConfigRecord, ServiceRecord> SERVICE_EXTRA_CONFIG__SERVICE_EXTRA_CONFIG_SERVICE_ID_FK = Internal.createForeignKey(ServiceExtraConfig.SERVICE_EXTRA_CONFIG, DSL.name("service_extra_config_service_id_fk"), new TableField[] { ServiceExtraConfig.SERVICE_EXTRA_CONFIG.ID }, Keys.SERVICE_PK, new TableField[] { Service.SERVICE.ID }, true);
    public static final ForeignKey<StyleRecord, CstlUserRecord> STYLE__STYLE_OWNER_FK = Internal.createForeignKey(Style.STYLE, DSL.name("style_owner_fk"), new TableField[] { Style.STYLE.OWNER }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final ForeignKey<StyledDataRecord, DataRecord> STYLED_DATA__STYLED_DATA_DATA_FK = Internal.createForeignKey(StyledData.STYLED_DATA, DSL.name("styled_data_data_fk"), new TableField[] { StyledData.STYLED_DATA.DATA }, Keys.DATA_PK, new TableField[] { Data.DATA.ID }, true);
    public static final ForeignKey<StyledDataRecord, StyleRecord> STYLED_DATA__STYLED_DATA_STYLE_FK = Internal.createForeignKey(StyledData.STYLED_DATA, DSL.name("styled_data_style_fk"), new TableField[] { StyledData.STYLED_DATA.STYLE }, Keys.STYLE_PK, new TableField[] { Style.STYLE.ID }, true);
    public static final ForeignKey<StyledLayerRecord, LayerRecord> STYLED_LAYER__STYLED_LAYER_LAYER_FK = Internal.createForeignKey(StyledLayer.STYLED_LAYER, DSL.name("styled_layer_layer_fk"), new TableField[] { StyledLayer.STYLED_LAYER.LAYER }, Keys.LAYER_PK, new TableField[] { Layer.LAYER.ID }, true);
    public static final ForeignKey<StyledLayerRecord, StyleRecord> STYLED_LAYER__STYLED_LAYER_STYLE_FK = Internal.createForeignKey(StyledLayer.STYLED_LAYER, DSL.name("styled_layer_style_fk"), new TableField[] { StyledLayer.STYLED_LAYER.STYLE }, Keys.STYLE_PK, new TableField[] { Style.STYLE.ID }, true);
    public static final ForeignKey<TaskRecord, CstlUserRecord> TASK__TASK_OWNER_FK = Internal.createForeignKey(Task.TASK, DSL.name("task_owner_fk"), new TableField[] { Task.TASK.OWNER }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
    public static final ForeignKey<TaskRecord, TaskParameterRecord> TASK__TASK_TASK_PARAMETER_ID_FK = Internal.createForeignKey(Task.TASK, DSL.name("task_task_parameter_id_fk"), new TableField[] { Task.TASK.TASK_PARAMETER_ID }, Keys.TASK_PARAMETER_PK, new TableField[] { TaskParameter.TASK_PARAMETER.ID }, true);
    public static final ForeignKey<ThesaurusLanguageRecord, ThesaurusRecord> THESAURUS_LANGUAGE__THESAURUS_LANGUAGE_FK = Internal.createForeignKey(ThesaurusLanguage.THESAURUS_LANGUAGE, DSL.name("thesaurus_language_fk"), new TableField[] { ThesaurusLanguage.THESAURUS_LANGUAGE.THESAURUS_ID }, Keys.THESAURUS_PK, new TableField[] { Thesaurus.THESAURUS.ID }, true);
    public static final ForeignKey<ThesaurusXServiceRecord, ServiceRecord> THESAURUS_X_SERVICE__SERVICE_THESAURUS_CROSS_ID_FK = Internal.createForeignKey(ThesaurusXService.THESAURUS_X_SERVICE, DSL.name("service_thesaurus_cross_id_fk"), new TableField[] { ThesaurusXService.THESAURUS_X_SERVICE.SERVICE_ID }, Keys.SERVICE_PK, new TableField[] { Service.SERVICE.ID }, true);
    public static final ForeignKey<ThesaurusXServiceRecord, ThesaurusRecord> THESAURUS_X_SERVICE__THESAURUS_SERVICE_CROSS_ID_FK = Internal.createForeignKey(ThesaurusXService.THESAURUS_X_SERVICE, DSL.name("thesaurus_service_cross_id_fk"), new TableField[] { ThesaurusXService.THESAURUS_X_SERVICE.THESAURUS_ID }, Keys.THESAURUS_PK, new TableField[] { Thesaurus.THESAURUS.ID }, true);
    public static final ForeignKey<UserXRoleRecord, RoleRecord> USER_X_ROLE__USER_X_ROLE_ROLE_FK = Internal.createForeignKey(UserXRole.USER_X_ROLE, DSL.name("user_x_role_role_fk"), new TableField[] { UserXRole.USER_X_ROLE.ROLE }, Keys.ROLE_PK, new TableField[] { Role.ROLE.NAME }, true);
    public static final ForeignKey<UserXRoleRecord, CstlUserRecord> USER_X_ROLE__USER_X_ROLE_USER_ID_FK = Internal.createForeignKey(UserXRole.USER_X_ROLE, DSL.name("user_x_role_user_id_fk"), new TableField[] { UserXRole.USER_X_ROLE.USER_ID }, Keys.USER_PK, new TableField[] { CstlUser.CSTL_USER.ID }, true);
}
