package evo.search.util;

import evo.search.ga.DiscretePoint;
import io.jenetics.util.RandomRegistry;

import java.util.Random;

/**
 * All utility methods associated with randomness.
 */
public class RandomUtils {

    /**
     * Returns a random {@code int} value between {@code min} (inclusive) and {@code max} (inclusive).
     * Utilizes the {@link RandomRegistry} from jenetics for thread-safety.
     *
     * @param min Lower range boundary (inclusive).
     * @param max Upper range boundary (inclusive).
     * @return Random {@code int} value between {@code min} (inclusive) and {@code max} (inclusive).
     */
    public static int inRange(int min, int max) {
        return min + RandomRegistry.random().nextInt(max - 1 - min);
    }

    /**
     * Returns a random {@code double} value between {@code min} (inclusive) and {@code max} (inclusive).
     * Utilizes the {@link RandomRegistry} from jenetics for thread-safety.
     *
     * @param min Lower range boundary (inclusive).
     * @param max Upper range boundary (inclusive).
     * @return Random {@code double} value between {@code min} (inclusive) and {@code max} (inclusive).
     */
    public static double inRange(double min, double max) {
        return min + RandomRegistry.random().nextDouble() * (max - min);
    }

    /**
     * Returns a shuffled {@link DiscretePoint}.
     * The point's position is randomly selected in the range [0, {@code positions}] and the distance is chosen in the range [{@code minDistance}, {@code maxDistance}).
     * Utilizes the {@link RandomRegistry} from jenetics for thread-safety.
     *
     * @param positions   Upper boundary for the points position.
     * @param minDistance Minimal distance for the point (inclusive).
     * @param maxDistance Maximal distance for the point (exclusive).
     * @return Shuffled discrete point.
     * @see Random#nextDouble()
     */
    public static DiscretePoint generatePoint(int positions, double minDistance, double maxDistance) {
        return new DiscretePoint(positions, RandomRegistry.random().nextInt(positions), inRange(minDistance, maxDistance));
    }

}
