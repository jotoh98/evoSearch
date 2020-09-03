package evo.search;

import evo.search.ga.AnalysisUtils;
import evo.search.ga.DiscreteGene;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.ga.mutators.DistanceMutator;
import evo.search.io.entities.Configuration;
import evo.search.io.service.EventService;
import evo.search.view.LangService;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.StochasticUniversalSelector;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Problem;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class holds a configuration and executes a corresponding evolution.
 *
 * @author jotoh
 */
@Builder
@Slf4j
public class Evolution implements Runnable, Serializable, Cloneable {

    /**
     * Consumer of the progress generation.
     * Mainly used to monitor the progress in a progress bar.
     */
    @Builder.Default
    private transient final Consumer<Integer> progressConsumer = integer -> {
    };
    /**
     * Consumer of the evolution progress.
     * Consumes an evolution result.
     */
    @Builder.Default
    private transient final Consumer<EvolutionResult<DiscreteGene, Double>> historyConsumer = result -> {
    };
    /**
     * Configuration to use during the evolution.
     */
    @Getter
    @Setter
    private Configuration configuration;
    /**
     * Final evolution result.
     */
    @Getter
    @Setter(AccessLevel.NONE)
    private transient Genotype<DiscreteGene> result;

    /**
     * History of individuals with the best phenotype in each generation.
     */
    @Getter
    @Setter
    private List<EvolutionResult<DiscreteGene, Double>> history;

    /**
     * Evolution abort flag.
     */
    @Setter
    private transient boolean aborted;

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

        final BiFunction<Evolution, List<DiscreteGene>, Double> fitnessMethod = configuration.getFitness().getMethod();

        final Problem<List<DiscreteGene>, DiscreteGene, Double> problem = Problem.of(
                list -> fitnessMethod.apply(this, list),
                Codec.of(
                        Genotype.of(configuration::shuffle, configuration.getPopulation()),
                        g -> g.chromosome().stream().collect(Collectors.toList())
                )
        );

        final Engine.Builder<DiscreteGene, Double> evolutionBuilder = Engine
                .builder(problem)
                .selector(new StochasticUniversalSelector<>())
                .minimizing();

        final List<? extends DiscreteAlterer> alterers = new ArrayList<>(configuration.getAlterers());

        if (alterers.size() > 0) {
            alterers.forEach(alterer -> {
                if (alterer instanceof DistanceMutator)
                    ((DistanceMutator) alterer).setConfiguration(configuration);
            });
            final DiscreteAlterer first = alterers.remove(0);
            final DiscreteAlterer[] rest = alterers.toArray(DiscreteAlterer[]::new);
            evolutionBuilder.alterers(first, rest);
        }

        try {
            evolutionBuilder
                    .offspringSize(configuration.getOffspring())
                    .survivorsSize(configuration.getSurvivors())
                    .populationSize(configuration.getPopulation());
        } catch (final IllegalArgumentException e) {
            EventService.LOG_LABEL.trigger("Configuration was incorrect: " + e.getMessage());
            return;
        }

