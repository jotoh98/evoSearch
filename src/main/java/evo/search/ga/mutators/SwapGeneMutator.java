package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import io.jenetics.SwapMutator;

public class SwapGeneMutator extends SwapMutator<DiscreteGene, Double> implements DiscreteMutator {
    public SwapGeneMutator(final double probability) {
        super(probability);
    }
}
