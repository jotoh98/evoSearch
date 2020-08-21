package evo.search.view;

import com.github.weisj.darklaf.ui.list.DarkDefaultListCellRenderer;
import evo.search.Evolution;
import evo.search.Main;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.Project;
import evo.search.io.entities.Workspace;
import evo.search.io.service.EventService;
import evo.search.io.service.MenuService;
import evo.search.io.service.ProjectService;
import evo.search.view.model.ConfigComboModel;
import evo.search.view.part.Canvas;
import io.jenetics.engine.EvolutionResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The main swing application forms class.
 * Holds the main components and controls the behaviour of them.
 */
@Slf4j
@Getter
public class MainForm extends JFrame {

    /**
     * Combo box model for the configurations combo box.
     */
    private final ConfigComboModel configComboModel = new ConfigComboModel();
    /**
     * List of registered configurations.
     */
    private final List<Configuration> configurations = new ArrayList<>();
    /**
     * Current project for this form.
     */
    private final Project project = ProjectService.getCurrentProject();
    /**
     * Main forms root panel.
     */
    private JPanel rootPanel;
    /**
     * Top toolbar pane.
     */
    private JPanel toolbar;
    /**
     * Label to display the projects version.
     */
    private JLabel versionLabel;
    /**
     * Text field to display/change the projects name.
     */
    private JTextField nameField;
    /**
     * First config button. Displayed if no configuration is registered yet.
     */
    private JButton addFirstConfigButton;
    /**
     * Combo box with configurations to choose.
     */
    private JComboBox<Object> configComboBox;
    /**
     * Evolution start button.
     */
    private JButton startButton;
    /**
     * Evolution stop button.
     */
    private JButton stopButton;
    /**
     * Vertical split pane dividing the upper widget/canvas and the lower logging area.
     */
    private JSplitPane logSplitPane;
    /**
     * Horizontal split pane containing the canvas and widget tab pane.
     */
    private JSplitPane mainSplit;
    /**
     * Left widget tab pane
     */
    private JTabbedPane widgetTabs;
    /**
     * Table displaying the evolutions best individuals and fitness per generation.
     */
    private JTable historyTable;
    /**
     * Table model of the history table.
     */
    private DefaultTableModel historyTableModel;
    /**
     * The canvas displaying individuals.
     */
    private Canvas canvas;
    /**
     * Big log text pane.
     */
    private JTextArea logTextPane;
    /**
     * Lower fixed toolbar.
     */
    private JPanel bottomBar;
    /**
     * Small log label.
     */
    private JLabel logLabel;
    /**
     * Evolution progress bar.
     */
    private JProgressBar progressBar;
    /**
     * History of best individuals.
     */
    private List<DiscreteChromosome> history;
    /**
     * Instantiated evolution generating the history.
     */
    private Evolution evolution;
    /**
     * Workspace configuration for this forms window.
     */
    @Setter
    private Workspace workspace = new Workspace();

