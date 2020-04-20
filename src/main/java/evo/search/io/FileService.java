package evo.search.io;

import evo.search.Environment;
import evo.search.io.entities.Configuration;
import evo.search.io.entities.Experiment;
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
    public static Experiment loadExperiment(File file) {
        try {
            final String jsonString = Files.readString(file.toPath());
            final Experiment experiment = JsonService.readExperiment(new JSONObject(jsonString));
            EventService.LOG_LABEL.trigger(LangService.get("environment.loaded"));
            EventService.REPAINT_CANVAS.trigger();
            return experiment;
        } catch (IOException e) {
            EventService.LOG.trigger(e.getLocalizedMessage());
            log.error(LangService.get("could.not.read.file"), e);
        } catch (JSONException e) {
            EventService.LOG.trigger(LangService.get("environment.config.broken") + ": " + e.getLocalizedMessage());
            EventService.LOG_LABEL.trigger(LangService.get("environment.config.broken"));
        }
        return null;
    }

    /**
     * Save an {@link Experiment} to a given {@link File}.
     *
     * @param file       File to save the json representation of the {@link Experiment} to.
     * @param experiment Experiment to save.
     */
    public static void saveExperiment(File file, Experiment experiment) {
        try {
            final JSONObject jsonObject = JsonService.write(experiment);
            Files.write(
                    file.toPath(),
                    jsonObject.toString().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException | JSONException e) {
            log.error(LangService.get("could.not.write.file"), e);
            EventService.LOG.trigger(LangService.get("could.not.write.file") + ": " + e.getLocalizedMessage());
        }
    }

    /**
     * Save an {@link Configuration} to a given {@link File}.
     *
     * @param file          File to save the json representation of the {@link Configuration} to.
     * @param configuration Configuration to save.
     */
    public static void saveConfiguration(File file, Configuration configuration) {

    }

    private static void save(File file, Object object) {
        try {
            final JSONObject jsonObject;
            if (object instanceof Configuration) {
                jsonObject = JsonService.write((Configuration) object);
            } else if (object instanceof Experiment) {
                jsonObject = JsonService.write((Experiment) object);
            } else {
                throw new IllegalArgumentException(LangService.get("unknown.data.type"));
            }
            Files.write(
                    file.toPath(),
                    jsonObject.toString().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException |
                JSONException | IllegalArgumentException e) {
            log.error(LangService.get("could.not.write.file"), e);
            EventService.LOG.trigger(LangService.get("could.not.write.file") + ": " + e.getLocalizedMessage());
        }
    }
}
