package evo.search.ga;

import evo.search.io.entities.Configuration;
import io.jenetics.util.ISeq;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class AnalysisUtilsTest {


    @Test
    void fillsChromosome() {

        final Configuration config = Configuration.builder()
                .positions(8)
                .build();

        final List<DiscretePoint> filled = AnalysisUtils.fill(new DiscreteChromosome(config, ISeq.of(
                new DiscreteGene(config, 0, 1),
                new DiscreteGene(config, 2, 1),
                new DiscreteGene(config, 4, 1),
                new DiscreteGene(config, 6, 1)
        )));

        Assertions.assertEquals(filled.size(), 7);

        for (int i = 0; i < filled.size(); i++) {
            Assertions.assertEquals(filled.get(i).getPosition(), i);
        }
    }

    @Test
    void triangleArea() {
        final double area = AnalysisUtils.areaInSector(1, 1, 4);
        Assertions.assertEquals(area, .5);
    }

    @Test
    void emptyTriangleArea() {
        final double area = AnalysisUtils.areaInSector(1, 0, 10);
        Assertions.assertEquals(area, 0);
    }

    @Test
    void areaExplored() {
        final double area = AnalysisUtils.areaExplored(List.of(
                new DiscretePoint(4, 0, 1),
                new DiscretePoint(4, 1, 1),
                new DiscretePoint(4, 2, 1),
                new DiscretePoint(4, 3, 1),
                new DiscretePoint(4, 0, 1)
        ));
        Assertions.assertEquals(area, 2);
    }

    @Test
    void emptyAreaExplored() {
        final double area = AnalysisUtils.areaExplored(List.of(new DiscretePoint(2, 0, 100), new DiscretePoint(2, 1, 100)));
        Assertions.assertEquals(area, 0);
    }

    @Test
    void emptyExploredInputBroken() {
        final double area = AnalysisUtils.areaExplored(List.of(new DiscretePoint(2, 0, 100)));
        Assertions.assertEquals(area, 0);
    }

    @Test
    void areaExploredComparison() {
        final double best = AnalysisUtils.areaExplored(List.of(
                new DiscretePoint(4, 0, 1),
                new DiscretePoint(4, 1, 1),
                new DiscretePoint(4, 2, 1),
                new DiscretePoint(4, 3, 1),
                new DiscretePoint(4, 0, 1)
        ));

        final double smaller = AnalysisUtils.areaExplored(List.of(
                new DiscretePoint(4, 0, .1),
                new DiscretePoint(4, 1, .1),
                new DiscretePoint(4, 2, .1),
                new DiscretePoint(4, 3, .1),
                new DiscretePoint(4, 0, .1)
        ));

        final double goesBack = AnalysisUtils.areaExplored(List.of(
                new DiscretePoint(4, 0, 1),
                new DiscretePoint(4, 1, 1),
                new DiscretePoint(4, 2, 1),
                new DiscretePoint(4, 3, 1),
                new DiscretePoint(4, 2, 1)
        ));
        Assertions.assertTrue(smaller < best);
        Assertions.assertTrue(goesBack < best);
    }

    @Test
    void emptyBetweenOpposing() {
        final int size = AnalysisUtils
                .inBetween(new DiscretePoint(4, 0, 1), new DiscretePoint(4, 2, 1))
                .size();
        Assertions.assertEquals(size, 0);
    }

    @Test
    void emptyBetweenNeighbors() {
        final int size = AnalysisUtils
                .inBetween(new DiscretePoint(2, 0, 1), new DiscretePoint(2, 1, 1))
                .size();
        Assertions.assertEquals(size, 0);
    }

    @Test
    void inBetweenDistanceCorrect() {
        final double distance = AnalysisUtils
                .inBetween(new DiscretePoint(8, 0, 1), new DiscretePoint(8, 2, 1))
                .get(0)
                .distance;
        Assertions.assertEquals(distance, Math.sqrt(2) / 2);
    }
}