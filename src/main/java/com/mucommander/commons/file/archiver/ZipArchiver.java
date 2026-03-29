package com.mucommander.commons.file.archiver;

import com.mucommander.commons.file.FileAttributes;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.SimpleFilePermissions;
import com.mucommander.commons.file.impl.zip.provider.ZipEntry;
import com.mucommander.commons.file.impl.zip.provider.ZipOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Archiver implementation using the Zip archive format.
 *
 * @author Maxence Bernard
 */
class ZipArchiver extends Archiver {

    private final ZipOutputStream zos;
    private boolean firstEntry = true;



    ZipArchiver(OutputStream outputStream) {
        super(outputStream);

        this.zos = new ZipOutputStream(outputStream);
    }


    /**
     * Overrides Archiver's no-op setComment method as Zip supports archive comment.
     */
    @Override
    public void setComment(String comment) {
        zos.setComment(comment);
    } 
	


    @Override
    public OutputStream createEntry(String entryPath, FileAttributes attributes) throws IOException {
        // Start by closing current entry
        if (!firstEntry) {
            zos.closeEntry();
        }

        boolean isDirectory = attributes.isDirectory();
		
        // Create the entry and use the provided file's date
        ZipEntry entry = new ZipEntry(normalizePath(entryPath, isDirectory));
        // Use provided file's size and date
        long size = attributes.getSize();
        if (!isDirectory && size >= 0) {    // Do not set size if file is directory or file size is unknown!
            entry.setSize(size);
        }

        entry.setTime(attributes.getLastModifiedDate());
        entry.setUnixMode(SimpleFilePermissions.padPermissions(attributes.getPermissions(), isDirectory
                    ? FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS
                    : FilePermissions.DEFAULT_FILE_PERMISSIONS).getIntValue());

        // Add the entry
        zos.putNextEntry(entry);

        if (firstEntry) {
            firstEntry = false;
        }
		
        // Return the OutputStream that allows to write to the entry, only if it isn't a directory 
        return isDirectory ? null : zos;
    }


    @Override
    public void close() throws IOException {
        zos.close();
    }
    
    @Override
    public void postProcess() {}
}
