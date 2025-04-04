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
 * Generated DAO object for table admin.service_details
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class ServiceDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String lang;
    private String content;
    private Boolean defaultLang;

    public ServiceDetails() {}

    public ServiceDetails(ServiceDetails value) {
        this.id = value.id;
        this.lang = value.lang;
        this.content = value.content;
        this.defaultLang = value.defaultLang;
    }

    public ServiceDetails(
        Integer id,
        String lang,
        String content,
        Boolean defaultLang
    ) {
        this.id = id;
        this.lang = lang;
        this.content = content;
        this.defaultLang = defaultLang;
    }

    /**
     * Getter for <code>admin.service_details.id</code>.
     */
    @NotNull
    public Integer getId() {
        return this.id;
    }

    /**
     * Setter for <code>admin.service_details.id</code>.
     */
    public ServiceDetails setId(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Getter for <code>admin.service_details.lang</code>.
     */
    @NotNull
    @Size(max = 3)
    public String getLang() {
        return this.lang;
    }

    /**
     * Setter for <code>admin.service_details.lang</code>.
     */
    public ServiceDetails setLang(String lang) {
        this.lang = lang;
        return this;
    }

    /**
     * Getter for <code>admin.service_details.content</code>.
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Setter for <code>admin.service_details.content</code>.
     */
    public ServiceDetails setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Getter for <code>admin.service_details.default_lang</code>.
     */
    public Boolean getDefaultLang() {
        return this.defaultLang;
    }

    /**
     * Setter for <code>admin.service_details.default_lang</code>.
     */
    public ServiceDetails setDefaultLang(Boolean defaultLang) {
        this.defaultLang = defaultLang;
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
        final ServiceDetails other = (ServiceDetails) obj;
        if (this.id == null) {
            if (other.id != null)
                return false;
        }
        else if (!this.id.equals(other.id))
            return false;
        if (this.lang == null) {
            if (other.lang != null)
                return false;
        }
        else if (!this.lang.equals(other.lang))
            return false;
        if (this.content == null) {
            if (other.content != null)
                return false;
        }
        else if (!this.content.equals(other.content))
            return false;
        if (this.defaultLang == null) {
            if (other.defaultLang != null)
                return false;
        }
        else if (!this.defaultLang.equals(other.defaultLang))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.lang == null) ? 0 : this.lang.hashCode());
        result = prime * result + ((this.content == null) ? 0 : this.content.hashCode());
        result = prime * result + ((this.defaultLang == null) ? 0 : this.defaultLang.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ServiceDetails (");

        sb.append(id);
        sb.append(", ").append(lang);
        sb.append(", ").append(content);
        sb.append(", ").append(defaultLang);

        sb.append(")");
        return sb.toString();
    }
}
