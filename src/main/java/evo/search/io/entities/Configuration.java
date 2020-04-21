package evo.search.io.entities;

import evo.search.Environment;
import evo.search.Main;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscretePoint;
import evo.search.view.LangService;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
@Data
public class Configuration {

    public static final String PRE_VERSION = Main.VERSION;
    public static final String PRE_NAME = "Unnamed";
    public static final int PRE_LIMIT = 1000;
    public static final int PRE_POSITIONS = 3;
    public static final List<Double> PRE_DISTANCES = new ArrayList<>();
    public static final List<DiscretePoint> PRE_TREASURES = new ArrayList<>();
    public static final Environment.Fitness PRE_FITNESS = Environment.Fitness.getDefault();

    /**
     * Version for configuration compatibility checks.
     */
    private String version;
    /**
     * The configurations name.
     */
    private String name;
    /**
     * Last execution limit for the evolution method.
     *
     * @see Environment#evolve(Function, int, List, Consumer)
     */
    private int limit;
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

    private Environment.Fitness fitness;

    public Configuration(String name, int limit, int position, List<Double> distances, List<DiscretePoint> treasures, Environment.Fitness fitness) {
        this(Main.VERSION, name, limit, position, distances, treasures, fitness);
    }

    public Configuration(int limit, int position, List<Double> distances, List<DiscretePoint> treasures, Environment.Fitness fitness) {
        this(LangService.get("unknown"), limit, position, distances, treasures, fitness);
    }

    public Configuration() {
        this(PRE_VERSION, PRE_NAME, PRE_LIMIT, PRE_POSITIONS, PRE_DISTANCES, PRE_TREASURES, PRE_FITNESS);
    }
}
