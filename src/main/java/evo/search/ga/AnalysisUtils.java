package evo.search.ga;

import evo.search.Environment;

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

        List<Double> distances = Environment
                .getInstance()
                .getConfiguration()
                .getDistances();

        if (chromosome.toSeq().size() == 0) {
            return 0;
        }

        int amountPositions = Environment
                .getInstance()
                .getConfiguration()
                .getPositions();

        distances.sort(Double::compareTo);
        int spiralPositionCounter = chromosome.getGene(0).getAllele().getPosition();
        int spiralPositionClock = chromosome.getGene(0).getAllele().getPosition();

        double spiralCounterLikeness = 0;
        double spiralClockLikeness = 0;

        int index = 0;
        for (DiscreteGene gene : chromosome) {
            final DiscretePoint actualPoint = gene.getAllele();
            final DiscretePoint spiralPointCounter = new DiscretePoint(spiralPositionCounter, distances.get(index));
            final DiscretePoint spiralPointClock = new DiscretePoint(spiralPositionClock, distances.get(index++));

            spiralPositionCounter++;
            spiralPositionCounter %= amountPositions;

            spiralPositionClock--;
            if (spiralPositionClock < 0) {
                spiralPositionClock = amountPositions - 1;
            }


            spiralCounterLikeness += actualPoint.distance(spiralPointCounter);
            spiralClockLikeness += actualPoint.distance(spiralPointClock);
        }

        if (spiralCounterLikeness == 0 || spiralClockLikeness == 0) {
            return 1;
        }

        return 1 / Math.min(spiralCounterLikeness, spiralClockLikeness);
    }


}
