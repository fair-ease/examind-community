/*
 *     Examind Community - An open source and standard compliant SDI
 *     https://community.examind.com/
 * 
 *  Copyright 2022 Geomatys.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
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
package com.examind.database.api.jooq.tables.pojos;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;


/**
 * Generated DAO object for table admin.layer
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Layer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String namespace;
    private String alias;
    private Integer service;
    private Integer data;
    private Long date;
    private String config;
    private Integer owner;
    private String title;

    public Layer() {}

    public Layer(Layer value) {
        this.id = value.id;
        this.name = value.name;
        this.namespace = value.namespace;
        this.alias = value.alias;
        this.service = value.service;
        this.data = value.data;
        this.date = value.date;
        this.config = value.config;
        this.owner = value.owner;
        this.title = value.title;
    }

    public Layer(
        Integer id,
        String name,
        String namespace,
        String alias,
        Integer service,
        Integer data,
        Long date,
        String config,
        Integer owner,
        String title
    ) {
        this.id = id;
        this.name = name;
        this.namespace = namespace;
        this.alias = alias;
        this.service = service;
        this.data = data;
        this.date = date;
        this.config = config;
        this.owner = owner;
        this.title = title;
    }

    /**
     * Getter for <code>admin.layer.id</code>.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Setter for <code>admin.layer.id</code>.
     */
    public Layer setId(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>admin.layer.name</code>.
     */
    @NotNull
    @Size(max = 512)
    public String getName() {
        return this.name;
    }

    /**
     * Setter for <code>admin.layer.name</code>.
     */
    public Layer setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Getter for <code>admin.layer.namespace</code>.
     */
    @Size(max = 256)
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Setter for <code>admin.layer.namespace</code>.
     */
    public Layer setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    /**
     * Getter for <code>admin.layer.alias</code>.
     */
    @Size(max = 512)
    public String getAlias() {
        return this.alias;
    }

    /**
     * Setter for <code>admin.layer.alias</code>.
     */
    public Layer setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * Getter for <code>admin.layer.service</code>.
     */
    @NotNull
    public Integer getService() {
        return this.service;
    }

    /**
     * Setter for <code>admin.layer.service</code>.
     */
    public Layer setService(Integer service) {
        this.service = service;
        return this;
    }

    /**
     * Getter for <code>admin.layer.data</code>.
     */
    @NotNull
    public Integer getData() {
        return this.data;
    }

    /**
     * Setter for <code>admin.layer.data</code>.
     */
    public Layer setData(Integer data) {
        this.data = data;
        return this;
    }

    /**
     * Getter for <code>admin.layer.date</code>.
     */
    @NotNull
    public Long getDate() {
        return this.date;
    }

    /**
     * Setter for <code>admin.layer.date</code>.
     */
    public Layer setDate(Long date) {
        this.date = date;
        return this;
    }

    /**
     * Getter for <code>admin.layer.config</code>.
     */
    public String getConfig() {
        return this.config;
    }

    /**
     * Setter for <code>admin.layer.config</code>.
     */
    public Layer setConfig(String config) {
        this.config = config;
        return this;
    }

    /**
     * Getter for <code>admin.layer.owner</code>.
     */
    public Integer getOwner() {
        return this.owner;
    }

    /**
     * Setter for <code>admin.layer.owner</code>.
     */
    public Layer setOwner(Integer owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Getter for <code>admin.layer.title</code>.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for <code>admin.layer.title</code>.
     */
    public Layer setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Layer other = (Layer) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.name == null) {
            if (other.name != null)
                return false;
        }
        else if (!this.name.equals(other.name))
            return false;
        if (this.namespace == null) {
            if (other.namespace != null)
                return false;
        }
        else if (!this.namespace.equals(other.namespace))
            return false;
        if (this.alias == null) {
            if (other.alias != null)
                return false;
        }
        else if (!this.alias.equals(other.alias))
            return false;
        if (this.service == null) {
            if (other.service != null)
                return false;
        }
        else if (!this.service.equals(other.service))
            return false;
        if (this.data == null) {
            if (other.data != null)
                return false;
        }
        else if (!this.data.equals(other.data))
            return false;
        if (this.date == null) {
            if (other.date != null)
                return false;
        }
        else if (!this.date.equals(other.date))
            return false;
        if (this.config == null) {
            if (other.config != null)
                return false;
        }
        else if (!this.config.equals(other.config))
            return false;
        if (this.owner == null) {
            if (other.owner != null)
                return false;
        }
        else if (!this.owner.equals(other.owner))
            return false;
        if (this.title == null) {
            if (other.title != null)
                return false;
        }
        else if (!this.title.equals(other.title))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.namespace == null) ? 0 : this.namespace.hashCode());
        result = prime * result + ((this.alias == null) ? 0 : this.alias.hashCode());
        result = prime * result + ((this.service == null) ? 0 : this.service.hashCode());
        result = prime * result + ((this.data == null) ? 0 : this.data.hashCode());
        result = prime * result + ((this.date == null) ? 0 : this.date.hashCode());
        result = prime * result + ((this.config == null) ? 0 : this.config.hashCode());
        result = prime * result + ((this.owner == null) ? 0 : this.owner.hashCode());
        result = prime * result + ((this.title == null) ? 0 : this.title.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Layer (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(namespace);
        sb.append(", ").append(alias);
        sb.append(", ").append(service);
        sb.append(", ").append(data);
        sb.append(", ").append(date);
        sb.append(", ").append(config);
        sb.append(", ").append(owner);
        sb.append(", ").append(title);

        sb.append(")");
        return sb.toString();
    }
}
