package evo.search.io.service;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * DSL {@link JMenu} and {@link JPopupMenu} service.
 */
public class MenuService {


    /**
     * Construct a menu bar with some menus.
     *
     * @param menus menus to add to the menu bar
     * @return menu bar with menus
     */
    public static MenuBar menuBar(final Menu... menus) {
        final MenuBar menuBar = new MenuBar();
        for (final Menu menu : menus)
            menuBar.add(menu);
        return menuBar;
    }

    /**
     * Construct a named menu with some children.
     *
     * @param text     name of the menu
     * @param children items in the menu
     * @return a named menu with given items
     */
    public static Menu menu(final String text, final Object... children) {
        final Menu menu = new Menu(text);

        for (final Object child : children) {
            if (child instanceof MenuItem) {
                menu.add((MenuItem) child);
            } else if (child instanceof String) {
                menu.add((String) child);
            }
        }
        return menu;
    }

    /**
     * Construct a menu item with a label, an action and a shortcut.
     *
     * @param label  menu label
     * @param action menu action
     * @param s      menu item shortcut
     * @return menu item with label, action and shortcut
     */
    public static MenuItem item(final String label, final ActionListener action, final MenuShortcut s) {
        final MenuItem menuItem = new MenuItem(label, s);
        menuItem.addActionListener(action);
        return menuItem;
    }

    /**
     * Construct a menu item with a label and an action.
     *
     * @param label  menu label
     * @param action menu action
     * @return menu item with label, action and shortcut
     */
    public static MenuItem item(final String label, final ActionListener action) {
        return item(label, action, null);
    }

    /**
     * Construct a new menu shortcut with a key code and the shift modifier flag.
     *
     * @param key              which key code to react to
     * @param useShiftModifier shift must be pressed
     * @return menu shortcut
     * @see KeyEvent
     */
    public static MenuShortcut shortcut(final int key, final boolean useShiftModifier) {
        return new MenuShortcut(key, useShiftModifier);
    }

    /**
     * Construct a new menu shortcut with a key code.
     * By default, the shift modifier flag is set to {@code false}.
     *
     * @param key which key code to react to
     * @return menu shortcut
     * @see KeyEvent
     */
    public static MenuShortcut shortcut(final int key) {
        return shortcut(key, false);
    }

    /**
     * Construct a new menu shortcut with a key char and the shift modifier flag.
     *
     * @param key              which key char to react to
     * @param useShiftModifier shift must be pressed
     * @return menu shortcut
     */
    public static MenuShortcut shortcut(final char key, final boolean useShiftModifier) {
        return new MenuShortcut(charToNum(key), useShiftModifier);
    }

    /**
     * Construct a new menu shortcut with a key char.
     * By default, the shift modifier flag is set to {@code false}.
     *
     * @param key which key char to react to
     * @return menu shortcut
     */
    public static MenuShortcut shortcut(final char key) {
        return shortcut(key, false);
    }


    /**
     * Convert a char to an {@link KeyEvent} keycode.
     *
     * @param inputChar char to convert to keycode
     * @return numeric key event key code
     */
    private static int charToNum(final char inputChar) {
        if (inputChar == '!') {
            return (KeyEvent.VK_EXCLAMATION_MARK);
        } else if (inputChar == ' ') {
            return (KeyEvent.VK_SPACE);
        }
        if (Character.isUpperCase(inputChar)) {
            return (int) inputChar + 10000;
        } else {
            return (int) inputChar - 32;
        }
    }
}
