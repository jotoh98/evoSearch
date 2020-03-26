package evo.search;

import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.ga.SwapPositionsMutator;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.SwapMutator;
import io.jenetics.engine.Codec;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Problem;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Setter
public class Experiment {

    private static Experiment instance = null;

    private Experiment() {
    }

    public static Experiment getInstance() {
        if (instance == null) {
            instance = new Experiment();
        }
        return instance;
    }

    private int positions = 2;

    private List<Double> distances = new ArrayList<>();

    private List<DiscretePoint> treasures = new ArrayList<>();

    private List<DiscreteChromosome> individuals = new ArrayList<>();

    public static void init(int amountDistances, int positions) {
        Experiment experiment = Experiment.getInstance();

        AtomicReference<Double> max = new AtomicReference<>(0d);
        List<Double> collect = IntStream.range(0, amountDistances)
                .mapToDouble(integer -> {
                    double distance = RandomRegistry.random().nextDouble() * 50;
                    if (distance > max.get()) {
                        max.set(distance);
                    }
                    return distance;
                })
                .boxed()
                .collect(Collectors.toList());

        experiment.setDistances(collect);
        experiment.setPositions(positions);

        List<DiscretePoint> treasures = experiment.getDistances()
                .stream()
                .map(aDouble -> {
                    final int position = RandomRegistry.random().nextInt(positions);
                    double distance = Math.max(0, RandomRegistry.random().nextDouble() * aDouble);
                    return new DiscretePoint(position, distance);
                })
                .collect(Collectors.toList());

        experiment.setTreasures(treasures);
    }

    public static boolean finds(final DiscretePoint point, final DiscretePoint treasure) {
        boolean distanceEqualOrGreater = point.getDistance() >= treasure.getDistance();
        boolean positionEquals = point.getPosition() == point.getPosition();
        return positionEquals && distanceEqualOrGreater;
    }

    public static double fitnessSingular(Chromosome<DiscreteGene> chromosome) {
        DiscretePoint treasure = Experiment.getInstance().getTreasures().get(0);
        return treasure.getDistance() / trace(chromosome, treasure);
    }

    public static double fitness(Chromosome<DiscreteGene> chromosome) {
        List<DiscretePoint> treasures = Experiment.getInstance().getTreasures();
        if (treasures.isEmpty()) {
            return 0;
        }
        return treasures.stream()
                .mapToDouble(treasure -> treasure.getDistance() / trace(chromosome, treasure))
                .reduce(Double::sum)
                .orElse(0d) / treasures.size();
    }

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

    public Genotype<DiscreteGene> evolve(final int limit, Consumer<Integer> consumer) {
        Problem<DiscreteChromosome, DiscreteGene, Double> problem = Problem.of(
                Experiment::fitness,
                Codec.of(
                        Genotype.of(DiscreteChromosome.shuffle()),
                        chromosomes -> DiscreteChromosome.of(ISeq.of(chromosomes.chromosome()))
                )
        );

        //TODO: prevent distances from being chosen multiple times
        // this happens in Mutator.mutate()
        final AtomicInteger progress = new AtomicInteger();
        final Genotype<DiscreteGene> indivual = Engine
                .builder(problem)
                .alterers(
                        new SwapMutator<>(0.5),
                        new SwapPositionsMutator(0.5)
                )
                .build()
                .stream()
                .limit(limit)
                .peek(result -> consumer.accept(progress.incrementAndGet()))
                .collect(EvolutionResult.toBestGenotype());
        instance.individuals.add((DiscreteChromosome) indivual.chromosome());
        return indivual;
    }
}
