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

package org.constellation.sos.io.filesystem;

import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.storage.DataStoreException;
import org.geotoolkit.sensor.SensorWriter;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import static org.constellation.api.CommonConstants.SENSOR_ID_BASE;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardOpenOption.*;

/**
 * A sensorML Writer working on a fileSystem.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileSensorWriter implements SensorWriter {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.sos.io.filesystem");

    /**
     * A JAXB unmarshaller used to unmarshall the xml generated by the XMLWriter.
     */
    private final MarshallerPool marshallerPool;

    /**
     * The directory where the data file are stored
     */
    private final Path dataDirectory;

    /**
     * The base identifier of all the sensor.
     */
    private final String sensorIdBase;

    public FileSensorWriter(final Path directory,  final Map<String, Object> properties) throws DataStoreException {

        this.sensorIdBase = (String) properties.get(SENSOR_ID_BASE);
        if (directory == null) {
            throw new DataStoreException("The sensor data directory is null");
        }
        this.dataDirectory  = directory;
        this.marshallerPool =  SensorMLMarshallerPool.getInstance();
        if (marshallerPool == null) {
            throw new DataStoreException("Unable to initialize the fileSensorWriter JAXB context");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeSensor(String id, final AbstractSensorML sensor) throws DataStoreException {

        id = id.replace(":", "µ");
        final Path currentFile = dataDirectory.resolve(id + ".xml");
        if (!Files.exists(currentFile)) {
            try {
                Files.createFile(currentFile);
            } catch (IOException ex) {
                throw new DataStoreException("the service was unable to create a new file:" + currentFile.getFileName().toString());
            }
        } else {
            LOGGER.log(Level.WARNING, "we overwrite the file: {0}", currentFile.toAbsolutePath().toString());
        }

        try (OutputStream os = Files.newOutputStream(currentFile, CREATE, WRITE, TRUNCATE_EXISTING)){
            final Marshaller marshaller = marshallerPool.acquireMarshaller();
            marshaller.marshal(sensor, os);
            marshallerPool.recycle(marshaller);
        } catch (JAXBException | IOException ex) {
            String msg = ex.getMessage();
            if (msg == null && ex.getCause() != null) {
                msg = ex.getCause().getMessage();
            }
            throw new DataStoreException("the service has throw a Exception:" + msg, ex);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteSensor(String id) throws DataStoreException {

        id = id.replace(":", "µ");
        final Path currentFile = dataDirectory.resolve(id + ".xml");
        String fileName = currentFile.getFileName().toString();
        if (Files.exists(currentFile)) {
            try {
                Files.delete(currentFile);
                return true;
            } catch (IOException e) {
                throw new DataStoreException("the service was unable to delete the file:" + fileName);
            }
        } else {
            LOGGER.log(Level.WARNING, "unable to find a file {0} to remove", fileName);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int replaceSensor(String id, final AbstractSensorML sensor) throws DataStoreException {

        id = id.replace(":", "µ");
        final Path currentFile = dataDirectory.resolve(id + ".xml");

        try (OutputStream os = Files.newOutputStream(currentFile, CREATE, WRITE, TRUNCATE_EXISTING)){
            final Marshaller marshaller = marshallerPool.acquireMarshaller();
            marshaller.marshal(sensor, os);
            marshallerPool.recycle(marshaller);
            return 1;//AbstractMetadataWriter.REPLACED;
        } catch (JAXBException | IOException ex) {
            String msg = ex.getMessage();
            if (msg == null && ex.getCause() != null) {
                msg = ex.getCause().getMessage();
            }
            throw new DataStoreException("the service has throw an exception:" + msg, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewSensorId() throws DataStoreException {
        int maxID = 0;
        if (dataDirectory != null && Files.isDirectory(dataDirectory)) {

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDirectory)) {
                for (Path f : stream) {
                    String id = f.getFileName().toString();
                    id = id.substring(0, id.indexOf(".xml"));
                    if (id.startsWith(sensorIdBase)) {
                        id = id.substring(id.indexOf(sensorIdBase) + sensorIdBase.length());
                        try {
                            final int curentID = Integer.parseInt(id);
                            if (curentID > maxID) {
                                maxID = curentID;
                            }
                        } catch (NumberFormatException ex) {
                            throw new DataStoreException("unable to parse the identifier:" + id, ex);
                        }
                    }
                }
            } catch (IOException e) {
                throw new DataStoreException("Error while scanning sensors directory", e);
            }
        }
        return sensorIdBase + maxID + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        //do nothing
    }
}