        EventService.LOG_LABEL.trigger(LangService.get("environment.evolving"));
        final AtomicInteger progressCounter = new AtomicInteger();
        result = evolutionBuilder.build()
                .stream()
                .limit(discreteGeneDoubleEvolutionResult -> !aborted)
                .limit(configuration.getLimit())
                .peek(historyConsumer)
                .peek(result -> {
                    history.add(result);
                    progressConsumer.accept(progressCounter.incrementAndGet());
                })
                .collect(EvolutionResult.toBestGenotype());
    }

    /**
     * Maps the list of {@link EvolutionResult} to a list of the best {@link io.jenetics.Phenotype}s of
     * each generation.
     *
     * @return list of best phenotypes per generation
     */
    public List<Chromosome<DiscreteGene>> getHistoryOfBestPhenotype() {
        return history.stream()
                .map(result -> result
                        .bestPhenotype()
                        .genotype()
                        .chromosome()
                )
                .collect(Collectors.toList());
    }

    /**
     * Compute the fitness of a {@link DiscreteGene} chromosome based on the
     * single first treasure {@link DiscreteGene}.
     * This is the trace length of the individual visiting it's {@link DiscreteGene}s
     * until the treasure is found.
     *
     * @param chromosome chromosome to evaluate
     * @return chromosome fitness based on single treasure
     * @see AnalysisUtils#traceLength(List, DiscreteGene)
     */
    public double fitnessSingular(final List<DiscreteGene> chromosome) {
        final DiscreteGene treasure = configuration.getTreasures().get(0);
        return AnalysisUtils.traceLength(chromosome, treasure);
    }

    /**
     * Compute the fitness of a {@link DiscreteGene} chromosome based on
     * all treasures.
     * <p>
     * The fitness of the chromosome is the sum of all singular fitnesses divided
     * by the amount of treasures.
     *
     * @param chromosome chromosome to evaluate
     * @return chromosome fitness based on multiple treasures
     * @see AnalysisUtils#traceLength(List, DiscreteGene)
     */
    public double fitnessMulti(final List<DiscreteGene> chromosome) {
        final List<DiscreteGene> treasures = configuration.getTreasures();
        return treasures.isEmpty() ? 0 : treasures.stream()
                .mapToDouble(treasure -> AnalysisUtils.traceLength(chromosome, treasure))
                .reduce(Double::sum)
                .orElse(0d) / treasures.size();
    }

    /**
     * Computes the fitness of a {@link DiscreteGene} chromosome based on the maximised
     * area explored in each section between two rays.
     *
     * @param chromosome chromosome to evaluate
     * @return chromosome fitness based on maximised area explored
     * @see AnalysisUtils#areaCovered(List)
     */
    public double fitnessMaximisingArea(final List<DiscreteGene> chromosome) {
        final List<DiscreteGene> points = AnalysisUtils.fill(chromosome);
        final double area = AnalysisUtils.areaCovered(points);
        if (area <= 0)
            return Double.POSITIVE_INFINITY;
        return AnalysisUtils.traceLength(points) / area;
    }

    /**
     * Worst case fitness scenario.
     *
     * @param chromosome chromosome to evaluate
     * @return worst case fitness
     * @see AnalysisUtils#worstCase(List, int)
     */
    public double fitnessWorstCase(final List<DiscreteGene> chromosome) {
        final List<DiscreteGene> points = AnalysisUtils.fill(chromosome);
        return AnalysisUtils.worstCase(points, 1);
    }

    /**
     * Clones the evolution shallow.
     *
     * @return shallow evolution clone
     */
    @Override
    public Evolution clone() {
        try {
            return (Evolution) super.clone();
        } catch (final CloneNotSupportedException e) {
            return new Evolution(progressConsumer, historyConsumer, configuration, result, history, aborted);
        }
    }

    /**
     * Resolve method for the {@link Serializable} functionality. Assigns the result
     * from the serialized history during reading.
     *
     * @return the deserialized evolution
     */
    public Object readResolve() {
        result = history.get(history.size() - 1).bestPhenotype().genotype();
        return this;
    }

    /**
     * Fitness method enum.
     */
    @Getter
    @AllArgsConstructor
    public enum Fitness {
        /**
         * The singular method computes the fitness based on the competitive ratio of
         * finding one distinct treasure point.
         *
         * @see #fitnessSingular(List)
         */
        SINGULAR(Evolution::fitnessSingular),
        /**
         * The multi method computes the fitness based on the competitive ratio of
         * finding a set of distinct treasure points.
         *
         * @see #fitnessMulti(List)
         */
        MULTI(Evolution::fitnessMulti),
        /**
         * The worst case method computes the fitness based on the maximum length over all
         * points to find their worst-case treasure (when the treasure is just an epsilon further
         * away from the origin than the point).
         *
         * @see #fitnessWorstCase(List)
         */
        WORST_CASE(Evolution::fitnessWorstCase),
        /**
         * The max area method computes the fitness based on the competitive ratio of explored space
         * divided by the path's length.
         *
         * @see #fitnessMaximisingArea(List)
         */
        MAX_AREA(Evolution::fitnessMaximisingArea);

        /**
         * Fitness method used in the evolution stream.
         */
        private final BiFunction<Evolution, List<DiscreteGene>, Double> method;

    }
}
