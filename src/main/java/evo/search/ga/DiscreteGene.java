package evo.search.ga;

import evo.search.Experiment;
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
        int availablePositions = Experiment.getInstance().getPositions();
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
     * {@link Experiment}'s positions and distances.
     */
    @Override
    public DiscreteGene newInstance() {
        int positions = Experiment.getInstance().getPositions();
        List<Double> distances = Experiment.getInstance().getDistances();
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
     * Checks, if a gene is valid. That means, than the alleles distance is
     * one of these in the {@link Experiment} and smaller than the
     * {@link Experiment}'s position property.
     *
     * @return Whether the discrete gene is valid.
     */
    @Override
    public boolean isValid() {
        boolean distanceValid = Experiment.getInstance().getDistances().contains(allele.getDistance());
        boolean positionValid = Experiment.getInstance().getPositions() >= allele.getPosition();
        return distanceValid && positionValid;
    }
}
