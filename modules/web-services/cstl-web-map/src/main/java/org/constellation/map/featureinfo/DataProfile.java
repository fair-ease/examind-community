
package org.constellation.map.featureinfo;

import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

import java.util.function.Consumer;
import java.util.stream.LongStream;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.coverage.grid.GridCoverageProcessor;
import org.apache.sis.coverage.grid.GridExtent;
import org.apache.sis.coverage.grid.GridGeometry;
import org.apache.sis.coverage.grid.GridRoundingMode;
import org.apache.sis.geometry.AbstractEnvelope;
import org.apache.sis.geometry.DirectPosition2D;
import org.apache.sis.image.Interpolation;
import org.apache.sis.image.PixelIterator;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.transform.LinearTransform;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.apache.sis.util.Utilities;
import org.apache.sis.util.collection.BackingStoreException;
import org.geotoolkit.coverage.grid.GridCoverageStack;
import org.geotoolkit.coverage.grid.GridIterator;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.util.grid.GridTraversal;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.LineString;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public class DataProfile implements Spliterator<DataProfile.DataPoint> {

    private static class Extractor {
        int[] sliceCoord;
        PixelIterator ite;

        public Extractor(int[] sliceCoord, PixelIterator ite) {
            this.sliceCoord = sliceCoord;
            this.ite = ite;
        }
    }

    private final List<Extractor> extractors = new ArrayList<>();

    /**
     * Cache des points de la polyligne d'entrée projetée dans la grille. On fait un tableau de valeurs contigües pour de
     * meilleures performances (MathTransform plus efficace, structure plus compacte, etc.).
     */
    private final double[] gridPoints;

    private final GridCalculator distanceCalculatorTemplate;

    private final CoordinateReferenceSystem lineCrs;

    private final MathTransform gridCornerToLineCrs;

    private int[] templateSize;

    private SegmentProfile currentSegment;
    private final int dimension;
    private int segmentIdx = 0;

    private DataPoint lastPoint;

    public DataProfile(GridCoverage datasource, final LineString profile) throws FactoryException, TransformException {

        this.lineCrs = JTS.findCoordinateReferenceSystem(profile);
        if (lineCrs == null) {
            throw new IllegalArgumentException("La géométrie envoyée ne stipule pas de système de référencement");
        } else if (lineCrs.getCoordinateSystem().getDimension() != 2) {
            throw new IllegalArgumentException("Only 2D geometries accepted.");
        }

        dimension = 2;

        /* Forcing resample of data source into profile CRS fix some distance computing on some datasets (NetCDF grd
         * for example). Also, bilinear interpolation could trigger better sampling.
         */
        final JTSEnvelope2D profileEnv = JTS.toEnvelope(profile);
        final GridGeometry workGrid = subgridAndReproject(datasource.getGridGeometry(), profileEnv);

        final GridCoverageProcessor gcp = new GridCoverageProcessor();
        gcp.setInterpolation(Interpolation.BILINEAR);
        datasource = gcp.resample(datasource, workGrid);

        //Transformation de la coordonnée dans l'image
        final GridGeometry gridGeometry = datasource.getGridGeometry();
        this.gridCornerToLineCrs = gridGeometry.getGridToCRS(PixelInCell.CELL_CORNER);

        final CoordinateSequence lineSeq = profile.getCoordinateSequence();
        final int nbPts = lineSeq.size();
        gridPoints = new double[nbPts * dimension];
        for (int i = 0, j = 0; i < nbPts; i++, j += dimension) {
            final Coordinate c = lineSeq.getCoordinate(i);
            gridPoints[j] = c.x;
            gridPoints[j + 1] = c.y;
        }

        final GridEnvelope globalExtent = gridGeometry.getExtent();
        /*
         * Converting values from geographic to coverage rendering must imply translation from coverage grid to rendered
         * image origin. As extractors are built using a null extent on rendering, the translation is to move point from
         * grid minimum to space origin (0).
         */
        final LinearTransform gridToOrigin = MathTransforms.translation(
                LongStream.of(globalExtent.getLow().getCoordinateValues())
                        .mapToDouble(v -> v)
                        .toArray()
        ).inverse();
        final MathTransform crsToRendering = MathTransforms.concatenate(gridCornerToLineCrs.inverse(), gridToOrigin);
        crsToRendering.transform(gridPoints, 0, gridPoints, 0, nbPts);

        buildExtractors(datasource);

        //compute a sample of expected dimension
        final int nbSamples = datasource.getSampleDimensions().size();
        final int nbDim = globalExtent.getDimension()-2;
        templateSize = new int[1+nbDim];
        templateSize[0] = nbSamples;
        for (int i=0;i<nbDim;i++) {
            templateSize[i+1] = (int) globalExtent.getSize(i+2);
        }

        distanceCalculatorTemplate = new GridCalculator(gridGeometry);
        currentSegment = new SegmentProfile(segmentIdx);
    }

    private void buildExtractors(GridCoverage coverage) {
        final GridExtent extent = coverage.getGridGeometry().getExtent();
        final int[] movableIndices = new int[extent.getDimension()];
        Arrays.fill(movableIndices, 1);
        movableIndices[0] = 0;
        movableIndices[1] = 0;

        final GridIterator gridIterator = new GridIterator(extent, movableIndices);

        while (gridIterator.hasNext()) {
            final GridEnvelope slice = gridIterator.next();
            final int[] crd = new int[movableIndices.length-2];
            for (int i=0;i<crd.length;i++) {
                crd[i] = (int) slice.getLow(i+2);
            }

            GridCoverage c = (GridCoverage) coverage;
            while (c instanceof GridCoverageStack) {
                GridCoverageStack cs = (GridCoverageStack) c;
                c = cs.coverageAtIndex(crd[cs.zDimension-2]);
            }

            RenderedImage im = c.forConvertedValues(true).render(null);
            extractors.add(new Extractor(crd, PixelIterator.create(im)));
        }
    }

    @Override
    public Spliterator<DataProfile.DataPoint> trySplit() {
        return null;
    }


    @Override
    public boolean tryAdvance(Consumer<? super DataProfile.DataPoint> action) {
        if (currentSegment == null) return false;

        DataPoint nextPoint;
        try {
            // First point of the polyline
            if (lastPoint == null) {
                nextPoint = currentSegment.compute(currentSegment.start, currentSegment.start);
            } else {
                do {
                    nextPoint = currentSegment.nextPoint();
                    // Emit last point of the segment (That is also the first point of the next one).
                    if (nextPoint == null) {
                        nextPoint = currentSegment.compute(currentSegment.end, currentSegment.end);
                        currentSegment = nextSegment();
                    }

                } while (nextPoint == null && currentSegment != null);
            }
        } catch (TransformException|FactoryException ex) {
            throw new BackingStoreException("Conversions between dataset grid and geographic space failed", ex);
        }

        if (nextPoint == null) {
            return false;
        }

        lastPoint = nextPoint;
        action.accept(nextPoint);

        return true;
    }

    private SegmentProfile nextSegment() throws TransformException, FactoryException {
        segmentIdx += dimension;
        // last point cannot be considered a segment start
        if (segmentIdx + dimension >= gridPoints.length) return null;
        return new SegmentProfile(segmentIdx);
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | IMMUTABLE | NONNULL;
    }

    public static class DataPoint {
        final DirectPosition2D geoLocation;
        final Point2D.Double gridLocation;
        /**
         * Distance in meters.
         */
        final double distanceFromPrevious;
        Object value;

        public DataPoint(
                DirectPosition2D geoLocation,
                Point2D.Double gridLocation,
                double distanceFromLastPoint
        ) {
            this.geoLocation = geoLocation;
            this.gridLocation = gridLocation;
            this.distanceFromPrevious = distanceFromLastPoint;
        }

        @Override
        public String toString() {
            return String.format(
                    "DataPoint{geoLocation=%s, gridLocation=%s, distanceFromPrevious=%s, value=%s}",
                    geoLocation, gridLocation, distanceFromPrevious, value instanceof double[] ? Arrays.toString(((double[]) value)) : value
            );
        }
    }

    /**
     * HACK: This complex strategy is needed only because {@link GridCoverageProcessor resample operator } does not
     * allow to set grid geometry rounding mode. If it were, we'd let it derive target geometry. However, currently, it
     * uses nearest mode by default, and it happen to create grid geometries that does not properly enclose requested
     * envelope.
     * What we do currently is search an approximation of source dataset resolution expressed in target (polyline) CRS.
     * Then, we create a fake affine geometry to represent evaluation space.
     *
     * @param source Grid geometry of source dataset. We want to "reproject" it, and conserve a resolution close to it.
     * @param target The area of interest, expressed in evaluation space.
     * @return A grid geometry to use as resampling target.
     */
    private static GridGeometry subgridAndReproject(final GridGeometry source, final Envelope target) throws TransformException, FactoryException {
        final CoordinateReferenceSystem targetCrs = target.getCoordinateReferenceSystem();
        final CoordinateReferenceSystem sourceCrs = source.isDefined(GridGeometry.CRS) ? source.getCoordinateReferenceSystem() : null;

        if (targetCrs == null || sourceCrs == null || Utilities.equalsIgnoreMetadata(sourceCrs, targetCrs)) {
            return source.derive()
                    .rounding(GridRoundingMode.ENCLOSING)
                    .subgrid(target)
                    .build();
        }

        final MathTransform sourceCrsToTargetCrs = CRS.findOperation(sourceCrs, targetCrs, null).getMathTransform();
        final MathTransform sourceToTarget = MathTransforms.concatenate(source.getGridToCRS(PixelInCell.CELL_CENTER), sourceCrsToTargetCrs);
        final DirectPosition medianPoint = sourceCrsToTargetCrs.inverse().transform(AbstractEnvelope.castOrCopy(target).getMedian(), null);
        final MathTransform targetGridToCrs = MathTransforms.linear(
                Matrices.createAffine(
                        sourceToTarget.derivative(medianPoint),
                        target.getLowerCorner()
                )
        );
        final GridGeometry reprojectedGrid = new GridGeometry(PixelInCell.CELL_CENTER, targetGridToCrs, target, GridRoundingMode.ENCLOSING);
        return reprojectedGrid;
    }

    /**
     * Computes intermediate points on a specific segment. The aim is to create "median" points between borders.
     * It does NOT compute neither start nor end points of the segment.
     */
    private class SegmentProfile {
        private final Point2D.Double start, end;
        private Point2D.Double previous;
        private final Iterator<double[]> traversal;
        private final GridCalculator distanceCalculator;
        private double distanceToSegmentStart = 0;

        private SegmentProfile(int startPointIdx) throws TransformException, FactoryException {
            final double[] segment = new double[dimension * 2];
            System.arraycopy(gridPoints, startPointIdx, segment, 0, segment.length);
            traversal = GridTraversal.stream(segment, dimension, false, false).iterator();
            start = previous = new Point2D.Double(segment[0], segment[1]);
            end = new Point2D.Double(segment[2], segment[3]);
            distanceCalculator = distanceCalculatorTemplate.copy();
            distanceCalculator.setStart(start);
        }

        private DataProfile.DataPoint nextPoint() throws TransformException {
            if (!traversal.hasNext()) return null;
            final double[] next = traversal.next();
            final Point2D.Double current = new Point2D.Double(next[0], next[1]);

            final DataPoint point = compute(previous, current);

            distanceToSegmentStart += point.distanceFromPrevious;
            previous = current;

            return point;
        }

        private DataPoint compute(final Point2D.Double previous, final Point2D.Double current) throws TransformException {
            /* Pour avoir la bonne valeur de pixel, on doit récupérer le pixel qui contient le segment entre les deux
             * points. Si on prend le pixel sur lequel est le dernier point trouvé, on s'expose à un risque de décalage si
             * le dit point est sur une bordure du pixel (ce qui arrivera dans 80% des cas, c'est le but du GridTraversal :
             * trouver les intersections entre la polyligne et la grille de pixels).
             */
            final DirectPosition2D subPixelMedian = new DirectPosition2D(previous.x + (current.x - previous.x) / 2, previous.y + (current.y - previous.y) / 2);
            int px = (int) subPixelMedian.x;
            int py = (int) subPixelMedian.y;

            final DirectPosition2D geoLoc = new DirectPosition2D(lineCrs);
            gridCornerToLineCrs.transform(subPixelMedian, geoLoc);

            distanceCalculator.setDest(subPixelMedian);
            final double distanceFromSegmentStart = distanceCalculator.getDistance();
            final double distanceToPreviousPoint = distanceFromSegmentStart - distanceToSegmentStart;

            final DataPoint dp = new DataPoint(geoLoc, subPixelMedian, distanceToPreviousPoint);
            dp.value = Array.newInstance(double.class, templateSize);

            for (Extractor ext : extractors) {
                try {
                    ext.ite.moveTo(px, py);
                    for (int b=0;b<templateSize[0];b++) {
                        double v = ext.ite.getSampleDouble(b);
                        Object array = dp.value;
                        int index = b;
                        for (int k=0;k<ext.sliceCoord.length;k++) {
                            array = Array.get(array, index);
                            index = ext.sliceCoord[k];
                        }
                        ((double[]) array)[index] = v;
                    }
                } catch (java.lang.IndexOutOfBoundsException ex) {
                    //outside image
                    dp.value = null;
                }
            }
            return dp;
        }
    }
}
