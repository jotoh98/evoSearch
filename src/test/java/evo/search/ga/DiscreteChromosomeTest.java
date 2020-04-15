package evo.search.ga;


import evo.search.Environment;
import evo.search.Run;
import io.jenetics.util.ISeq;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class DiscreteChromosomeTest {

    @BeforeAll
    static void setupExperiment() {
        Environment.init(5, 2);
    }

    @Test
    void shuffle() {
        final List<Run> history = Environment.getInstance().getConfiguration().getHistory();
        final boolean allValid = history.stream().map(Run::getIndividual)
                .allMatch(DiscreteChromosome::isValid);
        Assertions.assertTrue(allValid);
    }

    @Test
    void isValid() {
        final DiscreteChromosome chromosome = new DiscreteChromosome(ISeq.of(new DiscreteGene(new DiscretePoint(3, 10.0))));
        Assertions.assertFalse(chromosome.isValid());
    }
}