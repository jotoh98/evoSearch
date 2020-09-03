package evo.search.experiments;

import com.opencsv.CSVWriter;
import evo.search.Evolution;
import evo.search.ga.DiscreteGene;
import evo.search.ga.mutators.*;
import evo.search.io.entities.Configuration;
import evo.search.io.service.FileService;
import evo.search.util.ListUtils;
import evo.search.util.RandomUtils;
import io.jenetics.Chromosome;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tests for the multiple treasure fitness function.
 *
 * @author jotoh
 */
public class MultiTreasureFitnessExperiment extends Experiment {

    /**
     * Adds a name and two identifiers in front of a row.
     *
     * @param stringRow   row of string
     * @param name        name of the row
     * @param firstIdent  first row identifier
     * @param secondIdent second row identifier
     */
    private static void addRowIdentifier(final List<String> stringRow, final String name, final String firstIdent, final String secondIdent) {
        stringRow.addAll(0, List.of(name, firstIdent, secondIdent));
    }

    /**
     * Get the phenotype of the best individual from the evolution.
     *
     * @param evolution finished evolution with trained individual
     * @return phenotypical list of points of the best individual
     */
    @NotNull
    private static List<DiscreteGene> bestPhenotype(final Evolution evolution) {
        final EvolutionResult<DiscreteGene, Double> best = Collections.min(
                evolution.getHistory(),
                Comparator.comparingDouble(EvolutionResult::bestFitness)
        );
        final Chromosome<DiscreteGene> chromosome = best
                .bestPhenotype()
                .genotype()
                .chromosome();
        return ISeq.of(chromosome).asList();
    }

    @Override
    public void accept(final String[] args) {
        final String fileName = parseFileName(args);

        final long seed = parseSeed(args);
        if (seed > -1)
            RandomRegistry.random(new Random(1234));

        System.out.println("Starting " + getClass().getSimpleName());

        try (final OutputStream outputStream = Files.newOutputStream(FileService.uniquePath(fileName, ".csv"))) {


            try (final CSVWriter writer = createCSVWriter(outputStream)) {

                iterativeTreasureTest(writer);

            } catch (final IOException e) {
                e.printStackTrace();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Start the iterative treasure amount test.
     *
     * @param writer csv writer
     */
    private void iterativeTreasureTest(final CSVWriter writer) {
        final Configuration baseConfiguration = createConfiguration();

        final List<DiscreteGene> treasures = ListUtils.generate(15, () -> RandomUtils.generatePoint(6, 10, 50));

        for (int treasureAmount = 2; treasureAmount <= treasures.size(); treasureAmount += 2) {
            final int amount = treasureAmount;
            final List<List<DiscreteGene>> results = new ArrayList<>();
            final AtomicInteger runIndex = new AtomicInteger();

            final List<DiscreteGene> treasureSublist = treasures.subList(0, amount);
            final CompletableFuture<?>[] futures = new CompletableFuture[20];
            for (int iteration = 0; iteration < futures.length; iteration++) {
                final Configuration clone = baseConfiguration.clone();
                clone.setTreasures(treasureSublist);
                Collections.shuffle(clone.getDistances());
                final Evolution evolution = Evolution.builder().configuration(clone).build();

                futures[iteration] = CompletableFuture
                        .runAsync(evolution)
                        .thenRun(() -> {
                            System.out.printf("Run %d:%d finished\n", evolution.getConfiguration().getTreasures().size(), runIndex.getAndIncrement());


                            final List<DiscreteGene> result = bestPhenotype(evolution);
                            results.add(result);

                            final List<String> distances = result.stream().map(DiscreteGene::getDistance).map(d -> Double.toString(d)).collect(Collectors.toList());
                            final List<String> positions = result.stream().map(DiscreteGene::getPosition).map(d -> Integer.toString(d)).collect(Collectors.toList());

                            distances.addAll(0, List.of(
                                    Integer.toString(amount),
                                    Integer.toString(runIndex.get()),
                                    "Distances"

                            ));
                            positions.addAll(0, List.of(
                                    Integer.toString(amount),
                                    Integer.toString(runIndex.get()),
                                    "Positions"
                            ));

                            synchronized (writer) {
                                writer.writeNext(distances.toArray(String[]::new));
                                writer.writeNext(positions.toArray(String[]::new));
                            }
                        });
            }

            CompletableFuture.allOf(futures).join();

            final List<List<Point2D>> comparedPoints = new ArrayList<>();
            for (final List<DiscreteGene> geneList : ListUtils.transpose(results)) {
                final List<Point2D> pointList = new ArrayList<>();
                for (final DiscreteGene gene : geneList)
                    pointList.add(gene.getAllele());
                comparedPoints.add(pointList);
            }

            final List<String> deviationX = computePointMatrixDeviation(comparedPoints, Point2D::getX);

            deviationX.addAll(0, List.of(
                    Integer.toString(amount),
                    "",
                    "Deviation x"
            ));

            final List<String> deviationY = computePointMatrixDeviation(comparedPoints, Point2D::getY);

            deviationY.addAll(0, List.of(
                    Integer.toString(amount),
                    "",
                    "Deviation y"
            ));

            writer.writeNext(deviationX.toArray(String[]::new));
            writer.writeNext(deviationY.toArray(String[]::new));
        }
    }

    /**
     * Compute the standard deviations of the matrix's columns and map them to string.
     *
     * @param pointMatrix      matrix of points
     * @param coordinateSupply mapper to get a coordinate from a point
     * @return list of stringified standard deviations of the matrix's columns
     */
    @NotNull
    private List<String> computePointMatrixDeviation(final List<List<Point2D>> pointMatrix, final Function<Point2D, Double> coordinateSupply) {
        return pointMatrix
                .stream()
                .map(points -> points
                        .stream()
                        .map(coordinateSupply)
                        .collect(Collectors.toList())
                )
                .map(ListUtils::variance)
                .map(Math::sqrt)
                .map(x -> Double.toString(x))
                .collect(Collectors.toList());
    }

    /**
     * Create the requested configuration for the first test.
     *
     * @return configuration
     */
    private Configuration createConfiguration() {
        return Configuration.builder()
                .alterers(List.of(
                        new SwapDistanceMutator(.02),
                        new SwapPositionsMutator(.02),
                        new SwapGeneMutator(.02),
                        new DistanceMutator(.03),
                        new PositionMutator(.03)
                ))
                .distanceMutationDelta(.5)
                .distances(
                        RandomRegistry.random()
                                .doubles()
                                .map(d -> 10 + 40 * d)
                                .limit(10)
                                .boxed()
                                .collect(Collectors.toList())
                )
                .fitness(Evolution.Fitness.MULTI)
                .limit(500)
                .positions(6)
                .population(20)
                .offspring(7)
                .survivors(7)
                .build();
    }
}
