package evo.search.view.laf;

import com.bulenkov.darcula.ui.DarculaSplitPaneUI;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;

public class SplitPaneUI extends DarculaSplitPaneUI {

    @SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass", "UnusedDeclaration"})
    public static ComponentUI createUI(JComponent c) {
        return new SplitPaneUI();
    }

    @Override
    public BasicSplitPaneDivider createDefaultDivider() {
        return new SplitPaneDivider(this);
    }
}
