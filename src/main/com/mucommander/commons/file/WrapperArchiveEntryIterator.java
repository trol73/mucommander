package com.mucommander.commons.file;

import java.io.IOException;
import java.util.Iterator;

/**
 * This class wraps a <code>java.util.Iterator</code> and implements <code>ArchiveEntryIterator</code> by
 * delegating methods to their <code>java.util.Iterator</code> equivalent. {@link #close()} is implemented as a no-op.
 *
 * @author Maxence Bernard
 */
public class WrapperArchiveEntryIterator implements ArchiveEntryIterator {

    /** Wrapped iterator */
    protected Iterator<? extends ArchiveEntry> iterator;

    /**
     * Creates a new <code>WrapperArchiveEntryIterator</code> that iterates through the given
     * <code>java.util.Iterator</code>'s elements.
     *
     * @param iterator the wrapped iterator
     */
    public WrapperArchiveEntryIterator(Iterator<? extends ArchiveEntry> iterator) {
        this.iterator = iterator;
    }


    @Override
    public ArchiveEntry nextEntry() {
        return iterator.hasNext() ? iterator.next() : null;
    }

    /**
     * Implemented as a no-op (nothing to close).
     */
    @Override
    public void close() throws IOException {
    }
}
