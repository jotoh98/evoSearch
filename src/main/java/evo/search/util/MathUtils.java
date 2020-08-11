package evo.search.util;

/**
 * Math utility function library.
 */
public class MathUtils {

    /**
     * Euclidean distance between two polar coordinates.
     *
     * @param angleA    angle of the first point
     * @param distanceA distance of the first point
     * @param angleB    angle of the second point
     * @param distanceB distance of the second point
     * @return euclidean distance
     */
    public static double distance(final double angleA, final double distanceA, final double angleB, final double distanceB) {
        final double subtract = 2 * distanceA * distanceB * Math.cos(angleA - angleB);
        return Math.sqrt(distanceA * distanceA + distanceB * distanceB - subtract);
    }

}
