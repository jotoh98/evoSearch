package evo.search.view.part;

import evo.search.ga.DiscreteGene;
import evo.search.util.MathUtils;
import evo.search.view.render.Ray2D;
import evo.search.view.render.StringShape;
import evo.search.view.render.Style;
import evo.search.view.render.Transformation;
import io.jenetics.Chromosome;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * Label to display string data without rendering it on the canvas.
     */
    @Getter
    final private JLabel popover = new JLabel();

    /**
     * Amount of rays to display.
     */
    @Setter
    private int rays = 3;

    /**
     * Canvas constructor. Sets initial scale and the offset to the center of the canvas.
     */
    public Canvas() {
        add(popover);
        popover.setVisible(false);
        transformation.setScale(10);
        addMouseListener(transformation.getMouseListener());
        addMouseMotionListener(transformation.getMouseMotionListener(this));
        addMouseMotionListener(getHoverListener());
        addMouseWheelListener(transformation.getMouseWheelListener(this));
        addComponentListener(transformation.getComponentListener(this));
    }

    public void centerOffset() {
        transformation.getOffset().setLocation(getWidth() / 2f, getHeight() / 2f);
    }

    /**
     * Generates the listener for the mouses hovering position.
     * Updates the popovers location and/or displays underlying points in the popover.
     *
     * @return mouse motion listener for the popover
     */
    private MouseMotionListener getHoverListener() {
        return new MouseMotionAdapter() {

            /**
             * State of whether a point is being hovered.
             */
            boolean pointHovered = false;

            /**
             * Updates the popover's position in placing it at a slightly offset
             * mouse events (thus cursor) location.
             *
             * @param event mouse event to get the position
             */
            private void updatePopoverLocation(final MouseEvent event) {
                popover.setLocation(event.getX() + 10, event.getY() - 5);
            }

            /**
             * {@inheritDoc}
             * Updates the popover location on drag.
             *
             * @param event mouse event to pass through to the update
             * @see #updatePopoverLocation(MouseEvent)
             */
            @Override
            public void mouseDragged(final MouseEvent event) {
                updatePopoverLocation(event);
            }

            /**
             * Update the point being displayed in the popover (if one exists).
             * Otherwise, the popover disappears.
             *
             * @param event mouse event to get the mouse's position from.
             */
            @Override
            public void mouseMoved(final MouseEvent event) {
                final Point2D closestPoint = getClosestPoint(event);

                if (closestPoint != null) {
                    pointHovered = true;
                    popover.setText(String.format("(%.2f, %.2f)", closestPoint.getX(), closestPoint.getY()));
                    updatePopoverLocation(event);
                    popover.setVisible(true);
                    repaint();
                } else {
                    popover.setVisible(false);
                    popover.setText("");
                    if (pointHovered) repaint();
                    pointHovered = false;
                }
            }

            /**
             * Determine the closest point to the mouse position.
             *
             * @param event mouse hover event
             * @return closest drawn point to mouse cursor
             */
            @Nullable
            private Point2D getClosestPoint(final MouseEvent event) {
                final Point2D revertedPosition = transformation.revert(event.getPoint());
                final double epsilonEnvironment = 10 / transformation.getScale();

                Point2D closestPoint = null;
                double minDistance = Double.POSITIVE_INFINITY;

                for (final Point2D point2D : points.keySet()) {
                    final double thisDistance = point2D.distance(revertedPosition);
                    if (thisDistance < epsilonEnvironment && thisDistance < minDistance) {
                        minDistance = thisDistance;
                        closestPoint = point2D;
                    }
                }
                return closestPoint;
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
    @SneakyThrows
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D graphics2D = (Graphics2D) g.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
            final Point2D offset = transformation.getOffset();
            final double scale = transformation.getScale();
            graphics2D.drawString(string.getString(), (float) (scale * string.getX() + offset.getX()) + 5, (float) (scale * string.getY() + offset.getY()) + 8);
        } else if (shape instanceof Ray2D) {
            final double visible = getMaxDistanceVisible();
            render(graphics2D, ((Ray2D) shape).render(visible), style);
        } else {
            final Shape transformedShape = transformation.getAffineTransformation().createTransformedShape(shape);
            graphics2D.draw(transformedShape);
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
        final float scale = 3 / (float) transformation.getScale();
        switch (style.getShape()) {
            case DOT:
                render(graphics2D, new Ellipse2D.Double(x - scale, y - scale, 2 * scale, 2 * scale), style);
                return;
            case CROSS:
                final GeneralPath path = new GeneralPath();
                path.append(new Line2D.Double(x - scale, y + scale, x + scale, y - scale), false);
                path.append(new Line2D.Double(x - scale, y - scale, x + scale, y + scale), false);
                render(graphics2D, path, style);
                return;
            case RECT:
                render(graphics2D, new Rectangle2D.Double(x - scale, y - scale, 2 * scale, 2 * scale), style);
        }
    }

    /**
     * Render a single chromosome as a series of points connected with lines.
     *
     * @param chromosome chromosome to render
     */
    public void render(final Chromosome<DiscreteGene> chromosome) {

        renderRays(rays);

        if (chromosome == null) return;

        final AtomicInteger index = new AtomicInteger();
        chromosome.forEach(gene -> {
            final Point2D point = gene.allele();
            enqueue(String.valueOf(index.get() + 1), point, Style.builder().color(Color.WHITE).build());
            enqueue(point, Style.builder().color(Color.GREEN).build());
            index.getAndIncrement();
        });

        if (rays > 2) {
            Point2D previous = new Point2D.Double();
            for (final DiscreteGene discreteGene : chromosome) {
                final Point2D current = discreteGene.allele();
                enqueue(
                        new Line2D.Double(previous, current),
                        Style.builder().color(Color.WHITE).build()
                );
                previous = current;
            }
        }
    }

    /**
     * Render the rays from the origin.
     *
     * @param amount amount of rays to render
     */
    public void renderRays(final int amount) {
        for (int position = 0; position < amount; position++) {
            final double x = Math.cos(position / (double) amount * MathUtils.TWO_PI);
            final double y = Math.sin(position / (double) amount * MathUtils.TWO_PI);
            enqueue(
                    new Ray2D(0, 0, x, y),
                    Style.builder()
                            .color(Color.lightGray)
                            .stroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{4}, 0))
                            .build()
            );
        }
    }

    /**
     * Render the treasure points as red crosses.
     *
     * @param treasures list of treasure points
     */
    public void renderTreasures(final List<DiscreteGene> treasures) {
        treasures.forEach(treasure -> enqueue(
                treasure.allele(),
                Style.builder().color(Color.RED).shape(Style.Shape.CROSS).build()
        ));
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

    /**
     * The max distance visible is the maximal distance between the coordinate systems
     * origin and one of the four canvas boundary edges. This distance is given in a coordinate-
     * logical/unscaled way.
     *
     * @return maximal distance visible by canvas
     */
    public double getMaxDistanceVisible() {
        final Point2D offset = transformation.getOffset();
        return Collections.max(List.of(
                offset.distance(0, 0),
                offset.distance(getWidth(), 0),
                offset.distance(0, getHeight()),
                offset.distance(getWidth(), getHeight())
        )) / transformation.getScale();
    }

}
