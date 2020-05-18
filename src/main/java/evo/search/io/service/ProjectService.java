package evo.search.io.service;

import evo.search.Main;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.Experiment;
import evo.search.io.entities.IndexEntry;
import evo.search.io.entities.Project;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public class ProjectService {

    private static final String PROJECT_LEVEL_HIDDEN = ".evo";
    private static final String CONFIG = "config.json";
    private static final String PROJECT_SETTING = "settings.xml";
    private static final String CONFIG_XML = "config.xml";
    private static final String PROJECT_XML = "project.xml";
    private static final String CONFIG_FOLDER = "configs";

    @Getter
    private static final List<IndexEntry> indexEntries = new ArrayList<>();

    @Setter
    @Getter
    private static Project currentProject;

    public static Configuration getCurrentConfiguration() {
        int index = currentProject.getSelectedConfiguration();
        List<Configuration> configurations = currentProject.getConfigurations();
        if (index < 0 || index >= configurations.size()) {
            return null;
        }
        return configurations.get(index);
    }

    public static void addProjectEntry(Project project) {
        indexEntries.add(new IndexEntry(project.getPath(), project.getName(), project.getVersion(), LocalDateTime.now()));
    }

    public static boolean setupNewProject(File file, Project project) {
        if (file == null || !file.isDirectory()) {
            log.error("Project destination is no directory.");
            return false;
        }
        if (containsHidden(file)) {
            log.error("Project destination is already set up.");
            return false;
        }
        final String hiddenPath = file.getPath() + File.separator + PROJECT_LEVEL_HIDDEN;
        if (FileService.getDir(hiddenPath) == null) {
            log.error("Project setup didn't complete.");
        }
        File projectXmlFile = FileService.getFile(hiddenPath + File.separator + PROJECT_SETTING);
        if (projectXmlFile == null) {
            log.error("Project setup didn't complete.");
            return false;
        }
        FileService.write(projectXmlFile, DocumentHelper.createDocument(project.serialize()));
        return true;
    }

    public static File getProjectsIndexFile() {
        return FileService.getFile(Main.HOME_PATH + File.separator + PROJECT_XML);
    }

    public static boolean isSetUp() {
        File configXml = new File(Main.HOME_PATH + File.separator + PROJECT_XML);
        return configXml.exists();
    }

    public static Project loadProjectFromDirectory(File projectDirectory) {
        if (!projectDirectory.isDirectory()) {
            log.error("Project file is no directory.");
            return null;
        }
        final File hiddenFile = getHiddenFile(projectDirectory);
        if (!hiddenFile.exists()) {
            log.error("No " + PROJECT_LEVEL_HIDDEN + " file found.");
            return null;
        }

        Project project = new Project();

        if (hiddenFile.list() != null) {
            for (final String evoFileName : Objects.requireNonNull(hiddenFile.list())) {
                switch (evoFileName) {
                    case PROJECT_SETTING:
                        File projectSettingsFile = new File(hiddenFile.toPath() + File.separator + PROJECT_SETTING);
                        final Document projectSettings = FileService.read(projectSettingsFile);
                        project.parse(projectSettings.getRootElement());
                        break;
                    case CONFIG_FOLDER:
                        final File configDirectory = new File(hiddenFile.toPath() + File.separator + CONFIG_FOLDER);
                        final String[] configFilesList = configDirectory.list();
                        if (configFilesList == null) {
                            break;
                        }
                        for (final String configFileName : configFilesList) {
                            final File configFile = new File(configDirectory.getPath() + File.separator + configFileName);
                            final Document configDocument = FileService.read(configFile);
                            project.getConfigurations().add(Configuration.parseConfiguration(configDocument.getRootElement()));
                        }

                }
            }
        }
        return project;
    }

    @NotNull
    private static File getHiddenFile(final File projectFolder) {
        return new File(projectFolder.toPath() + File.separator + PROJECT_LEVEL_HIDDEN);
    }

    public static void saveConfigurations(File projectFolder, List<Configuration> configurations) {
        if (!projectFolder.exists()) {
            log.error("Cannot save configurations: Project folder does not exists: {}", projectFolder);
            EventService.LOG.trigger("Cannot save configurations: Project folder does not exists.");
            return;
        }

        if (!containsHidden(projectFolder)) {
            EventService.LOG.trigger("Cannot save configurations.");
        }
        File hiddenFile = getHiddenFile(projectFolder);

        File configFolder = FileService.getDir(hiddenFile.getPath() + File.separator + CONFIG_FOLDER);

        if (configFolder == null) {
            return;
        }
        FileService.clearFolder(configFolder);
        FileService.save(configFolder, configurations);
    }

    public static boolean isProjectRegistered(Project project) {
        return indexEntries.stream()
                .map(IndexEntry::getPath)
                .filter(Objects::nonNull)
                .anyMatch(path -> path.equals(project.getPath()));
    }

    public static boolean containsHidden(File file) {
        final String[] list = file.list();
        if (list != null && Arrays.asList(list).contains(".evo")) {
            return true;
        }
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
        File projectsRegisterFile = getProjectsIndexFile();
        if (projectsRegisterFile == null) {
            log.error("Not able to get globally registered projects.");
            return;
        }
        Document parsedDocument = FileService.read(projectsRegisterFile);
        indexEntries.addAll(XmlService.readRegister(parsedDocument));
    }

    public static void writeProjectIndex(List<IndexEntry> projects) {
        File projectsRegisterFile = getProjectsIndexFile();
        if (projectsRegisterFile == null) {
            log.error("Not able to get globally registered projects.");
            return;
        }
        Document document = XmlService.writeRegister(projects);
        FileService.write(projectsRegisterFile, document);
    }


    public static void saveRegistered() {
        writeProjectIndex(indexEntries);
    }

    public static void saveNewExperiment(Experiment experiment) {
        File workingDirectory = currentProject.getWorkingDirectory();
        String name = URLEncoder.encode(experiment.getConfiguration().getName(), StandardCharsets.UTF_8);
        File file = getAvailableFile(workingDirectory.toPath() + File.separator + name + "%s.xml");
        saveExperiment(file, experiment);
    }

    public static void saveExperiment(File file, Experiment experiment) {
        FileService.write(file, DocumentHelper.createDocument(experiment.serialize()));
    }

    public static List<Experiment> loadExperiments() {
        return Arrays.stream(getExperimentFilesList())
                .map(filename -> currentProject.getWorkingDirectory() + File.separator + filename)
                .map(File::new)
                .sorted(Comparator.comparingLong(File::lastModified))
                .map(ProjectService::loadExperiment)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static List<Experiment> loadExperiments(String[] list, Consumer<Integer> progress) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        return Arrays.stream(list)
                .map(filename -> currentProject.getWorkingDirectory() + File.separator + filename)
                .map(File::new)
                .sorted(Comparator.comparingLong(File::lastModified))
                .map(file -> {
                    Experiment experiment = loadExperiment(file);
                    progress.accept(atomicInteger.incrementAndGet());
                    return experiment;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static String[] getExperimentFilesList() {
        return currentProject.getWorkingDirectory().list((dir, name) -> name.endsWith(".xml"));
    }

    public static Experiment loadExperiment(File file) {
        Document experimentDocument = FileService.read(file);
        Experiment experiment = new Experiment(null);
        if (experimentDocument.getRootElement() == null) {
            log.error("No root node found in experiment file " + file.getName());
            return null;
        }
        experiment.parse(experimentDocument.getRootElement());
        return experiment;
    }

    private static File getAvailableFile(String name) {
        String initial = String.format(name, "");
        File file = new File(initial);
        int i = 0;
        while (file.exists()) {
            file = new File(String.format(name, i));
            i++;
        }

        return file;
    }
}
