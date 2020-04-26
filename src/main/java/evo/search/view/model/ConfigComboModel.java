package evo.search.view.model;

import evo.search.io.EventService;
import evo.search.io.entities.Configuration;
import evo.search.view.LangService;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class ConfigComboModel extends DefaultComboBoxModel<Object> {

    public static final String EDIT_TEXT = LangService.get("configuration.edit");

    public ConfigComboModel() {
        super();
    }

    @Override
    public Object getElementAt(final int index) {
        if (index == 0) {
            return EDIT_TEXT;
        }
        return super.getElementAt(index - 1);
    }

    @Override
    public int getSize() {
        return 1 + super.getSize();
    }

    @Override
    public void setSelectedItem(final Object anObject) {
        if (anObject.equals(EDIT_TEXT)) {
            if (getSelectedItem() == null) {
                super.setSelectedItem(getElementAt(1));
                EventService.OPEN_CONFIG.trigger(asList());
            }
            return;
        }
        super.setSelectedItem(anObject);
    }

    public List<Configuration> asList() {
        return IntStream.range(1, getSize())
                .mapToObj(index -> (Configuration) getElementAt(index))
                .collect(Collectors.toList());
    }
}

/*public class ConfigComboModel implements ComboBoxModel<Object> {

    private static final String BUTTON_TEXT = "Edit Configurations";

    @Getter
    List<Configuration> configurations = new ArrayList<>();

    Configuration selectedItem;

    @Override
    public void setSelectedItem(final Object anItem) {
        if(anItem instanceof Configuration) {
            selectedItem = (Configuration) anItem;
        }
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return 1 + configurations.size();
    }

    @Override
    public Object getElementAt(final int index) {
        if (index == 0) {
            return BUTTON_TEXT;
        }
        return configurations.get(index - 1);
    }

    @Override
    public void addListDataListener(final ListDataListener l) {

    }

    @Override
    public void removeListDataListener(final ListDataListener l) {

    }
}*/
