package evo.search;

import evo.search.ga.AnalysisUtils;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.io.entities.Configuration;
import evo.search.io.service.EventService;
import evo.search.view.LangService;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.StochasticUniversalSelector;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class holds a configuration and executes a corresponding evolution.
 */
@Builder
@Slf4j
public class Evolution implements Runnable {

    @Getter
    private final Configuration configuration;
    @Builder.Default
    private final Consumer<Integer> progressConsumer = integer -> {
    };
    @Builder.Default
    private final Consumer<EvolutionResult<DiscreteGene, Double>> historyConsumer = result -> {
    };
    @Getter
    private Genotype<DiscreteGene> result;
    @Getter
    private List<Genotype<DiscreteGene>> history;

    /**
     * Execute an evolution
     * Works with the {@link Configuration} held by this instance.
     * Clears out the history and the last result.
     * <p>
     * Stores the fittest resulting individual in the field {@link #result} and a history of the generations fittest in {@link #history}.
     */
    @Override
    public void run() {
        if (configuration == null)
            return;

        history = new ArrayList<>();

        final Engine.Builder<DiscreteGene, Double> evolutionBuilder = Engine.builder(
                chromosome -> configuration.getFitness().getMethod().apply(this, chromosome),
                Codec.of(
                        Genotype.of((Factory<DiscreteChromosome>) this::shuffleInstance, configuration.getPopulation()),
                        chromosomes -> DiscreteChromosome.of(configuration, ISeq.of(chromosomes.chromosome()))
                )
        )
                .selector(new StochasticUniversalSelector<>())
                .minimizing();

        List<? extends DiscreteAlterer> alterers = configuration.getAlterers();

        if (alterers.size() > 0) {
            final DiscreteAlterer first = alterers.remove(0);
            final DiscreteAlterer[] rest = alterers.toArray(DiscreteAlterer[]::new);
            evolutionBuilder.alterers(first, rest);
        }

        try {
            evolutionBuilder
                    .offspringSize(configuration.getOffspring())
                    .survivorsSize(configuration.getSurvivors())
                    .populationSize(configuration.getPopulation());
        } catch (IllegalArgumentException e) {
            EventService.LOG_LABEL.trigger("Configuration was incorrect: " + e.getMessage());
            return;
        }

        EventService.LOG_LABEL.trigger(LangService.get("environment.evolving"));
        final AtomicInteger progressCounter = new AtomicInteger();
        result = evolutionBuilder.build()
                .stream()
                .limit(configuration.getLimit())
                .peek(historyConsumer)
                .peek(result -> history.add(result.bestPhenotype().genotype()))
                .peek(result -> progressConsumer.accept(progressCounter.incrementAndGet()))
                .collect(EvolutionResult.toBestGenotype());
    }

    /**
     * Shuffle new instances.
     * Borrowed functionality from {@link Factory} because it needs information from the experiments {@link Configuration} only available through the {@link Evolution}.
     *
     * @return Shuffled {@link DiscreteChromosome}.
     */
    private DiscreteChromosome shuffleInstance() {
        ArrayList<Double> distancesClone = new ArrayList<>(configuration.getDistances());
        Collections.shuffle(distancesClone);

        List<DiscreteGene> geneList = distancesClone.stream()
                .map(distance -> {
                    final int positions = configuration.getPositions();
                    int position = RandomRegistry.random().nextInt(positions);
                    return new DiscretePoint(positions, position, distance);
                })
                .map(discretePoint -> new DiscreteGene(configuration, discretePoint.getPosition(), discretePoint.getDistance()))
                .collect(Collectors.toList());

        return new DiscreteChromosome(configuration, ISeq.of(geneList));
    }

    /**
     * Compute the fitness of a {@link DiscreteChromosome} based on the
     * single first treasure {@link DiscretePoint}.
     * This is the trace length of the individual visiting it's {@link DiscretePoint}s
     * until the treasure is found.
     *
     * @param chromosome chromosome to evaluate.
     * @return chromosome fitness based on single treasure
     * @see AnalysisUtils#traceLength(Chromosome, DiscretePoint)
     */
    public double fitnessSingular(Chromosome<DiscreteGene> chromosome) {
        DiscretePoint treasure = configuration.getTreasures().get(0);
        return AnalysisUtils.traceLength(chromosome, treasure);
    }

    /**
     * Compute the fitness of a {@link DiscreteChromosome} based on
     * all treasures.
     * <p>
     * The fitness of the chromosome is the sum of all singular fitnesses divided
     * by the amount of treasures.
     *
     * @param chromosome chromosome to evaluate.
     * @return chromosome fitness based on multiple treasures
     * @see AnalysisUtils#traceLength(Chromosome, DiscretePoint)
     */
    public double fitnessMulti(Chromosome<DiscreteGene> chromosome) {
        List<DiscretePoint> treasures = configuration.getTreasures();
        return treasures.isEmpty() ? 0 : treasures.stream()
                .mapToDouble(treasure -> AnalysisUtils.traceLength(chromosome, treasure))
                .reduce(Double::sum)
                .orElse(0d) / treasures.size();
    }

    /**
     * Computes the fitness of a {@link DiscreteChromosome} based on the maximised
     * area explored in each section between two rays.
     *
     * @param chromosome chromosome to evaluate
     * @return chromosome fitness based on maximised area explored
     * @see AnalysisUtils#areaCovered(List)
     */
    public double fitnessMaximisingArea(Chromosome<DiscreteGene> chromosome) {
        final List<DiscretePoint> points = AnalysisUtils.fill(chromosome);
        final double area = AnalysisUtils.areaCovered(points);
        if (area <= 0)
            return Double.POSITIVE_INFINITY;
        return AnalysisUtils.traceLength(points) / area;
    }

    @Getter
    @AllArgsConstructor
    public enum Fitness {
        SINGULAR(Evolution::fitnessSingular),
        MULTI(Evolution::fitnessMulti),
        MAX_AREA(Evolution::fitnessMaximisingArea);

        private final BiFunction<Evolution, DiscreteChromosome, Double> method;

        public static Fitness getDefault() {
            return MAX_AREA;
        }

        public static List<Fitness> getMethods() {
            return List.of(values());
        }
    }
}
