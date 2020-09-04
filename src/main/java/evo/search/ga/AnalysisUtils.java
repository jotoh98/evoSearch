package evo.search.ga;

import evo.search.util.ListUtils;
import evo.search.util.MathUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

/**
 * This class provides several analysis metrics to evaluate individuals upon
 */
public class AnalysisUtils {

    /**
     * The spiral-likeness measures the similarity between the individuals structure
     * and a spiral. This likeness is expressed as the summed up distances between where
     * a point should sit in a spiral and the actual placed point. Therefore, it starts
     * at the first position, that the individual visits. Then, it spirals out both clock-wise
     * and counter-clockwise counting the individual distances. Finally, the inverse of the minimal
     * clockwise and counter-clockwise likenesses is returned.
     *
     * @param distances  distances of the evolutions environment
     * @param chromosome the individual measured
     * @return Spiral likeness between 0.0 and 1.0. 1.0 means full spiral. 0.0 means empty individual.
     */
    public static double spiralLikeness(final List<Double> distances, final List<DiscreteGene> chromosome) {

        if (chromosome.size() == 0) return 0;

        final int positions = chromosome.get(0).getPositions();

        distances.sort(Double::compareTo);
        int spiralPositionCounter = chromosome.get(0).getPosition();
        int spiralPositionClock = chromosome.get(0).getPosition();

        double spiralCounterLikeness = 0;
        double spiralClockLikeness = 0;

        int index = 0;
        for (final DiscreteGene gene : chromosome) {
            final Point2D actualPoint = gene.getAllele();
            final DiscreteGene spiralPointCounter = new DiscreteGene(positions, spiralPositionCounter, distances.get(index));
            final DiscreteGene spiralPointClock = new DiscreteGene(positions, spiralPositionClock, distances.get(index++));

            spiralPositionCounter++;
            spiralPositionCounter %= positions;

            spiralPositionClock--;
            if (spiralPositionClock < 0) {
                spiralPositionClock = positions - 1;
            }


            spiralCounterLikeness += actualPoint.distance(spiralPointCounter.getAllele());
            spiralClockLikeness += actualPoint.distance(spiralPointClock.getAllele());
        }

        if (spiralCounterLikeness == 0 || spiralClockLikeness == 0) {
            return 1;
        }

        return 1 / Math.min(spiralCounterLikeness, spiralClockLikeness);
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

        final double sectorAngle = (2 * Math.PI) / points.get(0).getPositions();

        final AtomicReference<Double> areaCovered = new AtomicReference<>(0d);
        final AtomicInteger amount = new AtomicInteger(1);
        final List<Double> areas = ListUtils.consecMap(points, (pointA, pointB) -> {
            final int delta = Math.abs(pointA.getPosition() - pointB.getPosition());
            final double area = MathUtils.areaInTriangle(sectorAngle * delta, pointA.getDistance(), pointB.getDistance());
            areaCovered.set(areaCovered.get() + area);
            return Math.max(areaCovered.get() / amount.getAndIncrement(), 0d);
        });

        return ListUtils.sum(areas);
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
        final List<Double> areasPerStep = ListUtils.consecMap(filledIn, (pointA, pointB) -> {
            int index = Math.min(pointA.getPosition(), pointB.getPosition());
            final int delta = Math.abs(pointA.getPosition() - pointB.getPosition());
            if (index == 0 && delta > 1)
                index = delta;

            final double areaCoveredInSector = areaInSector(pointA.getDistance(), pointB.getDistance(), positions);
            final double newAreaCovered = areaCoveredInSector - areasCovered[index];
            areasCovered[index] = Math.max(areasCovered[index], areaCoveredInSector);

            return Math.max(newAreaCovered, 0d);
        });

        return ListUtils.sum(areasPerStep);
    }

    /**
     * Calculates the worst case trace length of a trace barely missing a treasure
     * every time by a distance given in epsilon.
     *
     * @param points  list of points forming a path
     * @param epsilon distance the treasure is missed by
     * @return worst case scenario fitness
     */
    public static double worstCase(final List<DiscreteGene> points, final int epsilon) {
        return IntStream
                .range(0, points.size()).mapToObj(k -> {
                    final List<DiscreteGene> list = points.subList(k, points.size() - 1);
                    final DiscreteGene worstCase = points.get(k).clone();
                    worstCase.setDistance(worstCase.getDistance() + epsilon);

                    double distance = points.get(k).getDistance();

                    if (distance == 0)
                        distance = 1;

                    final double arc = arcDistance(points.get(points.size() - 1), worstCase);

                    final double path = ListUtils
                            .consecMap(list, DiscreteGene::distance)
                            .stream()
                            .reduce(Double::sum)
                            .orElse(0d);

                    return (path + arc) / distance;
                })
                .max(Comparator.comparingDouble(Double::doubleValue))
                .orElse(Double.POSITIVE_INFINITY);
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
    private static double arcDistance(final DiscretePoint start, final DiscretePoint worstCase) {
        if (Math.abs(start.position - worstCase.position) < 2)
            return start.distance(worstCase);

        final int distancePositions = Math.abs(start.position - worstCase.position);
        final int steps = Math.min(distancePositions, start.positions - distancePositions);
        if (start.distance >= worstCase.distance) {
            return steps * 2 * start.distance * Math.sin(Math.PI / start.positions);
        } else {
            final double distanceDelta = (worstCase.distance - start.distance) / steps;
            final double angle = 2 * Math.PI / start.positions;
            double sum = 0;
            for (int i = 0; i < steps; i++)
                sum += MathUtils.polarDistance(
                        angle,
                        worstCase.distance + distanceDelta * i,
                        0,
                        worstCase.distance + distanceDelta * (i + 1)
                );
            return sum;
        }
    }

}
