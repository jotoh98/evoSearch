package evo.search.view;

import evo.search.Evolution;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.io.entities.Configuration;
import evo.search.util.RandomUtils;
import evo.search.view.listener.DocumentEditHandler;
import evo.search.view.model.MutatorTableModel;
import io.jenetics.AbstractAlterer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ConfigPanel extends JDialog {
    private JSpinner limitSpinner;
    private JSpinner positionsSpinner;
    private JTextArea distancesTextArea;
    private JTextArea treasuresTextArea;
    private JTable mutatorTable;
    private JScrollPane mutatorScrollPane;
    private JSpinner populationSpinner;
    private JSpinner offspringSpinner;
    private JSpinner survivorsSpinner;
    private final DefaultComboBoxModel<Evolution.Fitness> fitnessListModel = new DefaultComboBoxModel<>();
    private final MutatorTableModel mutatorTableModel = new MutatorTableModel();
    @Getter
    private JPanel rootPanel;
    private JScrollPane distancesScrollPane;
    private JScrollPane treasuresScrollPane;
    private JButton shuffleTreasuresButton;
    private JButton shuffleDistancesButton;
    private JButton permutateDistancesButton;

    private Configuration configuration;

    private ConfigurationDialog parent;

    public static void main(final String[] args) {
        final ConfigPanel dialog = new ConfigPanel();
        dialog.setConfiguration(Configuration.builder().build());
        dialog.pack();
        dialog.setSize(400, 400);
        dialog.setVisible(true);
        System.exit(0);
    }

    private JButton permutateTreasuresButton;

    public void setParent(final ConfigurationDialog parent) {
        this.parent = parent;
        propagateScroll(mutatorScrollPane);
        propagateScroll(distancesScrollPane);
        propagateScroll(treasuresScrollPane);
    }

    public void propagateScroll(final JScrollPane scrollPane) {
        if (parent == null) {
            return;
        }
        scrollPane.addMouseWheelListener(e -> {
            final JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();

            if (!verticalScrollBar.isShowing()) {
                parent.getConfigScrollWrapper().dispatchEvent(e);
                return;
            }

            if (e.getWheelRotation() < 0 && verticalScrollBar.getValue() == 0) {
                parent.getConfigScrollWrapper().dispatchEvent(e);
            } else if (verticalScrollBar.getValue() + scrollPane.getHeight() == verticalScrollBar.getMaximum()) {
                parent.getConfigScrollWrapper().dispatchEvent(e);
            }
        });
    }

    private void bindSpinner(final JSpinner spinner, final int initialValue, final Consumer<Integer> valueConsumer) {
        bindSpinner(spinner, initialValue, valueConsumer, () -> {
            if ((int) spinner.getValue() < 0) {
                spinner.setValue(0);
            }
        });
    }

    private void bindSpinner(final JSpinner spinner, final int initialValue, final Consumer<Integer> valueConsumer, final Runnable change) {
        spinner.setValue(initialValue);
        spinner.addChangeListener(e -> {
            change.run();
            parent.triggerChange();
            try {
                valueConsumer.accept((int) spinner.getValue());
            } catch (final Exception ignored) {
            }
        });
    }

    private void bindLimit() {
        bindSpinner(limitSpinner, configuration.getLimit(), configuration::setLimit);
    }

    private void bindPosition() {
        bindSpinner(positionsSpinner, configuration.getPositions(), configuration::setPositions);
    }

    private void bindPopulation() {
        bindSpinner(populationSpinner, configuration.getPopulation(), configuration::setPopulation, () -> {
            int populationInput = (int) populationSpinner.getValue();
            if (populationInput < 1) {
                populationSpinner.setValue(1);
                populationInput = 1;
            }
            if (populationInput < (int) offspringSpinner.getValue()) {
                offspringSpinner.setValue(populationInput);
            }
            if (populationInput < (int) survivorsSpinner.getValue()) {
                survivorsSpinner.setValue(populationInput);
            }
        });
    }

    private void bindOffspring() {
        bindSpinner(offspringSpinner, configuration.getOffspring(), configuration::setOffspring, () -> {
            final int offspring = (int) offspringSpinner.getValue();
            if (offspring < 0) {
                offspringSpinner.setValue(0);
            }
            if (offspring > (int) populationSpinner.getValue()) {
                populationSpinner.setValue(offspring);
            }
        });
    }

    private void bindDistances() {
        printDistances(configuration.getDistances());

        distancesTextArea.getDocument().addDocumentListener((DocumentEditHandler) e -> {
            parent.triggerChange();
            final String input = distancesTextArea.getText();

            final List<Double> distances = Arrays.stream(input.split("\\s*,\\s*"))
                    .map(string -> {
                        try {
                            return Double.parseDouble(string);
                        } catch (final Exception ignored) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            configuration.setDistances(distances);
        });

        distancesTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    printDistances(configuration.getDistances());
                    e.consume();
                }
            }
        });
    }

    private void printDistances(final List<Double> distances) {
        distancesTextArea.setText(printConsecutive(distances, String::valueOf));
    }

    private <T> String printConsecutive(final List<T> list, final Function<T, String> mapper) {
        return list
                .stream()
                .map(mapper)
                .reduce((s, s2) -> s + ", " + s2)
                .orElse("");
    }

    private void bindTreasures() {

        printTreasures(configuration.getTreasures());

        final AtomicBoolean smartInsert = new AtomicBoolean(false);

        treasuresTextArea.getDocument()
                .addDocumentListener((DocumentEditHandler) e -> getTreasureInput(configuration));

        treasuresTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                smartInsert.set(false);
            }
        });

        treasuresTextArea.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(final KeyEvent e) {
                parent.triggerChange();
                final String text = treasuresTextArea.getText();
                final int caretPosition = treasuresTextArea.getCaretPosition();
                if (!smartInsert.get() && e.getKeyChar() == ',' || e.getKeyChar() == '(') {
                    treasuresTextArea.insert(e.getKeyChar() == ',' ? ", (, )" : "(, )", caretPosition);
                    treasuresTextArea.setCaretPosition(caretPosition + (e.getKeyChar() == ',' ? 3 : 1));
                    smartInsert.set(true);
                    e.consume();
                    return;
                }
                if (smartInsert.get() && e.getKeyChar() == KeyEvent.VK_TAB) {
                    if (text.length() > caretPosition + 2) {
                        treasuresTextArea.setCaretPosition(caretPosition + 2);
                    }
                    smartInsert.set(false);
                    e.consume();
                }
            }

            @Override
            public void keyPressed(final KeyEvent e) {
                if (smartInsert.get() && e.getKeyChar() == KeyEvent.VK_TAB) {
                    e.consume();
                }
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    e.consume();
                    getTreasureInput(configuration);
                    printTreasures(configuration.getTreasures());
                }
            }
        });
    }

    private JComboBox<Evolution.Fitness> fitnessComboBox;

    private void printTreasures(final List<DiscretePoint> treasures) {
        treasuresTextArea.setText(printConsecutive(
                treasures,
                discretePoint -> String.format("(%s, %s)", discretePoint.getPosition(), discretePoint.getDistance())
        ));
    }

    private void bindSurvivors() {
        bindSpinner(survivorsSpinner, configuration.getSurvivors(), configuration::setSurvivors, () -> {
            final int survivors = (int) survivorsSpinner.getValue();
            if (survivors < 0) {
                survivorsSpinner.setValue(0);
            }
            if (survivors > (int) populationSpinner.getValue()) {
                populationSpinner.setValue(survivors);
            }
        });
    }

    public ConfigPanel() {
        setModal(true);

        shuffleDistancesButton.setEnabled(false);
        permutateDistancesButton.setEnabled(false);

        shuffleTreasuresButton.setEnabled(false);
        permutateTreasuresButton.setEnabled(false);

        shuffleTreasuresButton.addActionListener(e -> {
            final ShuffleDialog<DiscretePoint> shuffleDialog = new ShuffleDialog<>();
            shuffleDialog.setRandomSupplier(shuffleDialog::shuffleTreasures);
            shuffleDialog.setLocationRelativeTo(this);

            shuffleDialog.setPositions(configuration.getPositions());
            shuffleDialog.setTreasureConsumer(shuffledTreasures -> {
                if (shuffledTreasures == null) {
                    return;
                }
                configuration.setTreasures(shuffledTreasures);
                printTreasures(shuffledTreasures);
            });
            shuffleDialog.setVisible(true);
        });

        shuffleDistancesButton.addActionListener(e -> {
            final ShuffleDialog<Double> shuffleDialog = new ShuffleDialog<>();
            shuffleDialog.setRandomSupplier(RandomUtils::inRange);
            shuffleDialog.setLocationRelativeTo(this);

            shuffleDialog.setPositions(configuration.getPositions());
            shuffleDialog.setTreasureConsumer(shuffledTreasures -> {
                if (shuffledTreasures == null) {
                    return;
                }
                configuration.setDistances(shuffledTreasures);
                printDistances(shuffledTreasures);
            });
            shuffleDialog.setVisible(true);
        });

        permutateDistancesButton.addActionListener(e -> {
            Collections.shuffle(configuration.getDistances());
            printDistances(configuration.getDistances());
        });

        permutateTreasuresButton.addActionListener(e -> {
            Collections.shuffle(configuration.getTreasures());
            printTreasures(configuration.getTreasures());
        });

        fitnessComboBox.setModel(fitnessListModel);
        fitnessListModel.addAll(Arrays.asList(Evolution.Fitness.values()));

        mutatorTable.setModel(mutatorTableModel);
    }

    private void bindMutators() {

        for (DiscreteAlterer configAlterer : configuration.getAlterers())
            mutatorTableModel.addMutator(configAlterer.getClass().getSimpleName(), configAlterer.getProbability());

        mutatorTableModel.addTableModelListener(l -> {
            parent.triggerChange();
            configuration.setAlterers(mutatorTableModel.getSelected());
        });

        mutatorTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                final int clickedRow = mutatorTable.rowAtPoint(e.getPoint());
                final int clickedCol = mutatorTable.columnAtPoint(e.getPoint());
                if (clickedCol == 0) {
                    final boolean selected = (boolean) mutatorTableModel.getValueAt(clickedRow, clickedCol);
                    mutatorTableModel.setValueAt(!selected, clickedRow, clickedCol);
                }
            }
        });
    }

    public void setConfiguration(final Configuration configuration) {
        this.configuration = configuration;
        shuffleDistancesButton.setEnabled(true);
        permutateDistancesButton.setEnabled(true);
        shuffleTreasuresButton.setEnabled(true);
        permutateTreasuresButton.setEnabled(true);
        bindLimit();
        bindPosition();
        bindDistances();
        bindTreasures();
        bindFitness();
        bindMutators();
        bindPopulation();
        bindOffspring();
        bindSurvivors();
    }

    private void getTreasureInput(final Configuration configuration) {
        final String text = treasuresTextArea.getText();
        final Pattern pattern = Pattern.compile("\\((\\d+)\\s*,\\s*(\\d+(?:\\.\\d+)?)\\)");
        final Matcher matcher = pattern.matcher(text);

        final ArrayList<DiscretePoint> treasures = new ArrayList<>();
        while (matcher.find()) {
            try {
                final int position = Integer.parseInt(matcher.group(1));
                final double distance = Double.parseDouble(matcher.group(2));
                //TODO: add positions
                treasures.add(new DiscretePoint(0, position, distance));
            } catch (final NumberFormatException ignored) {
            }
        }
        configuration.setTreasures(treasures);
    }

    private void bindFitness() {
        fitnessListModel.setSelectedItem(configuration.getFitness());
        fitnessComboBox.addActionListener(e -> {
            parent.triggerChange();
            configuration.setFitness((Evolution.Fitness) fitnessComboBox.getSelectedItem());
        });
    }

}
