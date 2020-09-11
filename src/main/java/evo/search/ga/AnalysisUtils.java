package evo.search.ga;

import evo.search.util.ListUtils;
import evo.search.util.MathUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class provides several analysis metrics to evaluate individuals upon
 */
public class AnalysisUtils {

    /**
     * The spiral-likeness measures the similarity between the individuals structure
     * and a spiral. This likeness is expressed as the summed up distances between where
     * a point should sit in a spiral and the actual placed point. Therefore, it starts
     * at the first position, that the individual visits. Then, it spirals out both clock-wise
     * and counter-clockwise counting the individual distances. Finally, the minimum of the
     * clockwise and counter-clockwise likenesses is returned.
     *
     * @param chromosome the individual measured
     * @return Spiral likeness between 0.0 and infinity 0.0 means spiral
     */
    public static double spiralLikeness(final List<DiscreteGene> chromosome) {

        if (chromosome.size() == 0) return 0;

        final short positions = chromosome.get(0).getPositions();

        final List<Float> distances = ListUtils.map(chromosome, DiscreteGene::getDistance);
        distances.sort(Float::compareTo);

        double spiralCounterDistance = 0;
        double spiralClockDistance = 0;

        for (int index = 0; index < chromosome.size(); index++) {
            final DiscreteGene actualPoint = chromosome.get(index);

            spiralCounterDistance += MathUtils.polarDistance(
                    positions,
                    actualPoint.getPosition(),
                    actualPoint.getDistance(),
                    (short) (index % positions),
                    distances.get(index)
            );
            spiralClockDistance += MathUtils.polarDistance(
                    positions,
                    actualPoint.getPosition(),
                    actualPoint.getDistance(),
                    (short) Math.floorMod(-index, positions),
                    distances.get(index)
            );
        }

        if (spiralCounterDistance == 0 || spiralClockDistance == 0) return 1;

        return Math.min(spiralCounterDistance, spiralClockDistance);
    }

    /**
     * Measure for the spiral-likeness of the chromosome with rotation invariance.
     * Analyses the likeness pairwise.
     *
     * @param chromosome chromosome to measure
     * @return rotation independent spiral likeness
     */
    public static double spiralLikenessInvariant(final List<DiscreteGene> chromosome) {

        if (chromosome.size() < 2)
            return 1;

        final List<Float> distances = ListUtils.map(chromosome, DiscreteGene::getDistance);
        distances.sort(Float::compareTo);

        final short positions = chromosome.get(0).getPositions();

        double sumCounter = 0;
        double sumClock = 0;
        for (int i = 0; i < chromosome.size() - 1; i++) {
            final DiscreteGene current = chromosome.get(i);
            final int index = distances.indexOf(current.getDistance());
            if (index < 0 || index > distances.size() - 2)
                continue;
            final Float idealDistance = distances.get(index + 1);
            final DiscreteGene actualGene = chromosome.get(i + 1);

            sumCounter += MathUtils.polarDistance(
                    positions,
                    actualGene.getPosition(),
                    actualGene.getDistance(),
                    (short) ((current.getPosition() + 1) % positions),
                    idealDistance
            );
            sumClock += MathUtils.polarDistance(
                    positions,
                    actualGene.getPosition(),
                    actualGene.getDistance(),
                    (short) Math.floorMod(current.getPosition() - 1, positions),
                    idealDistance
            );

        }

        if (sumCounter == 0 || sumClock == 0)
            return 1;

        return Math.min(sumClock, sumCounter);
    }

    /**
     * Computes the trace length necessary for the {@link DiscreteGene} chromosome
     * to find the given treasure {@link DiscreteGene}.
     *
     * @param chromosome chromosome to evaluate the trace length on
     * @param treasure   treasure point to be found
     * @return trace length necessary for the individual to find the treasure
     */
    public static double traceLength(final List<DiscreteGene> chromosome, final DiscreteGene treasure) {
        double trace = 0d;

        DiscreteGene previous = new DiscreteGene(1, 0, 0d);
        for (final DiscreteGene gene : chromosome) {
            if (finds(previous, treasure)) break;
            trace += previous.distance(gene);
            previous = gene;
        }

        return trace;
    }

