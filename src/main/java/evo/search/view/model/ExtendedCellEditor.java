package evo.search.view.model;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ExtendedCellEditor extends DefaultCellEditor {
    public ExtendedCellEditor(final JTextField textField) {
        super(textField);
    }

    public ExtendedCellEditor(final JCheckBox checkBox) {
        super(checkBox);
    }

    public ExtendedCellEditor(final JComboBox<?> comboBox) {
        super(comboBox);
    }

    public ExtendedCellEditor(final JTextArea textArea) {
        //TODO: resize textarea's row with input
        this();
        editorComponent = textArea;
        this.clickCountToStart = 2;
        delegate = new EditorDelegate() {
            public void setValue(Object value) {
                textArea.setText((value != null) ? value.toString() : "");
            }

            public Object getCellEditorValue() {
                return textArea.getText();
            }
        };
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    delegate.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString(), e.getModifiersEx()));
                    editorComponent.setMinimumSize(editorComponent.getPreferredSize());
                }
            }
        });
    }

    public ExtendedCellEditor() {
        super(new JTextField());
    }


}
