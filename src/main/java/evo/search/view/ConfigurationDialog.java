package evo.search.view;

import com.github.weisj.darklaf.ui.list.DarkDefaultListCellRenderer;
import evo.search.Evolution;
import evo.search.Main;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.SwapPositionsMutator;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.Project;
import evo.search.io.service.EventService;
import evo.search.io.service.ProjectService;
import evo.search.view.listener.DocumentEditHandler;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The form holding all the configuration panels and a selection list with their names.
 */
public class ConfigurationDialog extends JDialog {
    /**
     * Root pane.
     */
    private JPanel contentPane;

    /**
     * Ok button.
     * Saves changes and closes the window immediately.
     */
    private JButton buttonOK;

    /**
     * Cancel button.
     * Deletes unsaved changes, if the deletion is also confirmed in a prompt.
     */
    private JButton buttonCancel;

    /**
     * Apply button.
     * Saves changes without closing the window.
     */
    private JButton applyButton;

    /**
     * Bottom pane holding the main buttons
     * {@link #buttonOK},
     * {@link #buttonCancel} and
     * {@link #applyButton}.
     */
    private JPanel bottomPane;

    /**
     * Selection list displaying the configs names to switch between {@link ConfigPanel}s.
     */
    private JList<ConfigPanel> configChooserList;

    /**
     * List model of the {@link #configChooserList}.
     */
    private final DefaultListModel<ConfigPanel> configListModel = new DefaultListModel<>();

    /**
     * Textfield to change the selected {@link Configuration}s name.
     */
    private JTextField nameTextField;

    /**
     * Button to add a new configuration to the {@link #configChooserList}.
     */
    private JButton addConfigButton;

    /**
     * Panel holding the {@link #nameTextField}.
     */
    private JPanel configNamePanel;

    /**
     * Button to remove a configuration from the {@link #configChooserList}.
     */
    private JButton removeConfigButton;

    /**
     * Button to duplicate a configuration of the {@link #configChooserList}.
     */
    private JButton duplicateConfigButton;

    /**
     * Bar holding the list edit buttons.
     */
    private JPanel listEditBar;

    @Getter
    private JScrollPane configScrollWrapper;

    /**
     * Test main method.
     *
     * @param args ignored java cli args
     */
    public static void main(final String[] args) {
        final List<Configuration> configurations = Arrays.asList(
                Configuration.builder()
                        .version("undefined")
                        .name("one")
                        .limit(1000)
                        .positions(2)
                        .distances(Arrays.asList(10d, 20d))
                        .treasures(Collections.singletonList(new DiscretePoint(2, 0, 10)))
                        .alterers(Collections.singletonList(new SwapPositionsMutator(0.7)))
                        .build(),
                Configuration.builder()
                        .version("undefined")
                        .name("two")
                        .limit(100)
                        .positions(3)
                        .distances(Arrays.asList(15d, 25d, 35d))
                        .treasures(Arrays.asList(new DiscretePoint(3, 0, 10), new DiscretePoint(3, 2, 20)))
                        .fitness(Evolution.Fitness.SINGULAR)
                        .build()
        );

        Main.setupEnvironment();
        final ConfigurationDialog dialog = new ConfigurationDialog(configurations);
        dialog.showFrame();
    }

