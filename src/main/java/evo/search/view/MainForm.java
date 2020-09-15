package evo.search.view;

import com.github.weisj.darklaf.ui.list.DarkDefaultListCellRenderer;
import evo.search.Evolution;
import evo.search.Main;
import evo.search.ga.AnalysisUtils;
import evo.search.ga.DiscreteGene;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.Project;
import evo.search.io.entities.Workspace;
import evo.search.io.service.EventService;
import evo.search.io.service.MenuService;
import evo.search.io.service.ProjectService;
import evo.search.view.model.ConfigComboModel;
import evo.search.view.model.FitnessTableModel;
import evo.search.view.part.Canvas;
import io.jenetics.Chromosome;
import io.jenetics.Phenotype;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.ISeq;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
    private FitnessTableModel historyTableModel;
    /**
     * Table to list all individuals of a generation.
     */
    private JTable populationTable;
    /**
     * Model of the generation table.
     */
    private FitnessTableModel populationTableModel;
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
     * List to display all saved evolutions.
     */
    private JList<Path> savedEvolutionsList;
    /**
     * Model for the saved evolutions list.
     */
    private final DefaultListModel<Path> savedEvolutionsModel = new DefaultListModel<>();
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
     * Thread where the evolution runs.
     */
    Thread evolutionThread;

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

        project.getConfigurations().sort(Comparator.comparing(Configuration::getName));
        configComboModel.addAll(project.getConfigurations());

        configComboModel.addSelectionListener(index -> workspace.setSelectedConfiguration(index));

        EventService.CONFIGS_CHANGED.addListener(changedConfigurations -> {
            final Object selectedItem = configComboModel.getSelectedItem();
            final int selectedIndex = configComboBox.getSelectedIndex();
            final int oldSize = configComboModel.getSize();
            configComboModel.removeAllElements();
            changedConfigurations.sort(Comparator.comparing(Configuration::getName));
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

        savedEvolutionSetup();
    }

    /**
     * Load and setup the list of saved evolutions.
     */
    private void savedEvolutionSetup() {
        savedEvolutionsList.setCellRenderer(new DarkDefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                final String name = ((Path) value).getFileName().toString();
                return super.getListCellRendererComponent(list, name.replace(".evolution", ""), index, isSelected, cellHasFocus);
            }
        });
        savedEvolutionsList.setModel(savedEvolutionsModel);

        savedEvolutionsList.addListSelectionListener(e -> {
            savedEvolutionsList.setEnabled(false);
            final Path selectedPath = savedEvolutionsList.getSelectedValue();
            if (selectedPath == null) return;

            EventService.LOG_LABEL.trigger("Loading Evolution...");
            CompletableFuture.supplyAsync(() -> ProjectService.readEvolution(selectedPath))
                    .thenAccept(loadedEvolution -> {
                        savedEvolutionsList.setEnabled(true);
                        if (loadedEvolution == null) {
                            EventService.LOG_LABEL.trigger("Loaded Evolution is empty.");
                            return;
                        }
                        EventService.LOG_LABEL.trigger("Evolution loaded.");
                        evolution = loadedEvolution;
                        if (evolution.getHistory().size() == 0)
                            return;
                        final int lastIndex = evolution.getHistory().size() - 1;
                        final Chromosome<DiscreteGene> chromosome = evolution.getHistory().get(lastIndex).bestPhenotype().genotype().chromosome();
                        EventService.REPAINT_CANVAS.trigger(chromosome);
                    });
        });
        CompletableFuture
                .supplyAsync(ProjectService::getSavedEvolutions)
                .thenAccept(savedEvolutionsModel::addAll);
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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                if (evolution != null)
                    evolution.setAborted(true);
            }
        });

        setWindowListeners();

        setVisible(true);
        canvas.centerOffset();
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
                        ),
                        MenuService.SEPARATOR,
                        MenuService.item("Save",
                                e -> SwingUtilities.invokeLater(this::saveEvolution)
                        ),
                        MenuService.menu(LangService.get("export"),
                                MenuService.item(LangService.get("selected"),
                                        event -> SwingUtilities.invokeLater(() -> {
                                            final int selectedIndex = getSelectedGenerationIndex();
                                            if (selectedIndex < 0) return;
                                            final Evolution clone = evolution.clone();
                                            clone.setHistory(evolution.getHistory().subList(selectedIndex, selectedIndex + 1));
                                            ExportDialog.showDialog(clone);
                                        })
                                ),
                                MenuService.item("All", event -> SwingUtilities.invokeLater(() -> ExportDialog.showDialog(evolution)))
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
     * Save the current evolution and configuration.
     */
    private void saveEvolution() {
        if (evolution == null) {
            JOptionPane.showMessageDialog(this, "Empty Evolution was not saved.");
            return;
        }
        EventService.LOG_LABEL.trigger("Saving Evolution...");
        CompletableFuture.runAsync(() -> {
            final Path evolutionPath = ProjectService.writeEvolution(evolution, "run");
            if (evolutionPath != null)
                savedEvolutionsModel.addElement(evolutionPath);
        }).thenRun(() -> EventService.LOG_LABEL.trigger("Evolution saved."));
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
        EventService.REPAINT_CANVAS.addListener(chromosome -> SwingUtilities.invokeLater(() -> {
            canvas.clear();
            canvas.setRays(evolution.getConfiguration().getPositions());
            canvas.render(chromosome);
            canvas.renderTreasures(evolution.getConfiguration().getTreasures());
        }));
    }

    /**
     * Custom create the history table's component and bind its behaviours.
     */
    public void createUIComponents() {
        createHistoryTable();
        createPopulationTable();
        widgetTabs = new JTabbedPane();
        widgetTabs.addChangeListener(new ChangeListener() {
            /**
             * Index of the selected generation.
             */
            private int generation = -1;

            @Override
            public void stateChanged(final ChangeEvent l) {
                if (widgetTabs.getSelectedIndex() == 2) {
                    final int selectedIndex = getSelectedGenerationIndex();
                    if (selectedIndex == generation)
                        return;

                    generation = selectedIndex;

                    final EvolutionResult<DiscreteGene, Double> evolutionResult = evolution.getHistory().get(generation);
                    populationTableModel.setColumnIdentifier(1, evolution.getConfiguration().getFitness().name());

                    if (evolutionResult == null) return;

                    final List<List<Double>> doubles = evolutionResult
                            .population()
                            .map(phenotype -> phenotypeToTableRow(phenotype))
                            .asList();
                    populationTableModel.setData(doubles);
                }
            }
        });
    }

    /**
     * Create the generation population table.
     */
    private void createPopulationTable() {
        populationTableModel = new FitnessTableModel();
        populationTableModel.setColumnIdentifier(0, "Individuum");
        populationTable = new JTable(populationTableModel);
        populationTable.setRowSorter(new TableRowSorter<>(populationTableModel));
        populationTable.getSelectionModel().addListSelectionListener(l -> {
            final int generation = getSelectedGenerationIndex();
            final int selectedRow = populationTable.getSelectedRow();
            if (selectedRow < 0 || selectedRow > populationTable.getRowCount() - 1)
                return;
            final int individual = populationTable.convertRowIndexToModel(selectedRow);
            if (individual < 0)
                return;
            try {
                final Phenotype<DiscreteGene, Double> phenotype = evolution
                        .getHistory()
                        .get(generation)
                        .population()
                        .get(individual);
                EventService.REPAINT_CANVAS.trigger(phenotype.genotype().chromosome());
            } catch (final IndexOutOfBoundsException ignored) {}
        });
    }

    /**
     * Create the history table and bind it's behaviours/listeners.
     */
    private void createHistoryTable() {
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
            final int selectedIndex = getSelectedGenerationIndex();
            if (selectedIndex < 0) return;
            final Chromosome<DiscreteGene> chromosome = getBestIndividual(selectedIndex);
            if (chromosome != null)
                EventService.REPAINT_CANVAS.trigger(chromosome);
        });

        historyTableModel = new FitnessTableModel();

        historyTable.setModel(historyTableModel);

        historyTable.setRowSorter(new TableRowSorter<>(historyTableModel));

        historyTableModel.addTableModelListener(e -> {
            try {
                historyTable.scrollRectToVisible(
                        historyTable.getCellRect(historyTable.getRowCount(), 0, true)
                );
            } catch (final IndexOutOfBoundsException | NullPointerException ignored) {
            }
        });
    }

    /**
     * Get the selected index in the {@link #historyTable}.
     *
     * @return selected index from {@link #historyTable}
     */
    private int getSelectedGenerationIndex() {
        final int selectedRow = historyTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow > historyTable.getRowCount()) return -1;

        final int index = historyTable.convertRowIndexToModel(selectedRow);
        if (index < 0 || index >= evolution.getHistory().size()) return -1;
        return index;
    }

    /**
     * Get the best phenotypes chromosome of the given generation.
     *
     * @param generation generation index
     * @return best phenotypes chromosome of the generation
     */
    private Chromosome<DiscreteGene> getBestIndividual(final int generation) {
        return evolution.getHistory().get(generation).bestPhenotype().genotype().chromosome();
    }

    /**
     * Start an evolution run.
     */
    private void onRun() {
        SwingUtilities.invokeLater(() -> {
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

            populationTableModel.setData(Collections.emptyList());
            historyTableModel.clear();
            historyTableModel.setColumnIdentifier(1, selectedConfiguration.getFitness().name());

            progressBar.setMaximum(selectedConfiguration.getLimit());
            updateUIOnEvolution(true);

            final Consumer<Integer> progressConsumer = progress -> SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
            final Consumer<EvolutionResult<DiscreteGene, Double>> resultConsumer = result -> {
                final Phenotype<DiscreteGene, Double> phenotype = result.bestPhenotype();
                EventService.REPAINT_CANVAS.trigger(phenotype.genotype().chromosome());
                historyTableModel.addRow(phenotypeToTableRow(phenotype));
            };

            evolution = Evolution.builder()
                    .configuration(selectedConfiguration)
                    .progressConsumer(progressConsumer)
                    .historyConsumer(resultConsumer)
                    .build();

            evolutionThread = new Thread(() -> {
                evolution.run();
                EventService.LOG_LABEL.trigger(LangService.get("environment.finished"));
                updateUIOnEvolution(false);
            });

            evolutionThread.start();
        });
    }

    /**
     * Converts a phenotype to a {@link FitnessTableModel} row.
     *
     * @param phenotype phenotype to add to the table
     * @return table row with doubles
     */
    private List<Double> phenotypeToTableRow(final Phenotype<DiscreteGene, Double> phenotype) {
        final List<DiscreteGene> genes = ISeq.of(phenotype.genotype().chromosome()).asList();
        final double worstCase = AnalysisUtils.worstCase(genes, 1f);
        final double optimalWorstCase = AnalysisUtils.optimalWorstCase(genes);
        return List.of(
                phenotype.fitness(),
                worstCase,
                optimalWorstCase,
                worstCase / optimalWorstCase
        );
    }

    /**
     * Set the ui components according to the evolution running.
     *
     * @param evolutionRunning whether the evolution runs or not
     */
    private void updateUIOnEvolution(final boolean evolutionRunning) {
        this.progressBar.setVisible(evolutionRunning);
        stopButton.setEnabled(evolutionRunning);
        startButton.setEnabled(!evolutionRunning);
    }

    /**
     * Abort evolution run.
     */
    private void onAbort() {
        if (evolution != null) {
            evolution.setAborted(true);
            updateUIOnEvolution(false);
        }
    }

}
