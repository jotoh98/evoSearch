package evo.search.view.model;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Table model to display several fitness values.
 *
 * @author jotoh
 */
public class FitnessTableModel extends DefaultTableModel {

    /**
     * Default constructor with default column names.
     */
    public FitnessTableModel() {
        this.columnIdentifiers = convertToVector(new String[]{
                "Generation",
                "Fitness",
                "Worst Case",
                "Optimal Worst Case",
                "Closeness Factor"
        });
    }

    /**
     * Setter for the tables fitness values.
     * Fires {@link javax.swing.event.TableModelEvent} for change.
     *
     * @param data fitness value to display
     */
    public void setData(final List<List<Double>> data) {
        final Object[][] rows = data.stream().map(row -> row.toArray(Object[]::new)).toArray(Object[][]::new);
        setDataVector(rows, columnIdentifiers.toArray());
        fireTableDataChanged();
    }

    /**
     * Set the column name at the {@code n}th column.
     *
     * @param n          column index
     * @param identifier new column name
     */
    public void setColumnIdentifier(final int n, final Object identifier) {
        columnIdentifiers.set(n, identifier);
        fireTableStructureChanged();
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return columnIndex == 0 ? Integer.class : Double.class;
    }

    /**
     * Add a row to the table.
     *
     * @param row row to add
     */
    public void addRow(final List<Double> row) {
        addRow(row.toArray(Double[]::new));
    }

    @Override
    public Object getValueAt(final int row, final int column) {
        if (column == 0)
            return row + 1;
        return super.getValueAt(row, column - 1);
    }

    /**
     * Clear the table.
     */
    public void clear() {
        final int size = dataVector.size();
        if (size == 0) return;
        dataVector.removeAllElements();
        fireTableRowsDeleted(0, size - 1);
    }
}
