package evo.search.ga;

import evo.search.Environment;
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
        int positions = Environment.getInstance().getConfiguration().getPositions();
        List<Double> distances = Environment.getInstance().getConfiguration().getDistances();
        int position = RandomRegistry.random().nextInt(positions);
        int index = RandomRegistry.random().nextInt(distances.size());
        return new DiscreteGene(new DiscretePoint(position, distances.get(index)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscreteGene newInstance(DiscretePoint value) {
        return new DiscreteGene(configuration, value.position, value.distance);
    }

    /**
     * Checks, if a gene is valid. That means, that the allele's distance is
     * one of these in the {@link Configuration} and smaller than the positions property.
     *
     * @return Whether the discrete gene is valid.
     */
    @Override
    public boolean isValid() {
        boolean distanceValid = configuration.getDistances().contains(distance);
        boolean positionValid = configuration.getPositions() > position && position >= 0;
        return distanceValid && positionValid;
    }

    @Override
    public String toString() {
        return String.format("DiscreteGene{ pos=%d, dist=%s}", position, distance);
    }
}
