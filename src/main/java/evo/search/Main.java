package evo.search;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import evo.search.io.service.EventService;
import evo.search.io.service.ProjectService;
import evo.search.view.ChooserForm;
import evo.search.view.ConfigurationDialog;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.io.File;

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
    public static final String HOME_PATH = System.getProperty("user.home") + File.separator + ".evoSearch";

    /**
     * Main application method.
     *
     * @param args cli and application arguments
     */
    public static void main(final String[] args) {
        log.info("Starting " + APP_TITLE + "...");
        ProjectService.setupService();
        setupEnvironment();
        ChooserForm.main(args);
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
