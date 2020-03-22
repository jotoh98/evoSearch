package evo.search.ga;

import evo.search.Experiment;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@AllArgsConstructor
public class DiscreteChromosome implements Chromosome<DiscreteGene> {

    private ISeq<DiscreteGene> genes;

    @Override
    public DiscreteGene getGene(int index) {
        return genes.get(index);
    }

    @Override
    public Chromosome<DiscreteGene> newInstance(ISeq<DiscreteGene> genes) {
        return new DiscreteChromosome(genes.copy().toISeq());
    }

    @Override
    public ISeq<DiscreteGene> toSeq() {
        return genes;
    }

    @Override
    public int length() {
        return genes.length();
    }

    @Override
    public Chromosome<DiscreteGene> newInstance() {
        return shuffle();
    }

    public static DiscreteChromosome shuffle() {
        List<Double> distances = Experiment.getInstance().getDistances();
        Collections.shuffle(distances);
        return of(distances);
    }

    public static DiscreteChromosome of(List<Double> distances) {
        DiscreteGene[] discreteGenes = distances.stream()
                .map(DiscreteGene::of)
                .toArray(DiscreteGene[]::new);
        return of(ISeq.of(discreteGenes));
    }

    public static DiscreteChromosome of(ISeq<DiscreteGene> genes) {
        return new DiscreteChromosome(genes);
    }

    @Override
    public boolean isValid() {
        return genes.stream().allMatch(DiscreteGene::isValid);
    }
}
