package com.mucommander.commons.file.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * ChainedFileFilter combines one or several {@link FileFilter} to act as just one.
 *{@link #addFileFilter(FileFilter)} and {@link #removeFileFilter(FileFilter)} allow to add or remove a
 * <code>FileFilter</code>, {@link #getFileFilterIterator()} to iterate through all the registered filters.
 *
 * <p>The {@link AndFileFilter} and {@link OrFileFilter} implementations match files that respectively match all of
 * the registered filters, or any of them.
 *
 * @see AndFileFilter
 * @see OrFileFilter
 * @author Maxence Bernard
 */
public abstract class ChainedFileFilter extends AbstractFileFilter {

    /** List of registered FileFilter */
    protected List<FileFilter> filters = new ArrayList<>();

    /**
     * Creates a new <code>ChainedFileFilter</code> operating in non-inverted mode and containing the specified filters,
     * if any.
     *
     * @param filters filters to add to this chained filter.
     */
    public ChainedFileFilter(FileFilter... filters) {
        this(false, filters);
    }

    /**
     * Creates a new <code>ChainedFileFilter</code> operating in the specified mode and containing the specified filters,
     * if any.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     * @param filters filters to add to this chained filter.
     */
    public ChainedFileFilter(boolean inverted, FileFilter... filters) {
        super(inverted);

        for (FileFilter filter : filters)
            addFileFilter(filter);
    }

    /**
     * Adds a new {@link FileFilter} to the list of chained filters.
     *
     * @param filter the FileFilter to add
     */
    public void addFileFilter(FileFilter filter) {
        filters.add(filter);
    }

    /**
     * Removes a {@link FileFilter} from the list of chained filters. Does nothing if the given <code>FileFilter</code>
     * is not contained by this <code>ChainedFileFilter</code>.
     *
     * @param filter the FileFilter to remove
     */
    public void removeFileFilter(FileFilter filter) {
        filters.remove(filter);
    }

    /**
     * Returns an <code>Iterator</code> that traverses all the registered filters.
     *
     * @return an <code>Iterator</code> that traverses all the registered filters. 
     */
    public Iterator<FileFilter> getFileFilterIterator() {
        return filters.iterator();
    }

    /**
     * Returns <code>true</code> if this chained filter doesn't contain any file filter.
     *
     * @return <code>true</code> if this chained filter doesn't contain any file filter.
     */
    public boolean isEmpty() {
        return filters.isEmpty();
    }
}
