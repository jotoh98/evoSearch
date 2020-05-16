package evo.search;

import evo.search.ga.AnalysisUtils;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.Experiment;
import evo.search.io.service.EventService;
import evo.search.view.LangService;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.Optimize;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.ISeq;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This singleton class is the running environment for the
 * evolution of {@link DiscreteChromosome}s.
 * It manages the environment and executes the evolution.
 * <p>
 * It also offers some utility methods for the individuals.
 */
@Slf4j
@Getter
@Setter
public class Environment {

    /**
     * Singleton environment instance.
     */
    private static Environment instance = null;
    /**
     * The currently active environments experiment.
     * Holds all information to reproduce runs and is serialized and saved.
     */
    private Experiment experiment = new Experiment();

    /**
     * Evolving the individuals.
     *
     * @param newConfiguration The evolutions configuration holding all information about the environment.
     * @param progressConsumer Progress consumer.
     * @return The resulting fittest individual of the evolution.
     */
    public Genotype<DiscreteGene> evolve(Configuration newConfiguration, Consumer<EvolutionResult<DiscreteGene, Double>> progressConsumer) {
        if (newConfiguration == null) {
            EventService.LOG_LABEL.trigger(LangService.get("environment.config.missing"));
            return null;
        }

        Configuration activeConfiguration = getConfiguration();

        Codec<DiscreteChromosome, DiscreteGene> codec = Codec.of(
                Genotype.of(DiscreteChromosome.shuffle()),
                chromosomes -> DiscreteChromosome.of(ISeq.of(chromosomes.chromosome()))
        );

        final Engine.Builder<DiscreteGene, Double> evolutionBuilder = Engine
                .builder(activeConfiguration.getFitness().getMethod(), codec)
                .optimize(Optimize.MINIMUM);

        List<? extends DiscreteAlterer> alterers = activeConfiguration.getAlterers();
        if (alterers.size() > 0) {
            final DiscreteAlterer first = alterers.remove(0);
            final DiscreteAlterer[] rest = alterers.toArray(DiscreteAlterer[]::new);
            evolutionBuilder.alterers(first, rest);
        }

        try {
            evolutionBuilder
                    .offspringSize(activeConfiguration.getOffspring())
                    .survivorsSize(activeConfiguration.getSurvivors())
                    .populationSize(activeConfiguration.getPopulation());
        } catch (IllegalArgumentException e) {
            EventService.LOG_LABEL.trigger("Configuration was incorrect: " + e.getMessage());
            return null;
        }

        EventService.LOG_LABEL.trigger(LangService.get("environment.evolving"));
        return evolutionBuilder.build()
                .stream()
                .limit(activeConfiguration.getLimit())
                .peek(progressConsumer)
                .collect(EvolutionResult.toBestGenotype());
    }

    /**
     * Hidden singleton constructor.
     */
    private Environment() {
    }

    /**
     * Singleton instance getter.
     *
     * @return Singleton experiment instance.
     */
    public static Environment getInstance() {
        if (instance == null) {
            instance = new Environment();
        }
        return instance;
    }

    public Configuration getConfiguration() {
        return experiment.getConfiguration();
    }

    public void setConfiguration(Configuration configuration) {
        if (experiment == null) {
            throw new RuntimeException("Environment has not experiment set up");
        }
        experiment.setConfiguration(configuration);
        experiment.clearHistory();
    }

    /**
     * Compute for two {@link DiscretePoint}s, whether the first
     * point {@code point} finds the second point {@code treasure}.
     * <p>
     * That equals the following statement:
     * {@code point.position == treasure.position && point.distance >= treasure.distance}
     *
     * @param point    Point to check, if it finds the second point.
     * @param treasure Point to be found.
     * @return Whether the first point finds the second point.
     */
    public static boolean finds(final DiscretePoint point, final DiscretePoint treasure) {
        boolean distanceEqualOrGreater = point.getDistance() >= treasure.getDistance();
        boolean positionEquals = point.getPosition() == point.getPosition();
        return positionEquals && distanceEqualOrGreater;
    }

    /**
     * Compute the fitness of a {@link DiscreteChromosome} based on the
     * single first treasure {@link DiscretePoint}.
     * This is the trace length of the individual visiting it's {@link DiscretePoint}s
     * until the treasure is found.
     *
     * @param chromosome Chromosome to evaluate.
     * @return Chromosome singular fitness.
     * @see #trace(Chromosome, DiscretePoint)
     */
    public static double fitnessSingular(Chromosome<DiscreteGene> chromosome) {
        DiscretePoint treasure = Environment.getInstance().getConfiguration().getTreasures().get(0);
        return trace(chromosome, treasure);
    }

    /**
     * Compute the fitness of a {@link DiscreteChromosome} based on
     * all treasures.
     * <p>
     * The fitness of the chromosome is the sum of all singular fitnesses divided
     * by the amount of treasures.
     *
     * @param chromosome Chromosome to evaluate.
     * @return Chromosome singular fitness.
     * @see #trace(Chromosome, DiscretePoint)
     */
    public static double fitness(Chromosome<DiscreteGene> chromosome) {
        List<DiscretePoint> treasures = Environment.getInstance().getConfiguration().getTreasures();
        if (treasures.isEmpty()) {
            return 0;
        }
        return treasures.stream()
                .mapToDouble(treasure -> trace(chromosome, treasure))
                .reduce(Double::sum)
                .orElse(0d) / treasures.size();
    }

    /**
     * Computes the trace length necessary for the {@link DiscreteChromosome}
     * necessary to find the given treasure {@link DiscretePoint}.
     *
     * @param chromosome Chromosome to evaluate the trace length on.
     * @param treasure   Treasure point to be found.
     * @return Trace length necessary for the individual to find the treasure.
     */
    public static double trace(Chromosome<DiscreteGene> chromosome, DiscretePoint treasure) {
        double trace = 0d;

        DiscretePoint previous = new DiscretePoint(0, 0d);
        for (DiscreteGene gene : chromosome) {
            if (finds(previous, treasure)) {
                break;
            }
            DiscretePoint current = gene.getAllele();
            trace += previous.distance(current);
            previous = current;
        }

        return trace;
    }

    public static List<Method> getFitnessMethods() {
        return List.of(Environment.class.getDeclaredMethods()).stream()
                .filter(method -> {
                    final Class<?>[] parameterTypes = method.getParameterTypes();
                    final Class<?> returnType = method.getReturnType();
                    if (parameterTypes.length != 1) {
                        return false;
                    }
                    return parameterTypes[0].equals(Chromosome.class) && (returnType.equals(double.class) || returnType.equals(Double.class));
                })
                .collect(Collectors.toList());
    }

    @Getter
    @AllArgsConstructor
    public enum Fitness {
        SINGULAR(Environment::fitnessSingular),
        GLOBAL(Environment::fitness),
        SPIRAL(AnalysisUtils::spiralLikeness);

        private final Function<DiscreteChromosome, Double> method;

        public static Fitness getDefault() {
            return GLOBAL;
        }
    }
}
