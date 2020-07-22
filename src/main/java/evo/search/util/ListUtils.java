package evo.search.util;

import io.jenetics.util.RandomRegistry;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ListUtils {
    public static <T> List<T> generate(final int limit, final Supplier<T> supplier) {
        return IntStream.range(0, limit).mapToObj(value -> supplier.get()).collect(Collectors.toList());
    }

    public static <T> T chooseRandom(final List<T> list, final int min, final int max) {
        return list.get(RandomUtils.inRange(Math.max(0, min), Math.min(list.size() - 1, max)));
    }

    public static <T> T chooseRandom(final List<T> list) {
        return list.get(RandomRegistry.random().nextInt(list.size()));
    }

    public static <T, R> List<R> consecutive(final List<T> list, BiFunction<T, T, R> predicate) {
        return IntStream.range(0, list.size() - 1)
                .mapToObj(index -> predicate.apply(list.get(index), list.get(index + 1)))
                .collect(Collectors.toList());
    }
}
