package evo.search.view.part;

import evo.search.ga.DiscreteChromosome;
import evo.search.ga.DiscreteGene;
import evo.search.view.render.StringShape;
import evo.search.view.render.Style;
import evo.search.view.render.Transformation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Canvas to be painted on.
 */
@Slf4j
public class Canvas extends JPanel {

    /**
     * Associated transformation.
     */
    final private Transformation transformation = new Transformation();

    /**
     * Points to be rendered on the canvas.
     */
    @Getter
    private final HashMap<Point2D, Style> points = new HashMap<>();

    /**
     * Shapes to be rendered on the canvas.
     */
    @Getter
    private final HashMap<Shape, Style> shapes = new HashMap<>();

    /**
     * Strings to be rendered on the canvas.
     */
    @Getter
    private final HashMap<StringShape, Style> strings = new HashMap<>();

    @Getter
    final private JLabel popover = new JLabel();

    /**
     * Canvas constructor. Sets initial scale and the offset to the center of the canvas.
     */
    public Canvas() {
        add(popover);
        popover.setVisible(false);
        transformation.setScale(10);
        transformation.getOffset().setLocation(getHeight() / 2d, getWidth() / 2d);
        addMouseListener(transformation.getMouseListener());
        addMouseMotionListener(transformation.getMouseMotionListener(this));
        addMouseMotionListener(getHoverListener());
        addMouseWheelListener(transformation.getMouseWheelListener(this));
        addComponentListener(transformation.getComponentListener(this));
    }

    private MouseMotionListener getHoverListener() {
        return new MouseMotionAdapter() {

            boolean hovered = false;

            @Override
            public void mouseDragged(final MouseEvent event) {
                popover.setLocation(event.getX(), event.getY());
            }

            @Override
            public void mouseMoved(final MouseEvent event) {
                popover.setLocation(event.getX(), event.getY());
                final Point2D mousePosition = new Point2D.Double(event.getX() * Transformation.UI_SCALE, event.getY() * Transformation.UI_SCALE);
                final Point2D revertedPosition = transformation.revert(mousePosition);
                final double epsilonEnvironment = 10 / transformation.getScale();

                final List<Point2D> hoveredPoints = points.keySet().stream()
                        .filter(point2D -> point2D.distance(revertedPosition) < epsilonEnvironment)
                        .sorted(Comparator.comparingDouble(point -> point.distance(revertedPosition)))
                        .collect(Collectors.toList());

                if (hoveredPoints.size() == 0) {
                    popover.setVisible(false);
                    popover.setText("");
                    if (hovered) {
                        repaint();
                    }
                    hovered = false;
                    return;
                }

                hovered = true;

                final Point2D point2D = hoveredPoints.get(0);

                popover.setText(point2D.toString());
                popover.setVisible(true);
                repaint();
            }


        };
    }

    /**
     * Clears all maps containing the rendered items.
     */
    public void clear() {
        EventQueue.invokeLater(() -> {
            points.clear();
            shapes.clear();
            strings.clear();
        });
    }

