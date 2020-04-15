package evo.search.ga;

import evo.search.Environment;
import io.jenetics.Gene;
import io.jenetics.util.RandomRegistry;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Discrete genome carrying a {@link DiscretePoint} allele.
 */
@AllArgsConstructor
public class DiscreteGene implements Gene<DiscretePoint, DiscreteGene> {

    /**
     * The discrete allele.
     */
    private DiscretePoint allele;

    /**
     * Create a new gene from its {@link DiscretePoint} allele.
     *
     * @param value Discrete point allele to clone into the new genome instance.
     * @return A discrete gene with a given allele.
     */
    public static DiscreteGene of(DiscretePoint value) {
        return new DiscreteGene(value.clone());
    }

    /**
     * Create a new gene from a given distance. Shuffles a position.
     *
     * @param distance Distance for the shuffled discrete gene.
     * @return A discrete gene with a given distance with a shuffled position.
     */
    public static DiscreteGene of(double distance) {
        int availablePositions = Environment.getInstance().getConfiguration().getPositions();
        int position = RandomRegistry.random().nextInt(availablePositions);
        return new DiscreteGene(new DiscretePoint(position, distance));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscretePoint getAllele() {
        return allele;
    }

    /**
     * {@inheritDoc}
     * For a {@link DiscreteGene}, it has to pull a valid position from the
     * {@link Environment}'s positions and distances.
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
        return of(value);
    }

    /**
     * Checks, if a gene is valid. That means, that the allele's distance is
     * one of these in the {@link Environment} and smaller than the
     * {@link Environment}'s position property.
     *
     * @return Whether the discrete gene is valid.
     */
    @Override
    public boolean isValid() {
        boolean distanceValid = Environment.getInstance().getConfiguration().getDistances().contains(allele.getDistance());
        boolean positionValid = Environment.getInstance().getConfiguration().getPositions() >= allele.getPosition();
        return distanceValid && positionValid;
    }
}
