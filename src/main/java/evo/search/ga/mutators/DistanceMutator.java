package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import evo.search.io.entities.Configuration;
import io.jenetics.Mutator;
import lombok.Setter;

import java.util.Random;

/**
 * The distance mutator alters the distance property of the chromosomes
 * genes individually by a given distance.
 *
 * @author jotoh
 */
public class DistanceMutator extends Mutator<DiscreteGene, Double> implements DiscreteAlterer {

    /**
     * Configuration for the alterers to work with.
     */
    @Setter
    Configuration configuration;

    /**
     * Standard probability constructor.
     * Utilized by the {@link evo.search.view.ConfigPanel}.
     *
     * @param probability the mutation probability
     */
    public DistanceMutator(final double probability) {
        super(probability);
    }

    /**
     * Mutate the genes distance.
     * Adds a random value from the interval [-1.0, 1.0) to the distance.
     *
     * @param gene   gene to mutate
     * @param random random object for random doubles
     * @return gene with mutated distance
     */
    @Override
    protected DiscreteGene mutate(final DiscreteGene gene, final Random random) {
        final double delta = configuration == null ? 1d : configuration.getDistanceMutationDelta();
        final double newDistance = gene.getDistance() + (random.nextDouble() * 2 - 1) * delta;
        return new DiscreteGene(gene.getPositions(), gene.getPosition(), Math.max(0, newDistance));
    }

}
