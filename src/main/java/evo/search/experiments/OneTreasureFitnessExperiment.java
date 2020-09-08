package evo.search.experiments;

import com.opencsv.CSVWriter;
import evo.search.Evolution;
import evo.search.ga.DiscreteGene;
import evo.search.ga.mutators.DistanceMutator;
import evo.search.ga.mutators.PositionMutator;
import evo.search.ga.mutators.SwapGeneMutator;
import evo.search.ga.mutators.SwapPositionsMutator;
import evo.search.io.entities.Configuration;
import evo.search.io.service.FileService;
import evo.search.util.ListUtils;
import evo.search.util.RandomUtils;
import io.jenetics.Chromosome;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

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
@Slf4j
public class OneTreasureFitnessExperiment extends Experiment {

    /**
     * Main test.
     *
     * @param args cli args (ignored)
     */
    public void accept(final String[] args) {
        log.info("Beginning with experiment: One Treasure Fitness");
        log.info("Shuffle treasures...");

        final String filename = parseFileName(args);

        try (final OutputStream outputStream = Files.newOutputStream(FileService.uniquePath(filename, ".csv"))) {

            final int positions = 4;
            final DiscreteGene treasure = RandomUtils.generatePoint(positions, 5, 10);
            final List<Double> distances = getDistances(treasure, 14);

            final List<Chromosome<DiscreteGene>> chromosomes = runSingularEvolutions(positions, treasure, distances);

            try (final CSVWriter csvWriter = createCSVWriter(outputStream)) {
                final List<List<String>> collected = ListUtils.map(
                        chromosomes,
                        chromosome -> ListUtils.map(
                                ISeq.of(chromosome).asList(),
                                gene -> Double.toString(gene.getDistance())
                        )
                );
                collected.add(0, IntStream.range(1, distances.size() + 1).mapToObj(Integer::toString).collect(Collectors.toList()));
                final List<String[]> lines = ListUtils.map(
                        ListUtils.transpose(collected),
                        strings -> strings.toArray(String[]::new)
                );
                System.out.println("Distance " + treasure.getDistance());
                csvWriter.writeAll(lines);

            } catch (final IOException e) {
                log.error("Could not open the csv writer.", e);
            }

        } catch (final IOException e) {
            log.error("Could not open the csv file.", e);
        }
    }

    /**
     * Create valid distances able to find the treasure.
     *
     * @param treasure treasure to find
     * @param amount   amount of distances to shuffle
     * @return shuffled distances
     */
    @NotNull
    private List<Double> getDistances(final DiscreteGene treasure, final int amount) {
        final List<Double> distances = ListUtils.generate(amount,
                () -> RandomUtils.inRange(treasure.getDistance() - 5, treasure.getDistance() + 5)
        );

        for (int i = 0; i < distances.size(); i++) {
            final boolean canFindTreasure = distances.stream()
                    .anyMatch(distance -> distance > treasure.getDistance());

            if (canFindTreasure) {continue;}

            final int index = RandomRegistry.random().nextInt(distances.size());

            distances.set(index, treasure.getDistance() + 0.1);
        }
        return distances;
    }

    /**
     * Run evolutions with shuffled distances and one treasure.
     *
     * @param positions amount of rays
     * @param treasure  single treasure point
     * @param distances distances used
     * @return list of evolution results
     */
    @NotNull
    private List<Chromosome<DiscreteGene>> runSingularEvolutions(final int positions, final DiscreteGene treasure, final List<Double> distances) {
        final AtomicInteger progress = new AtomicInteger();
        final int limit = 1000;
        final List<Chromosome<DiscreteGene>> chromosomes = new ArrayList<>();
        final CompletableFuture<?>[] futures = new CompletableFuture[100];
        for (int iteration = 0; iteration < futures.length; iteration++) {
            Collections.shuffle(distances);
            futures[iteration] = CompletableFuture
                    .supplyAsync(() -> Configuration.builder()
                            .positions(positions)
                            .limit(limit)
                            .distances(ListUtils.deepClone(distances, Double::doubleValue))
                            .alterers(List.of(
                                    new SwapGeneMutator(0.02),
                                    new SwapPositionsMutator(0.02),
                                    new DistanceMutator(0.02),
                                    new PositionMutator(0.02)
                            ))
                            .fitness(Evolution.Fitness.SINGULAR)
                            .treasures(List.of(treasure))
                            .build()
                    )
                    .thenApply(configuration -> Evolution.builder()
                            .configuration(configuration)
                            .progressConsumer(p -> printProgress(progress.incrementAndGet(), limit * 100))
                            .build())
                    .thenAccept(evolution -> {
                        evolution.run();
                        chromosomes.add(evolution.getHistory().get(evolution.getHistory().size() - 1).bestPhenotype().genotype().chromosome());
                    });
            System.out.println("Thread " + iteration + " started");
        }
        CompletableFuture.allOf(futures).join();
        return chromosomes;
    }

}
