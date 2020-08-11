package evo.search.view;

import java.util.ResourceBundle;

/**
 * Property based translation helper.
 */
public class LangService {

    /**
     * Language translation resource.
     */
    public static final ResourceBundle langBundle = ResourceBundle.getBundle("lang");

    /**
     * Shortcut function to retrieve a translated string.
     *
     * @param key property key for translation
     * @return translated string
     */
    public static String get(final String key) {
        return langBundle.getString(key);
    }

}
