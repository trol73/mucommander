package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

/**
 * <code>OrFileFilter</code> is a {@link ChainedFileFilter} that matches a file if one of its registered filters 
 * matches it.
 *
 * @author Maxence Bernard
 */
public class OrFileFilter extends ChainedFileFilter {

    /**
     * Creates a new <code>OrFileFilter</code> operating in non-inverted mode and containing the specified filters,
     * if any.
     *
     * @param filters filters to add to this chained filter.
     */
    public OrFileFilter(FileFilter... filters) {
        this(false, filters);
    }

    /**
     * Creates a new <code>OrFileFilter</code> operating in the specified mode and containing the specified filters,
     * if any.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     * @param filters filters to add to this chained filter.
     */
    public OrFileFilter(boolean inverted, FileFilter... filters) {
        super(inverted, filters);
    }



    /**
     * Calls {@link #match(com.mucommander.commons.file.AbstractFile)} on each of the registered filters, and returns
     * <code>true</code> if one of them matched the given file, <code>false</code> if none of them did.
     *
     * <p>If this {@link ChainedFileFilter} contains no filter, this method will always return <code>true</code>.
     *
     * @param file the file to test against the registered filters
     * @return if the file was matched by one filter, false if none of them did
     */
    @Override
    public boolean accept(AbstractFile file) {
        for (FileFilter filter : filters) {
            if (filter.match(file)) {
                return true;
            }
        }
        return filters.isEmpty();
    }
}