    /**
     * Calculates the trace length of a path consisting of the list of points.
     *
     * @param points points forming a trace
     * @return trace length of the path of points
     */
    public static double traceLength(final List<DiscreteGene> points) {
        final List<Double> distances = ListUtils.consecMap(points, DiscreteGene::distance);
        return ListUtils.sum(distances);
    }

    /**
     * Compute for two {@link DiscreteGene}s, whether the first
     * point {@code point} finds the second point {@code treasure}.
     * <p>
     * That equals the following statement:
     * {@code point.position == treasure.position && point.distance >= treasure.distance}
     *
     * @param point    Point to check, if it finds the second point.
     * @param treasure point to be found
     * @return whether the first point finds the second point
     */
    public static boolean finds(final DiscreteGene point, final DiscreteGene treasure) {
        final boolean distanceEqualOrGreater = point.getDistance() >= treasure.getDistance();
        final boolean positionEquals = point.getPosition() == treasure.getPosition();
        return positionEquals && distanceEqualOrGreater;
    }

    /**
     * Returns a filled list of points visited on each sector by the chromosome.
     * The chromosome may jump above multiple sectors. To resolve this behaviour
     * for the fitness evaluation using area coverage, the list of points need
     * consecutive position indices.
     *
     * @param chromosome chromosome with jumping genes
     * @return list of visited points with consecutive position indices
     */
    public static List<DiscreteGene> fill(final List<DiscreteGene> chromosome) {
        final ArrayList<DiscreteGene> discreteGenes = new ArrayList<>();

        ListUtils.consec(chromosome, (pointA, pointB) -> {
            discreteGenes.add(pointA);
            if (Math.abs(pointA.getPosition() - pointB.getPosition()) > 1)
                discreteGenes.addAll(inBetween(pointA, pointB));
        });

        discreteGenes.add(chromosome.get(chromosome.size() - 1));

        return discreteGenes;
    }

    /**
     * Returns a list of intersections between the line through points
     * a and b and the rays between these points.
     *
     * @param a point a for the line
     * @param b point b for the line
     * @return list of intersections between the line ab and the rays between a and b
     */
    public static List<DiscreteGene> inBetween(final DiscreteGene a, final DiscreteGene b) {
        final int positions = a.getPositions();

        final int difference = Math.abs(a.getPosition() - b.getPosition());
        final int shortestPath = Math.min(positions - difference, difference);

        if (((double) difference) == positions / 2d || difference == 0)
            return Collections.emptyList();

        final double anglePart = 2 * Math.PI / (double) positions;

        final double distanceToPointA = a.getDistance();
        final double angleAtPointA = Math.asin(Math.sin(anglePart * (shortestPath - 1)) * b.getDistance() / a.distance(b));

        final int increase = (a.getPosition() + shortestPath) % positions == b.getPosition() ? 1 : -1;

        final List<DiscreteGene> fill = new ArrayList<>();
        for (int i = 1; i < shortestPath; i++) {
            final double y = distanceToPointA * Math.sin(angleAtPointA) / Math.sin(angleAtPointA + (anglePart * i));
            fill.add(new DiscreteGene(positions, (a.getPosition() + positions + increase * i) % positions, y));
        }

        return fill;
    }

    /**
     * Calculates the triangle area in a sector between two rays given
     * the two distances of the outer edges and the amount of all rays.
     * The points have to lay on consecutive rays hence the need for a fix
     * through {@link #inBetween(DiscreteGene, DiscreteGene)} before invoking
     * this method.
     *
     * @param distanceA first outer edge distance
     * @param distanceB second outer edge distance
     * @param rayCount  amount of rays, gives the inner degree of the triangle
     * @return triangle area in a sector given two point distances
     */
    public static double areaInSector(final double distanceA, final double distanceB, final int rayCount) {
        return MathUtils.areaInTriangle((2 * Math.PI) / (double) rayCount, distanceA, distanceB);
    }

