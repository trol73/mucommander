package com.mucommander.commons.file.impl.lst;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;

import java.io.IOException;

/**
 * This class is the provider for the 'Lst' archive format implemented by {@link LstArchiveFile}.
 *
 * @see com.mucommander.commons.file.impl.lst.LstArchiveFile
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class LstFormatProvider implements ArchiveFormatProvider {

    private static final String[] EXTENSIONS = {".lst"};

    /**
     * Static instance of the filename filter that matches archive filenames
     */
    private final static ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);


    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new LstArchiveFile(file);
    }

    @Override
    public FilenameFilter getFilenameFilter() {
        return FILENAME_FILTER;
    }

    @Override
    public String[] getFileExtensions() {
        return EXTENSIONS;
    }
}
