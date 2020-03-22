package evo.search.view;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

@Slf4j
public class Transformation {

    @Getter
    @Setter
    private Point2D offset = new Point2D.Double();

    @Getter
    @Setter
    private double scale = 1d;

    private Point2D pressOffset;
    private Point2D offsetBackup = new Point2D.Double();

    public Point2D revert(Point2D point) {
        return new Point2D.Double((point.getX() - offset.getX()) / scale, (point.getY() - offset.getY()) / scale);
    }

    public Point2D transform(Point2D point) {
        return new Point2D.Double(scale * point.getX() + offset.getX(), scale * point.getY() - offset.getY());
    }

    public AffineTransform getAffineTransformation() {
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(offset.getX(), offset.getY());
        affineTransform.scale(scale, scale);
        return affineTransform;
    }

    public MouseListener getMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
                pressOffset = new Point2D.Double(e.getX() * 2, e.getY() * 2);
                offsetBackup.setLocation(offset.getX(), offset.getY());
            }
        };
    }

    public MouseMotionListener getMouseMotionListener(Component c) {
        return new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                Point2D mousePosition = new Point2D.Double(e.getX() * 2, e.getY() * 2);
                offset.setLocation(
                        offsetBackup.getX() + mousePosition.getX() - pressOffset.getX(),
                        offsetBackup.getY() + mousePosition.getY() - pressOffset.getY()
                );
                c.repaint();
            }
        };
    }

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
                c.repaint();
            }
        };
    }
}
