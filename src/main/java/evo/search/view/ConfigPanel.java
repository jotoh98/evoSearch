package evo.search.view;

import evo.search.Environment;
import evo.search.ga.DiscretePoint;
import evo.search.io.entities.Configuration;
import evo.search.view.listener.DocumentEditHandler;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ConfigPanel extends JDialog {
    private JScrollPane contentScrollPane;
    private JSpinner limitSpinner;
    private JSpinner positionsSpinner;
    private JTextArea distancesTextArea;
    private JTextArea treasuresTextArea;
    private JButton shuffleButton;
    private JComboBox<Environment.Fitness> fitnessComboBox;

    private DefaultComboBoxModel<Environment.Fitness> fitnessListModel = new DefaultComboBoxModel<>();

    private Configuration configuration;

    @Setter
    private ConfigurationDialog parent;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

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

            shuffleDialog.setPositions(configuration.getPositions());
            shuffleDialog.setTreasureConsumer(shuffledTreasures -> {
                configuration.setTreasures(shuffledTreasures);
                printTreasures(shuffledTreasures);
            });
            shuffleDialog.setVisible(true);
        });

        fitnessComboBox.setModel(fitnessListModel);
        fitnessListModel.addAll(Arrays.asList(Environment.Fitness.values()));
    }

    private void bindLimit() {
        limitSpinner.setValue(configuration.getLimit());
        limitSpinner.addChangeListener(e -> {
            parent.triggerChange();
            try {
                final int limit = (int) limitSpinner.getValue();
                configuration.setLimit(limit);
            } catch (Exception ignored) {
            }
        });
    }

    private void bindPosition() {
        positionsSpinner.setValue(configuration.getPositions());
        positionsSpinner.addChangeListener(e -> {
            parent.triggerChange();
            try {
                final int positions = (int) positionsSpinner.getValue();
                configuration.setPositions(positions);
            } catch (Exception ignored) {
            }
        });
    }

    private void bindDistances() {
        final String initText = configuration.getDistances()
                .stream()
                .map(String::valueOf)
                .reduce((s, s2) -> s + ", " + s2)
                .orElse("");
        distancesTextArea.setText(initText);

        distancesTextArea.getDocument().addDocumentListener((DocumentEditHandler) e -> {
            parent.triggerChange();
            final String input = distancesTextArea.getText();

            final List<Double> distances = Arrays.asList(input.split("\\s*,\\s*"))
                    .stream()
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
        final String text = treasures.stream()
                .map(discretePoint -> String.format("(%s, %s)", discretePoint.getPosition(), discretePoint.getDistance()))
                .reduce((s, s2) -> s + ", " + s2)
                .orElse("");
        treasuresTextArea.setText(text);
    }

    private void bindFitness() {
        fitnessListModel.setSelectedItem(configuration.getFitness());
        fitnessComboBox.addActionListener(e -> {
            parent.triggerChange();
            configuration.setFitness((Environment.Fitness) fitnessComboBox.getSelectedItem());
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
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentScrollPane = new JScrollPane();
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new BorderLayout(0, 0));
        contentScrollPane.setViewportView(panel1);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        panel1.add(panel2, BorderLayout.NORTH);
        panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 5, 10, 5), null));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("lang").getString("general"));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 0, 5, 5);
        panel2.add(label1, gbc);
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, ResourceBundle.getBundle("lang").getString("limit"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel2.add(label2, gbc);
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, ResourceBundle.getBundle("lang").getString("positions"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);
        panel2.add(label3, gbc);
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, ResourceBundle.getBundle("lang").getString("distances"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 0, 0, 5);
        panel2.add(label4, gbc);
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$(label5, ResourceBundle.getBundle("lang").getString("treasures"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 0, 0, 5);
        panel2.add(label5, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 3, 0, 3);
        panel2.add(panel3, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        panel3.add(panel4, BorderLayout.SOUTH);
        panel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0), null));
        shuffleButton = new JButton();
        this.$$$loadButtonText$$$(shuffleButton, ResourceBundle.getBundle("lang").getString("shuffle"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(shuffleButton, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel4.add(spacer1, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setMinimumSize(new Dimension(50, 54));
        scrollPane1.setPreferredSize(new Dimension(50, 54));
        panel3.add(scrollPane1, BorderLayout.CENTER);
        treasuresTextArea = new JTextArea();
        treasuresTextArea.setLineWrap(true);
        treasuresTextArea.setMargin(new Insets(2, 6, 2, 6));
        treasuresTextArea.setMinimumSize(new Dimension(2, 17));
        treasuresTextArea.setText("14.0, 15.0, 3.0, 5.5, 8.7, 3.6, 1.0, 4.0, 3.0, 5.5, 8.7, 3.6, 1.0, 4.0");
        treasuresTextArea.setWrapStyleWord(true);
        scrollPane1.setViewportView(treasuresTextArea);
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setMinimumSize(new Dimension(50, 54));
        scrollPane2.setPreferredSize(new Dimension(50, 54));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(3, 3, 0, 3);
        panel2.add(scrollPane2, gbc);
        distancesTextArea = new JTextArea();
        distancesTextArea.setLineWrap(true);
        distancesTextArea.setMargin(new Insets(2, 6, 2, 6));
        distancesTextArea.setMinimumSize(new Dimension(50, 34));
        distancesTextArea.setText("14.0, 15.0, 3.0, 5.5, 8.7, 3.6, 1.0, 4.0, 3.0, 5.5, 8.7, 3.6, 1.0, 4.0");
        distancesTextArea.setWrapStyleWord(true);
        scrollPane2.setViewportView(distancesTextArea);
        final JLabel label6 = new JLabel();
        label6.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(label6, gbc);
        fitnessComboBox = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(fitnessComboBox, gbc);
        limitSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(limitSpinner, gbc);
        positionsSpinner = new JSpinner();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(positionsSpinner, gbc);
        label4.setLabelFor(distancesTextArea);
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
        return contentScrollPane;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
