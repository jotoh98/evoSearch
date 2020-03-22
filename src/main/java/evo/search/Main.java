package evo.search;

import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import io.jenetics.BitGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Problem;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class Main {

    public static void main(String[] args) {
        int length = 15 + RandomRegistry.random().nextInt(30);
        Experiment.init(length,2);


        Problem<DiscreteChromosome, DiscreteGene, Double> problem = Problem.of(
                Experiment::fitness,
                Codec.of(
                        Genotype.of(DiscreteChromosome.shuffle()),
                        chromosomes -> DiscreteChromosome.of(ISeq.of(chromosomes.chromosome()))
                )
        );

        Genotype<DiscreteGene> result = Engine.builder(problem).build().stream().limit(100).collect(EvolutionResult.toBestGenotype());
        log.info("Treasure: {}", Experiment.getInstance().getTreasure());

        result.chromosome().forEach(gene -> log.info("Gene: {}", gene.getAllele()));

        log.info("Trace: {}", Experiment.trace(result.chromosome()));
        log.info("Fitness: {}", Experiment.fitness(result.chromosome()));

    }
}
