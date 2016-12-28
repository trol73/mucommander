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

    /** Static instance of the filename filter that matches archive filenames */
    private final static ExtensionFilenameFilter filenameFilter = new ExtensionFilenameFilter(new String[]
        {".zip", ".jar", ".war", ".wal", ".wmz", ".xpi", ".ear", ".sar", ".odt", ".ods", ".odp", ".odg", ".odf", ".egg", ".epub", ".cbz"}
    );


    //////////////////////////////////////////
    // ArchiveFormatProvider implementation //
    //////////////////////////////////////////

    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new ZipArchiveFile(file);
    }

    public FilenameFilter getFilenameFilter() {
        return filenameFilter;
    }
}
