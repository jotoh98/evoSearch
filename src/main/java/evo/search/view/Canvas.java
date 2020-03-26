package evo.search.view;

import evo.search.Experiment;
import evo.search.ga.DiscreteGene;
import io.jenetics.Chromosome;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class Canvas extends JPanel {

    private Transformation transformation = new Transformation();

    @Getter
    private HashMap<Point2D, Style> points = new HashMap<>();

    @Getter
    private HashMap<Shape, Style> shapes = new HashMap<>();

    public Canvas() {
        points.put(new Point2D.Double(5, 5), Style.builder().color(Color.lightGray).build());
        transformation.setScale(10);
        transformation.getOffset().setLocation(getHeight() / 2d, getWidth() / 2d);
        addMouseListener(transformation.getMouseListener());
        addMouseMotionListener(transformation.getMouseMotionListener(this));
        addMouseWheelListener(transformation.getMouseWheelListener(this));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                log.info("clicked: {}", e);
            }
        });
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

    public void render(final Chromosome<DiscreteGene> chromosome) {
        Experiment.getInstance().getTreasures().forEach(
                treasure -> enqueue(
                        treasure.toPoint2D(),
                        Style.builder().shape(Style.Shape.CROSS).build()
                )
        );
        final AtomicInteger index = new AtomicInteger();
        chromosome.forEach(gene -> {
            final Point2D point = gene.getAllele().toPoint2D();
            enqueue(String.valueOf(index.get()), point, 5.0f);
            enqueue(point);
            index.getAndIncrement();
        });
        int availablePosition = Experiment.getInstance().getPositions();

        for (int position = 0; position < availablePosition; position++) {
            int x = (int) Math.round(Math.cos(position / (double) availablePosition * 2 * Math.PI) * 100);
            int y = (int) Math.round(Math.sin(position / (double) availablePosition * 2 * Math.PI) * 100);
            enqueue(
                    new Line2D.Double(0, 0, x, y),
                    Style.builder()
                            .color(Color.lightGray)
                            .stroke(new BasicStroke(.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{1}, 0))
                            .build()
            );
        }

        if (availablePosition > 2) {
            Point2D previous = new Point2D.Double();
            for (final DiscreteGene discreteGene : chromosome) {
                Point2D current = discreteGene.getAllele().toPoint2D();
                enqueue(new Line2D.Double(previous, current));
                previous = current;
            }
        }
    }

    public void enqueue(String text, Point2D position, float size) {
        final Font font = getFont().deriveFont(Font.BOLD, size);
        final GlyphVector glyphVector = font.createGlyphVector(
                getFontMetrics(font).getFontRenderContext(),
                text
        );
        glyphVector.setGlyphPosition(1, position);
        enqueue(glyphVector.getOutline());
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
