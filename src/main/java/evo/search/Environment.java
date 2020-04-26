package evo.search;

import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.io.entities.Configuration;
import evo.search.io.service.EventService;
import evo.search.view.LangService;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.Optimize;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

    @AllArgsConstructor
    public enum Fitness {
        SINGULAR(Environment::fitnessSingular),
        GLOBAL(Environment::fitness);

        private Function<DiscreteChromosome, Double> method;

        public static Fitness getDefault() {
            return GLOBAL;
        }
    }

    /**
     * Singleton environment instance.
     */
    private static Environment instance = null;

    /**
     * The environments configuration.
     * Holds all information to reproduce experiments and is serialized and saved.
     */
    private Configuration configuration;

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

    /**
     * Evolving the individuals.
     *
     * @param fitness  Fitness function for the evolutionary algorithm.
     * @param limit    Amount of iterations to evolve the population.
     * @param alterers List of mutators to alter the individuals during evolution.
     * @param consumer Progress consumer.
     * @return The resulting fittest individual of the evolution.
     */
    public Genotype<DiscreteGene> evolve(final Function<DiscreteChromosome, Double> fitness, final int limit, List<DiscreteAlterer> alterers, Consumer<Integer> consumer) {
        if (configuration == null) {
            EventService.LOG_LABEL.trigger(LangService.get("environment.config.missing"));
        }
        EventService.LOG_LABEL.trigger(LangService.get("environment.evolving"));
        Problem<DiscreteChromosome, DiscreteGene, Double> problem = Problem.of(
                fitness,
                Codec.of(
                        Genotype.of(DiscreteChromosome.shuffle()),
                        chromosomes -> DiscreteChromosome.of(ISeq.of(chromosomes.chromosome()))
                )
        );

        final Engine.Builder<DiscreteGene, Double> evolutionBuilder = Engine.builder(problem).optimize(Optimize.MINIMUM);

        if (alterers.size() > 0) {
            final DiscreteAlterer first = alterers.remove(0);
            final DiscreteAlterer[] rest = alterers.toArray(DiscreteAlterer[]::new);
            evolutionBuilder.alterers(first, rest);
        }

        final AtomicInteger progressCounter = new AtomicInteger();
        final Genotype<DiscreteGene> individual = evolutionBuilder.build()
                .stream()
                .limit(limit)
                .peek(result -> consumer.accept(progressCounter.incrementAndGet()))
                .collect(EvolutionResult.toBestGenotype());

        return individual;
    }
}
