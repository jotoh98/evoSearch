package evo.search.io.service;

import evo.search.Main;
import evo.search.io.entities.Configuration;
import evo.search.view.LangService;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

/**
 * File prompting and loading service.
 */
@Slf4j
public class FileService {

    public static File promptForLoad(String title) {
        JFrame parent = new JFrame();
        FileDialog fileDialog = new FileDialog(parent, title, FileDialog.LOAD);
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return null;
        }
        return new File(fileDialog.getDirectory() + fileName);
    }

    public static File promptForDirectory(String title) {
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        final File loadDirectory = promptForLoad(title);

        if (loadDirectory != null && !loadDirectory.isDirectory()) {
            return null;
        }

        System.setProperty("apple.awt.fileDialogForDirectories", "false");
        return loadDirectory;
    }

    public static File promptForDirectory() {
        return promptForDirectory(LangService.get("load.directory"));
    }

    static void save(File configFolder, List<Configuration> configurations) {
        Hashtable<String, Integer> configurationNumber = new Hashtable<>();

        configurations.forEach(configuration -> {
            String fileName = configuration.getName();

            if (configurationNumber.containsKey(configuration.getName())) {
                configurationNumber.put(fileName, configurationNumber.get(fileName) + 1);
                fileName = fileName + configurationNumber.get(fileName).toString();
            }
            configurationNumber.putIfAbsent(fileName, 0);

            final String configFilePath = configFolder.getPath() + File.separator + fileName + ".xml";

            write(new File(configFilePath), configuration.serialize());
        });
    }

    public static void write(File file, Document document) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            document.write(fileWriter);
        } catch (IOException e) {
            log.error("Could not serialize document in XML file: " + file.getPath(), e);
        }
    }

    public static Document read(File file) {
        SAXReader reader = new SAXReader();
        Document document = DocumentHelper.createDocument();
        try {
            document = reader.read(file);
        } catch (DocumentException | MalformedURLException e) {
            log.error("Could not parse XML file: " + file.getPath(), e);
        }
        return document;
    }

    static void clearFolder(final File folder) {
        for (final String configFileName : Objects.requireNonNull(folder).list()) {
            try {
                Files.deleteIfExists(Path.of(folder.getPath(), configFileName));
            } catch (IOException ignored) {
            }
        }
    }

    static File getFile(String path) {
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

    static File getDir(String path) {
        final File file = new File(path);

        if (file.exists() || file.mkdirs()) {
            return file;
        }

        EventService.LOG.trigger("Could not create directory " + path);
        return null;
    }


    private static File getGlobalDir() {
        return getFile(Main.HOME_PATH);
    }
}
