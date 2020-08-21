package evo.search.experiments;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Basic experiment class providing utilities for experiments.
 */
@Slf4j
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
     * @param name name prefix to use
     * @param ext  file extension to use
     * @return file handler to a distinct file
     */
    public static Path uniquePath(final String name, final String ext) {
        String filename = name;
        try {
            final long count = Files.walk(Path.of("")).filter(
                    path -> path.getFileName().toString().matches(name + "-\\d+" + ext)
            ).count();
            if (count > 0)
                filename += "-" + count;
        } catch (final IOException e) {
            log.error("Could not find a unique path for " + name + ext, e);
            System.exit(1);
        }
        return Path.of(filename + ext);
    }

}
