package evo.search.view.model;

import evo.search.view.LangService;

import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class ConfigTableModel extends DefaultTableModel {

    public ConfigTableModel() {
        super();
        addColumn(LangService.get("property"));
        addColumn(LangService.get("value"));
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return columnIndex == 1;
    }

    public <T> void addConfig(String key, T value) {
        addRow(new Object[]{LangService.get(key), value});
    }

    public Object getConfig(String key) {
        return dataVector.stream()
                .filter(vector -> vector.get(0).equals(LangService.get(key)))
                .map(Vector::lastElement)
                .findFirst()
                .orElse(null);
    }

    public int getIntConfig(String key) {
        final Object config = getConfig(key);
        if (config instanceof Integer) {
            return (int) config;
        }
        if (config instanceof String) {
            return Integer.parseInt((String) config);
        }
        return 0;
    }

}
