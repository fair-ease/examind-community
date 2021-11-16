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

package org.constellation.json.binding;

import org.geotoolkit.style.MutableStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.constellation.json.util.StyleFactories.SF;
import static org.constellation.json.util.StyleUtilities.listType;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class Style implements StyleElement<MutableStyle> {

    private Integer id;
    private String name;
    private List<Rule> rules = new ArrayList<>();

    public Style() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(final List<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public MutableStyle toType() {
        final MutableStyle style = SF.style();
        style.setName(name);
        style.featureTypeStyles().add(SF.featureTypeStyle());
        style.featureTypeStyles().get(0).rules().addAll(listType(rules));
        return style;
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
            Style that = (Style) obj;
            return  Objects.equals(this.id, that.id) &&
                    Objects.equals(this.name, that.name) &&
                    Objects.equals(this.rules, that.rules);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.id);
        hash = 11 * hash + Objects.hashCode(this.name);
        hash = 11 * hash + Objects.hashCode(this.rules);
        return hash;
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Style]\n");
        sb.append("id=").append(id).append('\n');
        sb.append("name=").append(name).append('\n');
        if (rules != null) {
            sb.append("rules=\n");
            for (Rule ht : rules) {
                sb.append(ht).append('\n');
            }
        }
        return sb.toString();
    }
}
