package evo.search.view.handler;

import lombok.AllArgsConstructor;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.function.Function;

/**
 * @author jotoh
 */
@AllArgsConstructor
public class ListTransferHandler<E> extends TransferHandler {


    Function<E, String> toStringMethod;
    Function<String, E> fromStringMethod;

    @Override
    public int getSourceActions(final JComponent c) {
        return TransferHandler.COPY_OR_MOVE;
    }

    @Override
    protected Transferable createTransferable(final JComponent source) {
        @SuppressWarnings("unchecked") final JList<E> sourceList = (JList<E>) source;
        final E data = sourceList.getSelectedValue();
        return new StringSelection(toStringMethod.apply(data));
    }

    @Override
    protected void exportDone(final JComponent source, final Transferable data, final int action) {
        @SuppressWarnings("unchecked") final JList<E> sourceList = (JList<E>) source;
        final E movedItem = sourceList.getSelectedValue();
        if (action != TransferHandler.MOVE) return;
        final DefaultListModel<E> listModel = (DefaultListModel<E>) sourceList.getModel();
        listModel.removeElement(movedItem);
    }

    @Override
    public boolean canImport(final TransferHandler.TransferSupport support) {
        return support.isDrop() && support.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public boolean importData(final TransferHandler.TransferSupport support) {
        if (!this.canImport(support)) return false;
        final Transferable t = support.getTransferable();
        final String transferData;
        try {
            transferData = (String) t.getTransferData(DataFlavor.stringFlavor);
        } catch (final UnsupportedFlavorException | IOException ignored) {
            return false;
        }

        final E data = fromStringMethod.apply(transferData);

        if (data == null) return false;
        final JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();

        final int dropIndex = dropLocation.getIndex();
        @SuppressWarnings("unchecked") final JList<E> targetList = (JList<E>) support.getComponent();
        final DefaultListModel<E> listModel = (DefaultListModel<E>) targetList.getModel();

        if (dropLocation.isInsert()) listModel.add(dropIndex, data);
        else listModel.set(dropIndex, data);

        return true;
    }
}
