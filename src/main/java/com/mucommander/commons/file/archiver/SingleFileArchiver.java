package com.mucommander.commons.file.archiver;

import com.mucommander.commons.file.FileAttributes;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Generic single file Archiver.
 *
 * @author Maxence Bernard
 */
class SingleFileArchiver extends Archiver {

    private boolean firstEntry = true;


    SingleFileArchiver(OutputStream outputStream) {
        super(outputStream);
    }


    /**
     * This method is a no-op, and does nothing but throw an IOException if it is called more than once,
     * which should never be the case as this Archiver is only meant to store one file. 
     */
    @Override
    public OutputStream createEntry(String entryPath, FileAttributes attributes) throws IOException {
        if (firstEntry) {
            firstEntry = false;
        } else {
            throw new IOException();
        }

        return out;
    }
	
	
    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void postProcess() {}
}
