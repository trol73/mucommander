package com.mucommander.commons.file.impl.bzip2;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;

import java.io.IOException;

/**
 * This class is the provider for the 'Bzip2' archive format implemented by {@link Bzip2ArchiveFile}.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 * @see com.mucommander.commons.file.impl.bzip2.Bzip2ArchiveFile
 */
public class Bzip2FormatProvider implements ArchiveFormatProvider {

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
        return new Bzip2ArchiveFile(file);
    }

    @Override
    public FilenameFilter getFilenameFilter() {
        return FILENAME_FILTER;
    }

}
