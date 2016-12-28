package com.mucommander.commons.file.impl.gzip;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;

import java.io.IOException;

/**
 * This class is the provider for the 'Gzip' archive format implemented by {@link GzipArchiveFile}.
 *
 * @see com.mucommander.commons.file.impl.gzip.GzipArchiveFile
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class GzipFormatProvider implements ArchiveFormatProvider {

    /** Static instance of the filename filter that matches archive filenames */
    private final static ExtensionFilenameFilter filenameFilter = new ExtensionFilenameFilter(".gz");


    //////////////////////////////////////////
    // ArchiveFormatProvider implementation //
    //////////////////////////////////////////

    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new GzipArchiveFile(file);
    }

    public FilenameFilter getFilenameFilter() {
        return filenameFilter;
    }
}
