package evo.search.view.render;

import evo.search.view.part.Canvas;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

/**
 * The canvas rendering transformation.
 * Holds a scale and the offset.
 */
@Slf4j
public class Transformation {

    /**
     * A backup of the internal offset vector to translate upon.
     */
    final private Point2D offsetBackup = new Point2D.Double();

    /**
     * The transformation offset vector.
     */
    @Getter
    @Setter
    private Point2D offset = new Point2D.Double();

    /**
     * The transformation scaling factor.
     */
    @Getter
    @Setter
    private double scale = 1d;

    /**
     * The point where a drag event begins to translate relative to.
     */
    private Point2D pressOffset;

    /**
     * Revert a transformed {@link Point2D} on the canvas.
     *
     * @param point transformed point on the canvas
     * @return reverted point of inverted transformation
     */
    public Point2D revert(final Point2D point) {
        return new Point2D.Double((point.getX() - offset.getX()) / scale, (point.getY() - offset.getY()) / scale);
    }

    /**
     * Transform a {@link Point2D}.
     *
     * @param point point to transform
     * @return transformed point
     */
    public Point2D transform(final Point2D point) {
        return new Point2D.Double(scale * point.getX() + offset.getX(), scale * point.getY() + offset.getY());
    }

    /**
     * Transform a {@link Line2D}.
     *
     * @param line line to transform
     * @return transformed line
     */
    public Line2D transform(final Line2D line) {
        return new Line2D.Double(transform(line.getP1()), transform(line.getP2()));
    }

    /**
     * Generate an {@link AffineTransform} from the translation {@link #offset} and the {@link #scale}.
     *
     * @return Affine transformation from the internal {@link Transformation}.
     */
    public AffineTransform getAffineTransformation() {
        final AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(offset.getX(), offset.getY());
        affineTransform.scale(scale, scale);
        return affineTransform;
    }

    /**
     * Get the mouse press listener.
     *
     * @return the mouse listener for the mouse press event
     */
    public MouseListener getMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                pressOffset = e.getPoint();
                offsetBackup.setLocation(offset.getX(), offset.getY());
            }
        };
    }

    /**
     * Get the mouse dragging listener.
     *
     * @param canvas canvas being repainted
     * @return the mouse listener for the drag event
     */
    public MouseMotionListener getMouseMotionListener(final Canvas canvas) {
        return new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                offset.setLocation(
                        offsetBackup.getX() + e.getX() - pressOffset.getX(),
                        offsetBackup.getY() + e.getY() - pressOffset.getY()
                );
                canvas.repaint();
            }
        };
    }

    /**
     * Get the mouse zoom listener.
     *
     * @param c component being repainted
     * @return the mouse listener for the scroll event
     */
    public MouseWheelListener getMouseWheelListener(final Component c) {
        return new MouseAdapter() {
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                final double amount = Math.pow(1.05, e.getScrollAmount());
                final double dirX = offset.getX() - e.getX();
                final double dirY = offset.getY() - e.getY();
                if (e.getPreciseWheelRotation() > 0) {
                    scale *= amount;
                    offset.setLocation(e.getX() + dirX * amount, e.getY() + dirY * amount);
                } else {
                    scale /= amount;
                    offset.setLocation(e.getX() + dirX / amount, e.getY() + dirY / amount);
                }
                c.repaint();
            }
        };
    }

    /**
     * Get the resize component listener.
     *
     * @param c component being repainted
     * @return the component listener for the resize event
     */
    public ComponentListener getComponentListener(final Component c) {
        return new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                c.repaint();
            }
        };
    }
}
