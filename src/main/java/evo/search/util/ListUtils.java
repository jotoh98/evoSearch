package evo.search.util;

import io.jenetics.util.RandomRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
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

    public static <T, R> List<R> consecutive(final List<T> list, BiFunction<T, T, R> mapping) {
        return IntStream.range(0, list.size() - 1)
                .mapToObj(index -> mapping.apply(list.get(index), list.get(index + 1)))
                .collect(Collectors.toList());
    }

    public static <T> void consecutive(final List<T> list, BiConsumer<T, T> consumer) {
        for (int index = 0; index < list.size() - 1; index++) {
            consumer.accept(list.get(index), list.get(index + 1));
        }
    }

    public static <T> List<T> from(final Iterator<T> iterator) {
        final ArrayList<T> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }

    public static <T> List<List<T>> transpose(final List<List<T>> matrix) {
        List<List<T>> ret = new ArrayList<>();
        final int N = matrix.get(0).size();
        for (int i = 0; i < N; i++) {
            List<T> col = new ArrayList<T>();
            for (List<T> row : matrix) {
                col.add(row.get(i));
            }
            ret.add(col);
        }
        return ret;
    }
}
