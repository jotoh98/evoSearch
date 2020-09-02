package evo.search.experiments;

import com.opencsv.CSVWriter;
import evo.search.Evolution;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.mutators.DistanceMutator;
import evo.search.ga.mutators.PositionMutator;
import evo.search.ga.mutators.SwapGeneMutator;
import evo.search.ga.mutators.SwapPositionsMutator;
import evo.search.io.entities.Configuration;
import evo.search.io.service.FileService;
import evo.search.util.ListUtils;
import evo.search.util.RandomUtils;
import io.jenetics.util.RandomRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Test of the {@link Evolution.Fitness#SINGULAR} fitness.
 */
public class OneTreasureFitnessExperiment extends Experiment {

    /**
     * Main test.
     *
     * @param args cli args (ignored)
     */
    public void accept(final String[] args) {
        System.out.println("Beginning with experiment: One Treasure Fitness");
        System.out.println("Shuffle treasures...");

        final String filename = parseFileName(args);

        try (final OutputStream outputStream = Files.newOutputStream(FileService.uniquePath(filename, ".csv"))) {

            final int positions = 4;
            final int limit = 1000;

            final DiscreteGene treasure = RandomUtils.generatePoint(positions, 5, 10);

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

            final AtomicInteger progress = new AtomicInteger();

            final List<CompletableFuture<DiscreteChromosome>> futures = IntStream
                    .range(0, 100)
                    .peek(value -> Collections.shuffle(distances))
                    .mapToObj(index -> CompletableFuture
                            .supplyAsync(() -> Configuration.builder()
                                    .positions(positions)
                                    .limit(limit)
                                    .distances(new ArrayList<>(distances))
                                    .alterers(List.of(
                                            new SwapGeneMutator(0.02),
                                            new SwapPositionsMutator(0.02),
                                            new DistanceMutator(0.01),
                                            new PositionMutator(0.015)
                                    ))
                                    .fitness(Evolution.Fitness.SINGULAR)
                                    .treasures(List.of(treasure))
                                    .build()
                            )
                            .thenApply(configuration -> Evolution.builder()
                                    .configuration(configuration)
                                    .progressConsumer(p -> printProgress(progress.incrementAndGet(), limit * 100))
                                    .build())
                            .thenApply(evolution -> {
                                evolution.run();
                                return (DiscreteChromosome) evolution.getResult().chromosome();
                            })
                    )
                    .peek(future -> System.out.println("Thread started"))
                    .collect(Collectors.toList());


            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {


                try (final CSVWriter csvWriter = createCSVWriter(outputStream)) {

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

                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }).join();

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
