package evo.search.view;

import lombok.Value;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

@Value
public class StringShape implements Shape {

    String string;

    float x;

    float y;

    @Override
    public Rectangle getBounds() {
        return null;
    }

    @Override
    public Rectangle2D getBounds2D() {
        return null;
    }

    @Override
    public boolean contains(final double x, final double y) {
        return false;
    }

    @Override
    public boolean contains(final Point2D p) {
        return false;
    }

    @Override
    public boolean intersects(final double x, final double y, final double w, final double h) {
        return false;
    }

    @Override
    public boolean intersects(final Rectangle2D r) {
        return false;
    }

    @Override
    public boolean contains(final double x, final double y, final double w, final double h) {
        return false;
    }

    @Override
    public boolean contains(final Rectangle2D r) {
        return false;
    }

    @Override
    public PathIterator getPathIterator(final AffineTransform at) {
        return null;
    }

    @Override
    public PathIterator getPathIterator(final AffineTransform at, final double flatness) {
        return null;
    }
}
