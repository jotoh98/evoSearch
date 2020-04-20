package evo.search.io.entities;

import evo.search.ga.DiscreteChromosome;
import lombok.AllArgsConstructor;
import lombok.Data;

//TODO:delete
@AllArgsConstructor
@Data
public class Run {
    private int limit;
    private DiscreteChromosome individual;
}