    /**
     * Calculates the maximised area covered by the points.
     * A covered area in a sector is the area of the triangle formed between
     * two points on the rays limiting the sector and the origin.
     * This fitness maximises a area already covered and every time a
     * sector is explored again, the area already covered is subtracted to
     * induce larger areas being visited the next time in a same sector.
     *
     * @param points list of points distributed on consecutive rays
     * @return maximised area covered by the points
     */
    public static double areaCovered(final List<DiscreteGene> points) {
        if (points.size() < 2)
            return 0;

        final short positions = points.get(0).getPositions();
        final double sectorAngle = (2 * Math.PI) / positions;

        final double[] maxDistance = new double[positions];

        for (final DiscreteGene gene : points) {
            final short position = gene.getPosition();
            maxDistance[position] = Math.max(gene.getDistance(), maxDistance[position]);
        }

        double area = MathUtils.areaInTriangle(sectorAngle, maxDistance[0], maxDistance[maxDistance.length - 1]);

        for (int i = 0; i < positions - 1; i++)
            area = MathUtils.areaInTriangle(sectorAngle, maxDistance[i], maxDistance[i + 1]);

        return area;
    }

    /**
     * Computes the sum of newly covered area per step of the chromosome.
     *
     * @param points path of the chromosome
     * @return sum of the newly covered area per step
     */
    public static double newAreaCovered(final List<DiscreteGene> points) {
        if (points.size() < 2)
            return 0;

        final List<DiscreteGene> filledIn = fill(points);

        final int positions = points.get(0).getPositions();
        final double[] areasCovered = new double[positions];

        return ListUtils.consecSum(filledIn, (pointA, pointB) -> {
            int index = Math.min(pointA.getPosition(), pointB.getPosition());
            final int delta = Math.abs(pointA.getPosition() - pointB.getPosition());
            if (index == 0 && delta > 1)
                index = delta;

            final double areaCoveredInSector = areaInSector(pointA.getDistance(), pointB.getDistance(), positions);
            final double newAreaCovered = areaCoveredInSector - areasCovered[index];
            areasCovered[index] = Math.max(areasCovered[index], areaCoveredInSector);

            return Math.max(newAreaCovered, 0d);
        });
    }

    /**
     * Calculates the worst case trace length of a trace barely missing a treasure
     * every time by a distance given in epsilon.
     *
     * @param points  list of points forming a path
     * @param epsilon distance the treasure is missed by
     * @return worst case scenario fitness
     */
    public static double worstCase(final List<DiscreteGene> points, final float epsilon) {
        final int size = points.size();

        if (size < 1)
            return Double.POSITIVE_INFINITY;

        final short positions = points.get(0).getPositions();
        double pathLength = points.get(0).getDistance();

        double worstCaseFactor = 0;

        for (int i = 0; i < size; i++) {
            final DiscreteGene point = points.get(i);

            if (i > 0)
                pathLength += point.distance(points.get(i - 1));

            for (int position = 0; position < positions; position++) {
                final DiscreteGene worstCase = new DiscreteGene(positions, position, point.getDistance() + epsilon);

                boolean found = false;

                for (int remain = i + 1; remain < size; remain++)
                    if (finds(points.get(remain), worstCase)) {
                        found = true;
                        break;
                    }

                if (!found)
                    pathLength += arcDistance(points.get(size - 1), worstCase);

                worstCaseFactor = Math.max(worstCaseFactor, pathLength / point.getDistance());
            }
        }

        return worstCaseFactor;
    }

    /**
     * Computes the arc distance between a chromosome point and a treasure.
     * If the distance of the chromosome point is greater than or equal to the treasure,
     * the arc stays on the points radius. Otherwise, the radius gets increased in each step
     * until it reaches the treasure.
     *
     * @param start     last point in chromosome
     * @param worstCase worst case location
     * @return length of arc between last chromosome point and the worst-case location
     */
    private static double arcDistance(final DiscreteGene start, final DiscreteGene worstCase) {
        final int distancePositions = Math.abs(start.getPosition() - worstCase.getPosition());

        if (distancePositions < 2)
            return start.distance(worstCase);

        final int steps = Math.min(distancePositions, start.getPositions() - distancePositions);
        final double angle = 2 * Math.PI / start.getPositions();

        if (start.getDistance() >= worstCase.getDistance()) {
            return steps * 2 * start.getDistance() * Math.sin(Math.PI / start.getPositions());
        } else {
            final double distanceDelta = (worstCase.getDistance() - start.getDistance()) / steps;
            double sum = 0;
            for (int i = 0; i < steps; i++)
                sum += MathUtils.lawOfCosine(
                        angle,
                        worstCase.getDistance() + distanceDelta * i,
                        worstCase.getDistance() + distanceDelta * (i + 1)
                );
            return sum;
        }
    }

}
