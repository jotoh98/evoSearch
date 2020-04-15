package evo.search.view.model;

import evo.search.view.LangService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigTableModel extends AbstractTableModel {

    @Getter
    List<ConfigProperty> data = new ArrayList<>();

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final ConfigProperty row = data.get(rowIndex);
        return columnIndex == 0 ? LangService.get(row.getDisplay()) : row.getValue();
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {

        if (columnIndex == 0) {
            return;
        }

        final Object oldValue = getValueAt(rowIndex, columnIndex);
        final String input = aValue.toString();
        final ConfigProperty row = data.get(rowIndex);

        if (oldValue instanceof List) {
            setListValue(row, input);
        } else if (oldValue instanceof Double) {
            row.setValue(Double.parseDouble(input));
        } else if (oldValue instanceof Integer) {
            row.setValue(Integer.parseInt(input));
        } else if (oldValue instanceof String) {
            row.setValue(input);
        }
    }

    private void setListValue(final ConfigProperty row, final String string) {
        final String input = string
                .replaceAll("[\\[\\]\\(\\) ]*", "");
        final List<Double> distances = Arrays.stream(input.split("\\s*,\\s*"))
                .mapToDouble(Double::valueOf)
                .boxed()
                .collect(Collectors.toList());
        row.setValue(distances);
    }

    public void addConfig(String display, Object value) {
        data.add(new ConfigProperty(display, display, value));
    }

    public Object getConfig(String key) {
        return data.stream()
                .filter(property -> property.getKey().equals(key))
                .findFirst()
                .map(ConfigProperty::getValue)
                .orElse(null);
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return data.get(columnIndex).getValue().getClass();
    }

    @Override
    public String getColumnName(final int column) {
        return LangService.get(column == 0 ? "property" : "value");
    }

    @AllArgsConstructor
    @Data
    public static class ConfigProperty {
        String key;
        String display;
        Object value;
    }
}
