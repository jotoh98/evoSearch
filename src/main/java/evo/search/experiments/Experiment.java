package evo.search.experiments;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Basic experiment class providing utilities for experiments.
 */
@Slf4j
public abstract class Experiment implements Consumer<String[]> {

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
     * Take the experiments arguments and parse the csv file name from the first argument.
     *
     * @param args list of arguments
     * @return csv file name
     */
    public static String parseFileName(final String[] args) {
        String filename = "";
        if (args.length > 0)
            filename = Path.of(args[0]).getFileName().toString();

        if (filename.isEmpty())
            filename = "experiment";

        return filename;
    }

    /**
     * Take the experiments arguments and parse the seed from the second argument.
     *
     * @param args list of arguments
     * @return random seed
     */
    public static long parseSeed(final String[] args) {
        if (args.length < 2)
            return -1;
        try {
            return Long.parseLong(args[1]);
        } catch (final NumberFormatException ignored) {
            return -1;
        }
    }

    /**
     * Create the standard formatted csv output writer.
     *
     * @param outputStream output stream for the csv writer
     * @return standard formatted csv output writer
     */
    @NotNull CSVWriter createCSVWriter(final OutputStream outputStream) {
        return new CSVWriter(new OutputStreamWriter(outputStream), ',', ' ', '"', "\n");
    }
}
