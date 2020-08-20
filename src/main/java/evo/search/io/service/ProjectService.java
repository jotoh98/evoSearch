package evo.search.io.service;

import evo.search.Main;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.IndexEntry;
import evo.search.io.entities.Project;
import evo.search.io.entities.Workspace;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class ProjectService {

    public static final String PROJECT_LEVEL_HIDDEN = ".evo";
    private static final String PROJECT_SETTING = "settings.xml";
    private static final String WORKSPACE_XML = "workspace.xml";
    private static final String PROJECT_XML = "project.xml";
    private static final String CONFIG_FOLDER = "configs";

    @Getter
    private static final List<IndexEntry> indexEntries = new ArrayList<>();

    @Setter
    @Getter
    private static Project currentProject;

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
        if (folder == null || !Files.exists(folder)) {
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

    public static boolean isSetUp() {
        final File configXml = new File(Main.HOME_PATH + File.separator + PROJECT_XML);
        return configXml.exists();
    }

    public static Project loadProjectFromDirectory(final Path projectDirectory) {
        if (!Files.exists(projectDirectory)) {
            log.debug("Project file is no directory.");
            return null;
        }
        final Path hiddenPath = projectDirectory.resolve(PROJECT_LEVEL_HIDDEN);
        if (!Files.exists(hiddenPath)) {
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

    public static void saveConfigurations(final File projectFolder, final List<Configuration> configurations) {
        if (!projectFolder.exists()) {
            log.error("Cannot save configurations: Project folder does not exists: {}", projectFolder);
            EventService.LOG.trigger("Cannot save configurations: Project folder does not exists.");
            return;
        }

        final Path hiddenPath = projectFolder.toPath().resolve(PROJECT_LEVEL_HIDDEN);
        if (!Files.exists(hiddenPath)) {
            try {
                Files.createDirectory(hiddenPath);
            } catch (final IOException e) {
                EventService.LOG.trigger("Cannot save configurations: " + e.getLocalizedMessage());
                return;
            }
        }

        final Path configFolder = hiddenPath.resolve(CONFIG_FOLDER);

        if (!Files.isDirectory(configFolder)) {
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

    public static void saveProjectWorkspace(final Workspace workspace) {
        final Path workspaceFile = Path.of(currentProject.getPath(), PROJECT_LEVEL_HIDDEN, WORKSPACE_XML);
        FileService.write(workspaceFile, workspace.serialize());
    }

    public static Workspace loadCurrentWorkspace() {
        if (currentProject == null) return new Workspace();
        final Path workspacePath = Path.of(currentProject.getPath(), PROJECT_LEVEL_HIDDEN, WORKSPACE_XML);

        if (Files.exists(workspacePath)) {
            final Document workspaceDocument = FileService.read(workspacePath);
            return new Workspace().parse(workspaceDocument);
        }

        FileService.write(workspacePath, new Workspace().serialize());
        return new Workspace();
    }

    public static boolean isProjectRegistered(final Project project) {
        return indexEntries.stream()
                .map(IndexEntry::getPath)
                .filter(Objects::nonNull)
                .anyMatch(path -> path.equals(project.getPath()));
    }

    public static boolean containsHidden(final Path file) {
        try {
            return Files.walk(file).anyMatch(path -> path.endsWith(".evo"));
        } catch (final IOException ignored) {}
        EventService.LOG.trigger("Current working directory is no project.");
        return false;
    }

    public static void setupService() {
        if (!isSetUp()) {
            writeProjectIndex(Collections.emptyList());
            return;
        }
        if (indexEntries.size() == 0) {
            readProjectIndex();
        }
    }

    public static void readProjectIndex() {

        final Path globalPath = Path.of(Main.HOME_PATH, PROJECT_XML);
        if (!Files.exists(globalPath)) {
            log.error("Not able to get globally registered projects.");
            return;
        }
        final Document parsedDocument = FileService.read(globalPath);
        indexEntries.addAll(XmlService.readRegister(parsedDocument));
    }

    public static void writeProjectIndex(final List<IndexEntry> projects) {

        final Path projectRegisterPath = Path.of(Main.HOME_PATH, PROJECT_XML);
        if (!Files.exists(projectRegisterPath)) {
            log.error("Not able to get globally registered projects.");
            return;
        }
        final Document document = XmlService.writeRegister(projects);
        FileService.write(projectRegisterPath, document);
    }

    public static void saveRegistered() {
        writeProjectIndex(indexEntries);
    }

}
