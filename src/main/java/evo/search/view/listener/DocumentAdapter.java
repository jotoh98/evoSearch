package evo.search.view.listener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Functional adapter interface for the {@link DocumentListener}.
 * <p>
 * Every action performed in the listener is passed to the {@link #handle(DocumentEvent)} method.
 */
public interface DocumentAdapter extends DocumentListener {

    /**
     * Functional interface handle method.
     *
     * @param e any document event
     */
    void handle(DocumentEvent e);

    /**
     * {@inheritDoc}
     */
    @Override
    default void insertUpdate(final DocumentEvent e) {
        handle(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void removeUpdate(final DocumentEvent e) {
        handle(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default void changedUpdate(final DocumentEvent e) {
        handle(e);
    }

}
