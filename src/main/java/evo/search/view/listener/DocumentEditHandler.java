package evo.search.view.listener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface DocumentEditHandler extends DocumentListener {

    void handle(DocumentEvent e);

    @Override
    default void insertUpdate(DocumentEvent e) {
        handle(e);
    }

    @Override
    default void removeUpdate(DocumentEvent e) {
        handle(e);
    }

    @Override
    default void changedUpdate(DocumentEvent e) {
        handle(e);
    }
}
