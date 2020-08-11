package evo.search.view.model;

import evo.search.io.service.EventService;
import evo.search.io.service.ProjectService;
import evo.search.view.LangService;

import javax.swing.*;

/**
 * Combo box with edit button as first static element.
 */
public class ConfigComboModel extends DefaultComboBoxModel<Object> {

    /**
     * Translated "Edit Configuration" text.
     */
    public static final String EDIT_TEXT = LangService.get("configuration.edit");

    /**
     * Returns the item at the specified index.
     * <p>
     * Notice: The index is shifted upwards by one, because the first element is
     * the "Edit configurations" button.
     *
     * @return item at the specified index
     */
    @Override
    public Object getElementAt(final int index) {
        if (index == 0) {
            return EDIT_TEXT;
        }
        return super.getElementAt(index - 1);
    }

    /**
     * Get the amount of items in the combo box.
     * The size is increased by one due to the "Edit Configuration" button.
     *
     * @return amount of items in the combo box
     */
    @Override
    public int getSize() {
        return 1 + super.getSize();
    }

    /**
     * Set the selected item.
     * If the first item is selected (the "Edit Configuration" button), the {@link EventService#OPEN_CONFIG}
     * event is triggered.
     *
     * @param anObject item to select
     */
    @Override
    public void setSelectedItem(final Object anObject) {
        if (!anObject.equals(EDIT_TEXT)) {
            super.setSelectedItem(anObject);
            ProjectService.getCurrentProject()
                    .setSelectedConfiguration(getIndexOf(anObject));
            return;
        }
        EventService.OPEN_CONFIG.trigger(ProjectService.getCurrentProject().getConfigurations());
    }
}
