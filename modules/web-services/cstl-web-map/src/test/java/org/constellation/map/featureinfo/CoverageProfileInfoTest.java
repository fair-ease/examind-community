/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2020-2021 Geomatys.
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
package org.constellation.map.featureinfo;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.sis.coverage.SampleDimension;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.coverage.grid.GridCoverage2D;
import org.apache.sis.coverage.grid.GridExtent;
import org.apache.sis.coverage.grid.GridGeometry;
import org.apache.sis.coverage.grid.GridOrientation;
import org.apache.sis.image.Interpolation;
import org.apache.sis.image.PixelIterator;
import org.apache.sis.image.WritablePixelIterator;
import org.apache.sis.measure.Units;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.constellation.map.featureinfo.CoverageProfileInfoFormat.XY;
import org.constellation.map.featureinfo.DataProfile.DataPoint;
import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.apache.sis.coverage.grid.PixelInCell;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import static java.lang.Double.NaN;
import static java.util.Arrays.asList;
import static org.constellation.map.featureinfo.CoverageProfileInfoFormat.NaNPropagation.*;
import static org.constellation.map.featureinfo.CoverageProfileInfoFormat.ReductionMethod.*;
import static org.constellation.map.featureinfo.CoverageProfileInfoFormat.cleanupNans;
import static org.constellation.map.featureinfo.CoverageProfileInfoFormat.reduce;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @author Alexis Manin (Geomatys)
 */
public class CoverageProfileInfoTest {

    static final GeographicCRS LON_LAT_CRS84 = CommonCRS.defaultGeographic();
    private static final GeographicCRS LAT_LON_CRS84 = CommonCRS.WGS84.geographic();

    @Test
    public void testDecimateSamplingCount() {

        final List<XY> points = new ArrayList<>();
        points.add(new XY(0, 0));
        points.add(new XY(1, 0));
        points.add(new XY(3, 0));
        points.add(new XY(4, 0));
        points.add(new XY(7, 0));
        points.add(new XY(9, 0));
        points.add(new XY(9.5, 0));
        points.add(new XY(10, 0));

        //the remove expected order is :
        // 0,1,3,4,7,9,9.5,10    costs : [3,3,4,5,2.5,1]  => 9.5
        // 0,1,3,4,7,9,10      costs : [3,3,4,5,3] => 1
        // 0,3,4,7,9,10      costs : [4,4,5,3] => 9
        // 0,3,4,7,10      costs : [4,4,6] => 3
        // 0,4,7,10      costs : [7,6] => 7
        // 0,4,10      costs : [10] => 4

        { //remove a single point
            final List<XY> reduce = reduce(points, 7);
            Assert.assertEquals(7, reduce.size());
            int i = 0;
            Assert.assertEquals(0, reduce.get(i++).x, 0.0);
            Assert.assertEquals(1, reduce.get(i++).x, 0.0);
            Assert.assertEquals(3, reduce.get(i++).x, 0.0);
            Assert.assertEquals(4, reduce.get(i++).x, 0.0);
            Assert.assertEquals(7, reduce.get(i++).x, 0.0);
            Assert.assertEquals(9, reduce.get(i++).x, 0.0);
            Assert.assertEquals(10, reduce.get(i++).x, 0.0);
        }

        { //remove 2 points
            final List<XY> reduce = reduce(points, 6);
            Assert.assertEquals(6, reduce.size());
            int i = 0;
            Assert.assertEquals(0, reduce.get(i++).x, 0.0);
            Assert.assertEquals(3, reduce.get(i++).x, 0.0);
            Assert.assertEquals(4, reduce.get(i++).x, 0.0);
            Assert.assertEquals(7, reduce.get(i++).x, 0.0);
            Assert.assertEquals(9, reduce.get(i++).x, 0.0);
            Assert.assertEquals(10, reduce.get(i++).x, 0.0);
        }

        { //remove 3 points
            final List<XY> reduce = reduce(points, 5);
            Assert.assertEquals(5, reduce.size());
            int i = 0;
            Assert.assertEquals(0, reduce.get(i++).x, 0.0);
            Assert.assertEquals(3, reduce.get(i++).x, 0.0);
            Assert.assertEquals(4, reduce.get(i++).x, 0.0);
            Assert.assertEquals(7, reduce.get(i++).x, 0.0);
            Assert.assertEquals(10, reduce.get(i++).x, 0.0);
        }

        { //remove 4 points
            final List<XY> reduce = reduce(points, 4);
            Assert.assertEquals(4, reduce.size());
            int i = 0;
            Assert.assertEquals(0, reduce.get(i++).x, 0.0);
            Assert.assertEquals(4, reduce.get(i++).x, 0.0);
            Assert.assertEquals(7, reduce.get(i++).x, 0.0);
            Assert.assertEquals(10, reduce.get(i++).x, 0.0);
        }

        { //remove 5 points
            final List<XY> reduce = reduce(points, 3);
            Assert.assertEquals(3, reduce.size());
            int i = 0;
            Assert.assertEquals(0, reduce.get(i++).x, 0.0);
            Assert.assertEquals(4, reduce.get(i++).x, 0.0);
            Assert.assertEquals(10, reduce.get(i++).x, 0.0);
        }

        { //remove 6 points
            final List<XY> reduce = reduce(points, 2);
            Assert.assertEquals(2, reduce.size());
            int i = 0;
            Assert.assertEquals(0, reduce.get(i++).x, 0.0);
            Assert.assertEquals(10, reduce.get(i++).x, 0.0);
        }

        { //remove 7 or more points, should have no effect, we must keep at least 2 points
            final List<XY> reduce = reduce(points, 1);
            Assert.assertEquals(2, reduce.size());
            int i = 0;
            Assert.assertEquals(0, reduce.get(i++).x, 0.0);
            Assert.assertEquals(10, reduce.get(i++).x, 0.0);
        }
    }

