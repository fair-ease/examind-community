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

import com.examind.image.pointcloud.PointCloudResource;
import org.apache.sis.geometry.Envelope2D;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.image.ComputedImage;
import org.apache.sis.system.Loggers;
import org.apache.sis.util.collection.BackingStoreException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.image.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Simple implementation for HeatMap computation based on :
 * https://en.wikipedia.org/wiki/Multivariate_kernel_density_estimation
 * https://mapserver.org/output/kerneldensity.html
 * It uses an isotropic Gaussian function as Kernel.
 */
public final class HeatMapImage extends ComputedImage {

    //TODO make it configurable
    private static final int BATCH_SIZE = 60_000_000;
    /**
     * Points source to be used to compute the heatMap
     */
    private final PointCloudResource dataSource;
    private final Dimension imageDimension;
    private final Dimension tilingDimension;
    /**
     * MathTransform2D to compute coordinate in a given CRS from pixel corner of the current {@link HeatMapImage}.
     */
    private final MathTransform gridCornerToDataCRS;

    /**
     * MathTransform2D to compute coordinate in a given CRS from pixel center of the current {@link HeatMapImage}.
     */
    private final MathTransform dataCRSToGridCenter;

    private final double distanceX;
    private final double distanceXx2;
    private final double distanceY;
    private final double distanceYx2;

    //todo MAX_COMPUTATION: uncomment using atomic? Could be used to Colormodel definition
//    private float max = 0;

    private final double a = 1;

    private final DistanceOp op;
    /**
     * Boolean value indicating if it is expected only the position's pixel to be increment without neighbouring
     * consideration.
     */
    private final boolean pixelOnly;

