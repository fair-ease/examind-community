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

package org.constellation.coverage.ws.rs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import org.apache.sis.coverage.Category;
import org.apache.sis.coverage.SampleDimension;
import org.apache.sis.coverage.grid.DomainLinearizer;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.coverage.grid.GridCoverageProcessor;
import org.apache.sis.coverage.grid.GridGeometry;
import org.apache.sis.coverage.grid.GridOrientation;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.image.Interpolation;
import org.constellation.util.CRSUtilities;
import org.geotoolkit.nio.IOUtilities;
import org.apache.sis.coverage.grid.PixelInCell;
import org.apache.sis.storage.DataStores;
import org.apache.sis.storage.geotiff.GeoTiffStore;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class GridCoverageWriter implements HttpMessageConverter<GeotiffResponse> {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.coverage.ws.rs");

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return GeotiffResponse.class.isAssignableFrom(clazz);
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(MediaType.parseMediaType("image/tiff"),  MediaType.parseMediaType("image/geotiff"));
    }

    @Override
    public GeotiffResponse read(Class<? extends GeotiffResponse> type, HttpInputMessage him) throws IOException, HttpMessageNotReadableException {
        throw new HttpMessageNotReadableException("Entry message converter do not support reading.", him);
    }

    @Override
    public void write(GeotiffResponse entry, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        File f = null;
        try {
            f = writeInFile(entry);
            byte[] buf = new byte[8192];
            try (FileInputStream is = new FileInputStream(f);
                    OutputStream out = outputMessage.getBody()) {
                int c;
                while ((c = is.read(buf, 0, buf.length)) > 0) {
                    out.write(buf, 0, c);
                    out.flush();
                }
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new HttpMessageNotWritableException("Error while writing coverage", ex);
        } finally {
            if (f != null) IOUtilities.deleteSilently(f.toPath());
        }
    }

    public static File writeInFile(final GeotiffResponse entry) throws Exception {
        GridCoverage coverage = entry.coverage;
        coverage = coverage.forConvertedValues(false);

        // Tiff writer does not support non-linear grid to crs conversion
        if (!coverage.getGridGeometry().isConversionLinear(0, 1)) {
            DomainLinearizer linearizer = new DomainLinearizer();
            linearizer.setGridStartsAtZero(true);
            GridGeometry resampleGrid = linearizer.apply(coverage.getGridGeometry().selectDimensions(0,1));

            final GridCoverageProcessor processor = new GridCoverageProcessor();
            processor.setInterpolation(Interpolation.NEAREST);
            coverage = processor.resample(coverage, resampleGrid);
        }

        if (entry.outputCRS != null) {
            final GridGeometry gg = coverage.getGridGeometry();
            final GridGeometry outputGeom;
            if (gg.isDefined(GridGeometry.ENVELOPE)) {
                GeneralEnvelope env = CRSUtilities.reprojectWithNoInfinity(gg.getEnvelope(), entry.outputCRS);
                outputGeom = new GridGeometry(gg.isDefined(GridGeometry.EXTENT) ? gg.getExtent() : null, env, GridOrientation.REFLECTION_Y);
            } else outputGeom = new GridGeometry(null, PixelInCell.CELL_CENTER, null, entry.outputCRS);
            coverage = new GridCoverageProcessor().resample(coverage, outputGeom);
        }

        //see if we convert to geophysic or not before writing
        final List<SampleDimension> sampleDimensions = coverage.getSampleDimensions();
        if (sampleDimensions != null) {
            search:
            for (SampleDimension sd : sampleDimensions) {
                for(Category cat : sd.getCategories()) {
                    if (cat.isQuantitative()) {
                        coverage = coverage.forConvertedValues(true);
                        break search;
                    }
                }
            }
        }

        // TIFF writer do no support writing in output stream currently, we have to write in a file before
        final File f = File.createTempFile("data", ".tiff");
        try (final GeoTiffStore iowriter = (GeoTiffStore) DataStores.openWritable(f, "GeoTIFF")) {
            
        // TODO apply on new store
        
        /*TiffImageWriteParam param = new TiffImageWriteParam(iowriter);
        if (entry.compression != null && !entry.compression.equals("NONE")) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType(entry.compression);
        }
        if (entry.tiling) {
            param.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
            param.setTiling(entry.tileWidth, entry.tileHeight, 0, 0);
        }
          iowriter.write(null, iioimage, param);
        iowriter.dispose();
         */
        iowriter.append(coverage, null);
        }
        return f;
    }
}
