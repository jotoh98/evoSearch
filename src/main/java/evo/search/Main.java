package evo.search;

import evo.search.view.MainForm;
import org.jetbrains.annotations.NonNls;

/**
 * Main class.
 */
public class Main {

    @NonNls
    public static final String APP_TITLE = "evoSearch";

    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    /**
     * Main class method wrapper for the {@link MainForm#main(String[])} method.
     *
     * @param args cli and application arguments
     */
    public static void main(String[] args) {
        MainForm.main(args);
    }

}