    /**
     * HeatMapImage using input algorithm strategy from the input {@link PointCloudResource}.
     *
     *
     *  Particular case:
     *  ----------------
     *  User can use a simplified version of the heatmap by using 0 value for both distanceX and distanceY input
     *  parameters.
     *  In such a case, each position of the{@link PointCloudResource#points}will increment by 1 the value of it's associated pixel without neighbouring  consideration.
     *
     * @param imageDimension  : not null, dimension in pixel of the computed image
     * @param tilingDimension : not null, the dimension to be used to define the tiles of the computed image.
     * @param dataSource      : points source to be used to compute the heatMap.
     * @param distanceX       : distance on the 1st direction (x) to be used to compute the gaussian function. THe distance is in **pixels**
     * @param distanceY       : distance on the 2nd direction (y) to be used to compute the gaussian function The distance is in **pixels**
     * @param algorithm       : algorithm to use in order to define the influence of each data point in the heatMap
     */
    HeatMapImage(final Dimension imageDimension, final Dimension tilingDimension, final PointCloudResource dataSource,
                 final MathTransform dataCrsToGridCenter, final MathTransform gridCornerToDataCrs,
                 final double distanceX, final double distanceY,
                 Algorithm algorithm) {
        super(new BandedSampleModel(DataBuffer.TYPE_DOUBLE, tilingDimension.width, tilingDimension.height, 1));
        this.tilingDimension = tilingDimension;
        this.imageDimension = imageDimension;
        this.dataSource = dataSource;

        this.gridCornerToDataCRS = gridCornerToDataCrs;
        this.dataCRSToGridCenter = dataCrsToGridCenter;

        this.pixelOnly = distanceX == 0 && distanceY == 0;

        if (!pixelOnly) {
            this.distanceX = distanceX;
            this.distanceXx2 = this.distanceX * 2;
            this.distanceY = distanceY;
            this.distanceYx2 = this.distanceY * 2;
        } else {
            this.distanceX = 0;
            this.distanceXx2 = 0;
            this.distanceY = 0;
            this.distanceYx2 = 0;
        }

        /*
         * Result of following algo's application is multiplied by the default amplitude HeatMapImage#A.
         */
        this.op = switch (algorithm) {
            case GAUSSIAN_MASK -> new GaussianMask(distanceX, distanceY);
            case GAUSSIAN -> new Gaussian(distanceX, distanceY);
            case EUCLIDEAN -> new Euclidean(distanceX, distanceY);
            case ONE -> new One(distanceX, distanceY);
        };
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected Raster computeTile(int tileX, int tileY, WritableRaster previous) throws Exception {
        final int startXPixel = this.getMinX() + Math.multiplyExact((tileX - getMinTileX()), getTileWidth());
        final int startYPixel = this.getMinY() + Math.multiplyExact((tileY - getMinTileY()), getTileHeight());

        if (previous != null) {
            Logger.getLogger(Loggers.APPLICATION).log(Level.FINE, "Reuse of previous raster not implemented yet in HeatMapImage.class");
        }

        if (pixelOnly) {
            var data = new double[getTileWidth() * getTileHeight()];
            var roi = Envelopes.transform(this.gridCornerToDataCRS, new Envelope2D(null, startXPixel, startYPixel, tilingDimension.width, tilingDimension.height));
            roi.setCoordinateReferenceSystem(dataSource.getCoordinateReferenceSystem());
            try (var stream = this.dataSource.batchPoints(roi, false, BATCH_SIZE)) {
                stream.forEach(geoPts -> {
                    final int nbValues = geoPts.length; //it is not necessary equals to BATCH_SIZE/2 for the last chunk
                    if (nbValues < 2) return;

                    var packedPts = new double[nbValues];
                    try {
                        dataCRSToGridCenter.transform(geoPts, 0, packedPts, 0, nbValues / 2);
                    } catch (TransformException e) {
                        throw new BackingStoreException("Cannot project data points in image space", e);
                    }

                    for (int i = 0; i < packedPts.length; i += 2) {
                        final int x = Math.max((int) Math.floor(packedPts[i]), startXPixel);
                        final int y = Math.max((int) Math.floor(packedPts[i + 1]), startYPixel);
                        data[(y - startYPixel) * tilingDimension.width + (x - startXPixel)] += 1;
                    }
                });
            }
            final DataBufferDouble db = new DataBufferDouble(data, data.length);
            return WritableRaster.createWritableRaster(getSampleModel(), db, new Point(startXPixel, startYPixel));


        } else {
            final Envelope2D imageGrid = new Envelope2D(null, startXPixel - distanceX, startYPixel - distanceY, tilingDimension.width + distanceX * 2, tilingDimension.height + distanceY * 2);
            var roi = Envelopes.transform(this.gridCornerToDataCRS, imageGrid);
            roi.setCoordinateReferenceSystem(dataSource.getCoordinateReferenceSystem());


            try (final Stream<double[]> points = this.dataSource.batchPoints(roi, false, BATCH_SIZE)) {
                var data = new double[getTileWidth() * getTileHeight()];
                points.forEach(geoPts -> {
                    final int nbValues = geoPts.length; //it is not necessary equals to BATCH_SIZE/2 for the last chunk
                    if (nbValues < 2) return;

                    var packedPts = new double[nbValues];
                    try {
                        dataCRSToGridCenter.transform(geoPts, 0, packedPts, 0, nbValues / 2);
                    } catch (TransformException e) {
                        throw new BackingStoreException("Cannot project data points in image space", e);
                    }

                    for (int i = 0; i < packedPts.length; i += 2) {
                        writeGridPoint(packedPts[i], packedPts[i + 1], data, startXPixel, startYPixel);
                    }
                });

                final DataBufferDouble db = new DataBufferDouble(data, data.length);
                return WritableRaster.createWritableRaster(getSampleModel(), db, new Point(startXPixel, startYPixel));
            }
        }
    }


    private void writeGridPoint(double x, double y, double[] tileData, final int tileMinX, final int tileMinY) {

        final double minXInfluence = x - distanceX, minYInfluence = y - distanceY;

        // Compute intersection
        final int inclusiveStartX = Math.max((int) Math.floor(minXInfluence), tileMinX);
        final int inclusiveStartY = Math.max((int) Math.floor(minYInfluence), tileMinY);
        final int exclusiveEndX = Math.min((int) Math.ceil(minXInfluence + distanceXx2), tileMinX + tilingDimension.width);
        final int exclusiveEndY = Math.min((int) Math.ceil(minYInfluence + distanceYx2), tileMinY + tilingDimension.height);


        if (exclusiveEndX - inclusiveStartX <= 0 || exclusiveEndY - inclusiveStartY <= 0) return;

        for (int j = inclusiveStartY; j < exclusiveEndY; j++) {
            for (int i = inclusiveStartX; i < exclusiveEndX; i++) {
                tileData[(j - tileMinY) * tilingDimension.width + (i - tileMinX)] += a * op.apply(i - x, j - y);
            }
        }
    }

        /**
         * {@inheritDoc}
         */
    @Override
    public ColorModel getColorModel() {
        //TODO?
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return this.imageDimension.width;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return this.imageDimension.height;
    }

//    /**
//     * todo MAX_COMPUTE
//     * @return
//     */
//    public float getMax() {
//        return max;
//    }

    public enum Algorithm {
        EUCLIDEAN, GAUSSIAN, GAUSSIAN_MASK, ONE
    }

    @FunctionalInterface
    private interface DistanceOp {
        double apply(double vx, double vy);
    }

    private static final class Gaussian implements DistanceOp {

        /**
         * - 1 / (2 . σx²)
         */
        final double invσx2;
        /**
         * - 1 / (2 . σy²)
         */
        final double invσy2;

        Gaussian(double distanceX, double distanceY) {
            var σx = distanceX / 3d;
            var σy = distanceY / 3d;
            this.invσx2 = -1 / (2 * σx * σx); // -1 to prepare the exponential exponent.
            this.invσy2 = -1 / (2 * σy * σy); // -1 to prepare the exponential exponent.
        }

        @Override
        public double apply(double vx, double vy) {
            return Math.exp(vx * vx * invσx2 + vy * vy * invσy2);
        }
    }

    private static final class GaussianMask implements DistanceOp {

        final int maxX, maxY;

        final double[] mask;

        /**
         * Distances in pixel to compute the gaussian mask
         */
        GaussianMask(final double distanceX, final double distanceY) {
            var σx = distanceX / 3d;
            var σy = distanceY / 3d;
            final double invσx2 = -1 / (2 * σx * σx); // -1 to prepare the exponential exponent.
            final double invσy2 = -1 / (2 * σy * σy); // -1 to prepare the exponential exponent.

            maxX = Math.max(1, (int) Math.ceil(distanceX));
            maxY = Math.max(1, (int)  Math.ceil(distanceY));

            mask = new double[maxX*maxY];
            for(int j=0, k = 0; j < maxY; j++) {
                final double yComponent = j * j * invσy2;
                for (int i = 0 ; i < maxX; i++) {
                    mask[k++] = Math.exp(i * i * invσx2 + yComponent);
                }
            }
        }

        /**
         * @param vx,vy : absolute distances in pixel
         */
        @Override
        public double apply(double vx, double vy) {
            final int xInd = (int) (Math.abs(vx) + 0.5); // avoid the use of Math.round
            if (xInd >= maxX) return 0;
            final int yInd = (int) (Math.abs(vy) + 0.5);
            if (yInd >= maxY) return 0;
            return mask[xInd+(maxX)*yInd];
        }
    }



    private static final class Euclidean implements DistanceOp {

        private final double maxSquaredNorm, opposite;

        Euclidean(double distanceX, double distanceY) {
            this.maxSquaredNorm = distanceX * distanceX + distanceY * distanceY;
            this.opposite = 1/maxSquaredNorm;
        }

        @Override
        public double apply(double vx, double vy) {
            var vectorSquaredNorm = vx * vx + vy * vy;
            var squaredDistanceFromEdge = maxSquaredNorm - vectorSquaredNorm;
            if (squaredDistanceFromEdge <= 0) return 0;
            return squaredDistanceFromEdge * opposite;
        }
    }

    private static final class One implements DistanceOp {

        private final double distanceX, distanceY;

        private One(double distanceX, double distanceY) {
            this.distanceX = distanceX;
            this.distanceY = distanceY;
        }

        @Override
        public double apply(double vx, double vy) {
            return (vx < distanceX && vy < distanceY ? 1 : 0);
        }
    }
}
