/*
 *    Examind community - An open source and standard compliant SDI
 *    https://community.examind.com/
 *
 * Copyright 2022 Geomatys.
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
package com.examind.image.heatmap;

import org.apache.sis.coverage.grid.GridExtent;
import org.apache.sis.coverage.grid.GridGeometry;
import org.apache.sis.coverage.grid.GridOrientation;
import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.geometry.Envelope2D;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.storage.FeatureSet;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform2D;

import java.awt.*;
import java.awt.image.*;
import java.util.stream.IntStream;

import static com.examind.image.heatmap.FeatureSetAsPointsCloudTest.createTestFeatureSet;

public final class HeatMapImageTest {

    /**
     * Simple test verifying {@link HeatMapImage} computation in a test sample proceed without error and compute
     * values (!= 0). A commented code can be used to visualise a grayscale image from the produced raster.
     *
     * Based on the {@link PointCloudResource} initialized from featureSet test sample produced by
     * {@link FeatureSetAsPointsCloudTest#createTestFeatureSet()} located in the near Location of Montpellier - France
     *
     */
    @Test
    public void functionalTest() throws Exception {
        final FeatureSet featureSet = createTestFeatureSet();
        final FeatureSetAsPointsCloud pointCloud = new FeatureSetAsPointsCloud(CommonCRS.defaultGeographic(), featureSet);

        Envelope2D env = new Envelope2D(
                new DirectPosition2D(CommonCRS.defaultGeographic(), 3.212619925176625, 43.2289799480256),
                new DirectPosition2D(CommonCRS.defaultGeographic(), 4.788888527373842, 44.069434844792)
        );
        env.setCoordinateReferenceSystem(CommonCRS.defaultGeographic());

        GridGeometry gridGeom = new GridGeometry(new GridExtent(256, 128), env, GridOrientation.DISPLAY);

        MathTransform2D gridToCrsCorner = (MathTransform2D) gridGeom.getGridToCRS(PixelInCell.CELL_CORNER);
        MathTransform2D gridToCrsCenter = (MathTransform2D) gridGeom.getGridToCRS(PixelInCell.CELL_CENTER);
        MathTransform2D crsToGridCenter =  gridToCrsCorner.inverse();

        final HeatMapImage heatMap = new HeatMapImage(new Dimension(256, 128), new Dimension(256, 128), gridToCrsCorner, gridToCrsCenter, crsToGridCenter, pointCloud, 0.1f, 0.1f);

        final WritableRaster raster = (WritableRaster) heatMap.computeTile(0, 0, null);

        Assert.assertNotNull(raster);
        float[] randomSample= new float[100];
        raster.getDataElements(80, 80, 10, 10, randomSample);
        Assert.assertNotNull(randomSample);
        Assert.assertTrue(IntStream.range(0, randomSample.length)
                           .mapToDouble(i -> randomSample[i])
                        .anyMatch(d -> d>0));

//        BufferedImage image = new BufferedImage(ColorModelFactory.createGrayScale(DataBuffer.TYPE_FLOAT, 1, 0, 0, 5.4), raster, false, null);
//        BufferedImage image = new BufferedImage(ColorModelFactory.createGrayScale(DataBuffer.TYPE_FLOAT, 1, 0, 0, heatMap.getMax()), raster, false, null);

    }


}
