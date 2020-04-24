package evo.search.io;

import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import evo.search.ga.DiscreteGene;
import evo.search.io.entities.Project;
import io.jenetics.Chromosome;

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
     * Event to repaint the {@link evo.search.view.Canvas} upon.
     */
    public static final Event<Chromosome<DiscreteGene>> REPAINT_CANVAS = new SimpleEvent<>();

    public static final Event<Project> INIT_PROJECT = new SimpleEvent<>();


}
