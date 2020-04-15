package evo.search;

import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscretePoint;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class Configuration {
    /**
     * Historic list of optimal individuals.
     */
    private final List<Run> history = new ArrayList<>();
    /**
     * Last execution limit for the evolution method.
     *
     * @see Environment#evolve(int, List, Consumer)
     */
    int limit = 1000;
    /**
     * Version for configuration compatibility checks.
     */
    private String version;
    /**
     * The amount of positions available for the {@link DiscretePoint}s.
     */
    private int positions;
    /**
     * Input distances to choose a permutation from.
     * Forms single {@link DiscreteChromosome}s.
     */
    private List<Double> distances;
    /**
     * List of treasure {@link DiscretePoint}s to search for.
     */
    private List<DiscretePoint> treasures;

    public Configuration(int position, List<Double> distances, List<DiscretePoint> treasures) {
        this(Main.VERSION, position, distances, treasures);
    }

    public Configuration(final String version, final int positions, final List<Double> distances, final List<DiscretePoint> treasures) {
        this.version = version;
        this.positions = positions;
        this.distances = distances;
        this.treasures = treasures;
    }

    /**
     * Generate a random, but valid {@link Configuration} with 3 Positions
     * and min. one and max. 10 distances.
     *
     * @return Randomly shuffled configuration.
     * @see #shuffle(int, int)
     */
    public static Configuration shuffle() {
        return shuffle(1 + new Random().nextInt(9), 3);
    }

    /**
     * Generate a random, but valid {@link Configuration} with a given amount
     * of positions and distances.
     *
     * @param amountDistances The amount of distances to generate.
     * @param positions       The amount of positions to generate.
     * @return Randomly shuffled configuration.
     */
    public static Configuration shuffle(final int amountDistances, final int positions) {
        final Random random = new Random();
        final int amountTreasures = 1 + random.nextInt(amountDistances);
        final List<Double> distances = IntStream.range(0, amountDistances)
                .mapToDouble(i -> 1 + random.nextDouble() * 9)
                .boxed()
                .collect(Collectors.toList());

        final List<DiscretePoint> treasures = distances.stream()
                .map(distance -> new DiscretePoint(
                        random.nextInt(positions),
                        1.0 + random.nextDouble() * (distance - 1)
                ))
                .collect(Collectors.toList())
                .subList(0, amountTreasures);

        Collections.shuffle(distances);

        return new Configuration(positions, distances, treasures);
    }
}
