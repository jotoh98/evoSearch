package evo.search.io.service;

import evo.search.Evolution;
import evo.search.Main;
import evo.search.ga.DiscreteChromosome;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.IndexEntry;
import evo.search.io.entities.Project;
import evo.search.io.entities.Workspace;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Project utility service.
 * Handles current project and the global project registry.
 */
@Slf4j
public class ProjectService {

    /**
     * Hidden folder name for local project level.
     */
    public static final String PROJECT_LEVEL_HIDDEN = ".evo";
    /**
     * File name for global project registry.
     */
    private static final String PROJECT_XML = "project.xml";
    /**
     * Folder name for saved configurations in the hidden project folder.
     */
    private static final String CONFIG_FOLDER = "configs";

    /**
     * Settings file name for the project.
     */
    private static final String PROJECT_SETTING = "settings.xml";

    /**
     * Workspace file name for the project.
     */
    private static final String WORKSPACE_XML = "workspace.xml";

    /**
     * List of registered projects.
     */
    @Getter
    private static final List<IndexEntry> indexEntries = new ArrayList<>();

    /**
     * Currently opened project.
     */
    @Setter
    @Getter
    private static Project currentProject;

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    /**
     * Add a new project to the services index entries.
     *
     * @param project project to add
     */
    public static void addProjectEntry(final Project project) {
        indexEntries.add(new IndexEntry(project.getPath(), project.getName(), project.getVersion(), LocalDateTime.now()));
    }

    /**
     * Set up a new project with configs folder and basic project config files.
     *
     * @param folder  folder to create the projects files in
     * @param project project to save
     */
    public static void setupNewProject(final Path folder, final Project project) {
        if (folder == null || Files.notExists(folder)) {
            log.error("Project destination is no directory.");
            return;
        }

        final Path hiddenPath = folder.resolve(PROJECT_LEVEL_HIDDEN);
        try {
            Files.createDirectory(hiddenPath);
        } catch (final FileAlreadyExistsException e) {
            log.error("Project destination is already set up.");
            return;
        } catch (final IOException e) {
            log.error("Project setup didn't complete.", e);
            return;
        }

        final Path projectXML = hiddenPath.resolve(PROJECT_SETTING);
        try {
            final Document document = project.serialize();
            Files.write(projectXML, document.asXML().getBytes());
        } catch (final IOException e) {
            log.error("Project setup didn't complete.", e);
        }
    }

    /**
     * Check, if the global register is set up.
     *
     * @return true, if the ".evoSearch" folder exist in user root, false otherwise
     */
    public static boolean isSetUp() {
        return Files.exists(Main.HOME_PATH.resolve(PROJECT_XML));
    }

    /**
     * Load a project from a given directory.
     * Checks are performed to ensure, that the directory contains a project.
     *
     * @param projectDirectory directory with project
     * @return loaded project if it exists, null otherwise
     */
    public static Project loadProjectFromDirectory(final Path projectDirectory) {
        if (Files.notExists(projectDirectory)) {
            log.debug("Project file is no directory.");
            return null;
        }
        final Path hiddenPath = projectDirectory.resolve(PROJECT_LEVEL_HIDDEN);
        if (Files.notExists(hiddenPath)) {
            log.error("No " + PROJECT_LEVEL_HIDDEN + " file found.");
            return null;
        }

        final Project project = new Project();

        try (final Stream<Path> walk = Files.walk(hiddenPath)) {
            walk.forEach(path -> {
                switch (path.getFileName().toString()) {
                    case PROJECT_SETTING:
                        final Document projectSettings = FileService.read(path);
                        project.parse(projectSettings);
                        break;
                    case CONFIG_FOLDER:
                        try {
                            Files.walk(path).forEach(configFile -> {
                                if (!configFile.toString().endsWith(".xml")) return;
                                final Document configDocument = FileService.read(configFile);
                                project.getConfigurations().add(new Configuration().parse(configDocument));
                            });
                        } catch (final IOException ignored) {}
                }
            });
        } catch (final IOException ignored) {}

        return project;
    }

