package com.mucommander.commons.file.impl.iso;

import com.mucommander.commons.file.ArchiveEntry;
import com.mucommander.commons.file.WrapperArchiveEntryIterator;
import com.mucommander.commons.io.RandomAccessInputStream;

import java.io.IOException;
import java.util.Iterator;

/**
 * This class iterates through the entries of an ISO file, and keeps the ISO file's
 * {@link #getRandomAccessInputStream RandomAccessInputStream} so that it doesn't have to be opened each time a
 * new entry is read. {@link #close} closes the stream.
 *
 * @author Maxence Bernard
 */
class IsoEntryIterator extends WrapperArchiveEntryIterator {

    /**
     * The ISO file's InputStream
     */
    private RandomAccessInputStream rais;

    public IsoEntryIterator(Iterator<? extends ArchiveEntry> iterator, RandomAccessInputStream rais) {
        super(iterator);

        this.rais = rais;
    }

    /**
     * Returns the ISO file's {@link RandomAccessInputStream} that was passed to the constructor.
     *
     * @return the ISO file's {@link RandomAccessInputStream} that was passed to the constructor.
     */
    RandomAccessInputStream getRandomAccessInputStream() {
        return rais;
    }

    /**
     * Closes the ISO file's {@link RandomAccessInputStream} that was passed to the constructor.
     *
     * @throws IOException if an I/O error occurs while closing the stream
     */
    @Override
    public void close() throws IOException {
        rais.close();
    }
}
