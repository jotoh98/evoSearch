package evo.search.view.model;

import evo.search.Evolution;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.view.LangService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                .forEach(altererClass -> addRow(new Object[]{false, altererClass.getSimpleName(), 0.5d}));
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
        return columnIndex != 1;
    }

    /**
     * Adds a mutator class to be displayed as selected.
     *
     * @param name        SimpleName of the {@link DiscreteAlterer}s class
     * @param probability Standard probability for the mutator
     */
    public void addMutator(final String name, final double probability) {
        for (int row = 0; row < getRowCount(); row++) {
            final String simpleName = (String) getValueAt(row, 1);
            if (simpleName.equals(name)) {
                setValueAt(true, row, 0);
                setValueAt(probability, row, 2);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(final int column) {
        switch (column) {
            case 0:
                return LangService.get("select");
            case 1:
                return LangService.get("mutator");
        }
        return LangService.get("probability");
    }

    /**
     * Returns a Boolean and a String class for the first two columns
     * and a Double class for the rest.
     *
     * @return Boolean/String/Double class depending on column
     */
    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
        }
        return Double.class;
    }

    /**
     * Get a list of the selected alterers instances with associated probability
     *
     * @return list of selected alterers
     */
    public List<DiscreteAlterer> getSelected() {
        return IntStream.range(0, getRowCount())
                .filter(row -> (boolean) getValueAt(row, 0))
                .mapToObj(row -> {
                    final String simpleName = (String) getValueAt(row, 1);
                    final Class<? extends DiscreteAlterer> altererClass = DiscreteAlterer.getSubclasses().stream()
                            .filter(aClass -> aClass.getSimpleName().equals(simpleName))
                            .findFirst()
                            .orElse(null);

                    if (altererClass == null) return null;

                    final double probability = (double) getValueAt(row, 2);

                    try {
                        return altererClass.getDeclaredConstructor(double.class)
                                .newInstance(probability);
                    } catch (final Exception e) {
                        log.error("Could not instantiate mutator.", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