    /**
     * Rendering all the items on the canvas.
     *
     * @param g graphics to render the items with
     */
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D graphics2D = (Graphics2D) g.create();
        graphics2D.setTransform(transformation.getAffineTransformation());
        shapes.forEach((shape, style) -> render(graphics2D, shape, style));
        points.forEach((point2D, style) -> render(graphics2D, point2D, style));
        strings.forEach((stringShape, style) -> render(graphics2D, stringShape, style));
        graphics2D.dispose();
    }

    /**
     * Render a {@link Shape} associated with a {@link Style}.
     *
     * @param graphics2D graphics to render the items with
     * @param shape      shape to render
     * @param style      style to customize the shapes appearance
     */
    private void render(final Graphics2D graphics2D, final Shape shape, final Style style) {
        if (style.isFilled()) {
            graphics2D.setColor(style.getFill());
            graphics2D.fill(shape);
        }
        graphics2D.setColor(style.getColor());
        graphics2D.setStroke(style.getStroke());
        if (shape instanceof StringShape) {
            graphics2D.setFont(style.getFont());
            final StringShape string = (StringShape) shape;
            graphics2D.drawString(string.getString(), string.getX(), string.getY());
        } else {
            graphics2D.draw(shape);
        }
    }

    /**
     * Render a {@link Point2D} associated with a {@link Style}.
     * The {@link Point2D} is transformed into a {@link Shape} according to the corresponding {@link Style.Shape}.
     *
     * @param graphics2D graphics to render the items with
     * @param point2D    point to render
     * @param style      style to customize the shapes appearance
     * @see Style.Shape
     */
    private void render(final Graphics2D graphics2D, final Point2D point2D, final Style style) {
        final double x = point2D.getX();
        final double y = point2D.getY();
        switch (style.getShape()) {
            case DOT:
                render(graphics2D, new Ellipse2D.Double(x - .3, y - .3, .6, .6), style);
                return;
            case CROSS:
                final GeneralPath path = new GeneralPath();
                path.append(new Line2D.Double(x - .3, y + .3, x + .3, y - .3), false);
                path.append(new Line2D.Double(x - .3, y - .3, x + .3, y + .3), false);
                render(graphics2D, path, style);
                return;
            case RECT:
                render(graphics2D, new Rectangle2D.Double(x - .3, y - .3, .6, .6), style);
        }
    }

    /**
     * Render a single chromosome as a series of points connected with lines.
     *
     * @param chromosome chromosome to render
     */
    public void render(final DiscreteChromosome chromosome) {
        final int availablePosition = chromosome.getConfiguration().getPositions();

        renderRays(availablePosition);

        final AtomicInteger index = new AtomicInteger();
        chromosome.forEach(gene -> {
            final Point2D point = gene.getAllele().toPoint2D();
            enqueue(String.valueOf(index.get()), point, Style.builder().color(Color.WHITE).build());
            enqueue(point, Style.builder().color(Color.GREEN).build());
            index.getAndIncrement();
        });


        if (availablePosition > 2) {
            Point2D previous = new Point2D.Double();
            for (final DiscreteGene discreteGene : chromosome) {
                final Point2D current = discreteGene.getAllele().toPoint2D();
                enqueue(
                        new Line2D.Double(previous, current),
                        Style.builder().color(Color.WHITE).build()
                );
                previous = current;
            }
        }

        chromosome.getConfiguration().getTreasures().forEach(
                treasure -> enqueue(
                        treasure.toPoint2D(),
                        Style.builder().shape(Style.Shape.CROSS).color(Color.RED).build()
                )
        );
    }

    /**
     * Render the rays from the origin.
     *
     * @param amount amount of rays to render
     */
    public void renderRays(final int amount) {
        for (int position = 0; position < amount; position++) {
            final int x = (int) Math.round(Math.cos(position / (double) amount * 2 * Math.PI) * 100);
            final int y = (int) Math.round(Math.sin(position / (double) amount * 2 * Math.PI) * 100);
            enqueue(
                    new Line2D.Double(0, 0, x, y),
                    Style.builder()
                            .color(Color.lightGray)
                            .stroke(new BasicStroke(.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{1}, 0))
                            .build()
            );
        }
    }

    /**
     * Enqueue text to be rendered soon.
     *
     * @param text     text to enqueue
     * @param position position of the text
     * @param style    style of the rendered text
     */
    public void enqueue(final String text, final Point2D position, final Style style) {
        EventQueue.invokeLater(() -> {
            strings.put(new StringShape(text, (float) position.getX(), (float) position.getY()), style);
            repaint();
        });
    }

    /**
     * Enqueue a point to be rendered soon.
     *
     * @param point point to enqueue
     * @param style style of the rendered point
     */
    public void enqueue(final Point2D point, final Style style) {
        EventQueue.invokeLater(() -> {
            points.put(point, style);
            repaint();
        });
    }

    /**
     * Enqueue a shape to be rendered soon.
     *
     * @param shape shape to enqueue
     * @param style style of the rendered shape
     */
    public void enqueue(final Shape shape, final Style style) {
        EventQueue.invokeLater(() -> {
            shapes.put(shape, style);
            repaint();
        });
    }
}
