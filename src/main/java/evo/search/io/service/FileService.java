package evo.search.io.service;

import evo.search.Main;
import evo.search.io.entities.Configuration;
import evo.search.view.LangService;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * File prompting and loading service.
 */
@Slf4j
public class FileService {

    public static File promptForLoad(final String title) {
        final JFrame parent = new JFrame();
        final FileDialog fileDialog = new FileDialog(parent, title, FileDialog.LOAD);
        fileDialog.setVisible(true);
        final String fileName = fileDialog.getFile();
        if (fileName == null) {
            return null;
        }
        return new File(fileDialog.getDirectory() + fileName);
    }

    public static File promptForDirectory(final String title) {
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

    static void save(final File configFolder, final List<Configuration> configurations) {
        final HashMap<String, Integer> configurationNumber = new HashMap<>();

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

    public static void write(final File file, final Document document) {
        try (final FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(document.asXML().getBytes());
            fileOutputStream.flush();
        } catch (final IOException e) {
            log.error("Could not serialize document in XML file: " + file.getPath(), e);
        }
    }

    public static Document read(final File file) {
        final SAXReader reader = new SAXReader();
        try {
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (final SAXException ignored) {
        }
        Document document = DocumentHelper.createDocument();
        try {
            document = reader.read(file);
        } catch (final DocumentException | MalformedURLException e) {
            log.error("Could not parse XML file: " + file.getPath(), e);
        }
        return document;
    }

    static void clearFolder(final File folder) {
        for (final String configFileName : Objects.requireNonNull(folder).list()) {
            try {
                Files.deleteIfExists(Path.of(folder.getPath(), configFileName));
            } catch (final IOException ignored) {
            }
        }
    }

    static File getFile(final String path) {
        final File file = new File(path);

        try {
            if (file.exists() || file.createNewFile()) {
                return file;
            }
        } catch (final IOException e) {
            log.error("Could not create directory '" + path + "'", e);
        }

        return null;
    }

    static File getDir(final String path) {
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
