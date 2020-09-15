package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import io.jenetics.Chromosome;
import io.jenetics.MutatorResult;
import io.jenetics.util.MSeq;

import java.util.Random;

import static io.jenetics.internal.math.Randoms.indexes;

/**
 * @author jotoh
 */
public abstract class SwapPropertyMutator extends SwapGeneMutator {

    /**
     * Construct a swap mutator which swaps a component of the genes.
     *
     * @param probability the swapping probability
     */
    public SwapPropertyMutator(final double probability) {
        super(probability);
    }

    @Override
    protected MutatorResult<Chromosome<DiscreteGene>> mutate(final Chromosome<DiscreteGene> chromosome, final double p, final Random random) {
        final MutatorResult<Chromosome<DiscreteGene>> result;
        if (chromosome.length() > 1) {
            final MSeq<DiscreteGene> genes = MSeq.of(chromosome);
            final int mutations = (int) indexes(random, genes.length(), p)
                    .peek(i -> swapComponent(genes, i, random.nextInt(genes.length())))
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
     * Swap a property of two genes at positions a and b.
     *
     * @param mSeq sequence of genes
     * @param a    position a
     * @param b    position b
     */
    protected abstract void swapComponent(final MSeq<DiscreteGene> mSeq, final int a, final int b);

}
