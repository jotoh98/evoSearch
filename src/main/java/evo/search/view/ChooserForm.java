package evo.search.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.Spacer;
import evo.search.Main;
import evo.search.io.entities.Project;
import evo.search.io.service.FileService;
import evo.search.io.service.MenuService;
import evo.search.io.service.ProjectService;
import evo.search.view.part.ProjectListItem;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

@Slf4j
public class ChooserForm extends JFrame {
    private JButton addButton;
    private JPanel rootPanel;
    private JButton openButton;
    private JPanel listPanel;
    private JPanel optionPane;
    private JLabel evoSearchLogo;

    public ChooserForm() {
        fillProjectList();
        setupFrame();

        addButton.addActionListener(e -> onCreateProject());
        openButton.addActionListener(e -> onOpenProject());

        setOptionPaneUI();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                ProjectService.saveRegistered();
                super.windowClosing(e);
            }
        });

        setMenuBar(MenuService.menuBar(
                MenuService.menu(
                        LangService.get("project"),
                        MenuService.item(
                                LangService.get("new"),
                                actionEvent -> onCreateProject(),
                                MenuService.shortcut('n')
                        ),
                        MenuService.item(
                                LangService.get("open.dots"),
                                actionEvent -> onOpenProject(),
                                MenuService.shortcut('o')
                        )
                )
        ));
    }

    public static void main(final String[] args) {
        ProjectService.setupService();
        Main.setupEnvironment();
        new ChooserForm();
    }

    private void onCreateProject() {
        final File directory = FileService.promptForDirectory();
        if (directory == null) {
            return;
        }
        if (ProjectService.containsHidden(directory)) {
            JOptionPane.showConfirmDialog(
                    this,
                    LangService.get("project.not.created") + " " + LangService.get("directory.already.set.up") + ".",
                    LangService.get("directory.not.empty"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        final Project untitledProject = new Project();
        untitledProject.setPath(directory.getPath());
        untitledProject.setName("Untitled");
        untitledProject.setVersion(Main.VERSION);
        if (ProjectService.setupNewProject(Objects.requireNonNull(directory), untitledProject)) {
            ProjectService.addProjectEntry(untitledProject);
            ProjectService.setCurrentProject(untitledProject);
            openMainForm();
        }
    }

    private void onOpenProject() {
        final File directory = FileService.promptForDirectory();
        if (directory == null) {
            JOptionPane.showConfirmDialog(
                    this,
                    LangService.get("project.not.opened") + " " + LangService.get("directory.not.chosen") + ".",
                    LangService.get("directory.not.chosen"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        if (!ProjectService.containsHidden(directory)) {
            JOptionPane.showConfirmDialog(
                    this,
                    LangService.get("project.not.opened") + " " + LangService.get("directory.no.project") + ".",
                    LangService.get("directory.no.project"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        final Project openedProject = ProjectService.loadProjectFromDirectory(directory);

        if (openedProject == null) {
            return;
        }

        if (!ProjectService.isProjectRegistered(openedProject)) {
            ProjectService.addProjectEntry(openedProject);
        }
        ProjectService.setCurrentProject(openedProject);
        openMainForm();
    }

    private void fillProjectList() {
        ProjectService.getIndexEntries().forEach(project -> {
            final ProjectListItem listItem = new ProjectListItem(project);

            listItem.bindSelectionEvent(selectedProject -> {
                final Project projectFromDir = ProjectService
                        .loadProjectFromDirectory(new File(selectedProject.getPath()));

                if (projectFromDir == null) {
                    final int deleteOption = JOptionPane.showConfirmDialog(this, LangService.get("project.want.delete"), LangService.get("project.does.not.exist"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

                    if (deleteOption == 0) {
                        listPanel.remove(listItem);
                        listPanel.revalidate();
                        listPanel.repaint();
                        ProjectService.getIndexEntries().remove(project);
                    }

                    return;
                }

                projectFromDir.setPath(selectedProject.getPath());
                ProjectService.setCurrentProject(projectFromDir);
                openMainForm();
            });

            listItem.bindDeleteEvent(e -> {
                listPanel.remove(listItem);
                listPanel.revalidate();
                listPanel.repaint();
                ProjectService.getIndexEntries().remove(project);
            });

            listPanel.add(listItem);
        });
        final Spacer verticalSpacer = new Spacer();
        listPanel.add(verticalSpacer, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

    }

    private void setupFrame() {
        setTitle(Main.APP_TITLE);
        setSize(new Dimension(600, 400));
        setResizable(false);
        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void setOptionPaneUI() {
        optionPane.setBackground(UIManager.getColor("EvoSearch.darker"));
        addButton.setIcon(UIManager.getIcon("Spinner.plus.icon"));
        openButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
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

    private void openMainForm() {
        ProjectService.saveRegistered();
        final MainForm mainForm = new MainForm();
        mainForm.showFrame();
        dispose();
    }

    private void createUIComponents() {
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        evoSearchLogo = new JLabel();
        evoSearchLogo.setIcon(new ImageIcon(new ImageIcon("icon.png").getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
    }

}
