package evo.search.ga;

import evo.search.io.entities.Configuration;
import io.jenetics.Gene;
import lombok.Value;

/**
 * Discrete genome carrying a {@link DiscretePoint} allele.
 */
@Value
public class DiscreteGene implements Gene<DiscretePoint, DiscreteGene> {

    Configuration configuration;

    int position;

    double distance;

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
