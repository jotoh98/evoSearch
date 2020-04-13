package evo.search.ga;


import evo.search.Experiment;
import io.jenetics.util.ISeq;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class DiscreteChromosomeTest {

    @BeforeAll
    static void setupExperiment() {
        Experiment.init(5, 2);
    }

    @Test
    void shuffle() {
        final List<DiscreteChromosome> individuals = Experiment.getInstance().getIndividuals();
        final boolean allValid = individuals.stream().allMatch(DiscreteChromosome::isValid);
        Assertions.assertTrue(allValid);
    }

    @Test
    void isValid() {
        final DiscreteChromosome chromosome = new DiscreteChromosome(ISeq.of(new DiscreteGene(new DiscretePoint(3, 10.0))));
        Assertions.assertFalse(chromosome.isValid());
    }
}