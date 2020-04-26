package evo.search.view.render;

import evo.search.view.part.Canvas;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * The canvas rendering transformation.
 * Holds a scale and the offset.
 */
@Slf4j
public class Transformation {

    /**
     * A const for behaviour scaling factor.
     */
    public static final double UI_SCALE = 2.0;

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
     * The non-translated canvas visual boundary representation of the {@link Canvas}.
     */
    @Getter
    private Rectangle2D boundary = new Rectangle2D.Double();

    /**
     * Revert a transformed {@link Point2D} on the canvas.
     *
     * @param point Transformed point on the canvas.
     * @return Reverted point of inverted transformation.
     */
    public Point2D revert(Point2D point) {
        return new Point2D.Double((point.getX() - offset.getX()) / scale, (point.getY() - offset.getY()) / scale);
    }

    /**
     * Transform a {@link Point2D}.
     *
     * @param point Point to be transformed.
     * @return Transformed point.
     */
    public Point2D transform(Point2D point) {
        return new Point2D.Double(scale * point.getX() + offset.getX(), scale * point.getY() - offset.getY());
    }

    /**
     * Generate an {@link AffineTransform} from the translation {@link #offset} and the {@link #scale}.
     *
     * @return Affine transformation from the internal {@link Transformation}.
     */
    public AffineTransform getAffineTransformation() {
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(offset.getX(), offset.getY());
        affineTransform.scale(scale, scale);
        return affineTransform;
    }

    /**
     * Update the {@link #boundary} corresponding to the viewport {@link Component}.
     *
     * @param c The viewport component.
     */
    public void updateBoundary(Component c) {
        final Point2D upperLeft = revert(new Point2D.Double(0, 0));
        final Point2D lowerRight = revert(new Point2D.Double(UI_SCALE * c.getWidth(), UI_SCALE * c.getHeight()));
        boundary.setFrameFromDiagonal(
                upperLeft.getX(),
                upperLeft.getY(),
                lowerRight.getX(),
                lowerRight.getY()
        );
    }

    /**
     * Get the mouse press listener.
     *
     * @return The mouse listener for the mouse press event.
     */
    public MouseListener getMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                pressOffset = new Point2D.Double(e.getX() * UI_SCALE, e.getY() * UI_SCALE);
                offsetBackup.setLocation(offset.getX(), offset.getY());
            }
        };
    }

    /**
     * Get the mouse dragging listener.
     *
     * @param canvas The canvas being repainted and providing the visual boundary for {@link #updateBoundary(Component)}.
     * @return The mouse listener for the drag event.
     */
    public MouseMotionListener getMouseMotionListener(Canvas canvas) {
        return new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                final Point2D mousePosition = new Point2D.Double(e.getX() * UI_SCALE, e.getY() * UI_SCALE);
                offset.setLocation(
                        offsetBackup.getX() + mousePosition.getX() - pressOffset.getX(),
                        offsetBackup.getY() + mousePosition.getY() - pressOffset.getY()
                );
                updateBoundary(canvas);
                canvas.repaint();
            }
        };
    }

    /**
     * Get the mouse zoom listener.
     *
     * @param c The component being repainted and providing the visual boundary for {@link #updateBoundary(Component)}.
     * @return The mouse listener for the scroll event.
     */
    public MouseWheelListener getMouseWheelListener(Component c) {
        return new MouseAdapter() {
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                final double amount = Math.pow(1.05, e.getScrollAmount());
                if (e.getPreciseWheelRotation() > 0) {
                    scale *= amount;
                } else {
                    scale /= amount;
                }
                updateBoundary(c);
                c.repaint();
            }
        };
    }

    /**
     * Get the resize component listener.
     *
     * @param c The component being repainted and providing the visual boundary for {@link #updateBoundary(Component)}.
     * @return The component listener for the resize event.
     */
    public ComponentListener getComponentListener(Component c) {
        return new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                updateBoundary(c);
                c.repaint();
            }
        };
    }
}
