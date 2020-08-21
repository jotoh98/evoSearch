package evo.search.io.entities;

import evo.search.ga.DiscreteChromosome;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Experiment entity to save and load simulations.
 */
@AllArgsConstructor
@Data
public class Experiment {

    /**
     * Configuration used to generate the results.
     */
    private final Configuration configuration;

    /**
     * Results of the simulation.
     */
    private final List<DiscreteChromosome> individuals;

    /**
     * Initialize an empty experiment with a configuration.
     *
     * @param configuration configuration to use
     */
    public Experiment(final Configuration configuration) {
        this(configuration, new ArrayList<>());
    }

    /**
     * Get the experiment version. This corresponds to the configurations version.
     *
     * @return experiments version
     */
    public String getVersion() {
        return configuration.getVersion();
    }

}
