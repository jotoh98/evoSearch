package evo.search.io.service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * DSL {@link JMenu} and {@link JPopupMenu} service.
 */
public class MenuService {

    /**
     * Construct a named menu with some children.
     *
     * @param text     Name of the menu.
     * @param children Items in the menu.
     * @return A named menu with given items.
     */
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

    /**
     * Construct a named popup menu with some children.
     *
     * @param text     Name of the popup menu.
     * @param children Items in the popup menu.
     * @return A named popup menu with given items.
     */
    public static JPopupMenu popupMenu(String text, Object... children) {
        final JPopupMenu jPopupMenu = new JPopupMenu();
        for (Object child : children) {
            if (child instanceof JMenuItem) {
                jPopupMenu.add((JMenuItem) child);
            } else if (child instanceof Action) {
                jPopupMenu.add((Action) child);
            } else if (child instanceof JSeparator) {
                jPopupMenu.addSeparator();
            } else if (child instanceof Component) {
                jPopupMenu.add((Component) child);
            }
        }
        return jPopupMenu;
    }

    /**
     * Construct a named action item.
     *
     * @param name   Name of the action.
     * @param action Action to perform.
     * @return A named action item.
     */
    public static Action item(String name, Consumer<ActionEvent> action) {
        return item(name, action, null);
    }

    /**
     * Construct a named action item associated with a key stroke.
     *
     * @param name   Name of the action.
     * @param action Action to perform.
     * @param stroke Keystroke to invoke the action
     * @return A named action item associated with a key stroke.
     */
    public static Action item(String name, Consumer<ActionEvent> action, KeyStroke stroke) {
        return item(name, null, action, stroke);
    }

    /**
     * Construct a named action item associated with a key stroke and an icon.
     *
     * @param name   Name of the action.
     * @param icon   Icon for the action item.
     * @param action Action to perform.
     * @param stroke Keystroke to invoke the action
     * @return A named action item associated with a key stroke and an icon.
     */
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

    /**
     * Construct a separator with {@link SwingConstants#HORIZONTAL} orientation.
     *
     * @return Horizontal separator item.
     */
    public static JSeparator separator() {
        return separator(SwingConstants.HORIZONTAL);
    }

    /**
     * Construct a separator with {@link SwingConstants#HORIZONTAL} orientation.
     *
     * @param orientation An integer specifying <code>SwingConstants.HORIZONTAL</code>
     *                    or <code>SwingConstants.VERTICAL</code>.
     * @return A separator item with given orientation.
     */
    public static JSeparator separator(int orientation) {
        return new JSeparator(orientation);
    }
}
