package com.mucommander.commons.file.impl.xar;

import java.io.IOException;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.SevenZipJBindingROArchiveFile;

import net.sf.sevenzipjbinding.ArchiveFormat;

public class XarFormatProvider implements ArchiveFormatProvider {

    private static final String[] EXTENSIONS = { ".xar" };

    private final static ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);

    private final static byte[] SIGNATURE = {0x78, 0x61, 0x72, 0x21};

    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new SevenZipJBindingROArchiveFile(file, ArchiveFormat.XAR, SIGNATURE);
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
