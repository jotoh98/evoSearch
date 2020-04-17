package evo.search.view.model;

import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

@NoArgsConstructor
@Setter
public class FlexTable extends JTable {

    SpecificCellEditor specificCellEditor;

    SpecificCellRenderer specificCellRenderer;

    public FlexTable(final TableModel dm, final SpecificCellEditor specificCellEditor, final SpecificCellRenderer specificCellRenderer) {
        super(dm);
        this.specificCellEditor = specificCellEditor;
        this.specificCellRenderer = specificCellRenderer;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        TableCellEditor editor = specificCellEditor == null ? null : specificCellEditor.getEditor(row, column);
        if (editor != null) {
            return editor;
        }
        return super.getCellEditor(row, column);
    }

    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        TableCellRenderer renderer = specificCellRenderer == null ? null : specificCellRenderer.getRenderer(row, column);
        if (renderer != null) {
            return renderer;
        }
        return super.getCellRenderer(row, column);
    }
}
