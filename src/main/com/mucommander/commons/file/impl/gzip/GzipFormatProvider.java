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
 * @author Nicolas Rinaudo, Maxence Bernard
 * @see com.mucommander.commons.file.impl.gzip.GzipArchiveFile
 */
public class GzipFormatProvider implements ArchiveFormatProvider {

    public static final String[] EXTENSIONS = {".gz"};

    /**
     * Static instance of the filename filter that matches archive filenames
     */
    private static final ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);

    //////////////////////////////////////////
    // ArchiveFormatProvider implementation //
    //////////////////////////////////////////
    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new GzipArchiveFile(file);
    }

    @Override
    public FilenameFilter getFilenameFilter() {
        return FILENAME_FILTER;
    }

}
