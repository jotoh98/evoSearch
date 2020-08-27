package evo.search.experiments;

import com.opencsv.CSVWriter;
import evo.search.Evolution;
import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.ga.mutators.*;
import evo.search.io.entities.Configuration;
import evo.search.util.ListUtils;
import evo.search.util.RandomUtils;
import io.jenetics.Chromosome;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.RandomRegistry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author jotoh
 */
public class MultiTreasureFitnessExperiment extends Experiment {
    private static void writeResultCSV(final CSVWriter writer, final List<DiscretePoint> treasures, final int iteration, final int treasureAmount, final EvolutionResult<DiscreteGene, Double> result) {
        final Chromosome<DiscreteGene> chromosome = result.bestPhenotype().genotype().chromosome();


        final List<String> positions = chromosome.stream()
                .map(DiscreteGene::getPosition)
                .map(position -> Integer.toString(position))
                .collect(Collectors.toList());
        final List<String> distances = chromosome.stream()
                .map(DiscreteGene::getDistance)
                .map(distance -> Double.toString(distance))
                .collect(Collectors.toList());

        final AtomicInteger index = new AtomicInteger();
        final List<String> distancesToTreasure = chromosome.stream()
                .map(DiscreteGene::getAllele)
                .map(point -> point.distance(treasures.get(
                        index.getAndIncrement()
                )))
                .map(distance -> Double.toString(distance))
                .collect(Collectors.toList());

        final String firstIdentifier = Integer.toString(treasureAmount);
        final String secondIdentifier = Integer.toString(iteration);

        addRowIdentifier(positions, "Position", firstIdentifier, secondIdentifier);
        addRowIdentifier(distances, "Distance", firstIdentifier, secondIdentifier);
        addRowIdentifier(distancesToTreasure, "Min Distance to Treasure", firstIdentifier, secondIdentifier);

        distances.add(Double.toString(result.bestFitness()));

        writer.writeNext(positions.toArray(String[]::new));
        writer.writeNext(distances.toArray(String[]::new));
        writer.writeNext(distancesToTreasure.toArray(String[]::new));
    }

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

    @Override
    public void accept(final String[] args) {
        final String fileName = parseFileName(args);

        final long seed = parseSeed(args);
        if (seed > -1)
            RandomRegistry.random(new Random(1234));

        System.out.println("Starting " + getClass().getSimpleName());

        try (final OutputStream outputStream = Files.newOutputStream(uniquePath(fileName, ".csv"))) {

            final Configuration baseConfiguration = Configuration.builder()
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
                    .limit(1000)
                    .population(50)
                    .positions(6)
                    .offspring(15)
                    .survivors(20)
                    .build();


            try (final CSVWriter writer = createCSVWriter(outputStream)) {

                final List<DiscretePoint> treasures = ListUtils.generate(2, () -> RandomUtils.generatePoint(6, 10, 50));

                final ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();

                final HashMap<Integer, List<List<Double>>> distanceMap = new HashMap<>();

                for (int amount = 2; amount <= treasures.size(); amount++) {
                    final int finalAmount = amount;
                    distanceMap.put(amount, new ArrayList<>());
                    for (int iteration = 1; iteration <= 100; iteration++) {
                        final int finalIteration = iteration;

                        final Configuration clone = baseConfiguration.clone();
                        clone.setTreasures(treasures.subList(0, amount));
                        Collections.shuffle(clone.getDistances());

                        final Evolution evolution = Evolution.builder()
                                .configuration(clone)
                                .build();

                        final int treasureAmount = evolution.getConfiguration().getTreasures().size();

                        final CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                            evolution.run();
                            return evolution.getHistory()
                                    .stream()
                                    .min(Comparator.comparingDouble(EvolutionResult::bestFitness))
                                    .orElse(evolution.getHistory().get(evolution.getHistory().size() - 1));
                        }).thenAccept(result -> {
                            System.out.printf("Run %d:%d finished%n", treasureAmount, finalIteration);

                            final List<Double> distances = result.bestPhenotype().genotype().chromosome().stream()
                                    .map(DiscreteGene::getDistance)
                                    .collect(Collectors.toList());

                            distanceMap.get(finalAmount).add(distances);
                        });
                        futures.add(future);
                    }
                }
                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();


                for (int amount = 2; amount <= treasures.size(); amount++) {
                    final List<List<Double>> runs = distanceMap.get(amount);

                    final List<String> variances = ListUtils.transpose(runs)
                            .stream()
                            .map(ListUtils::variance)
                            .map(Math::sqrt)
                            .map(v -> Double.toString(v))
                            .collect(Collectors.toList());

                    variances.add(0, Integer.toString(amount));
                    writer.writeNext(variances.toArray(String[]::new));
                }

            }

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a list of treasure points to a csv file.
     *
     * @param writer    csv writer
     * @param treasures list of treasure points
     */
    private void writeTreasuresToCSV(final CSVWriter writer, final List<DiscretePoint> treasures) {
        final List<String> treasurePositions = treasures.stream().map(DiscretePoint::getPosition).map(pos -> Integer.toString(pos)).collect(Collectors.toList());
        final List<String> treasureDistances = treasures.stream().map(DiscretePoint::getDistance).map(pos -> Double.toString(pos)).collect(Collectors.toList());

        addRowIdentifier(treasurePositions, "Treasure-Position", "", "");
        addRowIdentifier(treasureDistances, "Treasure-Distance", "", "");

        writer.writeNext(treasurePositions.toArray(String[]::new));
        writer.writeNext(treasureDistances.toArray(String[]::new));

        writer.writeNext(new String[]{""});
    }