    /**
     * Configuration dialog constructor.
     * <p>
     * Creates and configures the panel window and creates the sub-panels
     * for the list of configurations to be displayed and edited.
     *
     * @param configurations list of configurations to be displayed/edited
     */
    public ConfigurationDialog(final List<Configuration> configurations) {
        customUISetup();

        configurations.forEach(this::createConfigPanel);

        setupChooserList();

        bindEmptyBehaviour();
        bindConfigListModel();
        bindConfigListButtons();

        nameTextField.getDocument().addDocumentListener((DocumentEditHandler) e -> {
            triggerChange();
            configChooserList.getSelectedValue().getConfiguration().setName(nameTextField.getText());
            configChooserList.repaint();
        });

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        applyButton.addActionListener(e -> onApply());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        getRootPane().setDefaultButton(buttonOK);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setContentPane(contentPane);
        setModal(true);
        setPreferredSize(new Dimension(600, 400));
        setMinimumSize(new Dimension(300, 200));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 700));
    }

    /**
     * Create a configuration panel.
     *
     * @param configuration configurations to be displayed/edited
     */
    public void createConfigPanel(final Configuration configuration) {
        final ConfigPanel configPanel = new ConfigPanel(configuration);
        configPanel.setParent(this);
        configListModel.addElement(configPanel);
    }

    /**
     * Bind the remove buttons enabled state to the condition, that
     * the configuration selection list contains items to remove.
     * Also bind an empty list behaviour.
     */
    private void bindConfigListModel() {
        configListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(final ListDataEvent e) {
                removeConfigButton.setEnabled(configListModel.size() > 0);
            }

            @Override
            public void intervalRemoved(final ListDataEvent e) {
                bindEmptyBehaviour();
            }

            @Override
            public void contentsChanged(final ListDataEvent e) {

            }
        });
    }

    /**
     * Bind the configuration selection list buttons behaviours.
     * In general, a button works on selected items (removing them, duplicating them, etc.).
     */
    private void bindConfigListButtons() {
        addConfigButton.addActionListener(e -> addConfiguration(new Configuration()));

        removeConfigButton.addActionListener(e -> {
            triggerChange();
            final int[] selectedIndices = configChooserList.getSelectedIndices();
            if (selectedIndices.length < 1) {
                return;
            }

            final int firstIndex = selectedIndices[0];

            if (selectedIndices.length == 1) {
                configListModel.remove(firstIndex);
                configChooserList.setSelectedIndex(firstIndex == configListModel.size() ? firstIndex - 1 : firstIndex);
                return;
            }

            configListModel.removeRange(firstIndex, selectedIndices[selectedIndices.length - 1]);
            if (configListModel.size() > 0) {
                configChooserList.setSelectedIndex(0);
            }
        });
        duplicateConfigButton.addActionListener(e -> duplicateSelectedConfiguration());
    }

    /**
     * Add a new configuration to the selection list and create a new panel.
     *
     * @param configuration configuration to be displayed
     */
    private void addConfiguration(final Configuration configuration) {
        triggerChange();
        final int selectedIndex = configChooserList.getSelectedIndex();
        if (selectedIndex == -1) {
            createConfigPanel(0, configuration);
            configChooserList.setSelectedIndex(0);
        } else {
            createConfigPanel(selectedIndex + 1, configuration);
            configChooserList.setSelectedIndex(selectedIndex + 1);
        }
    }

    /**
     * Create a new configuration panel and insert a handle into the selection list.
     *
     * @param index         insertion index in the selection list
     * @param configuration configuration to display
     */
    public void createConfigPanel(final int index, final Configuration configuration) {
        final ConfigPanel configPanel = new ConfigPanel(configuration);
        configPanel.setParent(this);
        configListModel.add(index, configPanel);
    }

    /**
     * Set up the configuration selection list.
     */
    private void setupChooserList() {
        configChooserList.setModel(configListModel);

        configChooserList.setCellRenderer(new DarkDefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, ((ConfigPanel) value).getConfiguration().getName(), index, isSelected, cellHasFocus);
            }
        });

        configChooserList.addListSelectionListener(e -> {
            final ConfigPanel selectedTuple = configChooserList.getSelectedValue();
            if (selectedTuple == null) {
                return;
            }
            nameTextField.setText(selectedTuple.getConfiguration().getName());
            configScrollWrapper.getViewport().setView(selectedTuple.getRootPanel());
        });

        configChooserList.setSelectedIndex(0);
    }

    /**
     * Do some custom ui setups.
     * Mainly changing colors and border.
     */
    private void customUISetup() {
        configNamePanel.setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("EvoSearch.darker"))
        );
        listEditBar.setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("SplitPane.dividerLineColor"))
        );
        bottomPane.setBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("SplitPane.dividerLineColor"))
        );
    }

    /**
     * React to an empty selection list by disabling the remove button and
     * displaying an empty message.
     */
    private void bindEmptyBehaviour() {
        final boolean listEmpty = configListModel.size() == 0;
        removeConfigButton.setEnabled(!listEmpty);
        if (listEmpty) {
            configScrollWrapper.getViewport().setView(new JLabel("No configuration set"));
        }
    }

    /**
     * Duplicate the selected configuration.
     */
    private void duplicateSelectedConfiguration() {
        triggerChange();
        final ConfigPanel selectedConfig = configChooserList.getSelectedValue();
        if (selectedConfig != null) {
            addConfiguration(selectedConfig.getConfiguration().clone());
        }

    }

    /**
     * Save the configuration is the apply button is enabled.
     * That happens, if a configuration was changed.
     */
    private void onApply() {
        if (applyButton.isEnabled()) {
            saveConfigurations();
        }
        applyButton.setEnabled(false);
    }

    /**
     * Trigger a changed configuration.
     * This state is only handled by the "Apply" Buttons enabled state.
     * This method is mainly used by the individual {@link ConfigPanel}s.
     */
    public void triggerChange() {
        applyButton.setEnabled(true);
    }

    /**
     * Show the configuration dialog frame.
     */
    public void showFrame() {
        setMinimumSize(new Dimension(768, 300));
        setTitle(LangService.get("run.configurations"));
        pack();
        setVisible(true);
    }

    /**
     * Save the configurations and trigger an {@link EventService#CONFIGS_CHANGED} event.
     */
    private void saveConfigurations() {
        final List<Configuration> configurations = IntStream.range(0, configListModel.size())
                .mapToObj(configListModel::getElementAt)
                .map(ConfigPanel::getConfiguration)
                .collect(Collectors.toList());

        final Project currentProject = ProjectService.getCurrentProject();
        ProjectService.saveConfigurations(new File(currentProject.getPath()), configurations);
        currentProject.setConfigurations(configurations);
        EventService.CONFIGS_CHANGED.trigger(configurations);
    }

    /**
     * Discard the changes, if the user presses "Cancel" and confirms to delete the changes.
     * Otherwise, it saves the changes immediately.
     *
     * @see #onApply()
     */
    private void onCancel() {
        if (applyButton.isEnabled()) {
            final int saveChanges = JOptionPane.showConfirmDialog(
                    this,
                    LangService.get("changes.not.saved.msg"),
                    LangService.get("changes.unsaved"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (saveChanges == JOptionPane.YES_OPTION) {
                onOK();
                return;
            }
        }
        dispose();
    }

    /**
     * If the button "OK" is pressed, the changes are applied and the window gets closed.
     *
     * @see #onApply()
     */
    private void onOK() {
        onApply();
        dispose();
    }

}
