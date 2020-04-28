package evo.search.io.service;

import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import evo.search.ga.DiscreteGene;
import evo.search.io.entities.Configuration;
import evo.search.view.part.Canvas;
import io.jenetics.Chromosome;

import java.util.List;

/**
 * Event bus holding all the used {@link Event}s.
 */
public class EventService {
    /**
     * Event for when something should be written to the log label.
     */
    public static final Event<String> LOG_LABEL = new SimpleEvent<>();

    /**
     * Event for when something should be appended to the big log.
     * Useful when cumulative error messages are necessary.
     */
    public static final Event<String> LOG = new SimpleEvent<>();

    /**
     * Event to repaint the {@link Canvas} upon.
     */
    public static final Event<Chromosome<DiscreteGene>> REPAINT_CANVAS = new SimpleEvent<>();

    public static final Event<List<Configuration>> OPEN_CONFIG = new SimpleEvent<>();


    public static final Event<List<Configuration>> CONFIGS_CHANGED = new SimpleEvent<>();


}
