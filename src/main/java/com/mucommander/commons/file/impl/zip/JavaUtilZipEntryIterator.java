package com.mucommander.commons.file.impl.zip;

import com.mucommander.commons.file.ArchiveEntry;
import com.mucommander.commons.file.ArchiveEntryIterator;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An <code>ArchiveEntryIterator</code> that iterates through a {@link ZipInputStream}.
 *
 * @author Maxence Bernard
 */
public class JavaUtilZipEntryIterator implements ArchiveEntryIterator  {

    /** InputStream to the archive file */
    private ZipInputStream zin;

    /** The current entry, where the ZipInputStream is currently positionned */
    private ArchiveEntry currentEntry;


    /**
     * Creates a new TarEntryIterator that iterates through the entries of the given {@link ZipInputStream}.
     *
     * @param zin the TarInputStream to iterate through
     */
    JavaUtilZipEntryIterator(ZipInputStream zin) {
        this.zin = zin;
    }

    /**
     * Returns the {@link ZipInputStream} instance that was used to create this object.
     *
     * @return the {@link ZipInputStream} instance that was used to create this object.
     */
    ZipInputStream getZipInputStream() {
        return zin;
    }

    /**
     * Returns the current entry, where the <code>ZipInputStream</code> is currently positionned.
     *
     * @return the current entry, where the <code>ZipInputStream</code> is currently positionned.
     */
    ArchiveEntry getCurrentEntry() {
        return currentEntry;
    }

    /**
     * Advances the {@link ZipInputStream} to the next entry and returns the corresponding {@link ArchiveEntry}.
     *
     * @return the next ArchiveEntry
     * @throws java.io.IOException if an I/O error occurred
     */
    private ArchiveEntry getNextEntry() throws IOException {
        try {
            ZipEntry entry = zin.getNextEntry();

            if(entry==null)
                return null;

            return ZipArchiveFile.createArchiveEntry(new com.mucommander.commons.file.impl.zip.provider.ZipEntry(entry));
        }
        catch(Exception e) {
            // java.util.zip.ZipInputStream can throw an IllegalArgumentException when the filename/comment encoding
            // is not UTF-8 as expected (ZipInputStream always expects UTF-8). The more general Exception is caught
            // (just to be safe) and an IOException thrown.
            throw new IOException();
        } catch(Error e) {
            // ZipInputStream#getNextEntry() will throw a java.lang.InternalError ("invalid compression method")
            // if the compression method is different from DEFLATED or STORED (happens with IMPLODED for example).
            throw new IOException();
        }
    }


    /////////////////////////////////////////
    // ArchiveEntryIterator implementation //
    /////////////////////////////////////////

    public ArchiveEntry nextEntry() throws IOException {
        // Get the next entry, if any
        this.currentEntry = getNextEntry();

        return currentEntry;
    }

    public void close() throws IOException {
        zin.close();
    }
}
