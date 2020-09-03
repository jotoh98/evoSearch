package evo.search.view;

import evo.search.Evolution;
import evo.search.Main;
import evo.search.ga.DiscreteGene;
import evo.search.io.service.FileService;
import evo.search.util.ListUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Export Dialog managing the export of {@link Evolution}s.
 */
public class ExportDialog extends JDialog {
    /**
     * Dialog root pane.
     */
    private JPanel contentPane;
    /**
     * Tab to select export format/strategy.
     */
    private JTabbedPane strategyTabs;
    /**
     * Text area to preview exported data.
     */
    private JTextArea exportTextArea;
    /**
     * Export invocation button.
     */
    private JButton exportButton;
    /**
     * Checkbox to determine whether to export also the treasures.
     */
    private JCheckBox exportTreasure;
    /**
     * Checkbox to determine whether to use latex makros.
     */
    private JCheckBox latexUseMakros;
    /**
     * Copy the exported string content of the {@link #exportTextArea}.
     */
    private JButton copyButton;
    /**
     * Combo box to select the csv separator.
     */
    private JComboBox<String> csvSeparator;
    /**
     * Button to save exported contents to a file.
     */
    private JButton saveButton;
    /**
     * Checkbox to determine whether to export non-repeating lists.
     */
    private JCheckBox distinct;

    /**
     * Evolution with data to export.
     */
    private Evolution evolution = null;

