package evo.search.view;

import lombok.Builder;
import lombok.Getter;

import java.awt.*;

@Builder
@Getter
public class Style {

    public static final Style DEFAULT = Style.builder().build();

    @Builder.Default
    private Stroke stroke = new BasicStroke(.3f);
    @Builder.Default
    private Color color = Color.BLACK;
    @Builder.Default
    private Color fill = Color.BLACK;
    @Builder.Default
    private boolean filled = false;
    @Builder.Default
    private Shape shape = Shape.RECT;

    public enum Shape {
        RECT,
        CROSS,
        DOT
    }
}
