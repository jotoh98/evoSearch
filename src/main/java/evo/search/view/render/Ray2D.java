package evo.search.view.render;

import evo.search.util.MathUtils;

import java.awt.geom.Line2D;

/**
 * Infinite 2d ray.
 * First point marks the origin and the second the direction.
 */
public class Ray2D extends Line2D.Double {

    /**
     * Two-point-coordinate constructor.
     * The direction is relative to the origin. The ray goes through both points.
     *
     * @param x1 x-coordinate of origin
     * @param y1 y-coordinate of origin
     * @param x2 x-coordinate of direction
     * @param y2 y-coordinate of direction
     */
    public Ray2D(final double x1, final double y1, final double x2, final double y2) {
        super(x1, y1, x2, y2);
    }

    /**
     * Render the ray as a scaled line.
     *
     * @param distance scaling factor to render the line
     * @return a line simulating an infinite ray
     */
    public Line2D render(final double distance) {
        final double length = MathUtils.distance(x1, y1, x2, y2);
        final double x3 = (x2 - x1) / length * distance;
        final double y3 = (y2 - y1) / length * distance;
        return new Line2D.Double(x1, y1, x3, y3);
    }

}
