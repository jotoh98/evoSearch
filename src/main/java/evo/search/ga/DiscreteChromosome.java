package evo.search.ga;

import evo.search.io.entities.Configuration;
import io.jenetics.AbstractChromosome;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;

/**
 * Chromosome consisting of a permutations of the distances
 * from the {@link Configuration} in an evolved order
 * associated with valid positional indices.
 */
public class DiscreteChromosome extends AbstractChromosome<DiscreteGene> {


    /**
     * Create a new {@code DiscreteChromosome} from the given {@code genes}
     * array.
     *
     * @param genes the genes that form the chromosome.
     * @throws NullPointerException     if the given gene array is {@code null}.
     * @throws IllegalArgumentException if the length of the gene sequence is
     *                                  empty.
     */
    public DiscreteChromosome(final ISeq<? extends DiscreteGene> genes) {
        super(genes);
    }

    /**
     * Create a new {@code DiscreteChromosome} from the given {@code genes}
     * array.
     *
     * @param genes the genes that form the chromosome.
     * @throws NullPointerException     if the given gene array is {@code null}.
     * @throws IllegalArgumentException if the length of the gene sequence is
     *                                  empty.
     */
    public DiscreteChromosome(final DiscreteGene... genes) {
        this(ISeq.of(genes));
    }

    @Override
    public Chromosome<DiscreteGene> newInstance(final ISeq<DiscreteGene> genes) {
        return new DiscreteChromosome(genes.map(DiscreteGene::clone));
    }

    @Override
    public Chromosome<DiscreteGene> newInstance() {
        return new DiscreteChromosome(_genes.map(DiscreteGene::newInstance));
    }
}
