package com.mucommander.commons.file.impl.gzip;

import com.mucommander.commons.file.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * GzipArchiveFile provides read-only access to archives in the Gzip format.
 *
 * <p>The actual decompression work is performed by the {@link java.util.zip.GZIPInputStream} class.</p>
 *
 * @see com.mucommander.commons.file.impl.gzip.GzipFormatProvider
 * @author Maxence Bernard
 */
public class GzipArchiveFile extends AbstractROArchiveFile {

    /**
     * Creates a GzipArchiveFile on top of the given file.
     *
     * @param file the underlying file to wrap this archive file around
     */
    public GzipArchiveFile(AbstractFile file) {
        super(file);
    }


    ////////////////////////////////////////
    // AbstractArchiveFile implementation //
    ////////////////////////////////////////

    @Override
    public ArchiveEntryIterator getEntryIterator() throws IOException {
        String extension = getExtension();
        String name = getName();
		
        if (extension != null) {
            extension = extension.toLowerCase();
			
            // Remove the 'gz' or 'tgz' extension from the entry's name
            if (extension.equals("tgz"))
                name = name.substring(0, name.length()-3) + "tar";
            else if (extension.equals("gz"))
                name = name.substring(0, name.length()-3);
        }

        return new SingleArchiveEntryIterator(new ArchiveEntry("/"+name, false, getLastModifiedDate(), -1, true));
    }


    @Override
    public InputStream getEntryInputStream(ArchiveEntry entry, ArchiveEntryIterator entryIterator) throws IOException, UnsupportedFileOperationException {
        return new GZIPInputStream(getInputStream());
    }
}
