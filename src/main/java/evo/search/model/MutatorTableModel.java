package evo.search.model;

import evo.search.ga.mutators.DiscreteMutator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class MutatorTableModel extends AbstractTableModel {

    private List<MutatorConfig> data = new ArrayList<>();

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex != 1;
    }

    public void addMutator(boolean selected, Class<? extends DiscreteMutator> mutator, double probability) {
        data.add(new MutatorConfig(selected, mutator, probability));
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (rowIndex >= data.size() || columnIndex >= getColumnCount()) {
            return null;
        }
        final MutatorConfig row = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return row.isSelected();
            case 1:
                return row.getMutator().getSimpleName();
        }
        return row.getProbability();
    }

    @Override
    public void setValueAt(final Object aValue, final int row, final int column) {
        final MutatorConfig config = data.get(row);
        switch (column) {
            case 0:
                config.setSelected((boolean) aValue);
                return;
            case 1:
                throw new IllegalStateException("You cannot edit existing mutator class names.");
            case 2:
                config.setProbability((double) aValue);
        }
    }

    @Override
    public String getColumnName(final int column) {
        switch (column) {
            case 0:
                return "Selected";
            case 1:
                return "Mutator";
        }
        return "Probability";
    }

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

    public List<DiscreteMutator> getSelected() {
        return data.stream()
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

    @Data
    @AllArgsConstructor
    public static class MutatorConfig {
        boolean selected;
        Class<? extends DiscreteMutator> mutator;
        double probability;
    }
}
