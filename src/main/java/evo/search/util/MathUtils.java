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
        final double subtract = 2 * distanceA * distanceB * Math.cos(angleA - angleB);
        return Math.sqrt(distanceA * distanceA + distanceB * distanceB - subtract);
    }

    /**
     * Calculate the euclidean distance between to points given through their coorindates.
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
