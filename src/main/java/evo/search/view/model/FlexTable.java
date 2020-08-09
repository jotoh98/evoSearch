package evo.search.view.model;

import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Flexible {@link JTable} extension for cell specific editors and renderers.
 */
@NoArgsConstructor
@Setter
public class FlexTable extends JTable {

    /**
     * Cell specific cell-editor.
     */
    SpecificCellEditor specificCellEditor;

    /**
     * Cell specific cell-renderer.
     */
    SpecificCellRenderer specificCellRenderer;

    /**
     * Full constructor with model, editor and renderer.
     *
     * @param dm                   table model
     * @param specificCellEditor   cell specific editor
     * @param specificCellRenderer cell specific renderer
     */
    public FlexTable(final TableModel dm, final SpecificCellEditor specificCellEditor, final SpecificCellRenderer specificCellRenderer) {
        super(dm);
        this.specificCellEditor = specificCellEditor;
        this.specificCellRenderer = specificCellRenderer;
    }

    /**
     * Get the editor for a specific cell.
     *
     * @param row    cell row index
     * @param column cell column index
     * @return cell editor for specific cell
     */
    @Override
    public TableCellEditor getCellEditor(final int row, final int column) {
        final TableCellEditor editor = specificCellEditor == null ? null : specificCellEditor.getEditor(row, column);
        if (editor != null) {
            return editor;
        }
        return super.getCellEditor(row, column);
    }

    /**
     * Get the renderer for a specific cell.
     *
     * @param row    cell row index
     * @param column cell column index
     * @return cell renderer for specific cell
     */
    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        final TableCellRenderer renderer = specificCellRenderer == null ? null : specificCellRenderer.getRenderer(row, column);
        if (renderer != null) {
            return renderer;
        }
        return super.getCellRenderer(row, column);
    }
}
