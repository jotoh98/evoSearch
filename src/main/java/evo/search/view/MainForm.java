package evo.search.view;

import com.bulenkov.darcula.DarculaLaf;
import evo.search.Experiment;
import evo.search.io.EventService;
import evo.search.io.FileService;
import evo.search.io.MenuService;
import evo.search.view.laf.SplitPaneUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
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
    private JTable experimentConfigTable;
    private JList list1;
    private Canvas canvas;
    private JButton editButton;
    private JProgressBar progressBar;
    private JLabel logLabel;

    public static void main(String[] args) {
        setUpEnvironment();
        final MainForm mainForm = new MainForm();

        final JPanel rootPanel = mainForm.getRootPanel();
        mainForm.getRunButton().addActionListener(e -> {
            Experiment.init(10, 3);
            mainForm.getProgressBar().setMaximum(1000);
            mainForm.getProgressBar().setVisible(true);
            CompletableFuture
                    .supplyAsync(() -> Experiment.getInstance()
                            .evolve(1000, integer -> mainForm.getProgressBar().setValue(integer)).chromosome())
                    .thenAccept(chromosome -> {
                        EventService.LOG_EVENT.trigger("Experiment finished.");
                        EventService.REPAINT_CANVAS.trigger(chromosome);
                    })
                    .thenRun(() -> mainForm.getProgressBar().setVisible(false));
        });

        setupExperimentTable(mainForm.getExperimentConfigTable());

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

    private static void setupExperimentTable(JTable table) {
        final DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return column == 1;
            }
        };
        table.setModel(tableModel);
        tableModel.addColumn("Property");
        tableModel.addColumn("Value");
        tableModel.addRow(new Object[]{"int", 2});
    }

    private static void setupFrame(final JPanel rootPanel) {
        final JFrame jFrame = new JFrame();
        final JMenuBar jMenuBar = new JMenuBar();
        setupMenuBar(jMenuBar);
        jFrame.setJMenuBar(jMenuBar);
        jFrame.setMinimumSize(new Dimension(400, 300));
        jFrame.setContentPane(rootPanel);
        jFrame.setVisible(true);
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
            //TODO: remove focus glow
            UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
        } catch (UnsupportedLookAndFeelException e) {
            log.error("Darcula theme is not supported", e);
        }

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "evoSearch");
    }
}
