package evo.search.view.render;

import evo.search.util.MathUtils;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class Ray2D extends Line2D.Double {

    public Ray2D(final double x1, final double y1, final double x2, final double y2) {
        super(x1, y1, x2, y2);
    }

    public Line2D render(final double distance) {
        final double length = MathUtils.distance(x1, y1, x2, y2);
        final double x3 = (x2 - x1) / length * distance;
        final double y3 = (y2 - y1) / length * distance;
        return new Line2D.Double(x1, y1, x3, y3);
    }
}
