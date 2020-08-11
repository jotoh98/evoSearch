package evo.search.view.model;

import evo.search.Evolution;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.view.LangService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.table.DefaultTableModel;

/**
 * This table model displays all registered {@link DiscreteAlterer} classes to select and configure
 * for the {@link Evolution#run()} method.
 */
@Slf4j
public class MutatorTableModel extends DefaultTableModel {

    /**
     * Default constructor.
     * Fills the table with all available alterers.
     */
    public MutatorTableModel() {
        DiscreteAlterer.getSubclasses()
                .forEach(altererClass -> addRow(new Object[]{altererClass.getSimpleName(), 0.5d}));
    }

    /**
     * Returns false just for the middle name column.
     *
     * @param rowIndex    Index of the queried row. Is ignored.
     * @param columnIndex Index of the column. Not editable is the second column.
     * @return {@code false} for the middle column, {@code true} for the other columns.
     */
    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(final int column) {
        return LangService.get(column == 0 ? "mutator" : "probability");
    }

    /**
     * Returns a String class for the first column
     * and a Double class for the rest.
     *
     * @return String/Double class depending on column
     */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {

        return columnIndex == 0 ? String.class : Double.class;
    }

    /**
     * Get the row index for the given name.
     *
     * @param name simple name in first column
     * @return row index
     */
    public int getRowByName(final String name) {
        for (int row = 0; row < getRowCount(); row++) {
            final String simpleName = (String) getValueAt(row, 0);
            if (simpleName.equals(name)) return row;
        }
        return -1;
    }
}
