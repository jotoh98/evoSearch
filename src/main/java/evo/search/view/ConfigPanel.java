package evo.search.view;

import evo.search.Environment;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.ga.mutators.SwapGeneMutator;
import evo.search.ga.mutators.SwapPositionsMutator;
import evo.search.io.entities.Configuration;
import evo.search.view.listener.DocumentEditHandler;
import evo.search.view.model.MutatorTableModel;
import io.jenetics.AbstractAlterer;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.List;
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
    private JButton shuffleButton;
    private JComboBox<Environment.Fitness> fitnessComboBox;
    private JTable mutatorTable;
    private JScrollPane mutatorScrollPane;
    private JSpinner populationSpinner;
    private JSpinner offspringSpinner;
    private JSpinner survivorsSpinner;
    private final DefaultComboBoxModel<Environment.Fitness> fitnessListModel = new DefaultComboBoxModel<>();
    private final MutatorTableModel mutatorTableModel = new MutatorTableModel();
    private JPanel rootPanel;
    private JScrollPane distancesScrollPane;
    private JScrollPane treasuresScrollPane;

    private Configuration configuration;

    private ConfigurationDialog parent;

    public static void main(String[] args) {
        ConfigPanel dialog = new ConfigPanel();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public ConfigPanel() {
        setModal(true);

        shuffleButton.setEnabled(false);
        shuffleButton.addActionListener(e -> {
            final TreasureShuffleDialog shuffleDialog = new TreasureShuffleDialog();
            shuffleDialog.setLocationRelativeTo(this);

            shuffleDialog.setPositions(configuration.getPositions());
            shuffleDialog.setTreasureConsumer(shuffledTreasures -> {
                configuration.setTreasures(shuffledTreasures);
                printTreasures(shuffledTreasures);
            });
            shuffleDialog.setVisible(true);
        });

        fitnessComboBox.setModel(fitnessListModel);
        fitnessListModel.addAll(Arrays.asList(Environment.Fitness.values()));

        mutatorTable.setModel(mutatorTableModel);
        mutatorTableModel.addMutator(false, SwapGeneMutator.class, .5);
        mutatorTableModel.addMutator(false, SwapPositionsMutator.class, .5);

        mutatorScrollPane.addMouseWheelListener(e -> {

        });
    }

    public void setParent(ConfigurationDialog parent) {
        this.parent = parent;
        propagateScroll(mutatorScrollPane);
        propagateScroll(distancesScrollPane);
        propagateScroll(treasuresScrollPane);
    }

    public void propagateScroll(JScrollPane scrollPane) {
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

    private void bindSpinner(JSpinner spinner, int initialValue, Consumer<Integer> valueConsumer) {
        bindSpinner(spinner, initialValue, valueConsumer, () -> {
            if ((int) spinner.getValue() < 0) {
                spinner.setValue(0);
            }
        });
    }

    private void bindSpinner(JSpinner spinner, int initialValue, Consumer<Integer> valueConsumer, Runnable change) {
        spinner.setValue(initialValue);
        spinner.addChangeListener(e -> {
            change.run();
            parent.triggerChange();
            try {
                valueConsumer.accept((int) spinner.getValue());
            } catch (Exception ignored) {
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
                        } catch (Exception ignored) {
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

    private void printDistances(List<Double> distances) {
        distancesTextArea.setText(printConsecutive(distances, String::valueOf));
    }

    private <T> String printConsecutive(List<T> list, Function<T, String> mapper) {
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

    private void getTreasureInput(final Configuration configuration) {
        final String text = treasuresTextArea.getText();
        final Pattern pattern = Pattern.compile("\\((\\d+)\\s*,\\s*(\\d+(?:\\.\\d+)?)\\)");
        final Matcher matcher = pattern.matcher(text);

        final ArrayList<DiscretePoint> treasures = new ArrayList<>();
        while (matcher.find()) {
            try {
                final int position = Integer.parseInt(matcher.group(1));
                final double distance = Double.parseDouble(matcher.group(2));
                treasures.add(new DiscretePoint(position, distance));
            } catch (NumberFormatException ignored) {
            }
        }
        configuration.setTreasures(treasures);
    }

    private void printTreasures(List<DiscretePoint> treasures) {
        treasuresTextArea.setText(printConsecutive(
                treasures,
                discretePoint -> String.format("(%s, %s)", discretePoint.getPosition(), discretePoint.getDistance())
        ));
    }

    private void bindFitness() {
        fitnessListModel.setSelectedItem(configuration.getFitness());
        fitnessComboBox.addActionListener(e -> {
            parent.triggerChange();
            configuration.setFitness((Environment.Fitness) fitnessComboBox.getSelectedItem());
        });
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

    private void bindMutators() {
        for (DiscreteAlterer configAlterer : configuration.getAlterers()) {
            for (MutatorTableModel.MutatorConfig mutatorConfig : mutatorTableModel.getMutatorConfigs()) {
                if (configAlterer.getClass().equals(mutatorConfig.getMutator())) {
                    mutatorConfig.setSelected(true);
                    final double probability = ((AbstractAlterer<?, ?>) configAlterer).probability();
                    mutatorConfig.setProbability(probability);
                    break;
                }
            }
        }

        mutatorTableModel.addTableModelListener(l -> {
            parent.triggerChange();
            final List<DiscreteAlterer> mutatorInstances = mutatorTableModel.getMutatorConfigs().stream()
                    .filter(MutatorTableModel.MutatorConfig::isSelected)
                    .map(mutatorConfig -> {
                        try {
                            return mutatorConfig.instantiate();
                        } catch (Exception e) {
                            log.error("Instantiation of mutator failed", e);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            configuration.setAlterers(mutatorInstances);
        });
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        shuffleButton.setEnabled(true);
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridBagLayout());
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("lang", "limit"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel1.add(label1, gbc);
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("lang", "positions"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel1.add(label2, gbc);
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("lang", "distances"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 0, 0, 5);
        panel1.add(label3, gbc);
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, this.$$$getMessageFromBundle$$$("lang", "treasures"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 0, 0, 5);
        panel1.add(label4, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 3, 0, 3);
        panel1.add(panel2, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        panel2.add(panel3, BorderLayout.SOUTH);
        panel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        shuffleButton = new JButton();
        this.$$$loadButtonText$$$(shuffleButton, this.$$$getMessageFromBundle$$$("lang", "shuffle"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(shuffleButton, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel3.add(spacer1, gbc);
        treasuresScrollPane = new JScrollPane();
        treasuresScrollPane.setMinimumSize(new Dimension(50, 54));
        treasuresScrollPane.setPreferredSize(new Dimension(50, 54));
        panel2.add(treasuresScrollPane, BorderLayout.CENTER);
        treasuresTextArea = new JTextArea();
        treasuresTextArea.setLineWrap(true);
        treasuresTextArea.setMargin(new Insets(2, 6, 2, 6));
        treasuresTextArea.setMinimumSize(new Dimension(2, 17));
        treasuresTextArea.setText("14.0, 15.0, 3.0, 5.5, 8.7, 3.6, 1.0, 4.0, 3.0, 5.5, 8.7, 3.6, 1.0, 4.0");
        treasuresTextArea.setWrapStyleWord(true);
        treasuresScrollPane.setViewportView(treasuresTextArea);
        distancesScrollPane = new JScrollPane();
        distancesScrollPane.setMinimumSize(new Dimension(50, 54));
        distancesScrollPane.setPreferredSize(new Dimension(50, 54));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 0, 3);
        panel1.add(distancesScrollPane, gbc);
        distancesTextArea = new JTextArea();
        distancesTextArea.setLineWrap(true);
        distancesTextArea.setMargin(new Insets(2, 6, 2, 6));
        distancesTextArea.setMinimumSize(new Dimension(50, 34));
        distancesTextArea.setText("14.0, 15.0, 3.0, 5.5, 8.7, 3.6, 1.0, 4.0, 3.0, 5.5, 8.7, 3.6, 1.0, 4.0");
        distancesTextArea.setWrapStyleWord(true);
        distancesScrollPane.setViewportView(distancesTextArea);
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$(label5, this.$$$getMessageFromBundle$$$("lang", "fitness"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label5, gbc);
        fitnessComboBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(fitnessComboBox, gbc);
        limitSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(limitSpinner, gbc);
        positionsSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(positionsSpinner, gbc);
        final JLabel label6 = new JLabel();
        this.$$$loadLabelText$$$(label6, this.$$$getMessageFromBundle$$$("lang", "mutators"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10, 0, 5, 5);
        panel1.add(label6, gbc);
        mutatorScrollPane = new JScrollPane();
        mutatorScrollPane.setHorizontalScrollBarPolicy(31);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        panel1.add(mutatorScrollPane, gbc);
        mutatorTable = new JTable();
        mutatorTable.setPreferredScrollableViewportSize(new Dimension(200, 100));
        mutatorScrollPane.setViewportView(mutatorTable);
        final JLabel label7 = new JLabel();
        this.$$$loadLabelText$$$(label7, this.$$$getMessageFromBundle$$$("lang", "population"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel1.add(label7, gbc);
        populationSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(populationSpinner, gbc);
        final JLabel label8 = new JLabel();
        this.$$$loadLabelText$$$(label8, this.$$$getMessageFromBundle$$$("lang", "offspring"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel1.add(label8, gbc);
        offspringSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(offspringSpinner, gbc);
        final JLabel label9 = new JLabel();
        this.$$$loadLabelText$$$(label9, this.$$$getMessageFromBundle$$$("lang", "survivors"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel1.add(label9, gbc);
        survivorsSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(survivorsSpinner, gbc);
        label3.setLabelFor(distancesTextArea);
        label6.setLabelFor(distancesScrollPane);
        label7.setLabelFor(populationSpinner);
        label8.setLabelFor(offspringSpinner);
        label9.setLabelFor(survivorsSpinner);
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
