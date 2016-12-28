package com.mucommander.commons.file.impl.sevenzip;

import java.io.IOException;
import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import net.sf.sevenzipjbinding.ArchiveFormat;

/**
 * This class is the provider for the '7z' archive format implemented by {@link SevenZipArchiveFile}.
 *
 * @see com.mucommander.commons.file.impl.rar.RarArchiveFile
 * @author Arik Hadas
 */
public class SevenZipFormatProvider implements ArchiveFormatProvider {
	/** Static instance of the filename filter that matches archive filenames */
    private final static ExtensionFilenameFilter filenameFilter = new ExtensionFilenameFilter(new String[]
        {".7z", ".cb7"}
    );

    private static final byte[] SIGNATURE = { 0x37, 0x7A, (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C };

    //////////////////////////////////////////
    // ArchiveFormatProvider implementation //
    //////////////////////////////////////////

    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new SevenZipArchiveFile(file, ArchiveFormat.SEVEN_ZIP, SIGNATURE);
    }

    public FilenameFilter getFilenameFilter() {
        return filenameFilter;
    }
}
