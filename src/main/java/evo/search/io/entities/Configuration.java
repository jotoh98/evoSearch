package evo.search.io.entities;

import evo.search.Environment;
import evo.search.Main;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscretePoint;
import evo.search.view.LangService;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@AllArgsConstructor
@Data
public class Configuration {
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

    private Function<DiscreteChromosome, Double> fitness;

    public Configuration(String name, int limit, int position, List<Double> distances, List<DiscretePoint> treasures, Function<DiscreteChromosome, Double> fitness) {
        this(Main.VERSION, name, limit, position, distances, treasures, fitness);
    }

    public Configuration(int limit, int position, List<Double> distances, List<DiscretePoint> treasures, Function<DiscreteChromosome, Double> fitness) {
        this(LangService.get("unknown"), limit, position, distances, treasures, fitness);
    }
}
