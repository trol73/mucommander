package com.mucommander.commons.file.impl.zip;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;

import java.io.IOException;

/**
 * This class is the provider for the 'Zip' archive format implemented by {@link ZipArchiveFile}.
 *
 * @see com.mucommander.commons.file.impl.zip.ZipArchiveFile
 * @author Nicolas Rinaudo, Maxence Bernard
 */
public class ZipFormatProvider implements ArchiveFormatProvider {
    private static final String[] EXTENSIONS =
            {".zip", ".jar", ".war", ".wal", ".wmz", ".xpi", ".ear", ".sar", ".odt", ".ods", ".odp", ".odg", ".odf",
                    ".egg", ".epub", ".cbz"};

    /**
     * Static instance of the filename filter that matches archive filenames
     * */
    private static final ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);


    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new ZipArchiveFile(file);
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
