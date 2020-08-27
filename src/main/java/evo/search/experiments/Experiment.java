package evo.search.experiments;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

    public static <T> CompletableFuture<Void> joinFutures(final List<CompletableFuture<T>> futures, final Consumer<List<T>> consumer) {
        return CompletableFuture
                .allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    final List<T> results = futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.toList());
                    consumer.accept(results);
                });
    }

    @NotNull CSVWriter createCSVWriter(final OutputStream outputStream) {
        return new CSVWriter(new OutputStreamWriter(outputStream), ',', ' ', '"', "\n");
    }
}
