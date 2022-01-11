/*
 * This file is generated by jOOQ.
 */
package org.constellation.database.api.jooq.tables.pojos;


import java.io.Serializable;

import javax.validation.constraints.NotNull;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class DatasourcePath implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer datasourceId;
    private String  path;
    private String  name;
    private Boolean folder;
    private String  parentPath;
    private Integer size;

    public DatasourcePath() {}

    public DatasourcePath(DatasourcePath value) {
        this.datasourceId = value.datasourceId;
        this.path = value.path;
        this.name = value.name;
        this.folder = value.folder;
        this.parentPath = value.parentPath;
        this.size = value.size;
    }

    public DatasourcePath(
        Integer datasourceId,
        String  path,
        String  name,
        Boolean folder,
        String  parentPath,
        Integer size
    ) {
        this.datasourceId = datasourceId;
        this.path = path;
        this.name = name;
        this.folder = folder;
        this.parentPath = parentPath;
        this.size = size;
    }

    /**
     * Getter for <code>admin.datasource_path.datasource_id</code>.
     */
    @NotNull
    public Integer getDatasourceId() {
        return this.datasourceId;
    }

    /**
     * Setter for <code>admin.datasource_path.datasource_id</code>.
     */
    public DatasourcePath setDatasourceId(Integer datasourceId) {
        this.datasourceId = datasourceId;
        return this;
    }

    /**
     * Getter for <code>admin.datasource_path.path</code>.
     */
    @NotNull
    public String getPath() {
        return this.path;
    }

    /**
     * Setter for <code>admin.datasource_path.path</code>.
     */
    public DatasourcePath setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Getter for <code>admin.datasource_path.name</code>.
     */
    @NotNull
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>admin.datasource_path.name</code>.
     */
    public DatasourcePath setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Getter for <code>admin.datasource_path.folder</code>.
     */
    @NotNull
    public Boolean getFolder() {
        return this.folder;
    }

    /**
     * Setter for <code>admin.datasource_path.folder</code>.
     */
    public DatasourcePath setFolder(Boolean folder) {
        this.folder = folder;
        return this;
    }

    /**
     * Getter for <code>admin.datasource_path.parent_path</code>.
     */
    public String getParentPath() {
        return this.parentPath;
    }

    /**
     * Setter for <code>admin.datasource_path.parent_path</code>.
     */
    public DatasourcePath setParentPath(String parentPath) {
        this.parentPath = parentPath;
        return this;
    }

    /**
     * Getter for <code>admin.datasource_path.size</code>.
     */
    @NotNull
    public Integer getSize() {
        return this.size;
    }

    /**
     * Setter for <code>admin.datasource_path.size</code>.
     */
    public DatasourcePath setSize(Integer size) {
        this.size = size;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DatasourcePath (");

        sb.append(datasourceId);
        sb.append(", ").append(path);
        sb.append(", ").append(name);
        sb.append(", ").append(folder);
        sb.append(", ").append(parentPath);
        sb.append(", ").append(size);

        sb.append(")");
        return sb.toString();
    }
}
