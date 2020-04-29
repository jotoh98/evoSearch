package evo.search.ga;

import evo.search.Environment;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chromosome consisting of a permutations of the distances
 * from the {@link Environment} in an evolved order
 * associated with valid positional indices.
 */
@AllArgsConstructor
public class DiscreteChromosome implements Chromosome<DiscreteGene> {

    /**
     * Sequence of {@link DiscreteGene}s forming the chromosome.
     */
    private final ISeq<DiscreteGene> genes;

    /**
     * Shuffle a new {@link DiscreteChromosome} from the distances in the
     * {@link Environment} and a random valid position.
     *
     * @return A valid shuffled chromosome.
     */
    public static DiscreteChromosome shuffle() {
        List<Double> distances = new ArrayList<>(Environment.getInstance().getConfiguration().getDistances());
        Collections.shuffle(distances);
        return of(distances);
    }

    /**
     * Shuffle a new {@link DiscreteChromosome} from the given distances.
     *
     * @param distances List of distances to construct the chromosome upon.
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
     * @param genes Sequence of genes to copy into the new instance.
     * @return A chromosome with the given sequence of genes.
     */
    public static DiscreteChromosome of(ISeq<DiscreteGene> genes) {
        return new DiscreteChromosome(genes.copy().toISeq());
    }

    /**
     * Generate a new {@link DiscreteChromosome} from the given sequence of points.
     * Simply maps the points to according {@link DiscreteGene}s.
     *
     * @param points Sequence of points to copy into the new instance.
     * @return A chromosome with the given sequence of genes.
     */
    public static DiscreteChromosome ofPoints(List<DiscretePoint> points) {
        return new DiscreteChromosome(ISeq.of(points.stream().map(DiscreteGene::new).collect(Collectors.toList())));
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
     * Has to be equal to the size of the {@link Environment}'s distances list.
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
     * and that all distances from the {@link Environment} are contained in the chromosome.
     *
     * @return Whether the chromosome is valid or not.
     */
    @Override
    public boolean isValid() {
        List<Double> distances = Environment.getInstance().getConfiguration().getDistances();
        if (distances.size() != length()) {
            return false;
        }
        List<DiscreteGene> genes = this.genes.asList();
        final boolean positionsValid = genes.stream()
                .map(DiscreteGene::getAllele)
                .mapToDouble(DiscretePoint::getPosition)
                .allMatch(value -> value >= 0 && value < Environment.getInstance().getConfiguration().getPositions());

        return genes.containsAll(distances) && positionsValid;
    }
}
