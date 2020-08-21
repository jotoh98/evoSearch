package evo.search.ga;

import evo.search.io.entities.Configuration;
import evo.search.util.ListUtils;
import evo.search.util.RandomUtils;
import io.jenetics.Gene;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Discrete genome carrying a {@link DiscretePoint} allele.
 */
@AllArgsConstructor
@Data
public class DiscreteGene implements Gene<DiscretePoint, DiscreteGene> {

    /**
     * Configuration providing context for the gene. Mainly used to calculate the allele.
     *
     * @see #getAllele()
     * @see DiscretePoint#positions
     */
    Configuration configuration;

    /**
     * The discrete genes position index. This is the index of the ray the gene's allele will be
     * sitting on.
     *
     * @see DiscretePoint#position
     */
    private int position;

    /**
     * The genes distance corresponding the the alleles distance.
     *
     * @see DiscretePoint#distance
     */
    private double distance;

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscretePoint getAllele() {
        return new DiscretePoint(configuration.getPositions(), position, distance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscreteGene newInstance() {
        final int position = RandomUtils.inRange(0, configuration.getPositions());
        if (configuration.isChooseWithoutPermutation())
            return new DiscreteGene(configuration, position, ListUtils.chooseRandom(configuration.getDistances()));
        return new DiscreteGene(configuration, position, distance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscreteGene newInstance(final DiscretePoint value) {
        return new DiscreteGene(configuration, value.position, value.distance);
    }

    /**
     * Checks, if a gene is valid. That means, that the allele's distance is
     * one of these in the {@link Configuration} and smaller than the positions property.
     *
     * @return whether the discrete gene is valid
     */
    @Override
    public boolean isValid() {
        final boolean distanceValid = configuration.getDistances().contains(distance);
        final boolean positionValid = configuration.getPositions() > position && position >= 0;
        return distanceValid && positionValid;
    }

    @Override
    public String toString() {
        return String.format("DiscreteGene{ pos=%d, dist=%s}", position, distance);
    }

}
