package evo.search.view.model;

import evo.search.ga.AnalysisUtils;
import evo.search.ga.DiscreteGene;
import io.jenetics.Phenotype;
import io.jenetics.util.ISeq;
import lombok.Getter;

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
    List<List<Double>> fitness = new ArrayList<>();

    /**
     * Column names.
     */
    @Getter
    List<String> columnNames = new ArrayList<>(List.of(
            "Generation",
            "Fitness",
            "Spiral likeness",
            "Spiral invariant"
    ));

    /**
     * Maps a evolution result to a output row for the table.
     *
     * @param phenotype evolution result to output
     * @return evaluated fitness/measure list
     */
    public static List<Double> mapResult(final Phenotype<DiscreteGene, Double> phenotype) {
        final List<DiscreteGene> genes = ISeq.of(phenotype.genotype().chromosome()).asList();
        return List.of(
                phenotype.fitness(),
                AnalysisUtils.spiralLikeness(genes),
                AnalysisUtils.spiralLikenessInvariant(genes)
        );
    }

    @Override
    public int getRowCount() {
        if (fitness == null) return 0;
        return fitness.size();
    }

    /**
     * Setter for the tables fitness values.
     * Fires {@link javax.swing.event.TableModelEvent} for change.
     *
     * @param fitness fitness value to display
     */
    public void setFitness(final List<List<Double>> fitness) {
        this.fitness = fitness;
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
            return fitness.get(rowIndex).get(columnIndex - 1);
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
     * Add a result from an evolution to the table.
     *
     * @param phenotype evolution to compute the fitness
     */
    public void addResult(final Phenotype<DiscreteGene, Double> phenotype) {
        fitness.add(mapResult(phenotype));
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
