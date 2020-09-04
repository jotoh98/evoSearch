package evo.search.util;

import io.jenetics.util.RandomRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.*;

/**
 * List utility methods.
 */
public class ListUtils {

    /**
     * Reduce function to connect two strings via a space character.
     */
    public static final BinaryOperator<String> REDUCE_WITH_SPACE = separator(" ");

    /**
     * Returns a binary string reducing operator concatenating a collection of strings
     * with a given separator.
     *
     * @param separator separator to place between each pair of strings
     * @return concatenated string of the collection's string separated by the given separator
     */
    public static BinaryOperator<String> separator(final String separator) {
        return (s1, s2) -> s1 + separator + s2;
    }

    /**
     * Generate a {@link List} with a given length through a supplier.
     *
     * @param limit    length of the new list
     * @param supplier supplier to retrieve the items from
     * @param <T>      list/supplier item type
     * @return a list with element generated by the given supplier
     */
    public static <T> List<T> generate(final int limit, final Supplier<T> supplier) {
        final List<T> result = new ArrayList<>();
        for (int i = 0; i < limit; i++)
            result.add(supplier.get());
        return result;
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
        final List<R> result = new ArrayList<>();
        for (int i = 0; i < list.size() - 1; i++)
            result.add(mapping.apply(list.get(i), list.get(i + 1)));
        return result;
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
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) builder.append(separator);
            builder.append(mapper.apply(list.get(i)));
        }
        return builder.toString();
    }

    /**
     * Calculates the variance for a list of numbers.
     *
     * @param list list of numbers
     * @return variance of the values
     */
    public static double variance(final List<? extends Number> list) {
        if (list.size() == 0)
            return 0;

        final List<Double> doubles = map(list, Number::doubleValue);

        final double mean = mean(doubles);
        double sum = 0;
        for (final Double x : doubles)
            sum += (x - mean) * (x - mean);

        return sum / list.size();
    }

    /**
     * Remove repeating elements from a list.
     *
     * @param list list with repeating elements
     * @param <T>  type of list items
     * @return list without items repeating
     */
    public static <T> List<T> removeRepeating(final List<T> list) {
        if (list.size() < 2)
            return list;

        final List<T> distinct = new ArrayList<>();
        T comparison = list.get(0);
        distinct.add(comparison);
        for (final T item : list)
            if (!item.equals(comparison)) {
                distinct.add(item);
                comparison = item;
            }

        return distinct;
    }

    /**
     * Deep clone a list of items.
     *
     * @param items  items to clone
     * @param cloner cloning method
     * @param <T>    type of items
     * @return list of deeply cloned items
     */
    public static <T> List<T> deepClone(final List<T> items, final UnaryOperator<T> cloner) {
        return map(items, cloner);
    }

    /**
     * Calculate the sum of the list of doubles.
     *
     * @param numbers list of doubles
     * @return sum of list of doubles
     */
    public static double sum(final List<? extends Number> numbers) {
        return reduce(map(numbers, Number::doubleValue), Double::sum, 0d);
    }

    /**
     * Reduce a list of items to a single item.
     *
     * @param list     list of items to reduce
     * @param reducer  binary reducing operation
     * @param optional starting value of reduction, gets returned if list is empty
     * @param <T>      type of list items and return value
     * @return result of list reduction
     */
    public static <T> T reduce(final List<T> list, final BinaryOperator<T> reducer, final T optional) {
        if (list.size() == 0) return optional;
        T reduce = list.get(0);
        for (int i = 1, listSize = list.size(); i < listSize; i++) {
            final T item = list.get(i);
            reduce = reducer.apply(reduce, item);
        }
        return reduce;
    }

    /**
     * Computes the mean value of the list of doubles.
     *
     * @param numbers list of doubles
     * @return mean of list of doubles
     */
    public static double mean(final List<Double> numbers) {
        if (numbers.size() == 0)
            return 0;
        return sum(numbers) / numbers.size();
    }

    /**
     * List mapping.
     * Elements mapped to null are ignored.
     *
     * @param list   list to map
     * @param mapper mapping function
     * @param <T>    input element type
     * @param <R>    output element type
     * @return list of mapped elements
     */
    public static <T, R> List<R> map(final List<T> list, final Function<T, R> mapper) {
        final List<R> result = new ArrayList<>();
        for (final T item : list) {
            final R mapped = mapper.apply(item);
            if (mapped != null)
                result.add(mapped);
        }
        return result;
    }

}
