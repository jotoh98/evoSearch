package evo.search.ga;

import evo.search.Experiment;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Chromosome consisting of a permutations of the distances
 * from {@link Experiment#getDistances()} in an evolved order
 * associated with positions smaller than the {@link Experiment#getPositions()}
 * property.
 */
@AllArgsConstructor
public class DiscreteChromosome implements Chromosome<DiscreteGene> {

    /**
     * Sequence of {@link DiscreteGene}s forming the chromosome.
     */
    private ISeq<DiscreteGene> genes;

    /**
     * Shuffle a new {@link DiscreteChromosome} from the distances in the
     * {@link Experiment} and a random valid position.
     *
     * @return A valid shuffled chromosome.
     */
    public static DiscreteChromosome shuffle() {
        List<Double> distances = Experiment.getInstance().getDistances();
        Collections.shuffle(distances);
        return of(distances);
    }

    /**
     * Shuffle a new {@link DiscreteChromosome} from the given distances.
     *
     * @return A valid shuffled chromosome.
     */
    public static DiscreteChromosome of(List<Double> distances) {
        DiscreteGene[] discreteGenes = distances.stream()
                .map(DiscreteGene::of)
                .toArray(DiscreteGene[]::new);
        return of(ISeq.of(discreteGenes));
    }

    /**
     * Generate a new {@link DiscreteChromosome} from the given sequence of genes.
     *
     * @return A chromosome with the given sequence of genes.
     */
    public static DiscreteChromosome of(ISeq<DiscreteGene> genes) {
        return new DiscreteChromosome(genes.copy().toISeq());
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
    public Chromosome<DiscreteGene> newInstance(ISeq<DiscreteGene> genes) {
        return new DiscreteChromosome(genes.copy().toISeq());
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
     * Has to be equal to the size of the {@link Experiment#getDistances()} list.
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
        return shuffle();
    }

    /**
     * Check if the chromosome is valid. That means, that all positions are valid
     * and that all distances from the {@link Experiment} are contained in the chromosome.
     *
     * @return Whether the chromosome is valid or not.
     */
    @Override
    public boolean isValid() {
        List<Double> distances = Experiment.getInstance().getDistances();
        if (distances.size() != length()) {
            return false;
        }
        List<DiscreteGene> genes = this.genes.asList();
        final boolean positionsValid = genes.stream()
                .map(DiscreteGene::getAllele)
                .mapToDouble(DiscretePoint::getPosition)
                .allMatch(value -> value >= 0 && value < Experiment.getInstance().getPositions());

        return genes.containsAll(distances) && positionsValid;
    }
}
