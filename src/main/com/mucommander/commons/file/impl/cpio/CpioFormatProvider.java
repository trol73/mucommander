package com.mucommander.commons.file.impl.cpio;

import java.io.IOException;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.SevenZipJBindingROArchiveFile;

import net.sf.sevenzipjbinding.ArchiveFormat;

public class CpioFormatProvider implements ArchiveFormatProvider {

    private static final String[] EXTENSIONS = { ".cpio" };
    private final static ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);

//    private final static byte[] SIGNATURE = { 0x30, 0x37, 0x30, 0x37, 0x30 }; //=google but sevenzipjbinding:C771050823
    private final static byte[] SIGNATURE = { }; 
    

    @Override
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
        return new SevenZipJBindingROArchiveFile(file, ArchiveFormat.CPIO, SIGNATURE);
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
