package evo.search.util;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

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
     * Compute the intersection point between the two infinite lines A and B.
     *
     * @param lineA infinite line A
     * @param lineB infinite line B
     * @return if A and B intersect, the intersection point, null otherwise
     */
    public static Point2D intersection(final Line2D lineA, final Line2D lineB) {

        final Point2D p1 = lineA.getP1();
        final Point2D p2 = lineA.getP2();
        final Point2D p3 = lineB.getP1();
        final Point2D p4 = lineB.getP2();

        final double x1 = p1.getX();
        final double y1 = p1.getY();
        final double x2 = p2.getX();
        final double y2 = p2.getY();
        final double x3 = p3.getX();
        final double y3 = p3.getY();
        final double x4 = p4.getX();
        final double y4 = p4.getY();

        if (p1.equals(p3) || p1.equals(p4))
            return p1;
        if (p2.equals(p3) || p2.equals(p4))
            return p2;

        final double a = x1 * y2 - y1 * x2;
        final double b = x3 * y4 - y3 * x4;

        final double div = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        if (div == 0)
            return null;

        return new Point2D.Double(
                (a * (x3 - x4) - (x1 - x2) * b) / div,
                (a * (y3 - y4) - (y1 - y2) * b) / div
        );
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
