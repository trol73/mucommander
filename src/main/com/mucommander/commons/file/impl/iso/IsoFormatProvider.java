package com.mucommander.commons.file.impl.iso;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;

import java.io.IOException;

/**
 * This class is the provider for the 'Iso' and 'Nrg' archive formats implemented by {@link IsoArchiveFile}.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 * @see com.mucommander.commons.file.impl.iso.IsoArchiveFile
 */
public class IsoFormatProvider implements ArchiveFormatProvider {

    /**
     * Array of format extensions
     */
    private final static String FORMAT_EXTENSIONS[] = {
            ".iso",
            ".nrg",
    };

    /**
     * Static instance of the filename filter that matches archive filenames
     */
    private final static ExtensionFilenameFilter filenameFilter = new ExtensionFilenameFilter(FORMAT_EXTENSIONS);

    //////////////////////////////////////////
    // ArchiveFormatProvider implementation //
    //////////////////////////////////////////

    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new IsoArchiveFile(file);
    }

    public FilenameFilter getFilenameFilter() {
        return filenameFilter;
    }
}