    @Test
    public void testDecimationStrategies() {
        final List<XY> points = new ArrayList<>();
        points.add(new XY(0, 0));
        points.add(new XY(1, 1));
        points.add(new XY(3, 2));
        points.add(new XY(4, 3));
        points.add(new XY(7, 4));
        points.add(new XY(9, 5));
        points.add(new XY(9.5, 6));
        points.add(new XY(10, 7));
        points.add(new XY(12.2, 8));
        points.add(new XY(13.1, 9));

        /* First, we try a sampling count too high. There should be a trigger in the code to force sampling count
         * change, to get reduction windows of 3 elements.
         */
        List<XY> reduced = reduce(points, 8, AVG, ALL);
        // Returned distances should be the same whatever reduction method is used.
        double[] expectedMeanDistances = {
                0, // First point preserved
                (0 + 1 + 3)       / 3d, // mean(lst[0..2])
                (4 + 7 + 9)       / 3d, // mean(lst[3..5])
                (9.5 + 10 + 12.2) / 3d, // mean(lst[6..8])
                13.1 // Last point preserved
        };
        double[] expectedValues = {
                0,
                (0 + 1 + 2) / 3d,
                (3 + 4 + 5) / 3d,
                (6 + 7 + 8) / 3d,
                9
        };
        assertSeriesEquals("Distance means are wrong", expectedMeanDistances, reduced, XY::getX);
        assertSeriesEquals("Average values are wrong", expectedValues, reduced, XY::getY);

        reduced = reduce(points, 8, MIN, ALL);
        assertSeriesEquals("Distance means are wrong", expectedMeanDistances, reduced, XY::getX);
        expectedValues[1] = 0;
        expectedValues[2] = 3;
        expectedValues[3] = 6;
        assertSeriesEquals("Min values are wrong", expectedValues, reduced, XY::getY);

        reduced = reduce(points, 8, MAX, ALL);
        assertSeriesEquals("Distance means are wrong", expectedMeanDistances, reduced, XY::getX);
        expectedValues[1] = 2;
        expectedValues[2] = 5;
        expectedValues[3] = 8;
        assertSeriesEquals("Min values are wrong", expectedValues, reduced, XY::getY);

        // If queried sampling makes sense, ensure that reduction windows are well-sized
        reduced = reduce(points, 2, MIN, ALL);
        expectedMeanDistances = new double[]{
                0, // First point preserved
                (0 + 1 + 3 + 4 + 7)          / 5d, // mean(lst[0..4])
                (9 + 9.5 + 10 + 12.2 + 13.1) / 5d, // mean(lst[5..9])
                13.1 // Last point preserved
        };
        expectedValues = new double[]{0, 0, 5, 9};
        assertSeriesEquals("Distance means are wrong", expectedMeanDistances, reduced, XY::getX);
        assertSeriesEquals("Min values are wrong", expectedValues, reduced, XY::getY);
    }