    /**
     * Default constructor. Binds component behaviour.
     */
    public ExportDialog() {
        setContentPane(contentPane);
        exportButton.addActionListener(l -> exportTextArea.setText(export()));
        copyButton.addActionListener(l -> onCopy());
        strategyTabs.addChangeListener(e -> saveButton.setVisible(strategyTabs.getSelectedIndex() == 1));
        saveButton.setVisible(false);
        saveButton.addActionListener(l -> onSave());
        setModal(true);
        getRootPane().setDefaultButton(exportButton);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Chromosome list constructor.
     *
     * @param evolution evolution to export
     */
    public ExportDialog(final Evolution evolution) {
        this();
        this.evolution = evolution;
    }

    /**
     * Static method to initialize and show the {@link ExportDialog} with a evolution to export.
     *
     * @param evolution evolution to export
     */
    public static void showDialog(final Evolution evolution) {
        Main.setupEnvironment();
        final ExportDialog exportDialog = new ExportDialog(evolution);
        exportDialog.setPreferredSize(new Dimension(500, 600));
        exportDialog.setLocationRelativeTo(null);
        exportDialog.pack();
        exportDialog.setVisible(true);
    }

    /**
     * Save the exported data to a file via prompt.
     */
    private void onSave() {
        SwingUtilities.invokeLater(() -> {
            final Path path = FileService.promptForSave(LangService.get("save.csv.file"), ".csv");
            if (path == null) return;
            try {
                if (Files.notExists(path))
                    Files.createFile(path);
                Files.write(path, export().getBytes());
            } catch (final IOException ignored) {
            }
        });
    }

    /**
     * Copy button behaviour.
     * Copies export string to clipboard.
     */
    private void onCopy() {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final StringSelection stringSelection = new StringSelection(exportTextArea.getText());
        clipboard.setContents(stringSelection, stringSelection);
    }

    /**
     * Export the data to a string.
     * Determines which export strategy to choose.
     *
     * @return exported data string
     */
    private String export() {
        if (evolution == null)
            return "Experiment empty";
        switch (strategyTabs.getSelectedIndex()) {
            case 0:
                return exportLatex();
            case 1:
                return exportCsv();
        }
        return exportLatex();
    }

    /**
     * Export the {@link DiscreteGene} chromosomes to csv.
     *
     * @return csv string
     */
    private String exportCsv() {

        final int separatorIndex = csvSeparator.getSelectedIndex();
        final String separator = separatorIndex == 0 ? ", " : "; ";

        List<String> lines = evolution
                .getHistoryOfBestPhenotype()
                .stream()
                .map(chromosome -> printRowSeparated(
                        chromosome
                                .stream()
                                .collect(Collectors.toList()),
                        DiscreteGene::getPosition,
                        DiscreteGene::getDistance,
                        separator
                ))
                .collect(Collectors.toList());

        if (distinct.isSelected() && lines.size() > 1)
            lines = ListUtils.removeRepeating(lines);

        String output = lines
                .stream()
                .reduce(ListUtils.separator("\n"))
                .orElse("");

        if (exportTreasure.isSelected() && evolution.getHistory().size() > 0) {
            final List<DiscreteGene> treasures = evolution.getConfiguration().getTreasures();
            output += "\n" + printRowSeparated(treasures, DiscreteGene::getPosition, DiscreteGene::getDistance, separator);
        }

        return output;
    }

    /**
     * Prints a list of items supplying an integer and double each in two separate rows.
     *
     * @param items     items supplying an integer and double each
     * @param position  integer mapper
     * @param distance  double mapper
     * @param separator column separator
     * @param <T>       type of items
     * @return row separated csv string of integers and doubles
     */
    private <T> String printRowSeparated(final List<T> items, final Function<T, Integer> position, final Function<T, Double> distance, final String separator) {
        final String positionStrings = items
                .stream()
                .map(position)
                .map(p -> Integer.toString(p))
                .reduce(ListUtils.separator(separator))
                .orElse("");

        final String distanceStrings = items
                .stream()
                .map(distance)
                .map(d -> Double.toString(d))
                .reduce(ListUtils.separator(separator))
                .orElse("");

        return positionStrings + "\n" + distanceStrings;
    }

    /**
     * Export the {@link DiscreteGene} chromosomes to latex.
     *
     * @return latex string
     */
    private String exportLatex() {
        List<String> individualStrings = evolution
                .getHistoryOfBestPhenotype()
                .stream()
                .map(chromosome -> chromosome
                        .stream()
                        .map(DiscreteGene::printSmall)
                        .reduce(ListUtils.REDUCE_WITH_SPACE)
                        .orElse("")
                )
                .collect(Collectors.toList());

        if (distinct.isSelected() && individualStrings.size() > 1)
            individualStrings = ListUtils
                    .removeRepeating(individualStrings);

        if (latexUseMakros.isSelected())
            individualStrings = individualStrings
                    .parallelStream()
                    .map(s -> "\\individual{" + s + "}")
                    .collect(Collectors.toList());

        final String separator = latexUseMakros.isSelected() ? "\n\t" : "\n";
        String reducedString = individualStrings
                .stream()
                .reduce(ListUtils.separator(separator))
                .orElse("");

        if (exportTreasure.isSelected() && evolution.getHistory().size() > 0 && evolution.getConfiguration().getTreasures().size() > 0) {
            String treasures = evolution
                    .getConfiguration()
                    .getTreasures()
                    .stream()
                    .map(DiscreteGene::printSmall)
                    .reduce(ListUtils.REDUCE_WITH_SPACE)
                    .orElse("");

            if (latexUseMakros.isSelected())
                treasures = "\\treasures{" + treasures + "}";

            reducedString = treasures + separator + reducedString;
        }
        if (evolution.getHistory().size() > 0 && latexUseMakros.isSelected())
            reducedString = "\\discretePlane{" + evolution.getConfiguration().getPositions() + "}{\n\t" + reducedString + "\n}";
        return reducedString;
    }

    /**
     * Custom create the csv separator combo box.
     */
    private void createUIComponents() {
        csvSeparator = new JComboBox<>();
        final DefaultComboBoxModel<String> boxModel = new DefaultComboBoxModel<>();
        boxModel.addAll(List.of(
                LangService.get("comma"),
                LangService.get("semicolon")
        ));
        csvSeparator.setModel(boxModel);
    }
}
