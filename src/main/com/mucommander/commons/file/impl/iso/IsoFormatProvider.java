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
    public final static String EXTENSIONS[] = {".iso", ".nrg",};

    /**
     * Static instance of the filename filter that matches archive filenames
     */
    private static final ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);

    //////////////////////////////////////////
    // ArchiveFormatProvider implementation //
    //////////////////////////////////////////
    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new IsoArchiveFile(file);
    }

    @Override
    public FilenameFilter getFilenameFilter() {
        return FILENAME_FILTER;
    }

}
