package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import evo.search.view.model.MutatorTableModel;
import io.jenetics.SwapMutator;

/**
 * Mutator which swaps whole genes thus changing
 * their position in the chromosomes sequence.
 */
public class SwapGeneMutator extends SwapMutator<DiscreteGene, Double> implements DiscreteAlterer {
    /**
     * Standard probability constructor.
     * Utilized by the {@link evo.search.view.model.MutatorTableModel}.
     *
     * @param probability The crossover probability.
     * @see MutatorTableModel#getSelected()
     */
    public SwapGeneMutator(final double probability) {
        super(probability);
    }
}
