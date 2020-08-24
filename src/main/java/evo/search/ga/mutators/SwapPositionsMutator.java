package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import io.jenetics.Chromosome;
import io.jenetics.MutatorResult;
import io.jenetics.SwapMutator;
import io.jenetics.util.MSeq;

import java.util.Random;

import static io.jenetics.internal.math.Randoms.indexes;

/**
 * Mutator which swaps whole the positions between {@link DiscreteGene}s.
 */
public class SwapPositionsMutator extends SwapPropertyMutator<DiscreteGene, Double> implements DiscreteAlterer {

    /**
     * Standard probability constructor.
     * Utilized by the {@link evo.search.view.model.MutatorTableModel}.
     *
     * @param probability the probability of a position swap
     */
    public SwapPositionsMutator(final double probability) {
        super(probability);
    }

    /**
     * Swap the positions of two {@link DiscreteGene}s in a
     * chromosome sequence of {@link DiscreteGene}s.
     *
     * @param mSeq chromosome sequence
     * @param a    integer position a for swap
     * @param b    integer position b for swap
     */
    @Override
    protected void swapComponent(final MSeq<DiscreteGene> mSeq, final int a, final int b) {
        final int temp = mSeq.get(a).getPosition();
        mSeq.get(a).setPosition(mSeq.get(b).getPosition());
        mSeq.get(b).setPosition(temp);
    }

}
