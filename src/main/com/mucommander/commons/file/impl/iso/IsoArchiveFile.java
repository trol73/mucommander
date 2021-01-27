package com.mucommander.commons.file.impl.iso;

import com.mucommander.commons.file.*;
import com.mucommander.commons.io.FilterRandomAccessInputStream;
import com.mucommander.commons.io.RandomAccessInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * IsoArchiveFile provides read-only access to archives in the ISO and NRG formats.
 *
 * @author Maxence Bernard
 * @see com.mucommander.commons.file.impl.iso.IsoFormatProvider
 */
public class IsoArchiveFile extends AbstractROArchiveFile {

    public IsoArchiveFile(AbstractFile file) {
        super(file);
    }

    //////////////////////////////////////////
    // AbstractROArchiveFile implementation //
    //////////////////////////////////////////

    @Override
    public ArchiveEntryIterator getEntryIterator() throws IOException {
        RandomAccessInputStream rais = getRandomAccessInputStream();

        return new IsoEntryIterator(IsoParser.getEntries(this, rais).iterator(), rais);
    }

    @Override
    public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException {
        // Cast the entry before creating the stream, in case it fails
        IsoArchiveEntry isoEntry = (IsoArchiveEntry) entry;

        RandomAccessInputStream rais;
        // If a IsoEntryIterator is specified, reuse the iterator's stream
        if (entryIterator != null && entryIterator instanceof IsoEntryIterator) {
            // Override close() as a no-op so that the stream is re-used from one entry to another -- the stream will
            // be closed when the iterator is closed.
            rais = new FilterRandomAccessInputStream(((IsoEntryIterator) entryIterator).getRandomAccessInputStream()) {
                @Override
                public void close() {
                    // No-op
                }
            };
        } else {
            rais = getRandomAccessInputStream();
        }

        return new IsoEntryInputStream(rais, isoEntry);
    }
}
