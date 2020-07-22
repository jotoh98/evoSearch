package evo.search.ga;

import evo.search.io.entities.Configuration;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Chromosome consisting of a permutations of the distances
 * from the {@link Configuration} in an evolved order
 * associated with valid positional indices.
 */
@Value
public class DiscreteChromosome implements Chromosome<DiscreteGene> {

    Configuration configuration;

    /**
     * Sequence of {@link DiscreteGene}s forming the chromosome.
     */
    ISeq<DiscreteGene> genes;

    /**
     * Generate a new {@link DiscreteChromosome} from the given sequence of genes.
     *
     * @param genes Sequence of genes to copy into the new instance.
     * @return A chromosome with the given sequence of genes.
     */
    public static DiscreteChromosome of(Configuration configuration, ISeq<DiscreteGene> genes) {
        return new DiscreteChromosome(configuration, genes.copy().toISeq());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscreteGene getGene(int index) {
        return genes.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISeq<DiscreteGene> toSeq() {
        return genes;
    }

    /**
     * {@inheritDoc}
     * Has to be equal to the size of the {@link Configuration}'s distances list.
     */
    @Override
    public int length() {
        return genes.length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Chromosome<DiscreteGene> newInstance() {
        return newInstance(genes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Chromosome<DiscreteGene> newInstance(ISeq<DiscreteGene> genes) {
        return new DiscreteChromosome(configuration, genes.copy().toISeq());
    }

    /**
     * {@inheritDoc}
     * That means, that all distances are distinct and all positions are greater than or equal to zero.
     */
    @Override
    public boolean isValid() {
        final List<Double> distinct = Stream.of(genes.toArray(DiscreteGene[]::new))
                .map(DiscreteGene::getAllele)
                .map(DiscretePoint::getDistance)
                .distinct()
                .collect(Collectors.toList());

        final boolean positionsValid = Stream.of(genes.toArray(DiscreteGene[]::new))
                .allMatch(discreteGene -> discreteGene.getAllele().getPosition() >= 0);

        return distinct.size() == genes.size() && positionsValid;
    }
}
