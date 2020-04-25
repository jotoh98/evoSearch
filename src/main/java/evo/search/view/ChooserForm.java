package evo.search.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import evo.search.Main;
import evo.search.io.EventService;
import evo.search.io.FileService;
import evo.search.io.entities.Project;
import evo.search.io.service.ProjectService;
import evo.search.view.part.ProjectListItem;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ChooserForm extends JFrame {
    private JButton addButton;
    private JPanel rootPanel;
    private JPanel optionsPane;
    private JButton openButton;
    private JPanel listPanel;

    private DefaultListModel<Project> projectsListModel = new DefaultListModel<>();

    List<Project> projectList = new ArrayList<>(Arrays.asList(
            new Project("alpha-0", "/larry/whatsup", "Hello there"),
            new Project("1.0", "/larry/where/is/this/damn/path", "General Kenobi!")
    ));

    public ChooserForm() {

        $$$setupUI$$$();


        for (int i = 0; i < 25; i++) {
            final ProjectListItem comp = new ProjectListItem(projectList.get(0));
            comp.bindSelectionEvent(project -> {
                EventService.INIT_PROJECT.trigger(project);
                dispose();
            });
            comp.bindDeleteEvent(e -> {
                listPanel.remove(comp);
                listPanel.revalidate();
                listPanel.repaint();
            });
            listPanel.add(comp);
        }

        final Spacer spacer1 = new Spacer();
        listPanel.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

        ProjectService.setupGlobal();

        optionsPane.setBackground(UIManager.getColor("EvoSearch.darker"));
        addButton.setIcon(UIManager.getIcon("Spinner.plus.icon"));
        openButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));

        setupFrame();
        addButton.addActionListener(e -> {
            final File directory = FileService.promptForDirectory();
            if (directory != null) {
                if (directory.list() != null && Objects.requireNonNull(directory.list()).length > 0) {
                    JOptionPane.showConfirmDialog(
                            this,
                            LangService.get("project.not.created") + " " + LangService.get("directory.not.empty") + ".",
                            LangService.get("directory.not.empty"),
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
                ProjectService.setupProject(Objects.requireNonNull(directory));
            }
        });

        openButton.addActionListener(e -> {
            final File directory = FileService.promptForDirectory();
            if (directory != null) {
                if (!ProjectService.isProject(directory)) {
                    JOptionPane.showConfirmDialog(
                            this,
                            LangService.get("project.not.opened") + " " + LangService.get("directory.no.project") + ".",
                            LangService.get("directory.no.project"),
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    final Project project = ProjectService.loadProject(directory);
                    EventService.INIT_PROJECT.trigger(project);
                }
            } else {
                JOptionPane.showConfirmDialog(
                        this,
                        LangService.get("project.not.opened") + " " + LangService.get("directory.not.chosen") + ".",
                        LangService.get("directory.not.chosen"),
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        Arrays.asList(addButton, openButton).forEach(jButton ->
                jButton.addMouseListener(new MouseAdapter() {

                    final Color backup = jButton.getForeground();

                    @Override
                    public void mouseEntered(final MouseEvent e) {
                        jButton.setForeground(UIManager.getColor("EvoSearch.reddish"));
                        super.mouseEntered(e);
                    }

                    @Override
                    public void mouseExited(final MouseEvent e) {
                        jButton.setForeground(backup);
                        super.mouseExited(e);
                    }
                })
        );
    }

    public static void main(String[] args) {
        Main.setupEnvironment();
        new ChooserForm();
    }

    private void setupFrame() {
        setTitle(Main.APP_TITLE);
        setSize(new Dimension(600, 400));
        setResizable(false);
        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 0, 0));
        final JScrollPane scrollPane1 = new JScrollPane();
        rootPanel.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(300, -1), null, 0, false));
        scrollPane1.setViewportView(listPanel);
        optionsPane = new JPanel();
        optionsPane.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        optionsPane.setBackground(new Color(-14868446));
        rootPanel.add(optionsPane, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(100, -1), new Dimension(300, -1), 0, false));
        optionsPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        addButton = new JButton();
        addButton.setBorderPainted(false);
        addButton.setContentAreaFilled(false);
        this.$$$loadButtonText$$$(addButton, this.$$$getMessageFromBundle$$$("lang", "create.new"));
        optionsPane.add(addButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(139, 30), null, 0, false));
        openButton = new JButton();
        openButton.setBorderPainted(false);
        openButton.setContentAreaFilled(false);
        this.$$$loadButtonText$$$(openButton, this.$$$getMessageFromBundle$$$("lang", "open"));
        optionsPane.add(openButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(139, 30), null, 0, false));
        final Spacer spacer1 = new Spacer();
        optionsPane.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(139, 14), null, 0, false));
        final Spacer spacer2 = new Spacer();
        optionsPane.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(139, 14), null, 0, false));
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

    private void createUIComponents() {
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
    }
}
