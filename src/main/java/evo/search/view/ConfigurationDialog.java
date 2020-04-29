package evo.search.view;

import com.github.weisj.darklaf.ui.list.DarkListCellRenderer;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import evo.search.Environment;
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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
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

    public static void main(String[] args) {
        List<Configuration> configurations = Arrays.asList(
                Configuration.builder()
                        .version("undefined")
                        .name("one")
                        .limit(1000)
                        .positions(2)
                        .distances(Arrays.asList(10d, 20d))
                        .treasures(Arrays.asList(new DiscretePoint(0, 10)))
                        .alterers(Collections.singletonList(new SwapPositionsMutator(0.7)))
                        .build(),
                Configuration.builder()
                        .version("undefined")
                        .name("two")
                        .limit(100)
                        .positions(3)
                        .distances(Arrays.asList(15d, 25d, 35d))
                        .treasures(Arrays.asList(new DiscretePoint(0, 10), new DiscretePoint(2, 20)))
                        .fitness(Environment.Fitness.SINGULAR)
                        .build()
        );

        Main.setupEnvironment();
        ConfigurationDialog dialog = new ConfigurationDialog(configurations);
        dialog.showFrame();
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

    public ConfigurationDialog(List<Configuration> configurations) {
        $$$setupUI$$$();
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
            public void windowClosing(WindowEvent e) {
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

        configChooserList.setCellRenderer(new DarkListCellRenderer() {
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
            configScrollWrapper.getViewport().setView(selectedTuple.getPanel().$$$getRootComponent$$$());
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
        boolean listEmpty = configListModel.size() == 0;
        removeConfigButton.setEnabled(!listEmpty);
        if (listEmpty) {
            configScrollWrapper.getViewport().setView(new JLabel("No configuration set"));
        }
    }

    private void duplicateSelectedConfiguration() {
        triggerChange();
        ConfigTuple selectedConfig = configChooserList.getSelectedValue();
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
        List<Configuration> configurations = IntStream.range(0, configListModel.size())
                .mapToObj(configListModel::getElementAt)
                .map(ConfigTuple::getConfiguration)
                .collect(Collectors.toList());

        Project currentProject = ProjectService.getCurrentProject();
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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, 0));
        bottomPane = new JPanel();
        bottomPane.setLayout(new GridLayoutManager(1, 2, new Insets(6, 10, 6, 10), -1, -1));
        contentPane.add(bottomPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        bottomPane.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        bottomPane.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        this.$$$loadButtonText$$$(buttonOK, this.$$$getMessageFromBundle$$$("lang", "ok"));
        panel1.add(buttonOK, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        this.$$$loadButtonText$$$(buttonCancel, this.$$$getMessageFromBundle$$$("lang", "cancel"));
        panel1.add(buttonCancel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        applyButton = new JButton();
        applyButton.setEnabled(false);
        this.$$$loadButtonText$$$(applyButton, this.$$$getMessageFromBundle$$$("lang", "apply"));
        panel1.add(applyButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(150);
        contentPane.add(splitPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.setMinimumSize(new Dimension(150, 24));
        splitPane1.setLeftComponent(panel2);
        final JScrollPane scrollPane1 = new JScrollPane();
        panel2.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 40), null, null, 0, false));
        configChooserList = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        configChooserList.setModel(defaultListModel1);
        scrollPane1.setViewportView(configChooserList);
        listEditBar = new JPanel();
        listEditBar.setLayout(new GridLayoutManager(1, 4, new Insets(2, 6, 0, 6), 1, -1));
        panel2.add(listEditBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 40), new Dimension(-1, 40), new Dimension(-1, 40), 0, false));
        addConfigButton = new JButton();
        addConfigButton.setBorderPainted(false);
        addConfigButton.setIcon(new ImageIcon(getClass().getResource("/icon/plus.png")));
        addConfigButton.setToolTipText(this.$$$getMessageFromBundle$$$("lang", "config.add"));
        listEditBar.add(addConfigButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(30, 30), new Dimension(30, 30), new Dimension(30, 30), 0, false));
        removeConfigButton = new JButton();
        removeConfigButton.setBorderPainted(false);
        removeConfigButton.setIcon(new ImageIcon(getClass().getResource("/icon/minus.png")));
        removeConfigButton.setToolTipText(this.$$$getMessageFromBundle$$$("lang", "configuration.delete"));
        listEditBar.add(removeConfigButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(30, 30), new Dimension(30, 30), new Dimension(30, 30), 0, false));
        final Spacer spacer2 = new Spacer();
        listEditBar.add(spacer2, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        duplicateConfigButton = new JButton();
        duplicateConfigButton.setBorderPainted(false);
        duplicateConfigButton.setIcon(new ImageIcon(getClass().getResource("/icon/copy.png")));
        duplicateConfigButton.setToolTipText(this.$$$getMessageFromBundle$$$("lang", "config.add"));
        listEditBar.add(duplicateConfigButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(30, 30), new Dimension(30, 30), new Dimension(30, 30), 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setMinimumSize(new Dimension(550, 200));
        panel3.setPreferredSize(new Dimension(500, 200));
        splitPane1.setRightComponent(panel3);
        configNamePanel = new JPanel();
        configNamePanel.setLayout(new GridLayoutManager(1, 2, new Insets(2, 6, 2, 6), -1, -1));
        panel3.add(configNamePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(-1, 40), new Dimension(-1, 40), new Dimension(-1, 40), 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("lang", "name"));
        configNamePanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        configNamePanel.add(nameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        configScrollWrapper = new JScrollPane();
        panel3.add(configScrollWrapper, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        label1.setLabelFor(nameTextField);
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
        return contentPane;
    }

    @Value
    private class ConfigTuple {
        Configuration configuration;
        ConfigPanel panel;
    }

}
