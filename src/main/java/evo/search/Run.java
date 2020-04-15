package evo.search;

import evo.search.ga.DiscreteChromosome;
import lombok.Value;

@Value
public class Run {
    int limit;
    DiscreteChromosome individual;
}
