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

package org.constellation.dto.service;

import java.util.Arrays;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Objects;

/**
 * @author Guilhem Legal (Geomatys)
 * @author Benjamin Garcia (Geomatys)
 */
@XmlRootElement(name ="Instance")
@XmlAccessorType(XmlAccessType.FIELD)
public class Instance {

    @XmlAttribute
    private int id;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String identifier;

    @XmlAttribute
    private String type;

    @XmlAttribute
    private ServiceStatus status;

    @XmlAttribute
    private String _abstract;

    @XmlAttribute
    private Integer layersNumber;

    @XmlAttribute
    private List<String> versions;

    @XmlAttribute
    private String baseUrl;

    public Instance() {

    }

     public Instance(final ServiceComplete service) {
         if (service != null) {
            this.id         = service.getId();
            this.identifier = service.getIdentifier();
            this.type       = service.getType();
            this.status     = ServiceStatus.valueOf(service.getStatus());
            this._abstract  = service.getDescription();
            this.name       = service.getTitle();
            if (service.getVersions() != null) {
                this.versions   = Arrays.asList(service.getVersions().split("µ"));
            }
         }
    }

    public Instance(final int id, final String identifier, final String type, final ServiceStatus status) {
        this.id         = id;
        this.identifier = identifier;
        this.type       = type;
        this.status     = status;
    }

    public Instance(final int id, final String identifier, final String name, final String _abstract, final String type,
            final List<String> versions, final Integer layerNumber, final ServiceStatus status, String baseUrl) {
        this(id, identifier, type, status);
        this.name         = name;
        this._abstract    = _abstract;
        this.versions     = versions;
        this.layersNumber = layerNumber;
        this.baseUrl      = baseUrl;
    }

    /**
     * Set id;
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Return the id;
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void setName(final String newName) {
        this.name = newName;
    }

    /**
     * @return the status
     */
    public ServiceStatus getStatus() {
        return status;
    }

    public void setStatus(ServiceStatus status) {
        this.status = status;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    public String get_abstract() {
        return _abstract;
    }

    public void set_abstract(String _abstract) {
        this._abstract = _abstract;
    }

    public Integer getLayersNumber() {
        return layersNumber;
    }

    public void setLayersNumber(Integer layersNumber) {
        this.layersNumber = layersNumber;
    }

    public void setVersions(final List<String> versions) {
        this.versions = versions;
    }

    public List<String> getVersions() {
        return versions;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() == obj.getClass()) {
            final Instance that = (Instance) obj;
            return Objects.equals(this.name,         that.name) &&
                   Objects.equals(this.identifier,   that.identifier) &&
                   Objects.equals(this.layersNumber, that.layersNumber) &&
                   Objects.equals(this._abstract,    that._abstract) &&
                   Objects.equals(this.versions,     that.versions) &&
                   Objects.equals(this.type,         that.type) &&
                   Objects.equals(this.baseUrl,      that.baseUrl) &&
                   Objects.equals(this.status,       that.status);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 73 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 73 * hash + (this.status != null ? this.status.hashCode() : 0);
        hash = 73 * hash + (this.layersNumber != null ? this.layersNumber.hashCode() : 0);
        hash = 73 * hash + (this._abstract != null ? this._abstract.hashCode() : 0);
        hash = 73 * hash + (this.identifier != null ? this.identifier.hashCode() : 0);
        hash = 73 * hash + (this.baseUrl != null ? this.baseUrl.hashCode() : 0);
        hash = 73 * hash + (this.versions != null ? this.versions.hashCode() : 0);
        return hash;
    }



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[Instance]\n");
        if (identifier != null) {
            sb.append("identifier:").append(identifier).append('\n');
        }
        if (name != null) {
            sb.append("name:").append(name).append('\n');
        }
        if (_abstract != null) {
            sb.append("_abstract:").append(_abstract).append('\n');
        }
        if (type != null) {
            sb.append("type:").append(type).append('\n');
        }
        if (versions != null) {
            sb.append("versions:\n");
            for (String version : versions) {
                sb.append("version:").append(version).append('\n');
            }
        }
        if (layersNumber != null) {
            sb.append("layersNumber:").append(layersNumber).append('\n');
        }
        if (status != null) {
            sb.append("status:").append(status).append('\n');
        }
        if (baseUrl != null) {
            sb.append("baseUrl:").append(baseUrl).append('\n');
        }
        return sb.toString();
    }
}
