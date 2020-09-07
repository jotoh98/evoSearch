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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tests for the multiple treasure fitness function.
 *
 * @author jotoh
 */
@Slf4j
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
                log.error("Could not open the csv writer.", e);
            }
        } catch (final IOException e) {
            log.error("Could not open the csv file stream.", e);
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
            final List<DiscreteGene> treasureSublist = treasures.subList(0, treasureAmount);

            final List<List<DiscreteGene>> results = getResultMatrix(writer, baseConfiguration, treasureSublist);

            final List<List<DiscreteGene>> transposed = ListUtils.transpose(results);
            final List<List<Point2D>> comparedPoints = ListUtils.map(transposed, row -> ListUtils.map(row, DiscreteGene::getAllele));

            writeDeviations(writer, treasureAmount, comparedPoints);
        }
    }

    /**
     * Writes the standard deviations for the x- and y-coordinates of
     * the points in a row of the supplied point matrix.
     *
     * @param writer         csv writer to output the deviations
     * @param treasureAmount amount of treasures
     * @param pointMatrix    matrix of points
     */
    private void writeDeviations(final CSVWriter writer, final int treasureAmount, final List<List<Point2D>> pointMatrix) {
        final List<String> deviationX = computePointMatrixDeviation(pointMatrix, Point2D::getX);

        deviationX.addAll(0, List.of(
                Integer.toString(treasureAmount),
                "",
                "Deviation x"
        ));

        final List<String> deviationY = computePointMatrixDeviation(pointMatrix, Point2D::getY);

        deviationY.addAll(0, List.of(
                Integer.toString(treasureAmount),
                "",
                "Deviation y"
        ));

        writer.writeNext(deviationX.toArray(String[]::new));
        writer.writeNext(deviationY.toArray(String[]::new));
    }

    /**
     * Complete an amount of evolutions and return a matrix of the best phenotypes points.
     * One row of the matrix corresponds to one individual.
     *
     * @param writer        csv writer to output the matrix
     * @param configuration configuration to use for each evolution
     * @param treasures     treasures to use in the configuration
     * @return matrix of individual phenotype points
     */
    @NotNull
    private List<List<DiscreteGene>> getResultMatrix(final CSVWriter writer, final Configuration configuration, final List<DiscreteGene> treasures) {
        final int amount = treasures.size();
        final List<List<DiscreteGene>> results = new ArrayList<>();
        final CompletableFuture<?>[] futures = new CompletableFuture[20];
        for (int iteration = 0; iteration < 20; iteration++) {
            final int runIndex = iteration;
            final Configuration clone = configuration.clone();
            clone.setTreasures(treasures);
            Collections.shuffle(clone.getDistances());
            final Evolution evolution = Evolution.builder().configuration(clone).build();

            final CompletableFuture<Void> future = CompletableFuture
                    .runAsync(evolution)
                    .thenRun(() -> {
                        System.out.printf("Run %d:%d finished\n", evolution.getConfiguration().getTreasures().size(), runIndex);


                        final List<DiscreteGene> result = bestPhenotype(evolution);
                        results.add(result);

                        final List<String> distances = result.stream().map(DiscreteGene::getDistance).map(d -> Double.toString(d)).collect(Collectors.toList());
                        final List<String> positions = result.stream().map(DiscreteGene::getPosition).map(d -> Integer.toString(d)).collect(Collectors.toList());

                        distances.addAll(0, List.of(
                                Integer.toString(amount),
                                Integer.toString(runIndex),
                                "Distances"

                        ));
                        positions.addAll(0, List.of(
                                Integer.toString(amount),
                                Integer.toString(runIndex),
                                "Positions"
                        ));

                        synchronized (writer) {
                            writer.writeNext(distances.toArray(String[]::new));
                            writer.writeNext(positions.toArray(String[]::new));
                        }
                    });
            futures[iteration] = future;
        }

        CompletableFuture.allOf(futures).join();
        return results;
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
                .build();
    }
}
