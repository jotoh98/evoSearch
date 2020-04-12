package evo.search.view.laf;

import com.bulenkov.darcula.ui.DarculaSplitPaneUI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SplitPaneDivider extends BasicSplitPaneDivider {

    private static final int DRAG_ICON_DIMENSION = 5;

    private boolean whiten = false;


    /**
     * Creates an instance of DarculaSplitPaneDivider. Registers this
     * instance for mouse events and mouse dragged events.
     */
    public SplitPaneDivider(DarculaSplitPaneUI ui) {
        super(ui);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseExited(final MouseEvent e) {
                whiten = false;
                repaint();
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
                whiten = true;
                repaint();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        final Graphics2D graphics2D = (Graphics2D) g;

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHints(rh);

        whiten &= isMouseOver();
        whiten |= draggingActive();
        graphics2D.setColor(new Color(255, 255, 255, whiten ? 40 : 10));

        if (splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
            final int width = Math.max(20, Math.min(100, splitPane.getWidth() - 20));
            final int y = (getHeight() - DRAG_ICON_DIMENSION) / 2;
            final int x = (splitPane.getWidth() - width) / 2;
            graphics2D.fillRoundRect(x, y, width, DRAG_ICON_DIMENSION, DRAG_ICON_DIMENSION, DRAG_ICON_DIMENSION);
        } else {
            final int height = Math.max(20, Math.min(100, splitPane.getWidth() - 20));
            final int y = (splitPane.getHeight() - height) / 2;
            final int x = (getWidth() - DRAG_ICON_DIMENSION) / 2;
            graphics2D.fillRoundRect(x, y, DRAG_ICON_DIMENSION, 100, DRAG_ICON_DIMENSION, DRAG_ICON_DIMENSION);
        }
    }

    private boolean draggingActive() {
        return dragger != null;
    }


}

