package evo.search;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import evo.search.io.EventService;
import evo.search.io.service.ProjectService;
import evo.search.view.ChooserForm;
import evo.search.view.MainForm;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Main class.
 */
public class Main {

    @NonNls
    public static final String APP_TITLE = "evoSearch";

    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    public static final String HOME_PATH = System.getProperty("user.home") + File.separator + ".evoSearch";

    /**
     * Main class method wrapper for the {@link MainForm#main(String[])} method.
     *
     * @param args cli and application arguments
     */
    public static void main(String[] args) {
        ProjectService.setupGlobal();
        setupEnvironment();

        ChooserForm.main(args);
        EventService.INIT_PROJECT.addListener(project -> {
            final MainForm mainForm = new MainForm(project);
            mainForm.showFrame();
        });
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
