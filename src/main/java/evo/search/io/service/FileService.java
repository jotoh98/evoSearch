package evo.search.io.service;

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
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

/**
 * File prompting and loading service.
 */
@Slf4j
public class FileService {

    /**
     * Prompt the user for a file path.
     *
     * @param title the prompts title
     * @return the chosen file's path from the prompt
     */
    public static Path promptForLoad(final String title) {
        final JFrame parent = new JFrame();
        final FileDialog fileDialog = new FileDialog(parent, title, FileDialog.LOAD);
        fileDialog.setVisible(true);
        final String fileName = fileDialog.getFile();
        if (fileName == null) {
            return null;
        }
        return Path.of(fileDialog.getDirectory(), fileName);
    }

    /**
     * Prompt the user for a directory.
     *
     * @param title the prompts title
     * @return the chosen directory's path from the prompt
     * @see #promptForLoad(String)
     */
    public static Path promptForDirectory(final String title) {
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        final Path loadDirectory = promptForLoad(title);

        if (loadDirectory == null || Files.exists(loadDirectory)) {
            return null;
        }

        System.setProperty("apple.awt.fileDialogForDirectories", "false");
        return loadDirectory;
    }

    /**
     * Shorthand for {@link #promptForDirectory(String)} with "Load directory" in the title.
     *
     * @return the chosen directory's path from the prompt
     * @see #promptForDirectory(String)
     * @see #promptForLoad(String)
     */
    public static Path promptForDirectory() {
        return promptForDirectory(LangService.get("load.directory"));
    }

    /**
     * Write a list of {@link Configuration}s to a folder.
     * File names which are not distinct are distinguished by a trailing number.
     *
     * @param configFolder   folder to save the configs in
     * @param configurations list of configurations to save
     */
    static void write(final Path configFolder, final List<Configuration> configurations) {
        final HashMap<String, Integer> configurationNumber = new HashMap<>();

        configurations.forEach(configuration -> {
            String fileName = configuration.getName();

            if (configurationNumber.containsKey(configuration.getName())) {
                configurationNumber.put(fileName, configurationNumber.get(fileName) + 1);
                fileName = fileName + configurationNumber.get(fileName).toString();
            }
            configurationNumber.putIfAbsent(fileName, 0);

            write(configFolder.resolve(fileName + ".xml"), configuration.serialize());
        });
    }

    /**
     * Write a xml {@link Document} to a file.
     *
     * @param file     the files path
     * @param document xml document to write
     */
    public static void write(final Path file, final Document document) {
        try {
            Files.write(file, document.asXML().getBytes());
        } catch (final IOException e) {
            log.error("Could not serialize document in XML file: " + file.toString(), e);
        }
    }

    /**
     * Read a xml {@link Document} from a file.
     *
     * @param file the files path
     * @return parsed xml document
     */
    public static Document read(final Path file) {
        final SAXReader reader = new SAXReader();
        try {
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (final SAXException ignored) {
        }
        Document document = DocumentHelper.createDocument();
        try {
            document = reader.read(file.toFile());
        } catch (final DocumentException | MalformedURLException e) {
            log.error("Could not parse XML file: " + file.toString(), e);
        }
        return document;
    }

    /**
     * Get a distinct file from the file system.
     *
     * @param name name prefix to use
     * @param ext  file extension to use
     * @return file handler to a distinct file
     */
    public static Path uniquePath(final String name, final String ext) {
        Path candidate = Path.of(name + ext);
        int count = 0;
        while (Files.exists(candidate))
            candidate = Paths.get(String.format("%s-%d%s", name, ++count, ext));
        return candidate;
    }

    /**
     * Counts the files matching the pattern prefix-number.suffix
     *
     * @param path   directory with files to count
     * @param prefix file name prefix
     * @param suffix file extension
     * @return amount of files matching the pattern
     */
    public static int counter(final Path path, final String prefix, final String suffix) {
        Path candidate = path.resolve(prefix + suffix);
        int count = 0;
        while (Files.exists(candidate))
            candidate = path.resolve(String.format("%s%d%s", prefix, ++count, suffix));
        return count;
    }
}
