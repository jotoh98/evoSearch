package evo.search.view.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.table.TableCellRenderer;
import java.util.Hashtable;

/**
 * Container for {@link TableCellRenderer}s associated to specific table cells.
 */
@AllArgsConstructor
public class SpecificCellRenderer {

    /**
     * Internal table of associated cell renderers.
     */
    @Getter
    private final Hashtable<Integer, Hashtable<Integer, TableCellRenderer>> renderers = new Hashtable<>();

    /**
     * Set a cell renderer to a specific table cell given by its row and column index.
     *
     * @param row      Row index of the cell.
     * @param column   Column index of the cell.
     * @param renderer Renderer to be associated to the table cell.
     */
    public void setRenderer(final int row, final int column, TableCellRenderer renderer) {
        renderers.computeIfAbsent(row, k -> new Hashtable<>());
        renderers.get(row).put(column, renderer);
    }

    /**
     * Get a cell renderer for a specific table cell given by its row and column index.
     *
     * @param row    Row index of the cell.
     * @param column Column index of the cell.
     * @return Renderer to be associated to the table cell.
     */
    public TableCellRenderer getRenderer(final int row, final int column) {
        return renderers.get(row) == null ? null : renderers.get(row).get(column);
    }
}
