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
     * @param key Property key for translation.
     * @return Translated string.
     */
    public static String get(String key) {
        return langBundle.getString(key);
    }

}
