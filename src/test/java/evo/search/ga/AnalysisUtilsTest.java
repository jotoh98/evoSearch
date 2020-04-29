package evo.search.ga;

import evo.search.Environment;
import evo.search.io.entities.Configuration;
import io.jenetics.util.ISeq;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalysisUtilsTest {

    private final DiscreteChromosome bestIndividual = DiscreteChromosome.ofPoints(Arrays.asList(
            new DiscretePoint(1, 1d),
            new DiscretePoint(2, 2d),
            new DiscretePoint(0, 3d),
            new DiscretePoint(1, 4d)
    ));
    private final DiscreteChromosome rotatedBest = DiscreteChromosome.ofPoints(Arrays.asList(
            new DiscretePoint(0, 1d),
            new DiscretePoint(2, 2d),
            new DiscretePoint(1, 3d),
            new DiscretePoint(0, 4d)
    ));

    @BeforeAll
    static void spiralEnvironmentSetup() {
        Environment.getInstance().setConfiguration(
                Configuration.builder()
                        .distances(Arrays.asList(1d, 2d, 3d, 4d))
                        .positions(3)
                        .build()
        );
    }

    @Test
    void spiralLikeness() {

        ArrayList<Double> distances = new ArrayList<>(Environment.getInstance().getConfiguration().getDistances());

        Collections.shuffle(distances);

        DiscreteChromosome worstIndividual = DiscreteChromosome.ofPoints(Arrays.asList(
                new DiscretePoint(0, 2d),
                new DiscretePoint(2, 1d),
                new DiscretePoint(0, 4d),
                new DiscretePoint(1, 3d)
        ));

        DiscreteChromosome betterIndividual = DiscreteChromosome.ofPoints(Arrays.asList(
                new DiscretePoint(2, 1d),
                new DiscretePoint(1, 2d),
                new DiscretePoint(0, 4d),
                new DiscretePoint(1, 3d)
        ));

        double worst = AnalysisUtils.spiralLikeness(worstIndividual);
        double better = AnalysisUtils.spiralLikeness(betterIndividual);
        double best = AnalysisUtils.spiralLikeness(bestIndividual);

        assertTrue(worst < better, String.format("Worst spiral-likeness %s required to be worse than better spiral-likeness %s", worst, better));
        assertTrue(better < best, String.format("Better spiral-likeness %s required to be worse than best spiral-likeness %s", better, best));
    }

    @Test
    void rotationInvariant() {
        double best = AnalysisUtils.spiralLikeness(bestIndividual);
        double rotated = AnalysisUtils.spiralLikeness(rotatedBest);
        assertEquals(best, rotated);
    }

    @Test
    void emptySpiral() {
        DiscreteChromosome empty = new DiscreteChromosome(ISeq.empty());
        double emptyLikeness = AnalysisUtils.spiralLikeness(empty);
        assertEquals(emptyLikeness, 0);
    }
}