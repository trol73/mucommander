package com.mucommander.commons.file.util;

import com.sun.jna.Library;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Exposes parts of the C standard library using JNA (Java Native Access).
 *
 * @author Maxence Bernard
 */
public interface CLibrary extends Library {

    //////////////////////
    // statvfs function //
    //////////////////////
    
    /**
     * Structure that holds the information returned by {@link CLibrary#statvfs(String, STATVFSSTRUCT)}.
     */
    class STATVFSSTRUCT extends Structure {
        /* file system block size */
        public int f_bsize;
        /* fragment size */
        public int f_frsize;
        /* size of fs in f_frsize units */
        public int f_blocks;
        /* # free blocks */
        public int f_bfree;
        /* # free blocks for non-root */
        public int f_bavail;
        /* # inodes */
        public int f_files;
        /* # free inodes */
        public int f_ffree;
        /* # free inodes for non-root */
        public int f_favail;
        /* file system ID */
        public long f_fsid;
        /* mount flags */
        public int f_flag;
        /* maximum filename length */
        public int f_namemax;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList("f_bsize", "f_frsize", "f_blocks", "f_bfree", "f_bavail", "f_files", "f_ffree", "f_favail", "f_fsid", "f_flag", "f_namemax");
        }
    }

    /**
     * Returns information about the filesystem on which the specified file resides.
     *
     * @param path pathname of any file within the mounted filesystem
     * @param struct a {@link STATVFSSTRUCT} object
     * @return 0 on success, -1 on error
     */
    int statvfs(String path, STATVFSSTRUCT struct);
}
