package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import io.jenetics.Alterer;

import java.util.List;

/**
 * Wrapper for all {@link Alterer}s working with {@link DiscreteGene}s.
 * Notice: All discrete alterers have to implement a constructor with
 * a double argument for the reflection to work.
 *
 * @author jotoh
 * @since 0.0.1
 */
public interface DiscreteAlterer extends Alterer<DiscreteGene, Double> {

    /**
     * Get all of the implementations of {@link DiscreteAlterer}.
     *
     * @return list of discrete alterer implementations
     */
    static List<Class<? extends DiscreteAlterer>> getSubclasses() {
        return List.of(
                SwapGeneMutator.class,
                SwapPositionsMutator.class,
                SwapDistanceMutator.class,
                DistanceMutator.class,
                PositionMutator.class
        );
    }

    /**
     * Get the probability for the {@link evo.search.view.ConfigPanel} view.
     *
     * @return the alterers probability
     */
    double getProbability();

}
