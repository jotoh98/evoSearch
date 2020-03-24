package evo.search;

import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.Mutator;
import io.jenetics.UniformCrossover;
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
import java.util.concurrent.atomic.AtomicReference;
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


        experiment.setTreasures(new ArrayList<>());
        int bound = RandomRegistry.random().nextInt(amountDistances - 1);
        for (int index = -1; index < bound; index++) {
            final int position = RandomRegistry.random().nextInt(positions);
            final double distance = RandomRegistry.random().nextDouble() * max.get();
            experiment.getTreasures().add(new DiscretePoint(position, distance));
        }
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

    public static Genotype<DiscreteGene> evolve(final int length, final int limit) {
        Experiment.init(length, 3);


        Problem<DiscreteChromosome, DiscreteGene, Double> problem = Problem.of(
                Experiment::fitness,
                Codec.of(
                        Genotype.of(DiscreteChromosome.shuffle()),
                        chromosomes -> DiscreteChromosome.of(ISeq.of(chromosomes.chromosome()))
                )
        );

        //TODO: prevent distances from being chosen multiple times
        // this happens in Mutator.mutate()
        return Engine
                .builder(problem)
                .alterers(
                        new UniformCrossover<>(0.2),
                        new Mutator<>(0.15)
                )
                .build()
                .stream()
                .limit(limit)
                .collect(EvolutionResult.toBestGenotype());
    }
}
