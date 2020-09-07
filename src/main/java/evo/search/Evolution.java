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
import io.jenetics.TournamentSelector;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;

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
     * Stores the history of the generations in {@link #history}.
     */
    @Override
    public void run() {
        if (configuration == null)
            return;

        if (configuration.getPopulation() == 0)
            configuration.setPopulation(1);

        history = new ArrayList<>();

        final Problem<Chromosome<DiscreteGene>, DiscreteGene, Double> problem = constructProblem();

        final Engine<DiscreteGene, Double> engine = buildEngine(problem);

        EventService.LOG_LABEL.trigger(LangService.get("environment.evolving"));
        final AtomicInteger progressCounter = new AtomicInteger();
        engine
                .stream()
                .limit(discreteGeneDoubleEvolutionResult -> !aborted)
                .limit(configuration.getLimit())
                .peek(historyConsumer)
                .peek(history::add)
                .forEach(result -> {
                    progressConsumer.accept(progressCounter.incrementAndGet());
                });
    }

    /**
     * Create the problem this evolution is trying to solve.
     *
     * @return evolution problem
     */
    @NotNull
    private Problem<Chromosome<DiscreteGene>, DiscreteGene, Double> constructProblem() {
        final BiFunction<Evolution, List<DiscreteGene>, Double> fitnessMethod = configuration.getFitness().getMethod();

        return Problem.of(
                list -> fitnessMethod.apply(this, ISeq.of(list).asList()),
                Codec.of(
                        Genotype.of(configuration::shuffle, configuration.getPopulation()),
                        Genotype::chromosome
                )
        );
    }

    /**
     * Build evolution engine.
     *
     * @param problem problem to solve
     * @return evolution engine
     */
    private Engine<DiscreteGene, Double> buildEngine(final Problem<Chromosome<DiscreteGene>, DiscreteGene, Double> problem) {
        final Engine.Builder<DiscreteGene, Double> evolutionBuilder = Engine
                .builder(problem)
                .selector(new TournamentSelector<>(configuration.getPopulation() / 4))
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

        return evolutionBuilder
                .offspringFraction(configuration.getOffspring() / (double) configuration.getPopulation())
                .populationSize(configuration.getPopulation())
                .build();
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
        if (configuration.getTreasures().size() == 0)
            return 0;
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
        if (treasures.size() == 0) return 0;
        double sum = 0;
        for (final DiscreteGene treasure : treasures)
            sum += AnalysisUtils.traceLength(chromosome, treasure);
        return sum / treasures.size();
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
        final double area = AnalysisUtils.areaCovered(chromosome);
        if (area <= 0)
            return Double.POSITIVE_INFINITY;
        return AnalysisUtils.traceLength(chromosome) / area;
    }

    /**
     * Computes the fitness of a {@link DiscreteGene} chromosome based on the maximised
     * area explored in each section between two rays.
     *
     * @param chromosome chromosome to evaluate
     * @return chromosome fitness based on maximised area explored
     * @see AnalysisUtils#areaCovered(List)
     */
    public double fitnessMaximisingCoveredArea(final List<DiscreteGene> chromosome) {
        final double area = AnalysisUtils.newAreaCovered(chromosome);
        if (area <= 0)
            return Double.POSITIVE_INFINITY;
        return AnalysisUtils.traceLength(chromosome) / area;
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
            return new Evolution(progressConsumer, historyConsumer, configuration, history, aborted);
        }
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
        MAX_AREA(Evolution::fitnessMaximisingArea),
        /**
         * This fitness method computes the quotient between the trace length
         * and the sum of explored areas.
         */
        COVERED_AREA(Evolution::fitnessMaximisingCoveredArea);

        /**
         * Fitness method used in the evolution stream.
         */
        private final BiFunction<Evolution, List<DiscreteGene>, Double> method;

    }
}
