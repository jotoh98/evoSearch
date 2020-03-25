package evo.search.ga;

import io.jenetics.Chromosome;
import io.jenetics.MutatorResult;
import io.jenetics.SwapMutator;
import io.jenetics.util.MSeq;

import java.util.Random;

import static io.jenetics.internal.math.Randoms.indexes;

public class SwapPositionsMutator extends SwapMutator<DiscreteGene, Double> {

    public SwapPositionsMutator(final double probability) {
        super(probability);
    }

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

    protected void swapPositions(MSeq<DiscreteGene> mSeq, int a, int b) {
        mSeq.get(a).getAllele().swapDistance(mSeq.get(b).getAllele());
    }
}
