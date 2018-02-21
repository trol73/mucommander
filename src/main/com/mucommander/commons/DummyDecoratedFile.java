package com.mucommander.commons;

import com.mucommander.commons.file.DummyFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.runtime.OsFamily;

/**
 *
 *  This class is an child of <code>DummyFile</code> with overwritten {@link #toString()}} method that return a "nice" value
 *  for local files (without 'file://localhost/')
 *
 *  @author Oleg Trifonov
 */
public class DummyDecoratedFile extends DummyFile {

    private static final String LOCAL_FILE_PREFIX;

    static {
        String prefix = "file://" + FileURL.LOCALHOST;
        LOCAL_FILE_PREFIX = OsFamily.WINDOWS.isCurrent() ? prefix + '/' : prefix;
    }

    public DummyDecoratedFile(FileURL url) {
        super(url);
    }

    @Override
    public String toString() {
        String result = super.toString();
        if (result.startsWith(LOCAL_FILE_PREFIX)) {
            return result.substring(LOCAL_FILE_PREFIX.length());
        }
        return result;
    }
}