    @Test public void decimationHandlesNaN() {
        final List<XY> points = new ArrayList<>();
        points.add(new XY(0, 0));
        points.add(new XY(1, 1));
        points.add(new XY(3, 2));
        points.add(new XY(4, NaN));
        points.add(new XY(7, 4));
        points.add(new XY(9, 5));
        points.add(new XY(9.5, NaN));
        points.add(new XY(10, 7));
        points.add(new XY(12.2, 8));
        points.add(new XY(13.1, 9));

        List<XY> reduced = reduce(points, 2, MIN, ALL);
        double[] expectedMeanDistances = {
                0, // First point preserved
                (0 + 1 + 3 + 4 + 7)          / 5d, // mean(lst[0..4])
                (9 + 9.5 + 10 + 12.2 + 13.1) / 5d, // mean(lst[5..9])
                13.1 // Last point preserved
        };
        double[] expectedValues = {0, 0, 5, 9};
        assertSeriesEquals("Distance means are wrong", expectedMeanDistances, reduced, XY::getX);
        assertSeriesEquals("Min values are wrong", expectedValues, reduced, XY::getY);
    }

    @Test public void testProfileOnSubset() throws Exception {
        final LineString line = profile(LON_LAT_CRS84,
                3, 4,
                4, 4,
                4, 5
        );
        int px_3_4 = 8 * 4 + 3;
        int px_4_4 = 8 * 4 + 4;
        int px_4_5 = 8 * 5 + 4;
        assertProfileEquals(
                createDatasource(0, 0),
                line,
                px_3_4, // start
                px_3_4, // median to pixel border
                px_4_4, // median from pixel border
                px_4_4, // last point of first segment
                px_4_4, // median to pixel border
                px_4_5, // median from pixel border
                px_4_5  // last point
        );

        px_3_4 = 8 * (4-3) + (3-2);
        px_4_4 = 8 * (4-3) + (4-2);
        px_4_5 = 8 * (5-3) + (4-2);
        assertProfileEquals(
                createDatasource(2, 3),
                line,
                px_3_4, // start
                px_3_4, // median to pixel border
                px_4_4, // median from pixel border
                px_4_4, // last point of first segment
                px_4_4, // median to pixel border
                px_4_5, // median from pixel border
                px_4_5  // last point
        );
    }

    @Test public void testProfileWithReprojection() throws Exception {
        final LineString line = profile(LAT_LON_CRS84,
                7, 6,
                6, 5
        );

        int px_6_7 = 8 * 7 + 6;
        int px_5_6 = 8 * 6 + 5;
        assertProfileEquals(
                createDatasource(0, 0),
                line,
                px_6_7, // start
                px_6_7, // median to pixel border
                px_5_6, // median from pixel border
                px_5_6  // end
        );
    }

    @Test
    public void nanRegionsShouldBePreserved() throws Exception {
        final LineString profile = profile(LAT_LON_CRS84,
                1, 1,
                2, 1,
                2, 2
        );

        assertProfileEquals(createNaNDataSource(), profile, NaN, NaN, NaN, NaN, NaN, NaN, NaN);
    }

    @Test
    public void reductionShouldPreserveNaNBatches() {
        final List<XY> points = new ArrayList<>();
        points.add(new XY(0, 0));
        points.add(new XY(1, 1));
        points.add(new XY(3, 2));
        points.add(new XY(4,  NaN));
        points.add(new XY(7,  NaN));
        points.add(new XY(9,  NaN));
        points.add(new XY(9.5, NaN));
        points.add(new XY(10, 7));
        points.add(new XY(12.2, 8));
        points.add(new XY(13.1, 9));

        List<XY> reduced = reduce(points, 3, MIN, ALL);
        double[] expectedMeanDistances = {
                0, // First point preserved
                (0 + 1 + 3)       / 3d, // mean(lst[0..2])
                (4 + 7 + 9)       / 3d, // mean(lst[3..5])
                (9.5 + 10 + 12.2) / 3d, // mean(lst[6..8])
                13.1 // Last point preserved
        };
        double[] expectedValues = {0, 0, NaN, 7, 9 };
        assertSeriesEquals("Distance means are wrong", expectedMeanDistances, reduced, XY::getX);
        assertSeriesEquals("Min values are wrong", expectedValues, reduced, XY::getY);
    }