    /**
     * Save a list of configurations to a project folder.
     * Ensures a project is set up in the provided directory and overwrites the old configurations.
     *
     * @param projectFolder  directory containing a project
     * @param configurations configurations to save in the directory
     */
    public static void saveConfigurations(final Path projectFolder, final List<Configuration> configurations) {
        if (Files.notExists(projectFolder)) {
            log.error("Cannot save configurations: Project folder does not exists: {}", projectFolder);
            EventService.LOG.trigger("Cannot save configurations: Project folder does not exists.");
            return;
        }

        final Path hiddenPath = projectFolder.resolve(PROJECT_LEVEL_HIDDEN);
        if (Files.notExists(hiddenPath)) {
            try {
                Files.createDirectory(hiddenPath);
            } catch (final IOException e) {
                EventService.LOG.trigger("Cannot save configurations: " + e.getLocalizedMessage());
                return;
            }
        }

        final Path configFolder = hiddenPath.resolve(CONFIG_FOLDER);

        if (Files.notExists(configFolder)) {
            try {
                Files.createDirectory(configFolder);
            } catch (final IOException e) {
                EventService.LOG.trigger("Cannot save configurations: " + e.getLocalizedMessage());
                return;
            }
        }

        try (final Stream<Path> pathStream = Files.walk(configFolder, 1, FileVisitOption.FOLLOW_LINKS)) {
            pathStream.forEach(path -> {
                try {
                    Files.delete(path);
                } catch (final IOException ignored) {}
            });

            FileService.write(configFolder, configurations);
        } catch (final IOException ignored) {}


    }

    /**
     * Save a workspace to the currently opened project.
     *
     * @param workspace workspace to save
     */
    public static void saveProjectWorkspace(final Workspace workspace) {
        final Path workspaceFile = currentProject.getPath().resolve(PROJECT_LEVEL_HIDDEN).resolve(WORKSPACE_XML);
        FileService.write(workspaceFile, workspace.serialize());
    }

    /**
     * Load the workspace for the current project.
     *
     * @return workspace of the current project, empty workspace if there is no current project
     */
    public static Workspace loadCurrentWorkspace() {
        if (currentProject == null) return new Workspace();
        final Path workspacePath = currentProject.getPath().resolve(PROJECT_LEVEL_HIDDEN).resolve(WORKSPACE_XML);

        if (Files.exists(workspacePath)) {
            final Document workspaceDocument = FileService.read(workspacePath);
            return new Workspace().parse(workspaceDocument);
        }

        FileService.write(workspacePath, new Workspace().serialize());
        return new Workspace();
    }

    /**
     * Check if the given project is registered in the services list. Checks by equality of paths.
     *
     * @param project project to check
     * @return true if the project's directory exists in the services list, false otherwise
     */
    public static boolean isProjectRegistered(final Project project) {
        return indexEntries.stream()
                .map(IndexEntry::getPath)
                .filter(Objects::nonNull)
                .anyMatch(path -> path.equals(project.getPath()));
    }

    /**
     * Check, if the directory contains the hidden ".evo" folder.
     *
     * @param file directory to check
     * @return true if the hidden folder is in the given directory, false otherwise
     */
    public static boolean containsHidden(final Path file) {
        try {
            return Files.walk(file).anyMatch(path -> path.endsWith(".evo"));
        } catch (final IOException ignored) {}
        EventService.LOG.trigger("Current working directory is no project.");
        return false;
    }

    /**
     * Set up the global register with no project.
     */
    public static void setupService() {
        if (!isSetUp()) {
            writeProjectIndex(Collections.emptyList());
            return;
        }
        if (indexEntries.size() == 0) {
            readProjectIndex();
        }
    }

