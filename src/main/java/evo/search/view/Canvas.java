package evo.search.view;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class Canvas extends JPanel {

    @Getter
    HashMap<Point2D, PointStyle> points = new HashMap<>();
    List<Shape> shapes = new ArrayList<>();
    private Transformation transformation = new Transformation();
    private AffineTransform affineTransform = new AffineTransform();

    public Canvas() {

        transformation.setScale(10);
        transformation.getOffset().setLocation(250, 250);
        addMouseListener(transformation.getMouseListener());
        addMouseMotionListener(transformation.getMouseMotionListener(this));
        addMouseWheelListener(transformation.getMouseWheelListener(this));

        addMouseMotionListener(new MouseMotionAdapter() {
            @SneakyThrows
            @Override
            public void mouseMoved(MouseEvent e) {
                Point2D inverted = affineTransform.inverseTransform(e.getPoint(), null);
                points.keySet().stream()
                        .filter(point2D -> point2D.distance(inverted) < 5 * affineTransform.getScaleX())
                        .min(Comparator.comparingDouble(point -> point.distance(inverted)))
                        .ifPresent(point2D -> log.info("hovered: {}", point2D));
            }
        });

    }

    @SneakyThrows
    public void onScroll(MouseWheelEvent e) {
        final double amount = Math.pow(1.05, e.getScrollAmount());
        final double delta = e.getPreciseWheelRotation() > 0 ? amount : 1 / amount;
        Point2D transform = affineTransform.inverseTransform(e.getPoint(), null);
        affineTransform.translate(transform.getX(), transform.getY());
        affineTransform.scale(delta, delta);
        affineTransform.translate(-transform.getX(), -transform.getY());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g.create();
        graphics2D.setTransform(transformation.getAffineTransformation());

        shapes.forEach(shape -> draw(graphics2D, shape));

        points.forEach((point, pointStyle) -> {
            switch (pointStyle) {
                case SQUARE:
                    draw(graphics2D, point);
                    break;
                case CROSS:
                    drawCross(graphics2D, point);
            }
        });

        graphics2D.dispose();
    }

    public void draw(Graphics2D g, Point2D point) {
        int x = (int) point.getX();
        int y = (int) point.getY();
        g.setColor(Color.WHITE);
        g.fillRect(x - 1, y - 1, 2, 2);
        g.setColor(Color.BLACK);
        g.drawRect(x - 1, y - 1, 2, 2);
    }

    public void drawCross(Graphics2D g, Point2D point) {
        int x = (int) point.getX();
        int y = (int) point.getY();
        g.setColor(Color.BLACK);
        g.drawLine(x - 2, y + 2, x + 2, y - 2);
        g.drawLine(x - 2, y - 2, x + 2, y + 2);
    }

    public void draw(Graphics2D g, Shape shape) {
        g.draw(shape);
    }

    public void render(Point2D point) {
        render(point, PointStyle.SQUARE);
    }

    public void render(Point2D point, PointStyle style) {
        points.put(point, style);
        repaint();
    }

    public void render(Shape shape) {
        shapes.add(shape);
        repaint();
    }

    public enum PointStyle {
        SQUARE,
        CROSS
    }
}
