package evo.search.view.part;

import evo.search.ga.DiscreteGene;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jotoh
 */
public class GeneListCellEditor extends DefaultCellEditor {

    public static final Pattern PATTERN = Pattern.compile("(\\d+(?:.\\d+)?)\\s*,\\s*(\\d+(?:.\\d+)?)");

    public GeneListCellEditor(final JTextField field) {
        super(field);
        delegate = new EditorDelegate() {

            private DiscreteGene old = null;

            @Override
            public Object getCellEditorValue() {
                final Matcher matcher = PATTERN.matcher(getTextField().getText());
                if (matcher.find()) {
                    return parseGene(matcher);
                }
                return old;
            }

            @Override
            public void setValue(final Object value) {
                if (value instanceof DiscreteGene) {
                    old = (DiscreteGene) value;
                    field.setText(old.printSmall());
                } else
                    super.setValue("");
            }
        };
    }

    @NotNull
    public static DiscreteGene parseGene(final Matcher matcher) {
        final short position = Short.parseShort(matcher.group(1));
        final float distance = Float.parseFloat(matcher.group(2));
        return new DiscreteGene(0, position, distance);
    }

    public JTextField getTextField() {
        return (JTextField) editorComponent;
    }
}
