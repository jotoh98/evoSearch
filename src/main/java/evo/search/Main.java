package evo.search;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import evo.search.experiments.Experiment;
import evo.search.io.service.ProjectService;
import evo.search.view.ChooserForm;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Main class.
 *
 * @author jotoh
 */
@Slf4j
public class Main {

    /**
     * Title of this application.
     */
    @NonNls
    public static final String APP_TITLE = "evoSearch";

    /**
     * The package version.
     */
    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    /**
     * Version to use for unversioned entities.
     */
    @NonNls
    public static final String UNKNOWN_VERSION = "unknown";

    /**
     * Home path of the system wide configuration files.
     */
    public static final Path HOME_PATH = Path.of(System.getProperty("user.home"), ".evoSearch");

    /**
     * Main application method.
     *
     * @param args cli and application arguments
     */
    public static void main(final String[] args) {
        if (log.isInfoEnabled())
            log.info("Starting " + APP_TITLE + "...");
        if (args.length > 0 && args[0].equals("experiment")) {
            if (args.length < 2) {
                System.err.println("No experiment chosen.");
                System.exit(1);
            }
            startExperiment(args[1], Arrays.copyOfRange(args, 2, args.length));
            return;
        }
        ProjectService.setupService();
        setupEnvironment();
        ChooserForm.main(args);
    }

    /**
     * Start an {@link Experiment} by it's class name.
     *
     * @param name name of the experiment class
     * @param args args to pass to the experiment
     */
    private static void startExperiment(final String name, final String[] args) {
        try {
            final Experiment experiment = (Experiment) Class.forName("evo.search.experiments." + name).getDeclaredConstructor().newInstance();
            experiment.accept(args);
        } catch (final ClassNotFoundException | ClassCastException e) {
            System.err.println("Experiment class '" + name + "' could not be found");
        } catch (final ReflectiveOperationException e) {
            System.err.println("Experiment start failed: " + e.getMessage());
        }
    }

    /**
     * Set up the static properties.
     * Installs the swing look and feel.
     */
    public static void setupEnvironment() {
        LafManager.install(new DarculaTheme());
        UIManager.getDefaults().addResourceBundle("evo.search.lang");
        UIManager.put("EvoSearch.darker", new Color(0x292929));
        UIManager.put("EvoSearch.reddish", new Color(0xB76D29));
        System.setProperty("-Xdock:name", Main.APP_TITLE);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", Main.APP_TITLE);
    }

}
