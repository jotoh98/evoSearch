package evo.search.io.service;

import evo.search.Main;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.Project;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Slf4j
public class ProjectService {

    private static final String HIDDEN = ".evo";
    private static final String CONFIG = "config.json";
    private static final String PROPERTIES = "pref.properties";
    private static final String CONFIG_XML = "config.xml";
    private static final String PROJECT_XML = "project.xml";

    @Getter
    private static final List<Project> projects = new ArrayList<>();

    public static Project newProject(File file) {
        final Project newProject = new Project("0", file.getPath(), "Untitled");

        final String hiddenPath = file.getPath() + File.separator + HIDDEN;
        new File(hiddenPath).mkdirs();
        Arrays.asList(CONFIG, PROPERTIES).forEach(name -> {
            final String filePAth = hiddenPath + File.separator + name;
            final File newFile = new File(filePAth);
            try {
                newFile.createNewFile();
            } catch (IOException e) {
                log.error("Could not create file during project setup.", e);
                //TODO: folder cleanup if one fails
            }
        });
        final Properties properties = new Properties();
        properties.setProperty("name", newProject.getName());
        properties.setProperty("version", newProject.getVersion());
        properties.setProperty("path", file.getPath());

        try {
            properties.store(new FileWriter(hiddenPath + File.separator + PROPERTIES), Main.APP_TITLE + " project");
        } catch (IOException e) {
            log.error("Could not create project properties during project setup.", e);
        }
        FileService.save(new File(hiddenPath + File.separator + CONFIG), Collections.emptyList());
        return newProject;
    }

    public static void addProject(Project project) {
        projects.add(project);
    }

    public static boolean setupNewProject(File file, Project project) {
        if (file == null || !file.isDirectory()) {
            log.error("Project destination is no directory.");
            return false;
        }
        if (file.list() == null || Objects.requireNonNull(file.list()).length != 0) {
            log.error("Project destination is not empty.");
            return false;
        }
        final String hiddenPath = file.getPath() + File.separator + HIDDEN;
        if (getDir(hiddenPath) == null) {
            log.error("Project setup didn't complete.");
        }
        File projectXmlFile = getFile(hiddenPath + File.separator + PROJECT_XML);
        if (projectXmlFile == null) {
            log.error("Project setup didn't complete.");
            return false;
        }
        Document document = XmlService.write(project);
        XmlService.write(projectXmlFile, document);

        return true;
    }

    private static File getFile(String path) {
        final File file = new File(path);

        try {
            if (file.exists() || file.createNewFile()) {
                return file;
            }
        } catch (IOException e) {
            log.error("Could not create directory '" + path + "'", e);
        }

        return null;
    }

    private static File getDir(String path) {
        final File file = new File(path);

        if (file.exists() || file.mkdirs()) {
            return file;
        }

        return null;
    }

    private static File getGlobalDir() {
        return getFile(Main.HOME_PATH);
    }

    public static File getProjectsRegisterFile() {
        return getFile(Main.HOME_PATH + File.separator + PROJECT_XML);
    }

    public static boolean isSetUp() {
        File configXml = new File(Main.HOME_PATH + File.separator + PROJECT_XML);
        return configXml.exists();
    }

    public static Project loadProject(File file) {
        final File hiddenFile = new File(file.toPath() + File.separator + HIDDEN);
        if (!hiddenFile.exists()) {
            return null;
        }

        String name = "Untitled";
        String version = "0";
        List<Configuration> configurations = new ArrayList<>();

        if (hiddenFile.list() != null) {
            for (final String evoFile : Objects.requireNonNull(hiddenFile.list())) {
                switch (evoFile) {
                    case PROPERTIES:
                        final Properties properties = new Properties();
                        try {
                            properties.load(new FileReader(hiddenFile.toPath() + File.separator + PROPERTIES));
                        } catch (IOException e) {
                            log.error("Projects properties could not be loaded", e);
                            return null;
                        }
                        name = properties.getProperty("name", "Untitled");
                        version = properties.getProperty("version", "0");
                        break;
                    case CONFIG:
                        configurations = FileService.loadConfigurations(new File(hiddenFile.toPath() + File.separator + CONFIG));
                }
            }
        }
        final Project project = new Project(version, file.getPath(), name);
        project.setConfigurations(configurations);
        return project;
    }

    public static boolean isProject(File file) {
        final String[] list = file.list();
        if (list != null) {
            return Arrays.asList(list).contains(".evo");
        }
        return false;
    }

    public static void setupService() {
        if (!isSetUp()) {
            writeProjectsRegister(Collections.emptyList());
        }
        readProjectsRegister();
    }

    public static void readProjectsRegister() {
        File projectsRegisterFile = getProjectsRegisterFile();
        if (projectsRegisterFile == null) {
            log.error("Not able to get globally registered projects.");
            return;
        }
        Document parsedDocument = XmlService.read(projectsRegisterFile);
        projects.addAll(XmlService.readRegister(parsedDocument));
    }

    public static void writeProjectsRegister(List<Project> projects) {
        File projectsRegisterFile = getProjectsRegisterFile();
        if (projectsRegisterFile == null) {
            log.error("Not able to get globally registered projects.");
            return;
        }
        Document document = XmlService.writeRegister(projects);
        XmlService.write(projectsRegisterFile, document);
    }

    public static void removeProjectRegister(Project project) {
        projects.remove(project);
    }

    public static void saveRegistered() {
        writeProjectsRegister(projects);
    }
}
