package evo.search.ga;

import evo.search.io.entities.Configuration;
import io.jenetics.Chromosome;

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

}
