package evo.search.view;

import evo.search.Evolution;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.io.entities.Configuration;
import evo.search.util.ListUtils;
import evo.search.util.RandomUtils;
import evo.search.view.listener.DocumentAdapter;
import evo.search.view.model.MutatorTableModel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private JButton permutateTreasuresButton;
    @Getter
    private Configuration configuration;
    private ConfigurationDialog parent;
    private JComboBox<Evolution.Fitness> fitnessComboBox;
    private JCheckBox noPermutationCheckbox;
    private JSlider distanceMutationSlider;
    private JLabel distanceMutationLabel;

    /**
     * Constructs a single configuration panel.
     * Inserts default values.
     */
    public ConfigPanel(final Configuration configuration) {
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

        setConfiguration(configuration);
    }

    /**
     * Assign a parent to the panel for button bindings and scroll propagation.
     * Only accepts a {@link ConfigurationDialog}.
     *
     * @param parent configuration dialog parent
     */
    public void setParent(final ConfigurationDialog parent) {
        this.parent = parent;
        propagateScroll(mutatorScrollPane);
        propagateScroll(distancesScrollPane);
        propagateScroll(treasuresScrollPane);
    }

    /**
     * Assign a configuration to the panel for value binding.
     *
     * @param configuration configuration to display
     */
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
        bindPermutationOnly();
        bindDistanceMutation();
    }

    /**
     * Propagate a scroll inside of the {@link ConfigPanel} to
     * the {@link ConfigurationDialog}s scroll panel, if the inner
     * scroll reaches its borders.
     *
     * @param scrollPane the inner scroll panel propagating the scroll event
     */
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

    /**
     * Print a consecutive list of distance values separated by a comma.
     *
     * @param distances list of double
     */
    private void printDistances(final List<Double> distances) {
        distancesTextArea.setText(ListUtils.printConsecutive(distances, String::valueOf, ", "));
    }

    /**
     * Print a consecutive list of discrete treasure points separated by a comma.
     *
     * @param treasures list of discrete points
     */
    private void printTreasures(final List<DiscretePoint> treasures) {
        treasuresTextArea.setText(ListUtils.printConsecutive(
                treasures,
                discretePoint -> String.format("(%s, %s)", discretePoint.getPosition(), discretePoint.getDistance()),
                ", "
        ));
    }

    /**
     * Parse the treasure point fields input and add it as a list of discrete points to the configuration.
     */
    private void getTreasureInput() {
        final String text = treasuresTextArea.getText();
        final Pattern pattern = Pattern.compile("\\((\\d+)\\s*,\\s*(\\d+(?:\\.\\d+)?)\\)");
        final Matcher matcher = pattern.matcher(text);

        final ArrayList<DiscretePoint> treasures = new ArrayList<>();
        while (matcher.find()) {
            try {
                final int position = Integer.parseInt(matcher.group(1));
                final double distance = Double.parseDouble(matcher.group(2));
                treasures.add(new DiscretePoint(0, position, distance));
            } catch (final NumberFormatException ignored) {
            }
        }
        configuration.setTreasures(treasures);
    }

    /**
     * Get a list of selected and instantiated discrete alterers.
     *
     * @return list of selected discrete alterers
     */
    private List<DiscreteAlterer> getSelectedAlterers() {
        return IntStream.of(mutatorTable.getSelectionModel().getSelectedIndices())
                .mapToObj(row -> {
                    final String simpleName = (String) mutatorTable.getValueAt(row, 0);
                    final Class<? extends DiscreteAlterer> altererClass = DiscreteAlterer.getSubclasses().stream()
                            .filter(aClass -> aClass.getSimpleName().equals(simpleName))
                            .findFirst()
                            .orElse(null);

                    if (altererClass == null) return null;

                    final double probability = (double) mutatorTable.getValueAt(row, 1);

                    try {
                        return altererClass.getDeclaredConstructor(double.class)
                                .newInstance(probability);
                    } catch (final Exception e) {
                        log.error("Could not instantiate mutator.", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Bind a value to an initial value.
     * It triggers a change in the assigned {@link ConfigurationDialog}, runs a change handler and a value consumer.
     *
     * @param spinner       spinner to bind the value to
     * @param initialValue  the initial value to be bound
     * @param valueConsumer consumer of the new int value
     * @param change        change listener
     */
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

    /**
     * Bind a positive value to a spinner.
     * The input value has to be positive.
     *
     * @param spinner       spinner to bind the value to
     * @param initialValue  the initial value to be bound
     * @param valueConsumer consumer of the new int value
     * @see #bindSpinner(JSpinner, int, Consumer, Runnable)
     */
    private void bindSpinner(final JSpinner spinner, final int initialValue, final Consumer<Integer> valueConsumer) {
        bindSpinner(spinner, initialValue, valueConsumer, () -> {
            if ((int) spinner.getValue() < 0) {
                spinner.setValue(0);
            }
        });
    }

    /**
     * Print the distances to their output and bind the inputs value to the distances in the {@link Configuration}.
     */
    private void bindDistances() {
        printDistances(configuration.getDistances());

        distancesTextArea.getDocument().addDocumentListener((DocumentAdapter) e -> {
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

    /**
     * Select the given fitness method and bind the selection to the fitness in the {@link Configuration}.
     */
    private void bindFitness() {
        fitnessListModel.setSelectedItem(configuration.getFitness());
        fitnessComboBox.addActionListener(e -> {
            parent.triggerChange();
            configuration.setFitness((Evolution.Fitness) fitnessComboBox.getSelectedItem());
        });
    }

    /**
     * Print the given limit to the spinner and bind the value to the limit in the {@link Configuration}.
     */
    private void bindLimit() {
        bindSpinner(limitSpinner, configuration.getLimit(), configuration::setLimit);
    }

    private void bindMutators() {

        mutatorTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        for (final DiscreteAlterer configAlterer : configuration.getAlterers()) {
            final int rowByName = mutatorTableModel.getRowByName(configAlterer.getClass().getSimpleName());
            if (rowByName >= 0) {
                mutatorTable.getSelectionModel().addSelectionInterval(rowByName, rowByName);
                mutatorTableModel.setValueAt(configAlterer.getProbability(), rowByName, 1);
            }
        }

        mutatorTable.getSelectionModel().addListSelectionListener(e -> {
            parent.triggerChange();
            configuration.setAlterers(getSelectedAlterers());
        });

        mutatorTable.getModel().addTableModelListener(l -> {
            parent.triggerChange();
            configuration.setAlterers(getSelectedAlterers());
        });

        final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
                final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected)
                    component.setBackground(new Color(36, 83, 6));

                if (!isSelected || hasFocus)
                    component.setBackground(null);
                return component;
            }
        };
        mutatorTable.setDefaultRenderer(Double.class, renderer);
        mutatorTable.setDefaultRenderer(String.class, renderer);

        mutatorTable.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent e) {
                mutatorScrollPane.requestFocus();
                mutatorTable.repaint();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                mutatorTable.repaint();
            }
        });
    }

    /**
     * Print the given offspring to the spinner and bind the value to the offspring in the {@link Configuration}.
     * The offset has to be in the interval [0, {@link Configuration#getPopulation()}]. If it gets bigger, the
     * {@link #populationSpinner}s value increases.
     */
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

    /**
     * Print the given positions to the spinner and bind the value to the positions in the {@link Configuration}.
     */
    private void bindPosition() {
        bindSpinner(positionsSpinner, configuration.getPositions(), configuration::setPositions);
    }

    /**
     * Print the given offspring to the spinner and bind the value to the limit in the {@link Configuration}.
     * The offset has to be greater than 0, {@link Configuration#getOffspring()} and
     * {@link Configuration#getSurvivors()} ()}. If it gets smaller than the last two, the two decrease.
     */
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

    /**
     * Print the given survivors to the spinner and bind the value to the survivors in the {@link Configuration}.
     * The offset has to be in the interval [0, {@link Configuration#getPopulation()}]. If it gets bigger, the
     * {@link #populationSpinner}s value increases.
     */
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

    /**
     * Print the treasure points to their output and bind the inputs value to the treasure points
     * in the {@link Configuration}.
     */
    private void bindTreasures() {

        printTreasures(configuration.getTreasures());

        final AtomicBoolean smartInsert = new AtomicBoolean(false);

        treasuresTextArea.getDocument()
                .addDocumentListener((DocumentAdapter) e -> getTreasureInput());

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
                    getTreasureInput();
                    printTreasures(configuration.getTreasures());
                }
            }
        });
    }

    /**
     * Sets the state for the checkbox regarding the permutation-independence.
     */
    private void bindPermutationOnly() {
        noPermutationCheckbox.setSelected(configuration.isChooseWithoutPermutation());
        noPermutationCheckbox.addChangeListener(e -> {
            configuration.setChooseWithoutPermutation(noPermutationCheckbox.isSelected());
            parent.triggerChange();
        });
    }

    private void bindDistanceMutation() {
        distanceMutationSlider.addChangeListener(e -> {
            final double value = distanceMutationSlider.getValue() / (double) distanceMutationSlider.getMaximum();
            distanceMutationLabel.setText(String.format("\u00B1 %.2f", value));
            if (parent != null)
                parent.triggerChange();
            configuration.setDistanceMutationDelta(value);
        });

        distanceMutationSlider.setValue((int) Math.round(configuration.getDistanceMutationDelta() * distanceMutationSlider.getMaximum()));
    }

    /**
     * Default intellij gui forms method for custom generated components.
     * The {@link #mutatorTable}s selection is limited to the first column and is extending and toggles the selected.
     *
     * @see JTable#changeSelection(int, int, boolean, boolean)
     */
    private void createUIComponents() {
        mutatorTable = new JTable() {
            @Override
            public void changeSelection(final int rowIndex, final int columnIndex, final boolean toggle, final boolean extend) {
                if (columnIndex == 0)
                    super.changeSelection(rowIndex, columnIndex, true, false);
            }
        };
    }

}
