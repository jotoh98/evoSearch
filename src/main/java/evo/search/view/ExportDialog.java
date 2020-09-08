package evo.search.view;

import evo.search.Evolution;
import evo.search.Main;
import evo.search.ga.DiscreteGene;
import evo.search.io.service.FileService;
import evo.search.util.ListUtils;
import io.jenetics.util.ISeq;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

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
     * Checkbox to determine whether to export fitness values.
     */
    private JCheckBox fitnessInfo;

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
        final int strategy = strategyTabs.getSelectedIndex();
        switch (strategy) {
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

        final boolean exportFitness = fitnessInfo.isSelected();
        List<String> lines = ListUtils.map(evolution.getHistory(), result -> {
            final String sep = exportFitness ? separator + result.bestFitness() : "";
            return printRowSeparated(
                    ISeq.of(result.bestPhenotype().genotype().chromosome()).asList(),
                    gene -> (int) gene.getPosition(),
                    gene -> (double) gene.getDistance(),
                    separator
            ) + sep;
        });

        if (distinct.isSelected() && lines.size() > 1)
            lines = ListUtils.removeRepeating(lines);

        String output = ListUtils.reduce(lines, ListUtils.separator("\n"), "");

        if (exportTreasure.isSelected() && evolution.getHistory().size() > 0) {
            final List<DiscreteGene> treasures = evolution.getConfiguration().getTreasures();
            output += "\n" + printRowSeparated(
                    treasures,
                    gene -> (int) gene.getPosition(),
                    gene -> (double) gene.getDistance(),
                    separator
            );
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
        final BinaryOperator<String> accumulator = ListUtils.separator(separator);
        final List<String> positionStrings = ListUtils.map(items, position.andThen(p -> Integer.toString(p)));
        final List<String> distanceStrings = ListUtils.map(items, distance.andThen(d -> Double.toString(d)));
        return ListUtils.reduce(positionStrings, accumulator, "") + "\n" + ListUtils.reduce(distanceStrings, accumulator, "");
    }

    /**
     * Export the {@link DiscreteGene} chromosomes to latex.
     *
     * @return latex string
     */
    private String exportLatex() {

        final boolean makro = latexUseMakros.isSelected();
        final boolean fitness = fitnessInfo.isSelected();

        List<String> individualStrings = ListUtils
                .map(evolution.getHistory(), result -> {
                    final StringBuilder chromosomeString = new StringBuilder();
                    if (makro)
                        chromosomeString.append("\\individual{");
                    result
                            .bestPhenotype()
                            .genotype()
                            .chromosome()
                            .forEach(gene -> chromosomeString.append(gene.printSmall()));
                    if (makro)
                        chromosomeString.append("}");
                    if (fitness) {
                        chromosomeString.append(" %");
                        chromosomeString.append(result.bestFitness());
                    }
                    return chromosomeString.toString();
                });

        if (distinct.isSelected() && individualStrings.size() > 1)
            individualStrings = ListUtils
                    .removeRepeating(individualStrings);

        final String separator = makro ? "\n\t" : "\n";
        String reducedString = ListUtils.reduce(individualStrings, ListUtils.separator(separator), "");

        if (exportTreasure.isSelected() && evolution.getHistory().size() > 0 && evolution.getConfiguration().getTreasures().size() > 0) {

            final List<String> smallPrints = ListUtils.map(evolution.getConfiguration().getTreasures(), DiscreteGene::printSmall);
            String treasures = ListUtils.reduce(smallPrints, ListUtils.REDUCE_WITH_SPACE, "");

            if (makro)
                treasures = "\\treasures{" + treasures + "}";

            reducedString = treasures + separator + reducedString;
        }
        if (evolution.getHistory().size() > 0 && makro)
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
