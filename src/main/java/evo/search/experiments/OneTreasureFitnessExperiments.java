package evo.search.experiments;

import evo.search.Evolution;
import evo.search.ga.DiscreteGene;
import evo.search.ga.DiscretePoint;
import evo.search.io.entities.Configuration;
import evo.search.util.ListUtils;
import evo.search.util.RandomUtils;
import io.jenetics.Genotype;
import io.jenetics.util.RandomRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OneTreasureFitnessExperiments extends Experiment {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Beginning with experiment: One Treasure Fitness");
        System.out.println("Shuffle treasures...");
        ArrayList<DiscretePoint> treasures = new ArrayList<>();

        final int positions = 4;
        final int limit = 10;

        for (int i = 0; i < 10; i++)
            treasures.add(RandomUtils.generatePoint(positions, 5, 10));


        List<List<Double>> distances = treasures.stream().map(
                treasure -> ListUtils.generate(2 * positions,
                        () -> RandomUtils.inRange(treasure.getDistance() - 1, treasure.getDistance() + 1)
                )
        ).collect(Collectors.toList());

        for (int i = 0; i < distances.size(); i++) {
            boolean canFindTreasure = distances.get(i)
                    .stream()
                    .anyMatch(distance -> distance > treasures.get(0).getDistance());
            if (canFindTreasure)
                continue;

            int index = RandomRegistry.random().nextInt(distances.get(i).size());

            distances.get(i).set(index, treasures.get(i).getDistance() + 0.1);
        }

        System.out.println("Treasures shuffled.");
        Consumer<Integer> printConsumer = progress -> printProgress(progress, limit);

        List<Genotype<DiscreteGene>> collect = IntStream
                .range(0, treasures.size())
                .mapToObj(index -> Configuration.builder()
                        .positions(positions)
                        .limit(limit)
                        .distances(distances.get(index))
                        .treasures(List.of(treasures.get(index)))
                        .build()
                )
                .map(configuration -> Evolution.builder()
                        .configuration(configuration)
                        .progressConsumer(printConsumer)
                        .build()
                )
                .map(evolution -> {
                    evolution.run();
                    return evolution.getResult();
                })
                .collect(Collectors.toList());

        System.out.println(collect);

    }

    private static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        return CompletableFuture
                .allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }
}
