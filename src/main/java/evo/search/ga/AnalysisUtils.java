package evo.search.ga;

import evo.search.io.entities.Configuration;
import evo.search.util.ListUtils;
import io.jenetics.Chromosome;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
     * @param chromosome the individual measured
     * @return Spiral likeness between 0.0 and 1.0. 1.0 means full spiral. 0.0 means empty individual.
     */
    public static double spiralLikeness(final DiscreteChromosome chromosome) {


        final Configuration configuration = chromosome.getConfiguration();
        final List<Double> distances = configuration.getDistances();
        final int positions = configuration.getPositions();

        if (chromosome.toSeq().size() == 0) {
            return 0;
        }

        distances.sort(Double::compareTo);
        int spiralPositionCounter = chromosome.getGene(0).getPosition();
        int spiralPositionClock = chromosome.getGene(0).getPosition();

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
     * Computes the trace length necessary for the {@link DiscreteChromosome}
     * necessary to find the given treasure {@link DiscreteGene}.
     *
     * @param chromosome chromosome to evaluate the trace length on
     * @param treasure   treasure point to be found
     * @return trace length necessary for the individual to find the treasure
     */
    public static double traceLength(final Chromosome<DiscreteGene> chromosome, final DiscreteGene treasure) {
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
        return ListUtils
                .consecMap(points, DiscreteGene::distance)
                .stream()
                .reduce(Double::sum)
                .orElse(0d);
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
    public static List<DiscreteGene> fill(final Chromosome<DiscreteGene> chromosome) {
        final ArrayList<DiscreteGene> discreteGenes = new ArrayList<>();

        int i = 0;

        for (; i < chromosome.length() - 1; i++) {
            final DiscreteGene pointA = chromosome.get(i);
            final DiscreteGene pointB = chromosome.get(i + 1);
            discreteGenes.add(pointA);
            if (Math.abs(pointA.getPosition() - pointB.getPosition()) > 1)
                discreteGenes.addAll(inBetween(pointA, pointB));
        }

        discreteGenes.add(chromosome.get(i));

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
        final int difference = Math.abs(a.getPosition() - b.getPosition());
        if (((double) difference) == a.getPositions() / 2d || difference == 0)
            return Collections.emptyList();

        final double anglePart = 1 / (double) a.getPositions() * 2 * Math.PI;

        final double fullAngle = difference * anglePart;
        final double pointsDistance = Math.sqrt(a.getDistanceSquared() + b.getDistanceSquared() - 2 * a.getDistance() * b.getDistance() * Math.cos(fullAngle));

        final double openAngle = Math.asin(b.getDistance() * Math.sin(fullAngle) / pointsDistance);

        final ArrayList<DiscreteGene> points = new ArrayList<>();

        final int startPosition = Math.min(a.getPosition(), b.getPosition());
        for (int currentAngleIndex = 1; currentAngleIndex < difference; currentAngleIndex++) {

            final double currentAngle = currentAngleIndex * anglePart;
            final double closingAngle = Math.PI - openAngle - currentAngle;

            final double newDistance = Math.sin(openAngle) / Math.sin(closingAngle) * a.getDistance();

            points.add(new DiscreteGene(a.getPositions(), startPosition + currentAngleIndex, newDistance));
        }

        return points;
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
        return 0.5 * distanceA * distanceB * Math.sin((2 * Math.PI) / (double) rayCount);
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

        final double[] areasCovered = new double[points.get(0).getPositions()];

        return ListUtils
                .consecMap(points, (pointA, pointB) -> {
                    int index = Math.min(pointA.getPosition(), pointB.getPosition());
                    final int delta = Math.abs(pointA.getPosition() - pointB.getPosition());

                    if (index == 0 && delta > 1)
                        index = delta;

                    if (delta == 0 || delta == pointA.getPositions() / 2d)
                        return 0d;

                    final double areaInSector = areaInSector(pointA.getDistance(), pointB.getDistance(), pointA.getPositions());
                    final double areaExplored = areaInSector - areasCovered[index];
                    areasCovered[index] = Math.max(areaInSector, areasCovered[index]);
                    return areaExplored;
                })
                .stream()
                .reduce(Double::sum)
                .orElse(0d);
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
                    list.add(worstCase);
                    double distance = points.get(k).getDistance();

                    if (distance == 0)
                        distance = 1;

                    return ListUtils.consecMap(list, DiscreteGene::distance).stream().reduce(Double::sum).orElse(0d) / distance;
                })
                .max(Comparator.comparingDouble(Double::doubleValue))
                .orElse(Double.POSITIVE_INFINITY);
    }

}
