package evo.search.experiments;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Basic experiment class providing utilities for experiments.
 */
public abstract class Experiment {

    /**
     * Print a progress bar graph to the console.
     *
     * @param progress current amount of progress
     * @param amount   overall amount of work
     */
    public static void printProgress(int progress, final int amount) {
        progress = Math.min(amount, progress);

        progress *= 40.0 / amount;

        System.out.print("\b".repeat(44));
        System.out.print("|" + "#".repeat(progress) + " ".repeat(40 - progress) + "|");
    }

    /**
     * Get a distinct file from the file system.
     *
     * @param name      name prefix to use
     * @param extension file extension to use
     * @return file handler to a distinct file
     */
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
