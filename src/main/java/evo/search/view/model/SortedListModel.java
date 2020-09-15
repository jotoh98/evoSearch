package evo.search.view.model;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.util.Comparator;
import java.util.Vector;

/**
 * @author jotoh
 */
@RequiredArgsConstructor
public class SortedListModel<E> extends AbstractListModel<E> {

    /**
     * Comparator to sort the list items.
     */
    @NonNull
    Comparator<E> comparator;

    /**
     * List of the models items.
     */
    Vector<E> delegate = new Vector<>();

    @Override
    public int getSize() {
        return delegate.size();
    }

    @Override
    public E getElementAt(final int index) {
        return delegate.elementAt(index);
    }

    /**
     * Add an element to the list. Sorts the list.
     *
     * @param element element to add
     */
    public void addElement(final E element) {
        delegate.addElement(element);
        sort();
        fireContentsChanged(this, 0, getSize());
    }

    /**
     * Removes the element at the given index.
     *
     * @param index position to remove an element from
     */
    public void remove(final int index) {
        delegate.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    /**
     * Deletes the components at the specified range of indexes.
     * The removal is inclusive, so specifying a range of (1,5)
     * removes the component at index 1 and the component at index 5,
     * as well as all components in between.
     *
     * @param fromIndex the index of the lower end of the range
     * @param toIndex   the index of the upper end of the range
     * @throws ArrayIndexOutOfBoundsException if the index was invalid
     * @throws IllegalArgumentException       if {@code fromIndex &gt; toIndex}
     * @see #remove(int)
     */
    public void removeRange(final int fromIndex, final int toIndex) {
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex must be <= toIndex");

        for (int i = toIndex; i >= fromIndex; i--)
            delegate.removeElementAt(i);

        fireIntervalRemoved(this, fromIndex, toIndex);
    }

    /**
     * Searches for the first occurrence of {@code elem}.
     *
     * @param element an object
     * @return the index of the first occurrence of the argument in this
     * list; returns {@code -1} if the object is not found
     * @see Vector#indexOf(Object)
     */
    public int indexOf(final E element) {
        return delegate.indexOf(element);
    }

    /**
     * Sort the list.
     */
    public void sort() {
        delegate.sort(comparator);
        fireContentsChanged(this, 0, getSize());
    }
}
