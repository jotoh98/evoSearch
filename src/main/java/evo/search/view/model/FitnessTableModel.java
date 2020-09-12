package evo.search.view.model;

import evo.search.Evolution;
import lombok.Getter;
import lombok.Setter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jotoh
 */
public class FitnessTableModel extends AbstractTableModel {

    /**
     * Evolution fitness values to display.
     */
    List<List<Double>> data = new ArrayList<>();

    /**
     * Fitness method used in the evolution.
     */
    @Setter
    Evolution.Fitness method = Evolution.Fitness.WORST_CASE;

    /**
     * Column names.
     */
    @Getter
    List<String> columnNames = new ArrayList<>(List.of(
            "Generation",
            "Fitness",
            "Spiral likeness",
            "Spiral invariant",
            "Optimal Worst Case"
    ));

    @Override
    public int getRowCount() {
        if (data == null) return 0;
        return data.size();
    }

    /**
     * Setter for the tables fitness values.
     * Fires {@link javax.swing.event.TableModelEvent} for change.
     *
     * @param data fitness value to display
     */
    public void setData(final List<List<Double>> data) {
        this.data = data;
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (columnIndex == 0)
            return rowIndex + 1;
        try {
            return data.get(rowIndex).get(columnIndex - 1);
        } catch (final NullPointerException | IndexOutOfBoundsException ignored) {}
        return 0;
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return columnIndex == 0 ? Integer.class : Double.class;
    }

    @Override
    public String getColumnName(final int column) {
        if (column < columnNames.size())
            return columnNames.get(column);
        return "";
    }

    /**
     * Add a row to the table.
     *
     * @param row row to add
     */
    public void addRow(final List<Double> row) {
        data.add(row);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    /**
     * Clear the table.
     */
    public void clear() {
        final int size = data.size();
        if (size == 0)
            return;
        data = new ArrayList<>();
        fireTableRowsDeleted(0, size - 1);
    }
}
