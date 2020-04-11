package evo.search.io;

import evo.search.Experiment;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileService {
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

    public static File promptForSave() {
        JFrame parent = new JFrame();
        FileDialog fileDialog = new FileDialog(parent, "Save experiment", FileDialog.SAVE);
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

    public static void loadExperiment(File file) {
        try {
            final String jsonString = Files.readString(file.toPath());
            JsonService.readExperiment(new JSONObject(jsonString));
        } catch (IOException e) {
            log.error("Could not read file.", e);
        }
    }

    public static void saveExperiment(File file, Experiment experiment) {
        final JSONObject jsonObject = JsonService.write(experiment);
        jsonObject.toString().getBytes();
        try {
            Files.write(
                    file.toPath(),
                    jsonObject.toString().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
            EventService.LOG_EVENT.trigger("Experiment loaded");
        } catch (IOException e) {
            log.error("Could not write file.", e);
        }

    }
}
