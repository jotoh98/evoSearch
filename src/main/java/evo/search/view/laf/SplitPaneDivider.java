package evo.search.view.laf;

import com.bulenkov.darcula.ui.DarculaSplitPaneUI;
import com.bulenkov.iconloader.util.DoubleColor;
import com.bulenkov.iconloader.util.Gray;
import com.bulenkov.iconloader.util.UIUtil;

import javax.swing.*;
import javax.swing.border.Border;
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


    @Override
    protected JButton createLeftOneTouchButton() {
        JButton b = new JButton() {
            public void setBorder(Border b) {
            }

            public void paint(Graphics g) {
                if (splitPane != null) {
                    int[] xs = new int[3];
                    int[] ys = new int[3];
                    int blockSize;

                    // Fill the background first ...
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, this.getWidth(), this.getHeight());

                    // ... then draw the arrow.
                    g.setColor(new DoubleColor(Gray._255, UIUtil.getLabelForeground()));
                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
                        blockSize = Math.min(getHeight(), ONE_TOUCH_SIZE);
                        xs[0] = blockSize;
                        xs[1] = 0;
                        xs[2] = blockSize << 1;
                        ys[0] = 0;
                        ys[1] = ys[2] = blockSize;
                        g.drawPolygon(xs, ys, 3); // Little trick to make the
                        // arrows of equal size
                    } else {
                        blockSize = Math.min(getWidth(), ONE_TOUCH_SIZE);
                        xs[0] = xs[2] = blockSize;
                        xs[1] = 0;
                        ys[0] = 0;
                        ys[1] = blockSize;
                        ys[2] = blockSize << 1;
                    }
                    g.fillPolygon(xs, ys, 3);
                }
            }

            // Don't want the button to participate in focus traversable.
            @Override
            public boolean isFocusable() {
                return false;
            }
        };
        b.setMinimumSize(new Dimension(ONE_TOUCH_SIZE, ONE_TOUCH_SIZE));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRequestFocusEnabled(false);
        return b;
    }

    @Override
    protected JButton createRightOneTouchButton() {
        JButton b = new JButton() {
            public void setBorder(Border border) {
            }

            public void paint(Graphics g) {
                if (splitPane != null) {
                    int[] xs = new int[3];
                    int[] ys = new int[3];
                    int blockSize;

                    // Fill the background first ...
                    g.setColor(this.getBackground());
                    g.fillRect(0, 0, this.getWidth(), this.getHeight());

                    // ... then draw the arrow.
                    if (orientation == JSplitPane.VERTICAL_SPLIT) {
                        blockSize = Math.min(getHeight(), ONE_TOUCH_SIZE);
                        xs[0] = blockSize;
                        xs[1] = blockSize << 1;
                        xs[2] = 0;
                        ys[0] = blockSize;
                        ys[1] = ys[2] = 0;
                    } else {
                        blockSize = Math.min(getWidth(), ONE_TOUCH_SIZE);
                        xs[0] = xs[2] = 0;
                        xs[1] = blockSize;
                        ys[0] = 0;
                        ys[1] = blockSize;
                        ys[2] = blockSize << 1;
                    }
                    g.setColor(new DoubleColor(Gray._255, UIUtil.getLabelForeground()));
                    g.fillPolygon(xs, ys, 3);
                }
            }

            // Don't want the button to participate in focus traversable.
            @Override
            public boolean isFocusable() {
                return false;
            }
        };
        b.setMinimumSize(new Dimension(ONE_TOUCH_SIZE, ONE_TOUCH_SIZE));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setRequestFocusEnabled(false);
        return b;
    }
}