    /**
     * Read all globally registered projects into the services index entries.
     */
    public static void readProjectIndex() {
        final Path globalPath = Main.HOME_PATH.resolve(PROJECT_XML);
        if (Files.notExists(globalPath)) {
            log.error("Not able to get globally registered projects.");
            return;
        }
        final Document parsedDocument = FileService.read(globalPath);
        indexEntries.addAll(XmlService.readRegister(parsedDocument));
    }

    /**
     * Write the given index entries into the global register.
     *
     * @param projects projects entries to save
     */
    public static void writeProjectIndex(final List<IndexEntry> projects) {
        final Path projectRegisterPath = Main.HOME_PATH.resolve(PROJECT_XML);
        if (Files.notExists(projectRegisterPath)) {
            log.error("Not able to get globally registered projects.");
            return;
        }
        final Document document = XmlService.writeRegister(projects);
        FileService.write(projectRegisterPath, document);
    }

    /**
     * Save the services index entries into the global register.
     *
     * @see #writeProjectIndex(List)
     */
    public static void saveRegistered() {
        writeProjectIndex(indexEntries);
    }

    /**
     * Save the evolution to a .evolution file and back up the configuration to an xml file
     * with the same number.
     *
     * @param evolution evolution to write
     * @param prefix    file name prefix
     * @return path to the saved evolution
     */
    public static Path writeEvolution(final Evolution evolution, final String prefix) {
        final int counter = FileService.counter(currentProject.getPath(), prefix + "-", ".evolution");
        final String filename = prefix + "-" + counter + ".evolution";
        final Path evolutionPath = currentProject.getPath().resolve(filename);
        try (final ObjectOutput out = new ObjectOutputStream(Files.newOutputStream(evolutionPath))) {
            out.writeObject(evolution);
            FileService.write(currentProject.getPath().resolve("config-" + counter + ".csv"), evolution.getConfiguration().serialize());
            EventService.LOG.trigger("Evolution was saved to file " + filename);
            return evolutionPath;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Read an evolution file and the configuration.
     *
     * @param evolutionFile .evolution file path
     * @return evolution with backed up configuration, if something fails, null
     */
    public static Evolution readEvolution(final Path evolutionFile) {
        final Configuration configuration = new Configuration();
        final int counter = getEvolutionFileNumber(evolutionFile);
        if (counter >= 0) {
            final Path configPath = evolutionFile.getParent().resolve("config-" + counter + ".csv");
            if (Files.exists(configPath))
                configuration.parse(FileService.read(configPath));
        }
        try (final ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(evolutionFile))) {
            final Evolution evolution = (Evolution) objectInputStream.readObject();
            evolution.setConfiguration(configuration);
            evolution.getHistory().forEach(result -> result.population().forEach(phenotype -> ((DiscreteChromosome) phenotype.genotype().chromosome()).setConfiguration(configuration)));
            return evolution;
        } catch (final IOException | ClassNotFoundException e) {
            EventService.LOG.trigger("Could not load evolution " + evolutionFile.toString() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Get the number from the evolution file.
     *
     * @param evolutionFile path to the evolution file
     * @return number in the filename of the evolution
     */
    private static int getEvolutionFileNumber(final Path evolutionFile) {
        final Matcher matcher = NUMBER_PATTERN.matcher(evolutionFile.getFileName().toString());
        int counter = -1;
        if (matcher.find()) {
            try {
                counter = Integer.parseInt(matcher.group());
            } catch (final NumberFormatException ignored) {}
        }
        return counter;
    }

    /**
     * Scan the current projects directory for .evolution files.
     *
     * @return list of evolution file paths
     */
    public static List<Path> getSavedEvolutions() {
        try {
            return Files.walk(currentProject.getPath(), 1)
                    .filter(path ->
                            path.getFileName().toString().endsWith(".evolution")
                    )
                    .filter(path -> {
                        final int number = getEvolutionFileNumber(path);
                        return Files.exists(currentProject.getPath().resolve("config-" + number + ".csv"));
                    })
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
