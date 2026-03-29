package com.mucommander.commons.file.impl.rar;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.SevenZipArchiveFormatDetector;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.SevenZipJBindingROArchiveFile;
import com.mucommander.commons.file.impl.sevenzip.SevenZipArchiveFile;
import net.sf.sevenzipjbinding.ArchiveFormat;

import java.io.IOException;

/**
 * This class is the provider for the 'Rar' archive format implemented by {@link SevenZipArchiveFile}.
 *
 * @author Arik Hadas
 */
public class RarFormatProvider implements ArchiveFormatProvider {

    private static final String[] EXTENSIONS = {".rar", ".cbr"};

    private final static byte[] RAR4_SIGNATURE = {0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00};
    private final static byte[] RAR5_SIGNATURE = {0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00};

	/**
     * Static instance of the filename filter that matches archive filenames
     */
    private final static ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);

    private static final SevenZipArchiveFormatDetector detector = new SevenZipArchiveFormatDetector(RAR5_SIGNATURE.length) {
        @Override
        protected ArchiveFormat detect(byte[] bytes) {
            if (checkSignature(bytes, RAR4_SIGNATURE)) {
                return ArchiveFormat.RAR;
            } else if (checkSignature(bytes, RAR5_SIGNATURE)) {
                return ArchiveFormat.RAR5;
            }
            return null;
        }
    };


    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new SevenZipJBindingROArchiveFile(file, detector);
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
