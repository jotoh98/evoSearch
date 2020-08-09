package evo.search.experiments;

import com.opencsv.CSVWriter;
import evo.search.Evolution;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscretePoint;
import evo.search.io.entities.Configuration;
import evo.search.util.ListUtils;
import evo.search.util.RandomUtils;
import io.jenetics.util.RandomRegistry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OneTreasureFitnessExperiments extends Experiment {
    public static void main(final String[] args) {
        System.out.println("Beginning with experiment: One Treasure Fitness");
        System.out.println("Shuffle treasures...");

        Writer writer = null;

        String filename;
        if (args.length > 0)
            filename = args[0].replaceFirst(".\\w+$", "");
        else
            filename = "experiment";

        if (Files.exists(Path.of(filename + ".csv"))) {
            int i = 0;
            String formatted;
            while (Files.exists(Path.of(formatted = String.format("%s-%d.csv", filename, i))))
                i++;
            filename = formatted;
        } else filename += ".csv";

        try {
            writer = new FileWriter(new File(filename));
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final int positions = 4;
        final int limit = 1000;

        final DiscretePoint treasure = RandomUtils.generatePoint(positions, 5, 10);


        final List<Double> distances = ListUtils.generate(14,
                () -> RandomUtils.inRange(treasure.getDistance() - 5, treasure.getDistance() + 5)
        );

        for (int i = 0; i < distances.size(); i++) {
            final boolean canFindTreasure = distances.stream()
                    .anyMatch(distance -> distance > treasure.getDistance());
            if (canFindTreasure)
                continue;

            final int index = RandomRegistry.random().nextInt(distances.size());

            distances.set(index, treasure.getDistance() + 0.1);
        }

        final List<CompletableFuture<DiscreteChromosome>> futures = IntStream
                .range(0, 100)
                .peek(value -> Collections.shuffle(distances))
                .mapToObj(index -> CompletableFuture
                        .supplyAsync(() -> Configuration.builder()
                                .positions(positions)
                                .limit(limit)
                                .distances(new ArrayList<>(distances))
                                .treasures(List.of(treasure))
                                .build()
                        )
                        .thenApply(configuration -> Evolution.builder()
                                .configuration(configuration)
                                .build())
                        .thenApply(evolution -> {
                            evolution.run();
                            return (DiscreteChromosome) evolution.getResult().chromosome();
                        })
                )
                .peek(future -> System.out.println("Thread started"))
                .collect(Collectors.toList());

        final Writer finalWriter = writer;
        final CSVWriter csvWriter = new CSVWriter(finalWriter, ',', ' ', '"', "\n");

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {

            if (finalWriter == null)
                return;

            final List<List<String>> collected = futures.stream()
                    .map(CompletableFuture::join)
                    .map(genes -> genes.toSeq().asList())
                    .map(genes -> genes.stream()
                            .map(discreteGene -> Double.toString(discreteGene.getDistance()))
                            .collect(Collectors.toList())
                    )
                    .collect(Collectors.toList());

            collected.add(0, IntStream.range(1, distances.size() + 1).mapToObj(Integer::toString).collect(Collectors.toList()));

            final List<String[]> lines = ListUtils.transpose(collected).stream().map(strings -> strings.toArray(String[]::new)).collect(Collectors.toList());

            System.out.println("Distance " + treasure.getDistance());

            csvWriter.writeAll(lines);
            try {
                csvWriter.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }).join();
    }
}