    @Test
    public void reduceWithNaNPropagation() {
        final List<XY> points = new ArrayList<>();
        points.add(new XY(0, 0));
        points.add(new XY(1, 1));
        points.add(new XY(2, 2));
        points.add(new XY(3, NaN));
        points.add(new XY(4, 4));
        points.add(new XY(5, 5));

        List<XY> reduced = reduce(points, 2, MIN, ANY);
        double[] expectedMeanDistances = { 0, 1, 4, 5 };
        double[] expectedValues = {0, 0, NaN, 5};
        assertSeriesEquals("Distance means are wrong", expectedMeanDistances, reduced, XY::getX);
        assertSeriesEquals("Min values are wrong", expectedValues, reduced, XY::getY);
    }

    @Test
    public void reduceWithNaNValuesIgnored() {
        final List<XY> points = new ArrayList<>();
        points.add(new XY(0, 0));
        points.add(new XY(1, 1));
        points.add(new XY(2, 2));
        points.add(new XY(3, NaN));
        points.add(new XY(4, 4));
        points.add(new XY(5, 5));

        List<XY> reduced = reduce(points, 2, AVG, ALL);
        double[] expectedMeanDistances = { 0, 1, 4, 5 };
        double[] expectedValues = {
                0,   // edge point returned as is
                1,   // mean from first 3 points
                4.5, // mean from last 3 points (and NaN removed from the equation)
                5    // edge point returned as is
        };
        assertSeriesEquals("Distance means are wrong", expectedMeanDistances, reduced, XY::getX);
        assertSeriesEquals("Min values are wrong", expectedValues, reduced, XY::getY);
    }

    @Test
    public void profileNoIntersection() throws Exception {
        final LineString line = profile(LAT_LON_CRS84,
                2, -2,
                -2, -2,
                -3, 2
        );

        DataProfile profile = new DataProfile(createDatasource(0, 0), line, Interpolation.NEAREST);

        Consumer<DataPoint> noValueExpected = pt -> Assert.assertNull(pt.value);
        int i = 0, limit = 20; boolean hasAdvanced;
        do {
            hasAdvanced = profile.tryAdvance(noValueExpected);
        } while (hasAdvanced && i++ < limit);

        assertFalse("Too many points returned", profile.tryAdvance(noValueExpected));
    }

    @Test
    public void cleanupNaN() {
        assertCleanupNaN("Simple case",
                asList(xy(0, 10), xy(1, 11), xy(2, 12), xy(3, NaN), xy(5, NaN), xy(6, 16)),
                asList(xy(0, 10), xy(1, 11), xy(2, 12), xy(3, NaN), xy(4, NaN), xy(5, NaN), xy(6, 16)));
        assertCleanupNaN("Starts with NaNs",
                asList(xy(0, NaN), xy(3, NaN), xy(4, 13)),
                asList(xy(0, NaN), xy(1, NaN), xy(2, NaN), xy(3, NaN), xy(4, 13)));
        assertCleanupNaN("Ends with NaNs",
                asList(xy(0, 1), xy(1, 2), xy(3, NaN), xy(5, NaN)),
                asList(xy(0, 1), xy(1, 2), xy(3, NaN), xy(4, NaN), xy(5, NaN)));
        assertCleanupNaN("Complex case",
                asList(xy(0, NaN), xy(1, 1), xy(2, 2), xy(3, NaN), xy(4, NaN), xy(5, 5),
                        xy(6, NaN), xy(9, NaN), xy(10, 10), xy(11, NaN), xy(13, NaN), xy(14, 14),
                        xy(15, NaN), xy(20, NaN), xy(21, 21), xy(22, 22), xy(23, NaN)),
                asList(xy(0, NaN), xy(1, 1), xy(2, 2), xy(3, NaN), xy(4, NaN), xy(5, 5),
                        xy(6, NaN), xy(7, NaN), xy(8, NaN), xy(9, NaN), xy(10, 10), xy(11, NaN),
                        xy(12, NaN), xy(13, NaN), xy(14, 14), xy(15, NaN), xy(16, NaN), xy(17, NaN),
                        xy(18, NaN), xy(19, NaN), xy(20, NaN), xy(21, 21), xy(22, 22), xy(23, NaN)));
    }

