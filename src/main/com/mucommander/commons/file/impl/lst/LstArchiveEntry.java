package com.mucommander.commons.file.impl.lst;

import com.mucommander.commons.file.ArchiveEntry;

/**
 * An LST archive entry. In addition to the common attributes found in {@link ArchiveEntry}, it contains a base
 * folder which, when concatenated with this entry's path, gives the absolute path to the file referenced by the
 * LST entry. 
 *
 * @author Maxence Bernard
 */
public class LstArchiveEntry extends ArchiveEntry {

    /** The base folder that when concatenated to this entry's path gives the absolute path to the file referenced
     * by this entry */
    protected String baseFolder;

    LstArchiveEntry(String path, boolean directory, long date, long size, String baseFolder) {
        super(path, directory, date, size, true);

        this.baseFolder = baseFolder;
    }

    /**
     * Returns the base folder which, when concatenated with this entry's path, gives the absolute path to the file
     * referenced by the LST entry. The returned path should always end with a trailing separator character.
     *
     * @return the base folder of this entry
     */
    protected String getBaseFolder() {
        return baseFolder;
    }
}
