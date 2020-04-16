package evo.search.io;

import evo.search.Configuration;
import evo.search.Environment;
import evo.search.view.LangService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * File prompting and loading service.
 */
@Slf4j
public class FileService {
    /**
     * Prompt the user via dialog for a file to load from.
     *
     * @return File to load from.
     */
    public static File promptForLoad() {
        JFrame parent = new JFrame();
        FileDialog fileDialog = new FileDialog(parent, "Save experiment", FileDialog.LOAD);
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null || !fileName.endsWith(".json")) {
            return null;
        }
        return new File(fileDialog.getDirectory() + fileName);
    }

    /**
     * Prompt the user via dialog for a file to save to.
     *
     * @return File to save to.
     */
    public static File promptForSave() {
        JFrame parent = new JFrame();

        //TODO: i18n for dialogs
        FileDialog fileDialog = new FileDialog(parent, LangService.get("environment.save"), FileDialog.SAVE);
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return null;
        }
        if (!fileName.endsWith(".json")) {
            fileName += ".json";
        }
        File selectedFile = new File(fileDialog.getDirectory() + fileName);
        try {
            selectedFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return selectedFile;
    }

    /**
     * Load an {@link Environment} from a given {@link File}.
     *
     * @param file File which contains the json representation of an {@link Environment}.
     */
    public static void loadExperiment(File file) {
        try {
            final String jsonString = Files.readString(file.toPath());
            final Configuration configuration = JsonService.readConfiguration(new JSONObject(jsonString));
            Environment.getInstance().setConfiguration(configuration);
            EventService.LOG_LABEL.trigger(LangService.get("environment.loaded"));
            EventService.REPAINT_CANVAS.trigger();
        } catch (IOException e) {
            EventService.LOG.trigger(e.getLocalizedMessage());
            log.error(LangService.get("could.not.read.file"), e);
        } catch (JSONException e) {
            EventService.LOG.trigger(LangService.get("environment.config.broken") + ": " + e.getLocalizedMessage());
            EventService.LOG_LABEL.trigger(LangService.get("environment.config.broken"));
        }
    }

    /**
     * Save an {@link Environment} to a given {@link File}.
     *
     * @param file          File to save the json representation of an {@link Environment} to.
     * @param configuration Configuration to create a json representation from.
     */
    public static void saveConfiguration(File file, Configuration configuration) {
        try {
            final JSONObject jsonObject = JsonService.write(configuration);
            Files.write(
                    file.toPath(),
                    jsonObject.toString().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            EventService.LOG_LABEL.trigger(LangService.get("environment.loaded"));
        } catch (IOException | JSONException e) {
            log.error(LangService.get("could.not.write.file"), e);
            EventService.LOG.trigger(LangService.get("could.not.write.file") + ": " + e.getLocalizedMessage());
        }

    }
}
