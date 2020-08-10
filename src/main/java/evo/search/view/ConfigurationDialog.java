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
import lombok.Value;

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

public class ConfigurationDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton applyButton;
    private JPanel bottomPane;
    private JList<ConfigTuple> configChooserList;
    private JTextField nameTextField;
    private JButton addConfigButton;
    private final DefaultListModel<ConfigTuple> configListModel = new DefaultListModel<>();
    private JPanel listEditBar;
    private JPanel configNamePanel;
    private JButton removeConfigButton;
    @Getter
    private JScrollPane configScrollWrapper;
    private JButton duplicateConfigButton;

    public ConfigurationDialog(final List<Configuration> configurations) {
        customUISetup();

        configurations.stream()
                .map(Configuration::clone)
                .forEach(this::showConfiguration);

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

    public static void main(final String[] args) {
        final List<Configuration> configurations = Arrays.asList(
                Configuration.builder()
                        .version("undefined")
                        .name("one")
                        .limit(1000)
                        .positions(2)
                        .distances(Arrays.asList(10d, 20d))
                        .treasures(Arrays.asList(new DiscretePoint(2, 0, 10)))
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
        final ConfigurationDialog dialog = new ConfigurationDialog(Collections.emptyList());
        dialog.showFrame();
    }

    private void bindConfigListButtons() {
        addConfigButton.addActionListener(e -> {
            addConfiguration(new Configuration());
        });

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

    private void addConfiguration(final Configuration configuration) {
        triggerChange();
        final int selectedIndex = configChooserList.getSelectedIndex();
        if (selectedIndex == -1) {
            showConfiguration(0, configuration);
            configChooserList.setSelectedIndex(0);
        } else {
            showConfiguration(selectedIndex + 1, configuration);
            configChooserList.setSelectedIndex(selectedIndex + 1);
        }
    }

    private void setupChooserList() {
        configChooserList.setModel(configListModel);

        configChooserList.setCellRenderer(new DarkDefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                final ConfigTuple configTuple = (ConfigTuple) value;
                return super.getListCellRendererComponent(list, configTuple.getConfiguration().getName(), index, isSelected, cellHasFocus);
            }
        });

        configChooserList.addListSelectionListener(e -> {
            final ConfigTuple selectedTuple = configChooserList.getSelectedValue();
            if (selectedTuple == null) {
                return;
            }
            nameTextField.setText(selectedTuple.getConfiguration().getName());
            configScrollWrapper.getViewport().setView(selectedTuple.getPanel().getRootPanel());
        });

        configChooserList.setSelectedIndex(0);
    }

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

    private void bindEmptyBehaviour() {
        final boolean listEmpty = configListModel.size() == 0;
        removeConfigButton.setEnabled(!listEmpty);
        if (listEmpty) {
            configScrollWrapper.getViewport().setView(new JLabel("No configuration set"));
        }
    }

    private void duplicateSelectedConfiguration() {
        triggerChange();
        final ConfigTuple selectedConfig = configChooserList.getSelectedValue();
        if (selectedConfig != null) {
            addConfiguration(selectedConfig.configuration.clone());
        }

    }

    public void showConfiguration(final Configuration configuration) {
        final ConfigPanel configPanel = new ConfigPanel();
        configPanel.setConfiguration(configuration);
        configPanel.setParent(this);
        configListModel.addElement(new ConfigTuple(configuration, configPanel));
    }

    private void onApply() {
        if (applyButton.isEnabled()) {
            saveConfigurations();
        }
        applyButton.setEnabled(false);
    }

    public void showConfiguration(final int index, final Configuration configuration) {
        final ConfigPanel configPanel = new ConfigPanel();
        configPanel.setConfiguration(configuration);
        configPanel.setParent(this);
        configListModel.add(index, new ConfigTuple(configuration, configPanel));
    }

    public void triggerChange() {
        applyButton.setEnabled(true);
    }

    public void showFrame() {
        setMinimumSize(new Dimension(768, 300));
        setTitle(LangService.get("run.configurations"));
        pack();
        setVisible(true);
    }

    private void saveConfigurations() {
        final List<Configuration> configurations = IntStream.range(0, configListModel.size())
                .mapToObj(configListModel::getElementAt)
                .map(ConfigTuple::getConfiguration)
                .collect(Collectors.toList());

        final Project currentProject = ProjectService.getCurrentProject();
        ProjectService.saveConfigurations(new File(currentProject.getPath()), configurations);
        currentProject.setConfigurations(configurations);
        EventService.CONFIGS_CHANGED.trigger(configurations);
    }

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

    private void onOK() {
        onApply();
        dispose();
    }

    @Value
    private class ConfigTuple {
        Configuration configuration;
        ConfigPanel panel;
    }

}
