package com.mucommander.commons.file;

/**
 * This class is an implementation of {@link ArchiveEntryIterator} that iterates through a single archive entry
 * specified at creation time. The entry passed to the constructor may be <code>null</code> -- the iterator will
 * act as an empty one. {@link #close()} is implemented as a no-op.
 *
 * @author Maxence Bernard
 */
public class SingleArchiveEntryIterator implements ArchiveEntryIterator {

    /** The single entry to iterate through */
    protected ArchiveEntry entry;

    public SingleArchiveEntryIterator(ArchiveEntry entry) {
        this.entry = entry;
    }

    @Override
    public ArchiveEntry nextEntry() {
        if (entry == null) {
            return null;
        }

        ArchiveEntry nextEntry = entry;
        entry = null;

        return nextEntry;
    }

    /**
     * Implemented as a no-op (nothing to close).
     */
    @Override
    public void close() {
    }
}
