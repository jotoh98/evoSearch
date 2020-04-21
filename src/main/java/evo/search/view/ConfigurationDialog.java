package evo.search.view;

import com.github.weisj.darklaf.ui.list.DarkListCellRenderer;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import evo.search.Environment;
import evo.search.ga.DiscretePoint;
import evo.search.io.entities.Configuration;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

public class ConfigurationDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton applyButton;
    Hashtable<Configuration, ConfigPanel> configPanels = new Hashtable<>();
    private JPanel bottomPane;
    private JList<Configuration> list1;
    private JTextField nameTextField;
    private JButton addConfigButton;
    private JPanel listEditBar;
    private ConfigPanel configPanel;
    private JPanel configNamePanel;
    private JScrollPane nestedScrollPane;
    private JButton removeConfigButton;
    private DefaultListModel<Configuration> configListModel = new DefaultListModel<>();

    private List<Configuration> configurations = Arrays.asList(
            new Configuration("undefined", "one", 100, 2, Arrays.asList(10d, 20d), Arrays.asList(new DiscretePoint(0, 10)), Environment.Fitness.GLOBAL),
            new Configuration("undefined", "two", 200, 3, Arrays.asList(15d, 25d, 35d), Arrays.asList(new DiscretePoint(0, 10), new DiscretePoint(2, 20)), Environment.Fitness.SINGULAR)
    );

    public ConfigurationDialog() {
        $$$setupUI$$$();

        configPanel.setMaximumSize(nestedScrollPane.getPreferredSize());
        list1.setModel(configListModel);
        configListModel.addAll(configurations);

        list1.setCellRenderer(new DarkListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
                final Configuration configurationValue = (Configuration) value;
                return super.getListCellRendererComponent(list, configurationValue.getName(), index, isSelected, cellHasFocus);
            }
        });

        list1.addListSelectionListener(e -> {
            final Configuration selectedConfiguration = list1.getSelectedValue();
            if (selectedConfiguration == null) {
                if (configListModel.size() == 0) {
                    nestedScrollPane.add(new JLabel("No configuration set"));
                }
                return;
            }
            nameTextField.setText(selectedConfiguration.getName());
            configPanel.setConfiguration(selectedConfiguration);
        });

        list1.setSelectedIndex(0);

        nameTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(final KeyEvent e) {
                list1.repaint();
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                final String text = nameTextField.getText();
                list1.getSelectedValue().setName(text);
                list1.repaint();
            }

            @Override
            public void keyPressed(final KeyEvent e) {
                keyTyped(e);
            }
        });

        configNamePanel.setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("EvoSearch.darker"))
        );
        listEditBar.setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("SplitPane.dividerLineColor"))
        );
        bottomPane.setBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("SplitPane.dividerLineColor"))
        );

        addConfigButton.setIcon(UIManager.getIcon("Spinner.plus.icon"));
        removeConfigButton.setIcon(UIManager.getIcon("Spinner.minus.icon"));
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        configListModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(final ListDataEvent e) {
                removeConfigButton.setEnabled(configListModel.size() > 0);
            }

            @Override
            public void intervalRemoved(final ListDataEvent e) {
                removeConfigButton.setEnabled(configListModel.size() > 0);
            }

            @Override
            public void contentsChanged(final ListDataEvent e) {

            }
        });
        removeConfigButton.addActionListener(e -> {
            final int[] selectedIndices = list1.getSelectedIndices();
            if (selectedIndices.length < 1) {
                return;
            }

            final int firstIndex = selectedIndices[0];

            if (selectedIndices.length == 1) {
                configListModel.remove(firstIndex);
                list1.setSelectedIndex(firstIndex == configListModel.size() - 1 ? firstIndex : firstIndex + 1);
                return;
            }

            configListModel.removeRange(firstIndex, selectedIndices[selectedIndices.length - 1]);
            if (configListModel.size() > 0) {
                list1.setSelectedIndex(0);
            }
        });

        addConfigButton.addActionListener(e -> {
            final int selectedIndex = list1.getSelectedIndex();
            if (selectedIndex == -1) {
                configListModel.add(0, new Configuration());
                list1.setSelectedIndex(0);
            } else {
                configListModel.add(selectedIndex + 1, new Configuration());
                list1.setSelectedIndex(selectedIndex + 1);
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private DefaultListModel<Configuration> getListModel() {
        return new DefaultListModel<>() {

        };
    }

    public static void main(String[] args) {
        MainForm.setupEnvironment();
        ConfigurationDialog dialog = new ConfigurationDialog();
        dialog.setMinimumSize(new Dimension(768, 300));
        dialog.setTitle(LangService.get("run.configurations"));
        dialog.pack();
        dialog.setVisible(true);
        //System.exit(0);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
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
        this.$$$loadButtonText$$$(buttonOK, ResourceBundle.getBundle("lang").getString("ok"));
        panel1.add(buttonOK, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        this.$$$loadButtonText$$$(buttonCancel, ResourceBundle.getBundle("lang").getString("cancel"));
        panel1.add(buttonCancel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        applyButton = new JButton();
        applyButton.setEnabled(false);
        this.$$$loadButtonText$$$(applyButton, ResourceBundle.getBundle("lang").getString("apply"));
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
        list1 = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        defaultListModel1.addElement("Config 1");
        defaultListModel1.addElement("Config 2");
        list1.setModel(defaultListModel1);
        scrollPane1.setViewportView(list1);
        listEditBar = new JPanel();
        listEditBar.setLayout(new GridLayoutManager(1, 3, new Insets(2, 6, 0, 6), 1, -1));
        panel2.add(listEditBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        addConfigButton = new JButton();
        addConfigButton.setBorderPainted(false);
        addConfigButton.setText("");
        addConfigButton.setToolTipText(ResourceBundle.getBundle("lang").getString("config.add"));
        listEditBar.add(addConfigButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(30, 30), new Dimension(30, 30), new Dimension(30, 30), 0, false));
        removeConfigButton = new JButton();
        removeConfigButton.setBorderPainted(false);
        removeConfigButton.setToolTipText(ResourceBundle.getBundle("lang").getString("config.add"));
        listEditBar.add(removeConfigButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, new Dimension(30, 30), new Dimension(30, 30), new Dimension(30, 30), 0, false));
        final Spacer spacer2 = new Spacer();
        listEditBar.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.setMinimumSize(new Dimension(550, 200));
        panel3.setPreferredSize(new Dimension(500, 200));
        splitPane1.setRightComponent(panel3);
        configNamePanel = new JPanel();
        configNamePanel.setLayout(new GridLayoutManager(1, 2, new Insets(2, 6, 2, 6), -1, -1));
        panel3.add(configNamePanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(300, -1), null, null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("lang").getString("name"));
        configNamePanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nameTextField = new JTextField();
        configNamePanel.add(nameTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        nestedScrollPane = new JScrollPane();
        panel3.add(nestedScrollPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        configPanel = new ConfigPanel();
        nestedScrollPane.setViewportView(configPanel.$$$getRootComponent$$$());
        label1.setLabelFor(nameTextField);
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

}
