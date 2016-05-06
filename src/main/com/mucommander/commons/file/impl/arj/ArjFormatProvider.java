package com.mucommander.commons.file.impl.arj;

import java.io.IOException;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.SevenZipJBindingROArchiveFile;

import net.sf.sevenzipjbinding.ArchiveFormat;

public class ArjFormatProvider implements ArchiveFormatProvider {

    private final static ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(new String[] { ".arj" });

    private final static byte[] SIGNATURE = { 0x60, (byte) 0xEA };

    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new SevenZipJBindingROArchiveFile(file, ArchiveFormat.ARJ, SIGNATURE);
    }

    @Override
    public FilenameFilter getFilenameFilter() {
        return FILENAME_FILTER;
    }

}
