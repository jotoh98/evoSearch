package evo.search.io.entities;

import evo.search.Environment;
import evo.search.Main;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.ga.mutators.SwapGeneMutator;
import evo.search.ga.mutators.SwapPositionsMutator;
import io.jenetics.engine.Engine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Configuration {

    //TODO: add population/offspring/survivors sizes
    /**
     * {@link Engine.Builder#offspringSize(int)}
     * {@link Engine.Builder#survivorsSize(int)}
     * {@link Engine.Builder#populationSize(int)}
     */

    /**
     * Version for configuration compatibility checks.
     */
    @Builder.Default
    private String version = Main.VERSION;
    /**
     * The configurations name.
     */
    @Builder.Default
    private String name = "Unnamed";
    /**
     * Last execution limit for the evolution method.
     *
     * @see Environment#evolve(Function, int, List, Consumer)
     */
    @Builder.Default
    private int limit = 1000;
    /**
     * The amount of positions available for the {@link DiscretePoint}s.
     */
    @Builder.Default
    private int positions = 3;
    /**
     * Input distances to choose a permutation from.
     * Forms single {@link DiscreteChromosome}s.
     */
    @Builder.Default
    private List<Double> distances = new ArrayList<>();
    /**
     * List of treasure {@link DiscretePoint}s to search for.
     */
    @Builder.Default
    private List<DiscretePoint> treasures = new ArrayList<>();

    @Builder.Default
    private Environment.Fitness fitness = Environment.Fitness.getDefault();

    @Builder.Default
    private List<? extends DiscreteAlterer> alterers = new ArrayList<>(
            Arrays.asList(
                    new SwapGeneMutator(0.5),
                    new SwapPositionsMutator(0.5)
            )
    );

    @Builder.Default
    private int offspring = 15;

    @Builder.Default
    private int survivors = 10;

    @Builder.Default
    private int population = 20;

}
