package com.mucommander.commons.file.impl.bzip2;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.SevenZipJBindingROArchiveFile;
import net.sf.sevenzipjbinding.ArchiveFormat;

import java.io.IOException;

/**
 * This class is the provider for the 'Bzip2' archive format.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class Bzip2FormatProvider implements ArchiveFormatProvider {

    private static final String[] EXTENSIONS = {".bz2"};

    private static final byte[] SIGNATURE = {};

    /**
     * Static instance of the filename filter that matches archive filenames
     * */
    private static final ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);

    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new SevenZipJBindingROArchiveFile(file, ArchiveFormat.BZIP2, SIGNATURE);
        //return new Bzip2ArchiveFile(file);
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
