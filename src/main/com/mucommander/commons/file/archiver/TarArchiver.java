package com.mucommander.commons.file.archiver;

import com.mucommander.commons.file.FileAttributes;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.SimpleFilePermissions;
import com.mucommander.commons.file.impl.tar.provider.TarEntry;
import com.mucommander.commons.file.impl.tar.provider.TarOutputStream;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Archiver implementation using the Tar archive format.
 *
 * @author Maxence Bernard
 */
class TarArchiver extends Archiver {

    private TarOutputStream tos;
    private boolean firstEntry = true;

    protected TarArchiver(OutputStream outputStream) {
        super(outputStream);

        this.tos = new TarOutputStream(outputStream);
        // Specifies how to handle files which filename is > 100 chars (default is to fail!)
        this.tos.setLongFileMode(TarOutputStream.LONGFILE_GNU);
    }


    /////////////////////////////
    // Archiver implementation //
    /////////////////////////////

    @Override
    public OutputStream createEntry(String entryPath, FileAttributes attributes) throws IOException {
        // Start by closing current entry
        if(!firstEntry)
            tos.closeEntry();

        boolean isDirectory = attributes.isDirectory();
		
        // create the entry
        TarEntry entry = new TarEntry(normalizePath(entryPath, isDirectory));
        // Use provided file's size (required by TarOutputStream) and date
        long size = attributes.getSize();
        if(!isDirectory && size>=0)		// Do not set size if file is directory or file size is unknown!
            entry.setSize(size);

        // Set the entry's date and permissions
        entry.setModTime(attributes.getLastModifiedDate());
        entry.setMode(SimpleFilePermissions.padPermissions(attributes.getPermissions(), isDirectory
                    ? FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS
                    : FilePermissions.DEFAULT_FILE_PERMISSIONS).getIntValue());

        // Add the entry
        tos.putNextEntry(entry);

        if(firstEntry)
            firstEntry = false;
	
        // Return the OutputStream that allows to write to the entry, only if it isn't a directory 
        return isDirectory?null:tos;
    }


    @Override
    public void close() throws IOException {
        // Close current entry
        if(!firstEntry)
            tos.closeEntry();
		
        tos.close();
    }
    
    @Override
    public void postProcess() throws IOException {}
}
