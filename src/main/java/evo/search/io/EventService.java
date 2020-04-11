package evo.search.io;

import com.pploder.events.Event;
import com.pploder.events.SimpleEvent;
import io.jenetics.Chromosome;

public class EventService {
    public static final Event<String> LOG_EVENT = new SimpleEvent<>();
    public static final Event<Chromosome> REPAINT_CANVAS = new SimpleEvent<>();
}
