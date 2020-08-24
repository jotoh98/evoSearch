package evo.search.ga.mutators;

import io.jenetics.Chromosome;
import io.jenetics.Gene;
import io.jenetics.MutatorResult;
import io.jenetics.SwapMutator;
import io.jenetics.util.MSeq;

import java.util.Random;

import static io.jenetics.internal.math.Randoms.indexes;

/**
 * @author jotoh
 */
public abstract class SwapPropertyMutator<G extends Gene<?, G>, C extends Comparable<? super C>> extends SwapMutator<G, C> {

    /**
     * Construct a swap mutator which swaps a component of the genes.
     *
     * @param probability the swapping probability
     */
    public SwapPropertyMutator(final double probability) {
        super(probability);
    }

    @Override
    protected MutatorResult<Chromosome<G>> mutate(final Chromosome<G> chromosome, final double p, final Random random) {
        final MutatorResult<Chromosome<G>> result;
        if (chromosome.length() > 1) {
            final MSeq<G> genes = MSeq.of(chromosome);
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
    protected abstract void swapComponent(final MSeq<G> mSeq, final int a, final int b);

}
