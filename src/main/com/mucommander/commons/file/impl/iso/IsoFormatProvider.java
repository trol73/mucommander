package com.mucommander.commons.file.impl.iso;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.SevenZipArchiveFormatDetector;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.SevenZipJBindingROArchiveFile;
import net.sf.sevenzipjbinding.ArchiveFormat;

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
    private final static String[] EXTENSIONS = {".iso", ".nrg"};

    private static final int[] SIGNATURE_FAT_MBR = {0xEB, -1, -1, 'M', 'S', 'D', 'O', 'S'};

    /**
     * Static instance of the filename filter that matches archive filenames
     */
    private final static ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);

    private static final SevenZipArchiveFormatDetector detector = new SevenZipArchiveFormatDetector(SIGNATURE_FAT_MBR.length) {
        @Override
        protected ArchiveFormat detect(byte[] bytes) {
            if (checkSignature(bytes, SIGNATURE_FAT_MBR)) {
                return ArchiveFormat.FAT;
            }
            return ArchiveFormat.ISO;
        }
    };

    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        if (file.getExtension().equalsIgnoreCase("nrg")) {
            return new IsoArchiveFile(file);
        }
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
