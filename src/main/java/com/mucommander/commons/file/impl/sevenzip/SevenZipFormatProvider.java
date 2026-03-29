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
 * @author Arik Hadas
 */
public class SevenZipFormatProvider implements ArchiveFormatProvider {

    private static final String[] EXTENSIONS = {".7z", ".cb7"};
	/**
     * Static instance of the filename filter that matches archive filenames
     * */
    private final static ExtensionFilenameFilter filenameFilter = new ExtensionFilenameFilter(EXTENSIONS);

    private static final byte[] SIGNATURE = { 0x37, 0x7A, (byte) 0xBC, (byte) 0xAF, 0x27, 0x1C };

    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new SevenZipArchiveFile(file, ArchiveFormat.SEVEN_ZIP, SIGNATURE);
    }

    public FilenameFilter getFilenameFilter() {
        return filenameFilter;
    }

    @Override
    public String[] getFileExtensions() {
        return EXTENSIONS;
    }
}
