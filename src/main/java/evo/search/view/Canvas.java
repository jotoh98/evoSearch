package evo.search.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.HashMap;

@Slf4j
public class Canvas extends JPanel {

    private Transformation transformation = new Transformation();

    @Getter
    private HashMap<Point2D, Style> points = new HashMap<>();

    @Getter
    private HashMap<Shape, Style> shapes = new HashMap<>();

    public Canvas() {
        transformation.setScale(10);
        transformation.getOffset().setLocation(getHeight() / 2d, getWidth() / 2d);
        addMouseListener(transformation.getMouseListener());
        addMouseMotionListener(transformation.getMouseMotionListener(this));
        addMouseWheelListener(transformation.getMouseWheelListener(this));
    }

    public void clear() {
        EventQueue.invokeLater(() -> {
            points.clear();
            shapes.clear();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g.create();
        graphics2D.setTransform(transformation.getAffineTransformation());
        shapes.forEach((shape, style) -> render(graphics2D, shape, style));
        points.forEach((point2D, style) -> render(graphics2D, point2D, style));
        graphics2D.dispose();
    }

    private void render(Graphics2D graphics2D, Shape shape, Style style) {
        if (style.isFilled()) {
            graphics2D.setColor(style.getFill());
            graphics2D.fill(shape);
        }
        graphics2D.setColor(style.getColor());
        graphics2D.setStroke(style.getStroke());
        graphics2D.draw(shape);
    }

    private void render(Graphics2D graphics2D, Point2D point2D, Style style) {
        double x = point2D.getX();
        double y = point2D.getY();
        switch (style.getShape()) {
            case DOT:
                render(graphics2D, new Ellipse2D.Double(x - .3, y - .3, .6, .6), style);
                return;
            case CROSS:
                GeneralPath path = new GeneralPath();
                path.append(new Line2D.Double(x - .3, y + .3, x + .3, y - .3), false);
                path.append(new Line2D.Double(x - .3, y - .3, x + .3, y + .3), false);
                render(graphics2D, path, style);
                return;
            case RECT:
                render(graphics2D, new Rectangle2D.Double(x - .3, y - .3, .6, .6), style);
        }
    }

    public void enqueue(Point2D point) {
        enqueue(point, Style.DEFAULT);
    }

    public void enqueue(Point2D point, Style style) {
        EventQueue.invokeLater(() -> {
            points.put(point, style);
            repaint();
        });
    }

    public void enqueue(Shape shape, Style style) {
        EventQueue.invokeLater(() -> {
            shapes.put(shape, style);
            repaint();
        });
    }

    public void enqueue(Shape shape) {
        enqueue(shape, Style.DEFAULT);
    }
}
