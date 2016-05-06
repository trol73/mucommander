package com.mucommander.commons.file.impl.wim;

import java.io.IOException;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.SevenZipJBindingROArchiveFile;

import net.sf.sevenzipjbinding.ArchiveFormat;

public class WimFormatProvider implements ArchiveFormatProvider {

    private final static ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(
            new String[] { ".wim" });

    private final static byte[] SIGNATURE = {0x4D, 0x53, 0x57, 0x49, 0x4D};

    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new SevenZipJBindingROArchiveFile(file, ArchiveFormat.WIM, SIGNATURE);
    }

    @Override
    public FilenameFilter getFilenameFilter() {
        return FILENAME_FILTER;
    }

}
