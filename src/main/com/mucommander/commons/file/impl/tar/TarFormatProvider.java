package com.mucommander.commons.file.impl.tar;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;

import java.io.IOException;

/**
 * This class is the provider for the 'Tar' archive format implemented by {@link TarArchiveFile}.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 * @see com.mucommander.commons.file.impl.tar.TarArchiveFile
 */
public class TarFormatProvider implements ArchiveFormatProvider {

    public static final String[] EXTENSIONS = {".tar", ".tar.gz", ".tgz", ".tar.bz2", ".tbz2", ".cbt"};

    /**
     * Static instance of the filename filter that matches archive filenames
     */
    private static final ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);

    //////////////////////////////////////////
    // ArchiveFormatProvider implementation //
    //////////////////////////////////////////

    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new TarArchiveFile(file);
    }

    @Override
    public FilenameFilter getFilenameFilter() {
        return FILENAME_FILTER;
    }

}