    /**
     * Construct the main form for the swing application.
     */
    public MainForm() {
        startButton.addActionListener(event -> onRun());
        stopButton.addActionListener(action -> onAbort());
        bindEvents();
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, UIManager.getColor("ToolBar.borderColor")));
        bottomBar.setBorder(new MatteBorder(1, 0, 0, 0, UIManager.getColor("ToolBar.borderColor")));

        configComboBox.setModel(configComboModel);
        configComboBox.setRenderer(new DarkDefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                if (value instanceof Configuration) {
                    return super.getListCellRendererComponent(list, ((Configuration) value).getName(), index, isSelected, cellHasFocus);
                }
                return super.getListCellRendererComponent(list, "Edit configurations", index, isSelected, cellHasFocus);
            }
        });

        configComboModel.addAll(project.getConfigurations());

        configComboModel.addSelectionListener(index -> workspace.setSelectedConfiguration(index));

        EventService.CONFIGS_CHANGED.addListener(changedConfigurations -> {
            final Object selectedItem = configComboModel.getSelectedItem();
            final int selectedIndex = configComboBox.getSelectedIndex();
            final int oldSize = configComboModel.getSize();
            configComboModel.removeAllElements();
            configComboModel.addAll(changedConfigurations);
            if (selectedItem instanceof Configuration && changedConfigurations.contains(selectedItem)) {
                configComboModel.setSelectedItem(selectedItem);
            } else if (selectedIndex > 0 && selectedIndex < configComboModel.getSize()) {
                configComboBox.setSelectedIndex(selectedIndex);
            }
            if (oldSize == 1 && configComboModel.getSize() > 1) {
                configComboBox.setSelectedIndex(1);
            }
            updateConfigListView();
        });

        updateConfigListView();

        configComboBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                configComboBox.setEnabled(configComboModel.getSize() > 1);
                super.mousePressed(e);
            }
        });

        addFirstConfigButton.addActionListener(
                e -> EventService.OPEN_CONFIG.trigger(project.getConfigurations())
        );

        nameField.setText(project.getName());
        nameField.setBorder(BorderFactory.createEmptyBorder());

        mainSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                l -> workspace.setMainDividerLocation(mainSplit.getDividerLocation())
        );

        logSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                l -> workspace.setLogDividerLocation(logSplitPane.getDividerLocation())
        );
    }

    /**
     * Bind the configuration combo boxes empty state.
     * If the list of configs is empty, the combo box is replaced by the button
     * to add the first configuration.
     */
    public void updateConfigListView() {
        final boolean listEmpty = configComboModel.getSize() == 1;
        configComboBox.setVisible(!listEmpty);
        addFirstConfigButton.setVisible(listEmpty);
        startButton.setEnabled(!listEmpty);
    }

    /**
     * Set up the jFrame for the swing application.
     */
    public void showFrame() {
        setupMenuBar();
        setTitle(Main.APP_TITLE);
        setMinimumSize(new Dimension(700, 500));
        final Dimension mainSize = workspace.getMainSize();
        final Point mainLocation = workspace.getMainLocation();
        final int selectedConfig = Math.max(0, workspace.getSelectedConfiguration());

        configComboBox.setSelectedIndex(1 + selectedConfig);

        if (mainSize.width > 0 && mainSize.height > 0)
            setPreferredSize(mainSize);

        pack();
        setLocation(mainLocation);
        logSplitPane.setDividerLocation(workspace.getLogDividerLocation());
        mainSplit.setDividerLocation(workspace.getMainDividerLocation());
        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setWindowListeners();

        setVisible(true);
    }

    /**
     * Add window listeners to update the workspace properties.
     */
    private void setWindowListeners() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                new ChooserForm();
                ProjectService.saveProjectWorkspace(workspace);
                super.windowClosed(e);
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                workspace.setMainSize(getSize());
                workspace.setMainLocation(getLocation());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                workspace.setMainLocation(getLocation());
            }
        });

    }

    /**
     * Set up the windows menu bar with shortcuts.
     */
    private void setupMenuBar() {
        setMenuBar(MenuService.menuBar(
                MenuService.menu(
                        LangService.get("run.menu"),
                        MenuService.item(
                                LangService.get("start"),
                                actionEvent -> onRun(),
                                MenuService.shortcut('r')
                        ),
                        MenuService.item(
                                LangService.get("stop"),
                                actionEvent -> onAbort(),
                                MenuService.shortcut('w')
                        )
                ),
                MenuService.menu(
                        LangService.get("configuration"),
                        MenuService.item(
                                "Edit",
                                actionEvent -> EventService.OPEN_CONFIG.trigger(project.getConfigurations()),
                                MenuService.shortcut('e')
                        )
                )
        ));
    }

    /**
     * Bind the {@link EventService}s events.
     */
    private void bindEvents() {
        EventService.LOG_LABEL.addListener(
                string -> getLogLabel().setText(string)
        );
        EventService.LOG.addListener(
                message -> logTextPane.append(message + "\n")
        );
        EventService.REPAINT_CANVAS.addListener(chromosome -> {
            canvas.clear();
            canvas.render(chromosome);
        });
    }

    /**
     * Custom create the history table's component and bind its behaviours.
     */
    public void createUIComponents() {

        historyTable = new JTable() {
            @Override
            public boolean isCellEditable(final int row, final int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(final int column) {
                return column == 0 ? Integer.class : Double.class;
            }
        };

        historyTable.getSelectionModel().addListSelectionListener(e -> {
            final int selectedRow = historyTable.getSelectedRow();
            if (selectedRow < 0 || selectedRow > historyTable.getRowCount()) return;

            final int index = historyTable.convertRowIndexToModel(selectedRow);
            if (index < 0 || index >= history.size()) return;

            EventService.REPAINT_CANVAS.trigger(history.get(index));
        });

        historyTable.setModel(new DefaultTableModel(new Object[]{"Generation", "Fitness"}, 0));

        historyTableModel = (DefaultTableModel) historyTable.getModel();

        final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(historyTableModel);
        sorter.setComparator(0, Comparator.comparingInt(value -> (int) value));
        sorter.setComparator(1, Comparator.comparingDouble(value -> (double) value));
        historyTable.setRowSorter(sorter);

        historyTableModel.addTableModelListener(e -> {
            try {
                historyTable.scrollRectToVisible(
                        historyTable.getCellRect(historyTable.getRowCount() - 1, 0, true)
                );
            } catch (final IndexOutOfBoundsException ignored) {
            }
        });
    }

    /**
     * Start an evolution run.
     */
    private void onRun() {
        final Configuration selectedConfiguration;
        try {
            selectedConfiguration = (Configuration) configComboModel.getSelectedItem();
            if (selectedConfiguration == null) {
                throw new NullPointerException();
            }
        } catch (final ClassCastException | NullPointerException ignored) {
            EventService.LOG_LABEL.trigger(LangService.get("evolution.init.error"));
            EventService.LOG.trigger(LangService.get("evolution.init.error") + ": " + LangService.get("configuration.selected.not.valid"));
            return;
        }

        clearHistory();

        progressBar.setMaximum(selectedConfiguration.getLimit());
        progressBar.setVisible(true);
        stopButton.setEnabled(true);
        startButton.setEnabled(false);

        final Consumer<Integer> progressConsumer = progress -> SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
        final Consumer<EvolutionResult<DiscreteGene, Double>> resultConsumer = result -> {
            EventService.REPAINT_CANVAS.trigger((DiscreteChromosome) result.bestPhenotype().genotype().chromosome());
            historyTableModel.addRow(new Object[]{(int) result.generation(), result.bestFitness()});
        };

        CompletableFuture
                .supplyAsync(() -> {
                    evolution = Evolution.builder()
                            .configuration(selectedConfiguration)
                            .progressConsumer(progressConsumer)
                            .historyConsumer(resultConsumer)
                            .build();

                    evolution.run();

                    history = evolution.getHistory();

                    return (DiscreteChromosome) evolution.getResult().chromosome();
                })
                .thenAccept(chromosome -> {
                    if (chromosome != null) {
                        EventService.LOG_LABEL.trigger(LangService.get("environment.finished"));
                        EventService.REPAINT_CANVAS.trigger(chromosome);
                    }
                })
                .thenRun(() -> {
                    getProgressBar().setVisible(false);
                    historyTable.setRowSelectionInterval(historyTable.getRowCount() - 1, historyTable.getRowCount() - 1);
                    stopButton.setEnabled(false);
                    startButton.setEnabled(true);
                });
    }

    /**
     * Abort evolution run.
     */
    private void onAbort() {
        if (evolution != null)
            evolution.setAborted(true);
    }

    /**
     * Removes all the entries from the history table.
     */
    private void clearHistory() {
        while (historyTableModel.getRowCount() > 0)
            historyTableModel.removeRow(0);
    }

}
