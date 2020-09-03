package evo.search.view;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.Spacer;
import evo.search.Main;
import evo.search.io.entities.Project;
import evo.search.io.entities.Workspace;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * The chooser form enabled the user to create new or add existing projects
 * in the file system and manage their register state in the global .evoSearch register.
 *
 * @see ProjectService
 */
@Slf4j
public class ChooserForm extends JFrame {

    /**
     * Button for adding a new project.
     * Activates a prompt for the directory of the new project.
     */
    private JButton addButton;

    /**
     * Root panel of the chooser form.
     */
    private JPanel rootPanel;

    /**
     * Button for opening an existing, not registered project.
     */
    private JButton openButton;

    /**
     * Parent panel for the list of projects to choose.
     */
    private JPanel listPanel;

    /**
     * Parent pane for the {@link #addButton}, {@link #openButton} and logo.
     */
    private JPanel optionPane;

    /**
     * Label to display the logo.
     * Logo is loaded dynamically.
     */
    private JLabel evoSearchLogo;

    /**
     * Constructor for the chooser form.
     * Sets up the button and window listeners and the forms menu bar.
     */
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

    /**
     * Chooser form entry function.
     * Sets up the global project register if necessary.
     *
     * @param args cli args
     */
    public static void main(final String[] args) {
        ProjectService.setupService();
        Main.setupEnvironment();
        new ChooserForm();
    }

    /**
     * The action handler of the create project event.
     * Creates an empty project at a location chosen by the user through a prompt.
     *
     * @see #openButton
     * @see FileService#promptForDirectory()
     */
    private void onCreateProject() {
        final Path directory = FileService.promptForDirectory();
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
        untitledProject.setPath(directory);
        untitledProject.setName("Untitled");
        untitledProject.setVersion(Main.VERSION);
        ProjectService.setupNewProject(directory, untitledProject);
        if (Files.exists(directory.resolve(ProjectService.PROJECT_LEVEL_HIDDEN))) {
            ProjectService.addProjectEntry(untitledProject);
            ProjectService.setCurrentProject(untitledProject);
            openMainForm();
        }
    }

    /**
     * The action handler of the create project event.
     *
     * @see #openButton
     */
    private void onOpenProject() {
        final Path directory = FileService.promptForDirectory();
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

    /**
     * Reads all registered projects in the global directory and
     * displays them in the project choosing list.
     * It also binds the selection and deletion event to each project's
     * list item.
     */
    private void fillProjectList() {
        ProjectService.getIndexEntries().forEach(project -> {
            final ProjectListItem listItem = new ProjectListItem(project);

            listItem.bindSelectionEvent(selectedProject -> {
                final Project projectFromDir = ProjectService
                        .loadProjectFromDirectory(selectedProject.getPath());

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

    /**
     * Initialize the window of the chooser form.
     * The window is set in center with a fixed size.
     */
    private void setupFrame() {
        setTitle(Main.APP_TITLE);
        setSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setResizable(false);
        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Set custom colors and icons for the chooser form ui.
     * The {@link #optionPane} is made darker and both buttons get
     * a descriptive icon as well as a custom hover text color.
     */
    private void setOptionPaneUI() {
        optionPane.setBackground(UIManager.getColor("EvoSearch.darker"));
        addButton.setIcon(UIManager.getIcon("Spinner.plus.icon"));
        openButton.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        Arrays.asList(addButton, openButton).forEach(jButton ->
                jButton.addMouseListener(new MouseAdapter() {

                    /**
                     * Backup for the buttons original foreground color.
                     */
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

    /**
     * Action for opening a {@link MainForm}.
     * This happens if a project is chosen. This action loads the {@link Workspace} for
     * the current {@link Project} and displays the new {@link MainForm}.
     */
    private void openMainForm() {
        ProjectService.saveRegistered();
        final Workspace workspace = ProjectService.loadCurrentWorkspace();
        final MainForm mainForm = new MainForm();
        mainForm.setWorkspace(workspace);
        mainForm.showFrame();
        dispose();
    }

    /**
     * Custom create the {@link #listPanel} and {@link #evoSearchLogo} to set their unique properties.
     */
    private void createUIComponents() {
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        evoSearchLogo = new JLabel();
        evoSearchLogo.setIcon(new ImageIcon(new ImageIcon("icon.png").getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
    }

}
