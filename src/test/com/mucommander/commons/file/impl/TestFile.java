package com.mucommander.commons.file.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.DummyFile;
import com.mucommander.commons.file.FileURL;

import java.net.MalformedURLException;

/**
 * TestFile is an {@link AbstractFile} that is used in unit tests.
 * This is an implementation of virtual file. Several methods are overridden to
 * return data passed in constructor.
 * @author Mariusz Jakubowski
 *
 */
public class TestFile extends DummyFile {
    
    private boolean isDir;
    private long size;
    private long date;
    private AbstractFile parent;

    public TestFile(String name, boolean isdir, long size, long date, AbstractFile parent) throws MalformedURLException {
        super(FileURL.getFileURL(name));
        this.isDir = isdir;
        this.size = size;
        this.date = date;
        this.parent = parent;
    }
    
    @Override
    public boolean isDirectory() {
        return isDir;
    }
    
    @Override
    public long getSize() {
        return size;
    }
 
    @Override
    public long getLastModifiedDate() {
        return date;
    }
    
    @Override
    public AbstractFile getParent() {
        return parent;
    }
    
}
