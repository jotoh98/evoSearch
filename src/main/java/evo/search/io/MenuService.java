package evo.search.io;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class MenuService {

    public static JMenu menu(String text, Object... children) {
        final JMenu jMenu = new JMenu(text);

        for (Object child : children) {
            if (child instanceof JMenuItem) {
                jMenu.add((JMenuItem) child);
            } else if (child instanceof Action) {
                jMenu.add((Action) child);
            } else if (child instanceof JSeparator) {
                jMenu.addSeparator();
            } else if (child instanceof Component) {
                jMenu.add((Component) child);
            }
        }
        return jMenu;
    }

    public static Action item(String name, Consumer<ActionEvent> action) {
        return item(name, action, null);
    }

    public static Action item(String name, Consumer<ActionEvent> action, KeyStroke stroke) {
        return item(name, null, action, stroke);
    }

    public static Action item(String name, Icon icon, Consumer<ActionEvent> action, KeyStroke stroke) {
        final AbstractAction abstractAction = new AbstractAction(name, icon) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                action.accept(e);
            }
        };
        if (stroke != null) {
            abstractAction.putValue(Action.ACCELERATOR_KEY, stroke);
        }
        return abstractAction;
    }

    public static JSeparator separator() {
        return separator(0);
    }

    public static JSeparator separator(int orientation) {
        return new JSeparator(orientation);
    }
}
