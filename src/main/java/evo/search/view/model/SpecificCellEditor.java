package evo.search.view.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.table.TableCellEditor;
import java.util.Hashtable;

/**
 * Container for {@link TableCellEditor}s associated to specific table cells.
 */
@AllArgsConstructor
public class SpecificCellEditor {

    /**
     * Internal table of associated cell editors.
     */
    @Getter
    private final Hashtable<Integer, Hashtable<Integer, TableCellEditor>> editors = new Hashtable<>();

    /**
     * Set a cell editor to a specific table cell given by its row and column index.
     *
     * @param row    Row index of the cell.
     * @param column Column index of the cell.
     * @param editor Editor to be associated to the table cell.
     */
    public void setEditor(final int row, final int column, TableCellEditor editor) {
        editors.computeIfAbsent(row, k -> new Hashtable<>());
        editors.get(row).put(column, editor);
    }

    /**
     * Get a cell editor for a specific table cell given by its row and column index.
     *
     * @param row    Row index of the cell.
     * @param column Column index of the cell.
     * @return Renderer to be associated to the table cell.
     */
    public TableCellEditor getEditor(final int row, final int column) {
        final Hashtable<Integer, TableCellEditor> editorRow = editors.get(row);
        return editorRow == null ? null : editorRow.get(column);
    }

}
