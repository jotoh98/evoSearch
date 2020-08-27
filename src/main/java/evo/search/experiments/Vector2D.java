package evo.search.experiments;

import java.awt.geom.Point2D;
import java.util.function.DoubleFunction;
import java.util.function.ToDoubleBiFunction;

/**
 * @author jotoh
 */
public class Vector2D extends Point2D {

    private double x;
    private double y;

    public Vector2D() {
        super();
    }

    public Vector2D(final double x, final double y) {
        setLocation(x, y);
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setLocation(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    private Vector2D reduce(final Vector2D v, final ToDoubleBiFunction<java.lang.Double, java.lang.Double> reduce) {
        return new Vector2D(reduce.applyAsDouble(x, v.x), reduce.applyAsDouble(y, v.y));
    }

    public Vector2D map(final DoubleFunction<java.lang.Double> mapper) {
        return new Vector2D(mapper.apply(x), mapper.apply(y));
    }

    public Vector2D add(final Vector2D v) {
        return reduce(v, java.lang.Double::sum);
    }

    public Vector2D subtract(final Vector2D v) {
        return reduce(v, (a, b) -> a - b);
    }

    public Vector2D multiply(final double a) {
        return new Vector2D(a * x, a * y);
    }

    public Vector2D divide(final double a) {
        return multiply(1 / a);
    }
}
