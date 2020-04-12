package evo.search.view;

import com.bulenkov.darcula.DarculaLaf;
import evo.search.Experiment;
import evo.search.ga.mutators.SwapGeneMutator;
import evo.search.ga.mutators.SwapPositionsMutator;
import evo.search.io.EventService;
import evo.search.io.FileService;
import evo.search.io.MenuService;
import evo.search.model.MutatorTableModel;
import evo.search.view.laf.SplitPaneUI;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Getter
public class MainForm {
    private JPanel rootPanel;
    private JPanel toolbar;
    private JButton runButton;
    private Canvas canvas;
    private JButton editButton;
    private JProgressBar progressBar;
    private JLabel logLabel;
    private JTable simpleConfigTable;
    private JTable mutatorConfigTable;

    @Setter
    private MutatorTableModel mutatorTableModel = null;

    public static void main(String[] args) {
        setUpEnvironment();
        final MainForm mainForm = new MainForm();

        final JPanel rootPanel = mainForm.getRootPanel();
        mainForm.getRunButton().addActionListener(e -> {
            if (mainForm.getMutatorTableModel() == null) {
                EventService.LOG_EVENT.trigger("Error with creating the config table.");
                return;
            }
            Experiment.init(10, 3);
            mainForm.getProgressBar().setMaximum(1000);
            mainForm.getProgressBar().setVisible(true);
            CompletableFuture
                    .supplyAsync(() -> Experiment.getInstance()
                            .evolve(
                                    1000,
                                    mainForm.getMutatorTableModel().getSelected(),
                                    integer -> mainForm.getProgressBar().setValue(integer)).chromosome()
                    )
                    .thenAccept(chromosome -> {
                        EventService.LOG_EVENT.trigger("Experiment finished.");
                        EventService.REPAINT_CANVAS.trigger(chromosome);
                    })
                    .thenRun(() -> mainForm.getProgressBar().setVisible(false));
        });

        mainForm.setupMutatorTable(mainForm.getMutatorConfigTable());

        setupFrame(rootPanel);

        EventService.LOG_EVENT.addListener(
                string -> SwingUtilities.invokeLater(
                        () -> mainForm.getLogLabel().setText(string)
                )
        );
        EventService.REPAINT_CANVAS.addListener(chromosome -> {
            mainForm.getCanvas().clear();
            mainForm.getCanvas().render(chromosome);
        });
    }

    private static void setupFrame(final JPanel rootPanel) {
        final JFrame jFrame = new JFrame();
        final JMenuBar jMenuBar = new JMenuBar();
        setupMenuBar(jMenuBar);
        jFrame.setJMenuBar(jMenuBar);
        jFrame.setMinimumSize(new Dimension(400, 300));
        jFrame.setContentPane(rootPanel);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);
    }

    private void setupMutatorTable(JTable table) {
        mutatorTableModel = new MutatorTableModel();
        mutatorTableModel.addMutator(true, SwapPositionsMutator.class, .5);
        mutatorTableModel.addMutator(true, SwapGeneMutator.class, .5);

        table.setCellSelectionEnabled(true);
        table.setModel(mutatorTableModel);
    }

    private static void setupMenuBar(JMenuBar menuBar) {
        final JMenu menu = MenuService.menu(
                "File",
                MenuService.item(
                        "Open...",
                        actionEvent -> {
                            final File loadFile = FileService.promptForLoad();
                            if (loadFile == null) {
                                return;
                            }
                            FileService.loadExperiment(loadFile);

                        }
                ),
                MenuService.item(
                        "Save...",
                        actionEvent -> {
                            final File saveFile = FileService.promptForSave();
                            if (saveFile == null) {
                                return;
                            }
                            FileService.saveExperiment(saveFile, Experiment.getInstance());
                        },
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
                )
        );
        menuBar.add(menu);
    }

    private static void setUpEnvironment() {
        try {
            UIManager.setLookAndFeel(new DarculaLaf());
            UIManager.put("SplitPaneUI", SplitPaneUI.class.getName());
            UIManager.put("Button.darcula.selection.color1", UIManager.get("Button.darcula.color1"));
            UIManager.put("Button.darcula.selection.color2", UIManager.get("Button.darcula.color2"));
            UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
        } catch (UnsupportedLookAndFeelException e) {
            log.error("Darcula theme is not supported", e);
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "evoSearch");
    }

    private void createUIComponents() {
    }
}