    @Test
    public void verify_interpolation_behavior_on_edges() throws Exception {
        final GridCoverage dataWithOriginAtZero = createDatasource(0, 0);
        assertProfileEquals(dataWithOriginAtZero, profile(LON_LAT_CRS84, -1, 0, 0, 0), Interpolation.BILINEAR, true, NaN, NaN, 0, 0);
        final int lastPxValue = 8 * 8 - 1;
        assertProfileEquals(dataWithOriginAtZero, profile(LON_LAT_CRS84, 8, 7, 7, 7), Interpolation.BILINEAR, true, NaN, NaN, lastPxValue, lastPxValue);
        final int middleRight = 4 * 8 - 1;
        assertProfileEquals(dataWithOriginAtZero, profile(LON_LAT_CRS84, 7, 3, 8, 3), Interpolation.BILINEAR, true, middleRight, middleRight, NaN, NaN);
        final int centerBottom = 7 * 8 + 3;
        assertProfileEquals(dataWithOriginAtZero, profile(LON_LAT_CRS84, 3, 7, 3, 8), Interpolation.BILINEAR, true, centerBottom, centerBottom, NaN, NaN);

        final GridCoverage dataWithNegativeOrigin = createDatasource(-2, -2);
        assertProfileEquals(dataWithNegativeOrigin, profile(LON_LAT_CRS84, -3, -2, -2, -2), Interpolation.BILINEAR, true, NaN, NaN, 0, 0);
        assertProfileEquals(dataWithNegativeOrigin, profile(LON_LAT_CRS84,  6,  5,  5,  5), Interpolation.BILINEAR, true, NaN, NaN, lastPxValue, lastPxValue);
        assertProfileEquals(dataWithNegativeOrigin, profile(LON_LAT_CRS84, 5, 1, 6, 1), Interpolation.BILINEAR, true, middleRight, middleRight, NaN, NaN);
        assertProfileEquals(dataWithNegativeOrigin, profile(LON_LAT_CRS84, 1, 5, 1, 6), Interpolation.BILINEAR, true, centerBottom, centerBottom, NaN, NaN);

        final GridCoverage dataWithPositiveOrigin = createDatasource(2, 2);
        assertProfileEquals(dataWithPositiveOrigin, profile(LON_LAT_CRS84,  1, 2, 2, 2), Interpolation.BILINEAR, true, NaN, NaN, 0, 0);
        assertProfileEquals(dataWithPositiveOrigin, profile(LON_LAT_CRS84, 10, 9, 9, 9), Interpolation.BILINEAR, true, NaN, NaN, lastPxValue, lastPxValue);
        assertProfileEquals(dataWithPositiveOrigin, profile(LON_LAT_CRS84, 9, 5, 10, 5), Interpolation.BILINEAR, true, middleRight, middleRight, NaN, NaN);
        assertProfileEquals(dataWithPositiveOrigin, profile(LON_LAT_CRS84, 5, 9, 5, 10), Interpolation.BILINEAR, true, centerBottom, centerBottom, NaN, NaN);
    }

    private static void assertCleanupNaN(final String title, final List<XY> expected, final List<XY> points) {
        final List<XY> result = cleanupNans(points);
        assertEquals(title+": size differ", expected.size(), result.size());
        for (int i = 0 ; i < expected.size() ; i++) {
            assertEquals(title+": X at index "+i, expected.get(i).x, result.get(i).x, 1e-1);
            assertEquals(title+": Y at index "+i, expected.get(i).y, result.get(i).y, 1e-1);
        }
    }

