package evo.search.view;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import evo.search.Experiment;
import evo.search.ga.mutators.SwapGeneMutator;
import evo.search.ga.mutators.SwapPositionsMutator;
import evo.search.io.EventService;
import evo.search.io.FileService;
import evo.search.io.MenuService;
import evo.search.view.model.MutatorTableModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main swing application forms class.
 * Holds the main components and controls the behaviour of them.
 */
@Slf4j
@Getter
public class MainForm extends JFrame {

    @NonNls
    public static final String APP_TITLE = "evoSearch";

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
    public static ResourceBundle langBundle = ResourceBundle.getBundle("lang");

    /**
     * Custom table model for the mutator selection model
     */
    @Setter
    private MutatorTableModel mutatorTableModel = null;
    private Canvas canvas;

    /**
     * Construct the main form for the swing application.
     */
    public MainForm() {
        setupFrame();
        bindRunButton();
        bindConfigButton();
        setupMutatorTable();
        setupConfigTable();
        bindEvents();
        getToolbar().setBorder(new MatteBorder(0, 0, 1, 0, UIManager.getColor("ToolBar.borderColor")));
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
        System.setProperty("-Xdock:name", APP_TITLE);
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_TITLE);
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
                            FileService.saveExperiment(saveFile, Experiment.getInstance());
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
        EventService.LOG_EVENT.addListener(
                string -> SwingUtilities.invokeLater(
                        () -> getLogLabel().setText(string)
                )
        );
        EventService.REPAINT_CANVAS.addListener(chromosome -> {
            getCanvas().clear();
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
                EventService.LOG_EVENT.trigger(LangService.get("error.creating.config.table"));
                return;
            }
            Experiment.init(10, 3);
            getProgressBar().setMaximum(1000);
            getProgressBar().setVisible(true);
            CompletableFuture
                    .supplyAsync(() -> Experiment.getInstance()
                            .evolve(
                                    1000,
                                    getMutatorTableModel().getSelected(),
                                    integer -> getProgressBar().setValue(integer)).chromosome()
                    )
                    .thenAccept(chromosome -> {
                        EventService.LOG_EVENT.trigger(LangService.get("experiment.finished"));
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

        setTitle(APP_TITLE);
        setMinimumSize(new Dimension(700, 500));
        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Set up the configuration table.
     */
    private void setupConfigTable() {
        final JTable table = getSimpleConfigTable();
        final DefaultTableModel dataModel = new DefaultTableModel();
        dataModel.addColumn(LangService.get("property"));
        dataModel.addColumn(LangService.get("value"));
        dataModel.addRow(new Object[]{LangService.get("positions"), 2});
        dataModel.addRow(new Object[]{LangService.get("treasures"), 7});
        table.setModel(dataModel);

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
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 10, 0, 10), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(panel1, gbc);
        progressBar = new JProgressBar();
        progressBar.setBorderPainted(false);
        progressBar.setOrientation(0);
        progressBar.setStringPainted(false);
        progressBar.setVisible(false);
        panel1.add(progressBar, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 3), new Dimension(50, 3), new Dimension(-1, 3), 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        logLabel = new JLabel();
        logLabel.setHorizontalTextPosition(11);
        logLabel.setText("");
        panel1.add(logLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 25), new Dimension(-1, 25), new Dimension(-1, 25), 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        rootPanel.add(panel2, gbc);
        mainSplit = new JSplitPane();
        mainSplit.setDividerLocation(300);
        mainSplit.setDividerSize(9);
        mainSplit.setEnabled(false);
        mainSplit.setFocusable(false);
        mainSplit.setRequestFocusEnabled(false);
        panel2.add(mainSplit, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
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
        canvas = new Canvas();
        mainSplit.setRightComponent(canvas);
        logLabel.setLabelFor(scrollPane2);
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
