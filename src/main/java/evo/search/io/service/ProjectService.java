package evo.search.io.service;

import evo.search.Main;
import evo.search.io.FileService;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.Project;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

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

    @Getter
    private static final List<Project> projects = new ArrayList<>();

    public static Project setupProject(File file) {
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

    public static void setupGlobal() {
        final File file = new File(Main.HOME_PATH);
        if (!file.exists() && !file.mkdirs()) {
            log.error("Could not create directory '" + Main.HOME_PATH + "'");
            return;
        }

        final String projectsPath = Main.HOME_PATH + File.separator + "projects.json";

        final File projectsFile = new File(projectsPath);
        try {
            if (!projectsFile.exists() && !projectsFile.createNewFile()) {
                log.error("Could not create file '" + projectsPath + "'");
                return;
            }
        } catch (IOException e) {
            log.error("Could not create file '" + projectsPath + "'", e);
        }

        loadProjects();
    }

    public static void loadProjects() {
        projects.addAll(
                FileService.loadProjects(new File(Main.HOME_PATH + File.separator + "projects.json"))
        );
    }
}
