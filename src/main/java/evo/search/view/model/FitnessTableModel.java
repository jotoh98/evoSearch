package evo.search.view.model;

import io.jenetics.Phenotype;
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
    List<Double> fitness = new ArrayList<>();

    /**
     * Name of the first column.
     */
    @Setter
    String firstColumnName = "Generation";

    /**
     * Setter for the tables fitness values.
     * Fires {@link javax.swing.event.TableModelEvent} for change.
     *
     * @param fitness fitness value to display
     */
    public void setFitness(final List<Double> fitness) {
        this.fitness = fitness;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        if (fitness == null) return 0;
        return fitness.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (columnIndex == 0)
            return rowIndex + 1;
        try {
            return fitness.get(rowIndex);
        } catch (final IndexOutOfBoundsException ignored) {}
        return 0;
    }

    @Override
    public String getColumnName(final int column) {
        if (column == 0)
            return firstColumnName;
        return "Fitness";
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return columnIndex == 0 ? Integer.class : Double.class;
    }

    /**
     * Add a result from an evolution to the table.
     *
     * @param phenotype evolution to compute the fitness
     */
    public void addResult(final Phenotype<?, Double> phenotype) {
        fitness.add(phenotype.fitness());
        fireTableDataChanged();
    }

    /**
     * Clear the table.
     */
    public void clear() {
        final int size = fitness.size();
        if (size == 0)
            return;
        fitness = new ArrayList<>();
        fireTableRowsDeleted(0, size - 1);
    }
}
