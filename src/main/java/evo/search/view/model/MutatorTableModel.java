package evo.search.view.model;

import evo.search.Environment;
import evo.search.ga.mutators.DiscreteAlterer;
import evo.search.view.LangService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This table model displays all registered {@link DiscreteAlterer} classes to select and configure
 * for the {@link Environment#evolve(Function, int, List, Consumer)} method.
 */
@Slf4j
public class MutatorTableModel extends AbstractTableModel {

    /**
     * Data stored in the table.
     */
    @Getter
    final private List<MutatorConfig> mutatorConfigs = new ArrayList<>();

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
     * Adds a mutator class to be displayed.
     *
     * @param selected    Standard selected value.
     * @param mutator     Class of the {@link DiscreteAlterer}.
     * @param probability Standard probability for the mutator.
     */
    public void addMutator(boolean selected, Class<? extends DiscreteAlterer> mutator, double probability) {
        mutatorConfigs.add(new MutatorConfig(selected, mutator, probability));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        return mutatorConfigs.size();
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
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (rowIndex >= mutatorConfigs.size() || columnIndex >= getColumnCount()) {
            return null;
        }
        final MutatorConfig row = mutatorConfigs.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return row.isSelected();
            case 1:
                return row.getMutator().getSimpleName();
        }
        return row.getProbability();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(final Object aValue, final int row, final int column) {
        final MutatorConfig config = mutatorConfigs.get(row);
        switch (column) {
            case 0:
                config.setSelected((boolean) aValue);
                return;
            case 1:
                throw new IllegalStateException("You cannot edit existing mutator class names.");
            case 2:
                config.setProbability(Double.parseDouble(aValue.toString()));
        }
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
     * {@inheritDoc}
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
     * Get instances of the selected {@link DiscreteAlterer}s.
     * They are filtered by the corresponding {@link MutatorConfig#selected} property.
     * The {@link MutatorConfig#mutator} class is instantiated using the {@code double} probability
     * constructor with the {@link MutatorConfig#probability} property.
     *
     * @return A list of instantiated mutators.
     */
    public List<DiscreteAlterer> getSelected() {
        return mutatorConfigs.stream()
                .filter(MutatorConfig::isSelected)
                .map(config -> {
                    try {
                        return config.getMutator()
                                .getDeclaredConstructor(double.class)
                                .newInstance(config.getProbability());
                    } catch (Exception e) {
                        log.error("Couldn't instantiate mutator", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * The configuration of the {@link DiscreteAlterer}s.
     * Consists of a field that filters them to be selected for the
     * {@link Environment}s evolution method and a
     * {@link MutatorConfig#probability} to construct an instance of their
     * {@link MutatorConfig#mutator} class.
     */
    @Data
    @AllArgsConstructor
    public static class MutatorConfig {
        boolean selected;
        Class<? extends DiscreteAlterer> mutator;
        double probability;

        public DiscreteAlterer instantiate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            return mutator.getConstructor(double.class).newInstance(probability);
        }
    }
}
