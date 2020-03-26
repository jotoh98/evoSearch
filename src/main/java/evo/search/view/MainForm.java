package evo.search.view;

import com.bulenkov.darcula.DarculaLaf;
import evo.search.Experiment;
import evo.search.view.laf.SplitPaneUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Getter
public class MainForm {
    private JPanel rootPanel;
    private JPanel toolbar;
    private JButton runButton;
    private JTable table1;
    private JList list1;
    private Canvas canvas;
    private JButton editButton;
    private JProgressBar progressBar;

    public static void main(String[] args) {
        setUpEnvironment();
        final MainForm mainForm = new MainForm();

        final JPanel rootPanel = mainForm.getRootPanel();
        mainForm.getRunButton().addActionListener(e -> {
            Experiment.init(10, 3);
            mainForm.getProgressBar().setMaximum(1000);
            mainForm.getProgressBar().setVisible(true);
            CompletableFuture
                    .supplyAsync(() -> Experiment.getInstance().evolve(1000, integer -> mainForm.getProgressBar().setValue(integer)).chromosome())
                    .thenAccept(chromosome -> {
                        mainForm.getCanvas().clear();
                        mainForm.getCanvas().render(chromosome);
                    })
                    .thenRun(() -> mainForm.getProgressBar().setVisible(false));
        });

        setupFrame(rootPanel);
    }

    private static void setupFrame(final JPanel rootPanel) {
        final JFrame jFrame = new JFrame();
        jFrame.setMinimumSize(new Dimension(400, 300));
        jFrame.setContentPane(rootPanel);
        jFrame.setVisible(true);
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
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WikiTeX");
    }
}
