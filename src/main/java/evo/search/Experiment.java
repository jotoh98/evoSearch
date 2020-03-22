package evo.search;

import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import io.jenetics.Chromosome;
import io.jenetics.util.RandomRegistry;
import lombok.Getter;
import lombok.Setter;

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

    private int positions;

    private List<Double> distances;

    private DiscretePoint treasure;

    public static void init(int amountDistances, int positions) {
        Experiment experiment = Experiment.getInstance();

        AtomicReference<Double> max = new AtomicReference<>(0d);
        List<Double> collect = IntStream.range(0, amountDistances)
                .mapToDouble(integer -> {
                    double distance = RandomRegistry.random().nextDouble() * 50;
                    if(distance > max.get()) {
                        max.set(distance);
                    }
                    return distance;
                })
                .boxed()
                .collect(Collectors.toList());

        experiment.setDistances(collect);
        experiment.setPositions(positions);

        int treasurePosition = RandomRegistry.random().nextInt(positions);
        double treasureDistance = RandomRegistry.random().nextDouble() * max.get();

        experiment.setTreasure(new DiscretePoint(treasurePosition, treasureDistance));
    }

    public static boolean finds(DiscreteChromosome chromosome) {
        return chromosome.toSeq().stream().anyMatch(Experiment::finds);
    }

    private static boolean finds(DiscreteGene gene) {
        return finds(gene.getAllele());
    }

    public static boolean finds(DiscretePoint point) {
        boolean distanceEqualOrGreater = point.getDistance() >= getInstance().getTreasure().getDistance();
        boolean positionEquals = point.getPosition() == getInstance().getTreasure().getPosition();
        return positionEquals && distanceEqualOrGreater;
    }

    public static double fitness(Chromosome<DiscreteGene> chromosome) {
        return Experiment.getInstance().getTreasure().getDistance() / trace(chromosome);
    }

    public static double trace(Chromosome<DiscreteGene> chromosome) {
        double trace = 0d;

        DiscretePoint previous = new DiscretePoint(0, 0d);
        for (DiscreteGene gene : chromosome) {
            if(finds(previous)) {
                break;
            }
            DiscretePoint current = gene.getAllele();
            trace += previous.distance(current);
            previous = current;
        }

        return trace;
    }
}
