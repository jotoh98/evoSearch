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
    public static double polarDistance(final double angleA, final double distanceA, final double angleB, final double distanceB) {
        return lawOfCosine(angleA - angleB, distanceA, distanceB);
    }

    /**
     * Euclidean distance between two {@link evo.search.ga.DiscreteGene}s.
     *
     * @param positions amount of rays
     * @param positionA first ray index
     * @param distanceA first gene distance
     * @param positionB second ray index
     * @param distanceB second gene distance
     * @return euclidean distance between two genes
     */
    public static double polarDistance(final short positions, final short positionA, final float distanceA, final short positionB, final float distanceB) {
        return lawOfCosine(sectorAngle(positions) * (positionA - positionB), distanceA, distanceB);
    }

    /**
     * Return the angle of one sector for a given amount of equally distanced rays.
     *
     * @param positions amount of rays
     * @return angle between two rays
     */
    public static double sectorAngle(final short positions) {
        return (2 * Math.PI) / positions;
    }

    /**
     * Applies the law of cosines to a given {@code angle} between two line segments with
     * respective lengths {@code distanceA} and {@code distanceB}.
     *
     * @param angle     angle between the two line segments
     * @param distanceA length of one line segment
     * @param distanceB length of other line segment
     * @return length of the third side forming a triangle
     */
    public static double lawOfCosine(final double angle, final double distanceA, final double distanceB) {
        final double subtract = 2 * distanceA * distanceB * Math.cos(angle);
        return Math.sqrt(distanceA * distanceA + distanceB * distanceB - subtract);
    }

    /**
     * Calculate the euclidean distance between to points given through their coordinates.
     *
     * @param x1 x-coordinate of point one
     * @param y1 y-coordinate of point one
     * @param x2 x-coordinate of point two
     * @param y2 y-coordinate of point two
     * @return euclidean distance between the points
     */
    public static double distance(final double x1, final double y1, final double x2, final double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    /**
     * Calculate the area covered by a triangle defined by an edge's angle and
     * the lengths of the two sides connected to the edge.
     *
     * @param angle   angle
     * @param lengthA one side's length
     * @param lengthB other side's length
     * @return area covered by triangle
     */
    public static double areaInTriangle(final double angle, final double lengthA, final double lengthB) {
        return lengthA * lengthB * Math.sin(angle) / 2;
    }

}
