package evo.search.view;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import evo.search.Configuration;
import evo.search.Environment;
import evo.search.Main;
import evo.search.Run;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.ga.mutators.SwapGeneMutator;
import evo.search.ga.mutators.SwapPositionsMutator;
import evo.search.io.EventService;
import evo.search.io.FileService;
import evo.search.io.MenuService;
import evo.search.view.model.ConfigTableModel;
import evo.search.view.model.MutatorTableModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * The main swing application forms class.
 * Holds the main components and controls the behaviour of them.
 */
@Slf4j
@Getter
public class MainForm extends JFrame {

    private JPanel rootPanel;
    private JPanel toolbar;
    private JButton runButton;
    private JProgressBar progressBar;
    private JLabel logLabel;
    private JTable simpleConfigTable;
    private JTable mutatorConfigTable;
    private JTabbedPane configTabs;
    private JButton configButton;
    private JSplitPane mainSplit;
    private Canvas canvas;
    private JPanel bottomBar;
    private JComboBox comboBox1;
    private JTextArea textArea1;

    /**
     * Custom table model for the simple configuration table.
     */
    @Setter
    private ConfigTableModel configTableModel = null;

    /**
     * Custom table model for the mutator selection table.
     */
    @Setter
    private MutatorTableModel mutatorTableModel = null;

