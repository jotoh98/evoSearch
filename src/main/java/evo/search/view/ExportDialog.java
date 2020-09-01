package evo.search.view;

import evo.search.Main;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.io.entities.Configuration;
import evo.search.util.ListUtils;
import io.jenetics.util.ISeq;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Export Dialog managing the export of {@link DiscreteChromosome}s.
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
    private JCheckBox latexExportTreasure;
    /**
     * Checkbox to determine whether to use latex makros.
     */
    private JCheckBox latexUseMakros;
    /**
     * Copy the exported string content of the {@link #exportTextArea}.
     */
    private JButton copyButton;

    /**
     * List of {@link DiscreteChromosome}s to export.
     */
    private List<DiscreteChromosome> chromosomeList = new ArrayList<>();

    /**
     * Default constructor. Binds component behaviour.
     */
    public ExportDialog() {
        setContentPane(contentPane);
        exportButton.addActionListener(l -> exportTextArea.setText(export()));
        copyButton.addActionListener(l -> onCopy());
        setModal(true);
        getRootPane().setDefaultButton(exportButton);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Chromosome list constructor.
     *
     * @param chromosomes list of chromosomes to export
     */
    public ExportDialog(final List<DiscreteChromosome> chromosomes) {
        this();
        this.chromosomeList = chromosomes;
    }

    /**
     * Serialize a discrete gene to the format (1, 1.4563).
     *
     * @param gene gene to serialize
     * @return serialized gene
     */
    private static String format(final DiscreteGene gene) {
        return String.format("(%s, %s)", gene.getPosition(), gene.getDistance());
    }

    /**
     * Serialize a discrete point to the format (1, 1.4563).
     *
     * @param point point to serialize
     * @return serialized point
     */
    private static String format(final DiscretePoint point) {
        return String.format("(%s, %s)", point.getPosition(), point.getDistance());
    }

    /**
     * Static method to initialize and show the {@link ExportDialog} with a list of chromosomes.
     *
     * @param chromosomes chromosomes to export
     */
    public static void showDialog(final List<DiscreteChromosome> chromosomes) {
        Main.setupEnvironment();
        final ExportDialog exportDialog = new ExportDialog(chromosomes);
        exportDialog.pack();
        exportDialog.setVisible(true);
    }

    /**
     * Test entry point for dialog evaluation.
     *
     * @param ignored ignored cli args
     */
    public static void main(final String[] ignored) {
        final Configuration build = Configuration
                .builder()
                .positions(6)
                .treasures(List.of(
                        new DiscretePoint(6, 3, 1),
                        new DiscretePoint(6, 6, 14),
                        new DiscretePoint(6, 9, 4),
                        new DiscretePoint(6, 1, 7)
                ))
                .build();
        ExportDialog.showDialog(List.of(
                new DiscreteChromosome(build, List.of(
                        new DiscreteGene(build, 1, 10),
                        new DiscreteGene(build, 5, 2),
                        new DiscreteGene(build, 3, 7),
                        new DiscreteGene(build, 0, 3)
                ).stream().collect(ISeq.toISeq()))
        ));
        System.exit(0);
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
        switch (strategyTabs.getSelectedIndex()) {
            case 0:
                return exportLatex();
            case 1:
                return exportCsv();
        }
        return exportLatex();
    }

    /**
     * Export the {@link DiscreteChromosome}s to csv.
     *
     * @return csv string
     */
    private String exportCsv() {
        //TODO: csv export, also file export
        return "";
    }

    /**
     * Export the {@link DiscreteChromosome}s to latex.
     *
     * @return latex string
     */
    private String exportLatex() {
        Stream<String> chromosomeStrings = chromosomeList.stream().map(chromosome -> chromosome
                .stream()
                .map(ExportDialog::format)
                .reduce(ListUtils.REDUCE_WITH_SPACE)
                .orElse("")
        );

        if (latexUseMakros.isSelected())
            chromosomeStrings = chromosomeStrings.map(s -> "\\individual{" + s + "}");

        final List<String> individualStrings = chromosomeStrings.collect(Collectors.toList());

        final String separator = latexUseMakros.isSelected() ? "\n\t" : "\n";
        String reducedString = individualStrings
                .stream()
                .reduce(ListUtils.separator(separator))
                .orElse("");

        if (latexExportTreasure.isSelected() && chromosomeList.size() > 0 && chromosomeList.get(0).getConfiguration().getTreasures().size() > 0) {
            String treasures = chromosomeList
                    .get(0)
                    .getConfiguration()
                    .getTreasures()
                    .stream()
                    .map(ExportDialog::format)
                    .reduce(ListUtils.REDUCE_WITH_SPACE)
                    .orElse("");

            if (latexUseMakros.isSelected())
                treasures = "\\treasures{" + treasures + "}";

            reducedString = treasures + separator + reducedString;
        }
        if (chromosomeList.size() > 0 && latexUseMakros.isSelected())
            reducedString = "\\discretePlane{" + chromosomeList.get(0).getConfiguration().getPositions() + "}{\n\t" + reducedString + "}";
        return reducedString;
    }
}
