package evo.search.util;

import io.jenetics.util.RandomRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * List utility methods.
 */
public class ListUtils {

    /**
     * Generate a {@link List} with a given length through a supplier.
     *
     * @param limit    length of the new list
     * @param supplier supplier to retrieve the items from
     * @param <T>      list/supplier item type
     * @return a list with element generated by the given supplier
     */
    public static <T> List<T> generate(final int limit, final Supplier<T> supplier) {
        return IntStream.range(0, limit).mapToObj(value -> supplier.get()).collect(Collectors.toList());
    }

    /**
     * Choose a random item from the given list in the index range between min and max.
     *
     * @param list list to choose from
     * @param min  minimum index
     * @param max  maximum index
     * @param <T>  list element and return type
     * @return random element from the list between the indices min and max
     */
    public static <T> T chooseRandom(final List<T> list, final int min, final int max) {
        return list.get(RandomUtils.inRange(Math.max(0, min), Math.min(list.size() - 1, max)));
    }

    /**
     * Choose a random item from the given list.
     *
     * @param list list to choose from
     * @param <T>  list element and return type
     * @return random element from the list
     */
    public static <T> T chooseRandom(final List<T> list) {
        return list.get(RandomRegistry.random().nextInt(list.size()));
    }

    /**
     * Map a list of consecutive items in pairs of two to a new list.
     *
     * @param list    list of consecutive items
     * @param mapping mapping function
     * @param <T>     list item type
     * @param <R>     mapping return and list item return type
     * @return list of mapped together elements
     */
    public static <T, R> List<R> consecMap(final List<T> list, final BiFunction<T, T, R> mapping) {
        return IntStream.range(0, list.size() - 1)
                .mapToObj(index -> mapping.apply(list.get(index), list.get(index + 1)))
                .collect(Collectors.toList());
    }

    /**
     * Look at a list of consecutive items in pairs of two.
     *
     * @param list     list of consecutive items
     * @param consumer consumer working with two consecutive items
     * @param <T>      list item type
     */
    public static <T> void consec(final List<T> list, final BiConsumer<T, T> consumer) {
        for (int index = 0; index < list.size() - 1; index++) {
            consumer.accept(list.get(index), list.get(index + 1));
        }
    }

    /**
     * Generate a list from an iterator.
     *
     * @param iterator iterator supplying elements
     * @param <T>      element type
     * @return list of elements supplied by the iterator
     */
    public static <T> List<T> from(final Iterator<T> iterator) {
        final ArrayList<T> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);
        return list;
    }

    /**
     * Transpose a two dimensional list matrix.
     *
     * @param matrix two dimensional list
     * @param <T>    element type of the list
     * @return transposed, two dimensional list matrix
     */
    public static <T> List<List<T>> transpose(final List<List<T>> matrix) {
        final List<List<T>> ret = new ArrayList<>();
        final int N = matrix.get(0).size();
        for (int i = 0; i < N; i++) {
            final List<T> col = new ArrayList<>();
            for (final List<T> row : matrix) {
                col.add(row.get(i));
            }
            ret.add(col);
        }
        return ret;
    }

    /**
     * Prints list of objects in a consecutive order.
     *
     * @param list      list of objects to be printed
     * @param mapper    mapping function to map the objects to strings
     * @param separator item string separator
     * @param <T>       list item type
     * @return string of printed list
     */
    public static <T> String printConsecutive(final List<T> list, final Function<T, String> mapper, final String separator) {
        return list
                .stream()
                .map(mapper)
                .reduce((s, s2) -> s + separator + s2)
                .orElse("");
    }

}
