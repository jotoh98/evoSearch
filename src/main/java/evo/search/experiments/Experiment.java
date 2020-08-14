package evo.search.experiments;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class Experiment {
    public static void printProgress(int progress, final int amount) {
        progress = Math.min(amount, progress);

        System.out.println("|" + "-".repeat(progress) + " ".repeat(amount - progress) + "|\r");
    }

    public static File getFile(final String name, final String extension) {
        String filename = name;
        if (Files.exists(Path.of(filename + extension))) {
            int i = 0;
            String formatted;
            while (Files.exists(Path.of(formatted = String.format("%s-%d%s", filename, i, extension))))
                i++;
            filename = formatted;
        } else filename += extension;
        return new File(filename);
    }
}
