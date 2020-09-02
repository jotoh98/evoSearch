package evo.search.view;

import evo.search.Main;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.io.entities.Configuration;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private JCheckBox distinct;

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
                ).stream().collect(ISeq.toISeq())),
                new DiscreteChromosome(build, List.of(
                        new DiscreteGene(build, 1, 10),
                        new DiscreteGene(build, 5, 2),
                        new DiscreteGene(build, 3, 7),
                        new DiscreteGene(build, 0, 3)
                ).stream().collect(ISeq.toISeq())),
                new DiscreteChromosome(build, List.of(
                        new DiscreteGene(build, 1, 10),
                        new DiscreteGene(build, 4, 2),
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

        final int separatorIndex = csvSeparator.getSelectedIndex();
        final String separator = separatorIndex == 0 ? ", " : "; ";

        List<String> lines = chromosomeList
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

        if (exportTreasure.isSelected() && chromosomeList.size() > 0) {
            final List<DiscretePoint> treasures = chromosomeList
                    .get(0)
                    .getConfiguration()
                    .getTreasures();
            output += "\n" + printRowSeparated(treasures, DiscretePoint::getPosition, DiscretePoint::getDistance, separator);
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
     * Export the {@link DiscreteChromosome}s to latex.
     *
     * @return latex string
     */
    private String exportLatex() {
        List<String> individualStrings = chromosomeList
                .stream()
                .map(chromosome -> chromosome
                        .stream()
                        .map(ExportDialog::format)
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

        if (exportTreasure.isSelected() && chromosomeList.size() > 0 && chromosomeList.get(0).getConfiguration().getTreasures().size() > 0) {
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
            reducedString = "\\discretePlane{" + chromosomeList.get(0).getConfiguration().getPositions() + "}{\n\t" + reducedString + "\n}";
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
