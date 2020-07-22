package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.view.model.MutatorTableModel;
import io.jenetics.Chromosome;
import io.jenetics.MutatorResult;
import io.jenetics.SwapMutator;
import io.jenetics.util.MSeq;

import java.util.Random;

import static io.jenetics.internal.math.Randoms.indexes;

/**
 * Mutator which swaps whole the positions between {@link DiscreteGene}s.
 */
public class SwapPositionsMutator extends SwapMutator<DiscreteGene, Double> implements DiscreteAlterer {
    /**
     * Standard probability constructor.
     * Utilized by the {@link evo.search.view.model.MutatorTableModel}.
     *
     * @param probability The crossover probability.
     * @see MutatorTableModel#getSelected()
     */
    public SwapPositionsMutator(final double probability) {
        super(probability);
    }

    /**
     * {@inheritDoc}
     * Swaps the positions of two random {@link DiscreteGene}s.
     *
     * @see evo.search.ga.DiscretePoint#swapDistances(DiscretePoint)
     */
    @Override
    protected MutatorResult<Chromosome<DiscreteGene>> mutate(final Chromosome<DiscreteGene> chromosome, final double p, final Random random) {
        final MutatorResult<Chromosome<DiscreteGene>> result;
        if (chromosome.length() > 1) {
            final MSeq<DiscreteGene> genes = MSeq.of(chromosome);
            final int mutations = (int) indexes(random, genes.length(), p)
                    .peek(i -> swapPositions(genes, i, random.nextInt(genes.length())))
                    .count();
            result = MutatorResult.of(
                    chromosome.newInstance(genes.toISeq()),
                    mutations
            );
        } else {
            result = MutatorResult.of(chromosome);
        }
        return result;
    }

    /**
     * Swap the positions of two {@link DiscretePoint}s in a
     * chromosomes sequence of {@link DiscreteGene}s.
     *
     * @param mSeq Chromosome sequence.
     * @param a    Integer position a for swap.
     * @param b    Integer position b for swap.
     */
    protected void swapPositions(MSeq<DiscreteGene> mSeq, int a, int b) {
        mSeq.get(a).getAllele().swapDistances(mSeq.get(b).getAllele());
    }
}
