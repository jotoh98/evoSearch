package evo.search.view;

import lombok.Value;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * String shape representation.
 */
@Value
public class StringShape implements Shape {

    /**
     * String to be rendered.
     */
    String string;

    /**
     * y-coordinate of the upper-left edge of the string shape.
     */
    float x;

    /**
     * y-coordinate of the upper-left edge of the string shape.
     */
    float y;

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle getBounds() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle2D getBounds2D() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final double x, final double y) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Point2D p) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean intersects(final double x, final double y, final double w, final double h) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean intersects(final Rectangle2D r) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final double x, final double y, final double w, final double h) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Rectangle2D r) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathIterator getPathIterator(final AffineTransform at) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
        return null;
    }
}