    /**
     * Create a grid-coverage whose grid extent starts at given x and y coordinates. Data specifications:
     * <ul>
     *     <li>Data coordinate reference system is {@link CommonCRS#defaultGeographic() CRS:84}.</li>
     *     <li>
     *         The <em>{@link PixelInCell#CELL_CENTER}</em> grid to CRS is identity. It imply that:
     *         <ul>
     *             <li>The grid orientation matches a matrix, i.e it is {@link GridOrientation#HOMOTHETY}</li>
     *             <li>The data envelope is : {@code new Envelope2D(CommonCRS.defaultGeographic(), lowX - 0.5, lowY - 0.5, 8 + 1, 8 + 1);}</li>
     *         </ul>
     *     </li>
     *     <li>
     *         The values of the coverage rendering pixel values are their position in linear browsing (y * width + x).
     *         <em>However</em>, the origin used for pixel value computing is always (0, 0), not the grid extent origin.
     *         It means that, if user specify a (-1, -1) origin:
     *         <ol>
     *             <li>The pixel value at origin is 0</li>
     *             <li>Pixel value at coordinate (1, 0) is 1</li>
     *             <li>Pixel value at coordinate (0, 1) is 8</li>
     *         </ol>,
     *     </li>
     * </ul>
     *
     * .
     *  The
     * origin used to compute rendering indices is always (0, 0), it does not match grid extent indices.
     *
     * @param lowX grid extent origin in X
     * @param lowY Grid extent origin in Y
     */
    private static GridCoverage createDatasource(int lowX, int lowY) {
        final BufferedImage values = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        final WritablePixelIterator it = new PixelIterator.Builder().createWritable(values, values);
        final int width = it.getDomain().width;
        while (it.next()) {
            final Point pos = it.getPosition();
            it.setSample(0, pos.y * width + pos.x);
        }

        final GridGeometry domain = new GridGeometry(
                new GridExtent(8, 8).translate(lowX, lowY),
                PixelInCell.CELL_CENTER,
                MathTransforms.identity(2),
                LON_LAT_CRS84
        );

        final SampleDimension sampleDim = new SampleDimension.Builder()
                .setName("numbers")
                .setBackground("no-data", -1)
                .build();

        return new GridCoverage2D(domain, Collections.singletonList(sampleDim), values);
    }

    private static GridCoverage createNaNDataSource() {
        final BufferedImage values = new BufferedImage(4, 4, BufferedImage.TYPE_BYTE_GRAY);

        try (WritablePixelIterator it = new PixelIterator.Builder().createWritable(values)) {
            while (it.next()) it.setSample(0, 2);
        }

        final GridGeometry domain = new GridGeometry(
                new GridExtent(4, 4),
                PixelInCell.CELL_CENTER,
                MathTransforms.identity(2),
                LON_LAT_CRS84
        );

        final SampleDimension sampleDim = new SampleDimension.Builder()
                .setName("numbers")
                .addQuantitative("values", 0, 1, 1, 0, Units.UNITY)
                .setBackground("no-data", 2)
                .build();

        final GridCoverage2D data = new GridCoverage2D(domain, Collections.singletonList(sampleDim), values);
        return data.forConvertedValues(true);
    }

    static void assertProfileEquals(final GridCoverage source, final LineString line, final double... expectedValues) throws FactoryException, TransformException {
        assertProfileEquals(source, line, Interpolation.NEAREST, false, expectedValues);
    }

    static void assertProfileEquals(final GridCoverage source, final LineString line, final Interpolation interpolation, boolean mapNullToNaN, final double... expectedValues) throws FactoryException, TransformException {
        DataProfile profile = new DataProfile(source, line, interpolation);
        Stream<DataPoint> samples = StreamSupport.stream(profile, false);
        // TODO: mapping null values to NaN should not be needed. This is a workaround to cope with poor lifecycle of values.
        if (mapNullToNaN) samples = samples.map(CoverageProfileInfoTest::replaceNullValue);
        final double[] values = samples
                .peek(CoverageProfileInfoTest::errorIfNoValue)
                .mapToDouble(point -> ((double[]) point.value)[0])
                .toArray();

        Assert.assertArrayEquals("Profile values: "+ Arrays.toString(values), expectedValues, values, .1);
    }

    private static DataPoint replaceNullValue(DataPoint sample) {
        if (sample.value != null) return sample;
        var replacement = new DataPoint(sample.geoLocation, sample.gridLocation, sample.distanceFromPrevious);
        replacement.value = new double[] { NaN };
        return replacement;
    }

    private static void errorIfNoValue(final DataPoint point) {
        final Object value = point.value;
        if (value instanceof double[] && ((double[])value).length > 0) return;
        throw new AssertionError("No value set for point "+point);
    }

    private static void assertSeriesEquals(final String message, final double[] expectedValues, List<XY> values, ToDoubleFunction<XY> extractor) {
        Assert.assertArrayEquals(
                message,
                expectedValues,
                values.stream()
                        .mapToDouble(extractor)
                        .toArray(),
                1e-2
        );
    }

    static LineString profile(CoordinateReferenceSystem crs, double... ordinates) {
        final CoordinateSequence coordinates = PackedCoordinateSequenceFactory.DOUBLE_FACTORY.create(ordinates, 2);
        final LineString line = new GeometryFactory().createLineString(coordinates);
        line.setUserData(crs);
        return line;
    }

    private XY xy(double x, double y) {
        return new XY(x, y);
    }
}
