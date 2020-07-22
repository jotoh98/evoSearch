package evo.search.ga;

import evo.search.io.entities.Configuration;
import evo.search.util.ListUtils;
import evo.search.util.MathUtils;
import io.jenetics.Chromosome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param chromosome The individual measured.
     * @return Spiral likeness between 0.0 and 1.0. 1.0 means full spiral. 0.0 means empty individual.
     */
    public static double spiralLikeness(DiscreteChromosome chromosome) {


        final Configuration configuration = chromosome.getConfiguration();
        List<Double> distances = configuration.getDistances();
        final int positions = configuration.getPositions();

        if (chromosome.toSeq().size() == 0) {
            return 0;
        }

        distances.sort(Double::compareTo);
        int spiralPositionCounter = chromosome.getGene(0).getAllele().getPosition();
        int spiralPositionClock = chromosome.getGene(0).getAllele().getPosition();

        double spiralCounterLikeness = 0;
        double spiralClockLikeness = 0;

        int index = 0;
        for (DiscreteGene gene : chromosome) {
            final DiscretePoint actualPoint = gene.getAllele();
            final DiscretePoint spiralPointCounter = new DiscretePoint(positions, spiralPositionCounter, distances.get(index));
            final DiscretePoint spiralPointClock = new DiscretePoint(positions, spiralPositionClock, distances.get(index++));

            spiralPositionCounter++;
            spiralPositionCounter %= positions;

            spiralPositionClock--;
            if (spiralPositionClock < 0) {
                spiralPositionClock = positions - 1;
            }


            spiralCounterLikeness += actualPoint.distance(spiralPointCounter);
            spiralClockLikeness += actualPoint.distance(spiralPointClock);
        }

        if (spiralCounterLikeness == 0 || spiralClockLikeness == 0) {
            return 1;
        }

        return 1 / Math.min(spiralCounterLikeness, spiralClockLikeness);
    }

    /**
     * Computes the trace length necessary for the {@link DiscreteChromosome}
     * necessary to find the given treasure {@link DiscretePoint}.
     *
     * @param chromosome Chromosome to evaluate the trace length on.
     * @param treasure   Treasure point to be found.
     * @return Trace length necessary for the individual to find the treasure.
     */
    public static double trace(Chromosome<DiscreteGene> chromosome, DiscretePoint treasure) {
        double trace = 0d;

        DiscretePoint previous = new DiscretePoint(1, 0, 0d);
        for (DiscreteGene gene : chromosome) {
            if (finds(previous, treasure)) {
                break;
            }
            DiscretePoint current = gene.getAllele();
            trace += previous.distance(current);
            previous = current;
        }

        return trace;
    }

    public static double trace(Chromosome<DiscreteGene> chromosome) {
        final List<DiscretePoint> points = ListUtils.from(chromosome.iterator())
                .stream()
                .map(DiscreteGene::getAllele)
                .collect(Collectors.toList());
        return trace(points);
    }

    public static double trace(final List<DiscretePoint> points) {
        return ListUtils
                .consecutive(points, DiscretePoint::distance)
                .stream()
                .reduce(Double::sum)
                .orElse(0d);
    }

    /**
     * Compute for two {@link DiscretePoint}s, whether the first
     * point {@code point} finds the second point {@code treasure}.
     * <p>
     * That equals the following statement:
     * {@code point.position == treasure.position && point.distance >= treasure.distance}
     *
     * @param point    Point to check, if it finds the second point.
     * @param treasure Point to be found.
     * @return Whether the first point finds the second point.
     */
    public static boolean finds(final DiscretePoint point, final DiscretePoint treasure) {
        boolean distanceEqualOrGreater = point.getDistance() >= treasure.getDistance();
        boolean positionEquals = point.getPosition() == point.getPosition();
        return positionEquals && distanceEqualOrGreater;
    }

    public static List<DiscretePoint> fill(final Chromosome<DiscreteGene> chromosome) {
        final ArrayList<DiscretePoint> discretePoints = new ArrayList<>();

        int i = 0;

        for (; i < chromosome.length() - 1; i++) {
            final DiscretePoint pointA = chromosome.get(i).getAllele();
            final DiscretePoint pointB = chromosome.get(i + 1).getAllele();
            discretePoints.add(pointA);
            if (Math.abs(pointA.position - pointB.position) > 1)
                discretePoints.addAll(inBetween(pointA, pointB));
        }

        discretePoints.add(chromosome.get(i).getAllele());

        return discretePoints;
    }


    public static List<DiscretePoint> inBetween(DiscretePoint a, DiscretePoint b) {
        final int difference = Math.abs(a.position - b.position);
        if (((double) difference) == a.positions / 2d || difference == 0)
            return Collections.emptyList();

        final double anglePart = 1 / (double) a.positions * 2 * Math.PI;

        final double fullAngle = difference * anglePart;
        final double pointsDistance = Math.sqrt(a.getDistanceSquared() + b.getDistanceSquared() - 2 * a.distance * b.distance * Math.cos(fullAngle));

        final double openAngle = Math.asin(b.distance * Math.sin(fullAngle) / pointsDistance);

        final ArrayList<DiscretePoint> points = new ArrayList<>();

        final int startPosition = Math.min(a.position, b.position);
        for (int currentAngleIndex = 1; currentAngleIndex < difference; currentAngleIndex++) {

            final double currentAngle = currentAngleIndex * anglePart;
            final double closingAngle = Math.PI - openAngle - currentAngle;

            final double newDistance = Math.sin(openAngle) / Math.sin(closingAngle) * a.distance;

            points.add(new DiscretePoint(a.positions, startPosition + currentAngleIndex, newDistance));
        }

        return points;
    }

    public static double areaInSector(double distanceA, double distanceB, int positions) {
        return 0.5 * distanceA * distanceB * Math.sin((2 * Math.PI) / (double) positions);
    }

    public static double areaExplored(final List<DiscretePoint> points) {
        if (points.size() < 2)
            return 0;

        final int positions = points.get(0).positions;

        final double[] maxDistances = new double[positions * 2];

        return ListUtils
                .consecutive(points, (pointA, pointB) -> {


                    if (pointA.getPosition() > pointB.getPosition()) {
                        final DiscretePoint temp = pointA;
                        pointA = pointB;
                        pointB = temp;
                    }

                    final int positionA = pointA.getPosition();
                    final int positionB = pointB.getPosition();

                    if (positionA == positionB)
                        return 0;


                    final int index = 2 * (positionA == 0 && positionB == positions - 1 ? positions - 1 : positionA);

                    if (index >= maxDistances.length || 2 * positionB >= maxDistances.length)
                        return Double.POSITIVE_INFINITY;


                    final double distanceA = pointA.getDistance();
                    final double distanceB = pointA.getDistance();

                    final double maxA = maxDistances[index];
                    final double maxB = maxDistances[index + 1];

                    if (distanceA > maxA)
                        maxDistances[index] = distanceA;

                    if (distanceB > maxB)
                        maxDistances[index + 1] = distanceB;

                    if (distanceA >= maxA && distanceB >= maxB)
                        return areaInSector(distanceA, distanceB, positions) - areaInSector(maxA, maxB, positions);

                    else if (distanceA < maxA && distanceB < maxB)
                        return 0;

                    final double newDistance = pointA.distance(pointB);
                    final double oldDistance = MathUtils.distance(0, maxA, (2 * Math.PI) / positions, maxB);

                    final double newFactor = Math.min(maxA / distanceA, maxB / distanceB);
                    final double oldFactor = Math.min(distanceB / maxB, distanceA / maxA);
                    final double offsetDistance = Math.max(distanceA - maxA, distanceB - maxB);

                    return MathUtils.areaInTriangle(offsetDistance, newDistance * newFactor, oldDistance * oldFactor);
                })
                .stream()
                .map(Number::doubleValue)
                .reduce(Double::sum)
                .orElse(Double.NEGATIVE_INFINITY);
    }

}
