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
package org.constellation.ws;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.constellation.dto.service.config.csw.BriefNode;
import org.constellation.exception.ConfigurationException;
import org.constellation.dto.StringList;
import org.constellation.exception.ConstellationException;
import org.w3c.dom.Node;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public interface ICSWConfigurer extends IOGCConfigurer {

    boolean refreshIndex(final String id, final boolean asynchrone, final boolean forced) throws ConstellationException;

    /**
     * Add some CSW record to the index.
     *
     * @param id identifier of the CSW service.
     * @param identifierList list of metadata identifier to add into the index.
     *
     * @return
     * @throws ConfigurationException
     */
    boolean addToIndex(final String id, final List<String> identifierList) throws ConstellationException;

    /**
     * Remove some CSW record to the index.
     *
     * @param id identifier of the CSW service.
     * @param identifierList list of metadata identifier to add into the index.
     *
     * @return
     * @throws ConfigurationException
     */
    boolean removeFromIndex(final String id, final List<String> identifierList) throws ConfigurationException;

    boolean removeIndex(final String id) throws ConfigurationException;

    /**
     * Stop all the indexation going on.
     *
     * @param id identifier of the CSW service.
     * @return an Acknowledgment.
     */
    boolean stopIndexation(final String id);

    boolean importRecord(final String id, final String metadataId) throws ConstellationException ;

    boolean importRecords(final String id, final Collection<String> metadataIds) throws ConstellationException ;

    boolean importRecords(final String id, final Path f, final String fileName) throws ConstellationException ;

    boolean importRecord(final String id, final Node n) throws ConstellationException;

    boolean removeRecords(final String id, final String metadataId) throws ConstellationException;

    boolean removeAllRecords(final String id) throws ConstellationException;

    boolean metadataExist(final String id, final String metadataID) throws ConfigurationException;

    List<BriefNode> getMetadataList(final String id, final int count, final int startIndex) throws ConfigurationException;

    List<Node> getFullMetadataList(final String id, final int count, final int startIndex, String type) throws ConfigurationException;

    Node getMetadata(final String id, final String metadataID) throws ConstellationException;

    int getMetadataCount(final String id) throws ConfigurationException;

    StringList getAvailableCSWDataSourceType();
}
