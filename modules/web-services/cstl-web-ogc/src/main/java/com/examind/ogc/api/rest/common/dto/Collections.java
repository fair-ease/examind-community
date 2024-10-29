package com.examind.ogc.api.rest.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.geotoolkit.atom.xml.Link;

import java.util.List;

/**
 * @author Quentin BIALOTA
 * @author Guilhem LEGAL
 */
@XmlRootElement(name = "Collections")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Collections extends CommonResponse {

    @XmlElement(
            name = "link",
            namespace = "http://www.w3.org/2005/Atom"
    )
    @JsonProperty("links")
    private List<Link> links;

    @XmlElement(
            name = "Collection"
    )
    @JsonProperty("collections")
    private List<Collection> collections;

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public void setCollections(List<Collection> collections) {
        this.collections = collections;
    }
}