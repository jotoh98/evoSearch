package evo.search.io.entities;

import evo.search.ga.DiscreteChromosome;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class Experiment {
    private final Configuration configuration;
    private final List<DiscreteChromosome> individuals;

    public Experiment(final Configuration configuration) {
        this(configuration, new ArrayList<>());
    }

    public String getVersion() {
        return configuration.getVersion();
    }
}
