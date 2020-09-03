package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import evo.search.util.RandomUtils;
import io.jenetics.Mutator;

import java.util.Random;

/**
 * The position mutator alters the position property of the chromosomes
 * genes individually by a given chance.
 *
 * @author jotoh
 */
public class PositionMutator extends Mutator<DiscreteGene, Double> implements DiscreteAlterer {

    /**
     * Standard probability constructor.
     * Utilized by the {@link evo.search.view.ConfigPanel}.
     *
     * @param probability the mutation probability
     */
    public PositionMutator(final double probability) {
        super(probability);
    }

    /**
     * Mutate the genes position.
     * Chooses a new position randomly.
     *
     * @param gene   gene to mutate
     * @param random random object for random doubles
     * @return gene with mutated position
     */
    @Override
    protected DiscreteGene mutate(final DiscreteGene gene, final Random random) {
        final int newPosition = RandomUtils.inRange(0, gene.getPositions());
        return new DiscreteGene(gene.getPositions(), newPosition, gene.getDistance());
    }

}