    /**
     * Run the incremental amount of treasures test. Calculates the variance between the points resulting from the
     * evolution per iteration.
     *
     * @param configuration    configuration to test
     * @param varianceConsumer consumer of the amount of treasures and the resulting variances
     * @return completable future of all amount iterations run
     */
    private CompletableFuture<Void> incrementalAmountTest(final Configuration configuration, final BiConsumer<Integer, List<DiscreteChromosome>> varianceConsumer) {
        final Configuration clone = configuration.clone();

        clone.getTreasures().add(
                RandomUtils.generatePoint(6, 10, 50)
        );

        final ArrayList<CompletableFuture<Void>> voidFutures = new ArrayList<>();

        for (int amount = 2; amount < 3; amount++) {
            final int finalAmount = amount;
            voidFutures.add(joinFutures(
                    createIterationFutures(clone),
                    results -> varianceConsumer.accept(finalAmount, results)
            ));
        }

        return CompletableFuture.allOf(voidFutures.toArray(CompletableFuture[]::new));
    }


    /**
     * Calculate the variance of the chromosomes point positions.
     *
     * @param individuals list of chromosomes
     * @return variance vector between the points at the same index of the individuals
     */
    private List<Vector2D> calculateVariance2D(final List<DiscreteChromosome> individuals) {

        if (individuals == null) return Collections.emptyList();

        final AtomicReference<Vector2D> expectedValue = new AtomicReference<>();

        return individuals.stream()
                .map(chromosome -> chromosome.getGenes()
                        .map(DiscreteGene::getAllele)
                        .map(DiscretePoint::toPoint2D)
                        .map(point2D -> new Vector2D(point2D.getX(), point2D.getY()))
                        .asList()
                )
                .peek(vectors -> expectedValue.set(calculateExpectedValue2D(vectors)))
                .map(vectorList ->
                        vectorList.stream()
                                .map(vector2D -> vector2D.subtract(expectedValue.get()))
                                .map(vector2D -> vector2D.map(a -> a * a))
                                .collect(Collectors.toList())
                )
                .reduce((vectorListA, vectorListB) ->
                        IntStream.range(0, vectorListA.size())
                                .mapToObj(i -> vectorListA.get(i).add(vectorListB.get(i)))
                                .map(vector2D -> vector2D.divide(vectorListA.size()))
                                .collect(Collectors.toList())
                )
                .orElse(Collections.emptyList());

    }

    /**
     * Calculate the expected value for a list of vectors.
     *
     * @param vectors list of vectors
     * @return expected value vector
     */
    private Vector2D calculateExpectedValue2D(final List<Vector2D> vectors) {
        if (vectors.size() == 0)
            return new Vector2D();

        return vectors.stream()
                .reduce(Vector2D::add)
                .orElse(new Vector2D())
                .divide(vectors.size());
    }

    /**
     * Adds a treasure to the given configuration returns a completable future
     * running 100 evolutions.
     *
     * @param configuration configuration to add the treasure to and run
     * @return completable future running the evolution
     */
    private ArrayList<CompletableFuture<DiscreteChromosome>> createIterationFutures(final Configuration configuration) {
        final ArrayList<CompletableFuture<DiscreteChromosome>> futures = new ArrayList<>();

        configuration.getTreasures().add(RandomUtils.generatePoint(6, 10, 50));

        System.out.println("Running with " + configuration.getTreasures().size() + " treasures");
        for (int iteration = 0; iteration < 100; iteration++) {
            final Configuration clone = configuration.clone();
            final int finalIteration = iteration;
            final Evolution evolution = Evolution.builder()
                    .configuration(clone)
                    .progressConsumer(integer -> printProgress(finalIteration * integer, 100000))
                    .build();
            Collections.shuffle(clone.getDistances());
            futures.add(CompletableFuture.supplyAsync(() -> {
                evolution.run();
                return (DiscreteChromosome) evolution.getResult().chromosome();
            }));
        }
        return futures;
    }
}
