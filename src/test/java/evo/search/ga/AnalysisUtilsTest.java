package evo.search.ga;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests for the {@link AnalysisUtils} utilities.
 */
class AnalysisUtilsTest {

    /**
     * Test for the {@link AnalysisUtils#fill(List)} method.
     */
    @Test
    void fillsChromosome() {
        final List<DiscreteGene> filled = AnalysisUtils.fill(List.of(
                new DiscreteGene(8, 0, 1),
                new DiscreteGene(8, 2, 1),
                new DiscreteGene(8, 4, 1),
                new DiscreteGene(8, 6, 1)
        ));

        Assertions.assertEquals(filled.size(), 7);

        for (int i = 0; i < filled.size(); i++)
            Assertions.assertEquals(filled.get(i).getPosition(), i);
    }

    /**
     * Test for the {@link AnalysisUtils#areaInSector(double, double, int)} method.
     * Tests standard functionality.
     */
    @Test
    void triangleArea() {
        final double area = AnalysisUtils.areaInSector(1, 1, 4);
        Assertions.assertEquals(area, .5);
    }

    /**
     * Test for the {@link AnalysisUtils#areaInSector(double, double, int)} method.
     * Tests empty area functionality.
     */
    @Test
    void emptyTriangleArea() {
        final double area = AnalysisUtils.areaInSector(1, 0, 10);
        Assertions.assertEquals(area, 0);
    }

    /**
     * Test for the {@link AnalysisUtils#inBetween(DiscreteGene, DiscreteGene)} method.
     * Tests if the list is empty, because the points are opposed.
     */
    @Test
    void emptyBetweenOpposing() {
        final int size = AnalysisUtils
                .inBetween(new DiscreteGene(4, 0, 1), new DiscreteGene(4, 2, 1))
                .size();
        Assertions.assertEquals(size, 0);
    }

    /**
     * Test for the {@link AnalysisUtils#inBetween(DiscreteGene, DiscreteGene)} method.
     * Tests if the list is empty, because the points are opposed, although they're neighbours.
     */
    @Test
    void emptyBetweenNeighbors() {
        final int size = AnalysisUtils
                .inBetween(new DiscreteGene(2, 0, 1), new DiscreteGene(2, 1, 1))
                .size();
        Assertions.assertEquals(size, 0);
    }

    /**
     * Test for the {@link AnalysisUtils#inBetween(DiscreteGene, DiscreteGene)} method.
     * Tests if a filled-in points is correct.
     */
    @Test
    void inBetweenDistanceCorrect() {
        final double distance = AnalysisUtils
                .inBetween(new DiscreteGene(8, 0, 1), new DiscreteGene(8, 2, 1))
                .get(0)
                .getDistance();
        Assertions.assertEquals(distance, Math.sqrt(2) / 2);
    }

    @Test
    void inBetweenCorrect() {
        final List<DiscreteGene> genes = AnalysisUtils.fill(List.of(
                new DiscreteGene(50, 0, 1),
                new DiscreteGene(50, 10, 1),
                new DiscreteGene(50, 20, 1)
        ));

        final AtomicInteger atomicInteger = new AtomicInteger(0);
        Assertions.assertTrue(
                genes
                        .stream()
                        .map(DiscreteGene::getPosition)
                        .allMatch(p -> p == atomicInteger.getAndIncrement())
        );
    }

    /**
     * Test for the {@link AnalysisUtils#areaCovered(List)} method.
     * Tests, if the area is calculated correctly.
     */
    @Test
    void areaMaximised() {
        final double quarter = AnalysisUtils.areaCovered(List.of(new DiscreteGene(4, 0, 1), new DiscreteGene(4, 1, 1)));

        final double maximisedQuarter = AnalysisUtils.areaCovered(List.of(new DiscreteGene(4, 0, 1), new DiscreteGene(4, 1, 1), new DiscreteGene(4, 1, 2), new DiscreteGene(4, 0, 2)));

        final double better = AnalysisUtils.areaCovered(List.of(new DiscreteGene(4, 0, 1), new DiscreteGene(4, 1, 1), new DiscreteGene(4, 1, 2), new DiscreteGene(4, 2, 2)));


        Assertions.assertEquals(quarter, .5);
        Assertions.assertEquals(maximisedQuarter, 2);
        Assertions.assertTrue(maximisedQuarter < better);
    }

}