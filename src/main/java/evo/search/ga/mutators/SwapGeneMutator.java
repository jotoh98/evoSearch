package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import io.jenetics.SwapMutator;

/**
 * Mutator which swaps whole genes thus changing
 * their position in the chromosomes sequence.
 */
public class SwapGeneMutator extends SwapMutator<DiscreteGene, Double> implements DiscreteAlterer {

    /**
     * Standard probability constructor.
     * Utilized by the {@link evo.search.view.ConfigPanel}.
     *
     * @param probability the crossover probability
     */
    public SwapGeneMutator(final double probability) {
        super(probability);
    }

}
