package evo.search.io.service;

import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import evo.search.ga.DiscreteGene;
import evo.search.io.entities.Configuration;
import evo.search.view.ChooserForm;
import evo.search.view.ConfigurationDialog;
import evo.search.view.part.Canvas;

import javax.swing.*;
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
    public static final Event<Iterable<DiscreteGene>> REPAINT_CANVAS = new SimpleEvent<>();

    /**
     * Event to open the {@link evo.search.view.ConfigurationDialog}.
     */
    public static final Event<List<Configuration>> OPEN_CONFIG = new SimpleEvent<>();

    /**
     * Event to reload configurations to use in the {@link evo.search.view.MainForm}.
     */
    public static final Event<List<Configuration>> CONFIGS_CHANGED = new SimpleEvent<>();

    /**
     * Event for displaying the chooser form.
     */
    public static final Event<Void> OPEN_CHOOSER_FORM = new SimpleEvent<>();

    static {
        OPEN_CONFIG.addListener(configurations -> SwingUtilities.invokeLater(() -> {
            final ConfigurationDialog configurationDialog = new ConfigurationDialog(configurations);
            configurationDialog.showFrame();
        }));
        OPEN_CHOOSER_FORM.addListener(unused -> new ChooserForm());
    }
}
