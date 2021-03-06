package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import io.jenetics.util.MSeq;

/**
 * Mutator which swaps whole the positions between {@link DiscreteGene}s.
 */
public class SwapDistanceMutator extends SwapPropertyMutator {

    /**
     * Standard probability constructor.
     * Utilized by the {@link evo.search.view.model.MutatorTableModel}.
     *
     * @param probability the probability of a distance swap
     */
    public SwapDistanceMutator(final double probability) {
        super(probability);
    }

    /**
     * Swap the distances of two {@link DiscreteGene}s in a
     * chromosome sequence of {@link DiscreteGene}s.
     *
     * @param mSeq chromosome sequence
     * @param a    integer position a for swap
     * @param b    integer position b for swap
     */
    @Override
    protected void swapComponent(final MSeq<DiscreteGene> mSeq, final int a, final int b) {
        final float temp = mSeq.get(a).getDistance();
        mSeq.get(a).setDistance(mSeq.get(b).getDistance());
        mSeq.get(b).setDistance(temp);
    }

}
