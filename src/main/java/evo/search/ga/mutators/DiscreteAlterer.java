package evo.search.ga.mutators;

import evo.search.ga.DiscreteGene;
import io.jenetics.Alterer;

/**
 * Wrapper for all {@link Alterer}s working with {@link DiscreteGene}s.
 */
public interface DiscreteAlterer extends Alterer<DiscreteGene, Double> {
}
