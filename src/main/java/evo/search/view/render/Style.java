package evo.search.view.render;

import lombok.Builder;
import lombok.Getter;

import java.awt.*;

/**
 * Render style class for {@link java.awt.Shape}s.
 */
@Builder
@Getter
public class Style {

    /**
     * Default style instance.
     */
    public static final Style DEFAULT = Style.builder().build();

    /**
     * The stroke style.
     */
    @Builder.Default
    private final Stroke stroke = new BasicStroke(2);

    /**
     * The stroke color of the shape.
     */
    @Builder.Default
    private final Color color = Color.BLACK;

    /**
     * The fill color of the shape.
     * Is ignored, if {@link #filled} is set to {@code false}.
     */
    @Builder.Default
    private final Color fill = Color.BLACK;

    /**
     * Defines, if the shape is filled or not.
     */
    @Builder.Default
    private final boolean filled = false;

    /**
     * Point shape representation.
     *
     * @see Shape
     */
    @Builder.Default
    private final Shape shape = Shape.RECT;

    /**
     * Rendering font for {@link java.awt.font.GlyphVector}s.
     */
    @Builder.Default
    private final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    /**
     * List of point shape styles.
     */
    public enum Shape {
        RECT,
        CROSS,
        DOT
    }
}