    /**
     * Construct the main form for the swing application.
     */
    public MainForm() {
        $$$setupUI$$$();
        setupFrame();
        bindRunButton();
        bindConfigButton();
        setupMutatorTable();
        setupConfigTable();
        bindEvents();
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, UIManager.getColor("ToolBar.borderColor")));
        bottomBar.setBorder(new MatteBorder(1, 0, 0, 0, UIManager.getColor("ToolBar.borderColor")));
    }

    /**
     * Application main method.
     *
     * @param args cli arguments
     */
    public static void main(String[] args) {
        setupEnvironment();
        new MainForm();
    }

    /**
     * Set up the static properties.
     * Installs the swing look and feel.
     */
    private static void setupEnvironment() {
        LafManager.install(new DarculaTheme());
        UIManager.getDefaults().addResourceBundle("evo.search.lang");
        System.setProperty("-Xdock:name", Main.APP_TITLE);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", Main.APP_TITLE);
    }

    /**
     * Set up the menu bar.
     *
     * @param menuBar the menu bar to set up
     */
    private static void setupMenuBar(JMenuBar menuBar) {
        final JMenu menu = MenuService.menu(
                LangService.get("file"),
                MenuService.item(
                        LangService.get("open.dots"),
                        actionEvent -> {
                            final File loadFile = FileService.promptForLoad();
                            if (loadFile == null) {
                                return;
                            }
                            FileService.loadExperiment(loadFile);

                        }
                ),
                MenuService.item(
                        LangService.get("save.dots"),
                        actionEvent -> {
                            final File saveFile = FileService.promptForSave();
                            if (saveFile == null) {
                                return;
                            }
                            FileService.saveConfiguration(saveFile, Environment.getInstance().getConfiguration());
                        },
                        KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx())
                )
        );
        menuBar.add(menu);
    }

    /**
     * Bind the {@link EventService}s events.
     */
    private void bindEvents() {
        EventService.LOG_LABEL.addListener(
                string -> getLogLabel().setText(string)
        );
        EventService.LOG.addListener(
                message -> textArea1.append(message + "\n")
        );
        EventService.REPAINT_CANVAS.addListener(chromosome -> {
            getCanvas().clear();
            if (chromosome == null) {
                final List<Run> history = Environment.getInstance().getConfiguration().getHistory();
                chromosome = history.get(history.size() - 1).getIndividual();
            }
            getCanvas().render(chromosome);
        });
    }

    /**
     * Bind the configuration button to its behaviour.
     */
    private void bindConfigButton() {
        final AtomicInteger mainDividerLocation = new AtomicInteger(300);
        final AtomicInteger dividerSize = new AtomicInteger(9);
        getConfigButton().addActionListener(e -> {
            if (getMainSplit().isEnabled()) {
                mainDividerLocation.set(getMainSplit().getDividerLocation());
                dividerSize.set(getMainSplit().getDividerSize());
                getMainSplit().setDividerSize(0);
                getMainSplit().setEnabled(false);
                getConfigTabs().setVisible(false);
            } else {
                getMainSplit().setDividerLocation(mainDividerLocation.get());
                getMainSplit().setDividerSize(dividerSize.get());
                getMainSplit().setEnabled(true);
                getConfigTabs().setVisible(true);
            }
        });
        getMainSplit().setDividerSize(0);
        getMainSplit().setEnabled(false);
        getConfigTabs().setVisible(false);
    }

    /**
     * Bind the run button to its behaviour.
     */
    private void bindRunButton() {
        getRunButton().addActionListener(e -> {
            if (getMutatorTableModel() == null) {
                EventService.LOG_LABEL.trigger(LangService.get("error.creating.config.table"));
                return;
            }

            final Random random = new Random();
            final int positions = (int) configTableModel.getConfig("positions");
            final int limit = (int) configTableModel.getConfig("limit");
            final int treasureAmount = (int) configTableModel.getConfig("treasures");
            final Object distanceObject = configTableModel.getConfig("distances");
            if (!(distanceObject instanceof List)) {
                return;
            }

            final List<Double> distances = ((List<?>) distanceObject).stream()
                    .filter(o -> o instanceof Number)
                    .mapToDouble(number -> ((Number) number).doubleValue())
                    .boxed()
                    .collect(Collectors.toList());

            final List<DiscretePoint> treasures = distances.subList(0, Math.min(distances.size() - 1, treasureAmount))
                    .stream()
                    .mapToDouble(Number::doubleValue)
                    .mapToObj(distance -> {
                        final int position = random.nextInt(positions);
                        final double randomDistance = 1 + random.nextDouble() * distance;
                        return new DiscretePoint(position, randomDistance);
                    })
                    .collect(Collectors.toList());

            final List<DiscreteAlterer> selected = getMutatorTableModel().getSelected();

            Environment.getInstance()
                    .setConfiguration(new Configuration(positions, distances, treasures));
            getProgressBar().setMaximum(limit);
            getProgressBar().setVisible(true);
            CompletableFuture.supplyAsync(() ->
                    Environment.getInstance()
                            .evolve(
                                    limit,
                                    selected,
                                    integer -> getProgressBar().setValue(integer)
                            )
                            .chromosome()
            )
                    .thenAccept(chromosome -> {
                        EventService.LOG_LABEL.trigger(LangService.get("environment.finished"));
                        EventService.REPAINT_CANVAS.trigger(chromosome);
                    })
                    .thenRun(() -> getProgressBar().setVisible(false));
        });
    }

    /**
     * Set up the jFrame for the swing application.
     */
    private void setupFrame() {
        final JMenuBar jMenuBar = new JMenuBar();
        setupMenuBar(jMenuBar);
        setJMenuBar(jMenuBar);

        setTitle(Main.APP_TITLE);
        setMinimumSize(new Dimension(700, 500));
        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Set up the configuration table.
     */
    private void setupConfigTable() {
        configTableModel = new ConfigTableModel();

        configTableModel.addConfig("positions", 2);
        configTableModel.addConfig("treasures", 7);
        configTableModel.addConfig("limit", 1000);
        configTableModel.addConfig("distances", Arrays.asList(45, 2.0, 2.0, 7.0, 12.0, 5.0));

        getSimpleConfigTable().setModel(configTableModel);
    }

    /**
     * Set up the mutator's configuration table.
     */
    private void setupMutatorTable() {

        mutatorTableModel = new MutatorTableModel();
        mutatorTableModel.addMutator(true, SwapPositionsMutator.class, .5);
        mutatorTableModel.addMutator(true, SwapGeneMutator.class, .5);

        final JTable table = getMutatorConfigTable();
        final TableColumnModel columnModel = table.getColumnModel();

        table.setModel(mutatorTableModel);

        columnModel.getColumn(0).setPreferredWidth(55);
        columnModel.getColumn(1).setPreferredWidth(200);
        columnModel.getColumn(2).setPreferredWidth(80);
    }

    public void createUIComponents() {

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
        toolbar = new JPanel();
        toolbar.setLayout(new GridLayoutManager(1, 3, new Insets(0, 10, 0, 10), -1, -1));
        toolbar.setBackground(new Color(-12105140));
        toolbar.setEnabled(false);
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(toolbar, gbc);
        runButton = new JButton();
        runButton.setAlignmentY(0.0f);
        runButton.setBackground(new Color(-4866622));
        runButton.setBorderPainted(true);
        runButton.setContentAreaFilled(false);
        runButton.setEnabled(true);
        runButton.setFocusPainted(false);
        runButton.setHideActionText(false);
        runButton.setHorizontalTextPosition(4);
        runButton.setInheritsPopupMenu(true);
        runButton.setLabel(ResourceBundle.getBundle("lang").getString("run"));
        runButton.setMargin(new Insets(0, 0, 0, 0));
        runButton.setOpaque(false);
        runButton.setSelected(true);
        this.$$$loadButtonText$$$(runButton, ResourceBundle.getBundle("lang").getString("run"));
        toolbar.add(runButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        toolbar.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        configButton = new JButton();
        configButton.setBorderPainted(true);
        configButton.setContentAreaFilled(false);
        this.$$$loadButtonText$$$(configButton, ResourceBundle.getBundle("lang").getString("config"));
        toolbar.add(configButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        bottomBar = new JPanel();
        bottomBar.setLayout(new GridLayoutManager(1, 3, new Insets(0, 10, 0, 10), -1, -1));
        bottomBar.setBackground(new Color(-12105140));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(bottomBar, gbc);
        progressBar = new JProgressBar();
        progressBar.setBorderPainted(false);
        progressBar.setOrientation(0);
        progressBar.setStringPainted(false);
        progressBar.setVisible(false);
        bottomBar.add(progressBar, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 3), new Dimension(50, 3), new Dimension(-1, 3), 0, false));
        final Spacer spacer2 = new Spacer();
        bottomBar.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        logLabel = new JLabel();
        logLabel.setHorizontalTextPosition(11);
        logLabel.setText("");
        bottomBar.add(logLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(panel1, gbc);
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(300);
        splitPane1.setOrientation(0);
        panel1.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        mainSplit = new JSplitPane();
        mainSplit.setDividerLocation(103);
        mainSplit.setDividerSize(9);
        mainSplit.setEnabled(false);
        mainSplit.setFocusable(false);
        mainSplit.setRequestFocusEnabled(false);
        splitPane1.setLeftComponent(mainSplit);
        configTabs = new JTabbedPane();
        configTabs.setVisible(true);
        mainSplit.setLeftComponent(configTabs);
        final JScrollPane scrollPane1 = new JScrollPane();
        configTabs.addTab(ResourceBundle.getBundle("lang").getString("general"), scrollPane1);
        simpleConfigTable = new JTable();
        scrollPane1.setViewportView(simpleConfigTable);
        final JScrollPane scrollPane2 = new JScrollPane();
        configTabs.addTab(ResourceBundle.getBundle("lang").getString("mutators"), scrollPane2);
        mutatorConfigTable = new JTable();
        mutatorConfigTable.setName("Mutators");
        mutatorConfigTable.setVisible(true);
        scrollPane2.setViewportView(mutatorConfigTable);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        configTabs.addTab("Test", panel2);
        comboBox1 = new JComboBox();
        comboBox1.setEditable(true);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("13");
        defaultComboBoxModel1.addElement("12");
        defaultComboBoxModel1.addElement("15");
        defaultComboBoxModel1.addElement("17");
        comboBox1.setModel(defaultComboBoxModel1);
        panel2.add(comboBox1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel2.add(spacer3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        canvas = new Canvas();
        mainSplit.setRightComponent(canvas);
        final JScrollPane scrollPane3 = new JScrollPane();
        splitPane1.setRightComponent(scrollPane3);
        scrollPane3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5), null));
        textArea1 = new JTextArea();
        textArea1.setEditable(false);
        Font textArea1Font = this.$$$getFont$$$("JetBrains Mono", Font.PLAIN, 11, textArea1.getFont());
        if (textArea1Font != null) textArea1.setFont(textArea1Font);
        textArea1.setInheritsPopupMenu(true);
        textArea1.setOpaque(false);
        textArea1.setText(":");
        textArea1.setVisible(true);
        scrollPane3.setViewportView(textArea1);
        logLabel.setLabelFor(scrollPane2);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
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
