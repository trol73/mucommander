/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file.impl;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileProtocols;
import com.mucommander.commons.file.filter.FileFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.local.LocalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * CachedFile is a ProxyFile that caches the return values of most {@link AbstractFile} getter methods. This allows
 * to limit the number of calls to the underlying file methods which can have a cost since they often are I/O bound.
 * The methods that are cached are those overridden by this class, except for the <code>ls</code> methods, which are
 * overridden only to allow recursion (see {@link #CachedFile(com.mucommander.commons.file.AbstractFile, boolean)}).
 *
 * <p>The values are retrieved and cached only when the 'cached methods' are called for the first time; they are
 * not preemptively retrieved in the constructor, so using this class has no negative impact on performance,
 * except for the small extra CPU cost added by proxying the methods and the extra RAM used to store cached values.
 *
 * <p>Once the values are retrieved and cached, they never change: the same value will always be returned once a method
 * has been called for the first time. That means if the underlying file changes (e.g. its size or date has changed),
 * the changes will not be reflected by this CachedFile. Thus, this class should only be used when a 'real-time' view
 * of the file is not required, or when the file instance is used only for a small amount of time.
 *
 * @author Maxence Bernard
 */
public class CachedFile extends ProxyFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedFile.class);

    // Used to access the java.io.FileSystem#getBooleanAttributes method
    private static final boolean GET_FILE_ATTRIBUTES_AVAILABLE;
    private static final Method M_GET_BOOLEAN_ATTRIBUTES;
    private static final int BA_DIRECTORY, BA_EXISTS, BA_HIDDEN;
    private static final Object FS;

    // set-flags
    private static final int SIZE_SET_MASK = 1;
    private static final int DATE_SET_MASK = 1 << 1;
    private static final int SYMLINK_SET_MASK = 1 << 2;
    private static final int DIRECTORY_SET_MASK = 1 << 3;
    private static final int ARCHIVE_SET_MASK = 1 << 4;
    private static final int EXECUTABLE_SET_MASK = 1 << 5;
    private static final int HIDDEN_SET_MASK = 1 << 6;
    private static final int ABSOLUTE_PATH_SET_MASK = 1 << 7;
    private static final int CANONICAL_PATH_SET_MASK = 1 << 8;
    private static final int EXTENSION_SET_MASK = 1 << 9;
    private static final int NAME_SET_MASK = 1 << 10;
    private static final int FREE_SPACE_SET_MASK = 1 << 11;
    private static final int TOTAL_SPACE_SET_MASK = 1 << 12;
    private static final int EXISTS_SET_MASK = 1 << 13;
    private static final int PERMISSIONS_SET_MASK = 1 << 14;
    private static final int PERMISSIONS_STRING_SET_MASK = 1 << 15;
    private static final int OWNER_SET_MASK = 1 << 16;
    private static final int GROUP_SET_MASK = 1 << 17;
    private static final int IS_ROOT_SET_MASK = 1 << 18;
    private static final int PARENT_SET_MASK = 1 << 19;
    private static final int GET_ROOT_SET_MASK = 1 << 20;
    private static final int CANONICAL_FILE_SET_MASK = 1 << 21;

    // boolean values
    private static final int SYMLINK_VALUE_MASK = 1 << 22;
    private static final int DIRECTORY_VALUE_MASK = 1 << 23;
    private static final int ARCHIVE_VALUE_MASK = 1 << 24;
    private static final int EXECUTABLE_VALUE_MASK = 1 << 25;
    private static final int HIDDEN_VALUE_MASK = 1 << 26;
    private static final int EXISTS_VALUE_MASK = 1 << 27;
    private static final int IS_ROOT_VALUE_MASK = 1 << 28;

    // others
    /** If true, AbstractFile instances returned by this class will be wrapped into CachedFile instances */
    private static final int RECURSE_INSTANCES_MASK = 1 << 29;

    /**
     * All boolean values stored here as bits
     */
    private int bitmask;

    ///////////////////
    // Cached values //
    ///////////////////

    private long getSize;
    private long getDate;
    private String getAbsolutePath;
    private String getCanonicalPath;
    private String getExtension;
    private String getName;
    private long getFreeSpace;
    private long getTotalSpace;
    private FilePermissions getPermissions;
    private String getPermissionsString;
    private String getOwner;
    private String getGroup;
    private AbstractFile getParent;
    private AbstractFile getRoot;
    private AbstractFile getCanonicalFile;


    static {
        // Exposes the java.io.FileSystem class which by default has package access, in order to use its
        // 'getBooleanAttributes' method to speed up access to file attributes under Windows.
        // This method allows to retrieve the values of the 'exists', 'isDirectory' and 'isHidden' attributes in one
        // pass, resolving the underlying file only once instead of 3 times. Since resolving a file is a particularly
        // expensive operation under Windows due to improper use of the Win32 API, this helps speed things up a little.
        // References:
        //  - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5036988
        //  - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6240028
        //
        // This hack was made for Windows, but is now used for other platforms as well as it is necessarily faster than
        // retrieving file attributes individually.

        boolean getFileAttributesAvailable;
        Method mGetBooleanAttributes;
        int baExists, baDirectory, baHidden;
        Object fs;
        try {
            // Resolve FileSystem class, 'getBooleanAttributes' method and fields
            Class<?> cFile = File.class;
            Class<?> cFileSystem = Class.forName("java.io.FileSystem");
            mGetBooleanAttributes = cFileSystem.getDeclaredMethod("getBooleanAttributes", cFile);
            Field fBA_EXISTS = cFileSystem.getDeclaredField("BA_EXISTS");
            Field fBA_DIRECTORY = cFileSystem.getDeclaredField("BA_DIRECTORY");
            Field fBA_HIDDEN = cFileSystem.getDeclaredField("BA_HIDDEN");
            Field fFs = cFile.getDeclaredField("fs");

            // Allow access to the 'getBooleanAttributes' method and to the fields we're interested in
            mGetBooleanAttributes.setAccessible(true);
            fFs.setAccessible(true);
            fBA_EXISTS.setAccessible(true);
            fBA_DIRECTORY.setAccessible(true);
            fBA_HIDDEN.setAccessible(true);

            // Retrieve constant field values once for all
            baExists = (Integer) fBA_EXISTS.get(null);
            baDirectory = (Integer) fBA_DIRECTORY.get(null);
            baHidden = (Integer) fBA_HIDDEN.get(null);
            fs = fFs.get(null);

            getFileAttributesAvailable = true;
            LOGGER.trace("Access to java.io.FileSystem granted");
        } catch(Exception e) {
            getFileAttributesAvailable = false;
            mGetBooleanAttributes = null;
            baExists = 0;
            baDirectory = 0;
            baHidden = 0;
            fs = null;
            LOGGER.info("Error while allowing access to java.io.FileSystem", e);
        }
        GET_FILE_ATTRIBUTES_AVAILABLE = getFileAttributesAvailable;
        M_GET_BOOLEAN_ATTRIBUTES = mGetBooleanAttributes;
        BA_EXISTS = baExists;
        BA_DIRECTORY = baDirectory;
        BA_HIDDEN = baHidden;
        FS = fs;
    }


    /**
     * Creates a new CachedFile instance around the specified AbstractFile, caching returned values of cached methods
     * as they are called. If recursion is enabled, the methods returning AbstractFile will return CachedFile instances,
     * allowing the cache files recursively.
     *
     * @param file the AbstractFile instance for which returned values of getter methods should be cached
     * @param recursiveInstances if true, AbstractFile instances returned by this class will be wrapped into CachedFile instances
     */
    public CachedFile(AbstractFile file, boolean recursiveInstances) {
        super(file);
        if (recursiveInstances) {
            bitmask |= RECURSE_INSTANCES_MASK;
        }
    }


    /**
     * Creates a CachedFile instance for each of the AbstractFile instances in the given array.
     */
    private AbstractFile[] createCachedFiles(AbstractFile files[]) {
        int nbFiles = files.length;
        for (int i = 0; i < nbFiles; i++) {
            files[i] = new CachedFile(files[i], true);
        }

        return files;
    }


    /**
     * Pre-fetches values of {@link #isDirectory}, {@link #exists} and {@link #isHidden} for the given local file,
     * using the <code>java.io.FileSystem#getBooleanAttributes(java.io.File)</code> method.
     * The given {@link AbstractFile} must be a local file or a proxy to a local file ('file' protocol). This method
     * must only be called if the {@link #GET_FILE_ATTRIBUTES_AVAILABLE} field is <code>true</code>.
     */
    private void getFileAttributes(AbstractFile file) {
        file = file.getTopAncestor();

        if (file instanceof LocalFile) {
            try {
                int ba = (Integer) M_GET_BOOLEAN_ATTRIBUTES.invoke(FS, file.getUnderlyingFileObject());

                if ((ba & BA_DIRECTORY) != 0) {
                    bitmask |= DIRECTORY_VALUE_MASK;
                } else {
                    bitmask &= ~DIRECTORY_VALUE_MASK;
                }
                if ((ba & BA_EXISTS) != 0) {
                    bitmask |= EXISTS_VALUE_MASK;
                } else {
                    bitmask &= ~EXISTS_VALUE_MASK;
                }
                if ((ba & BA_HIDDEN) != 0) {
                    bitmask |= HIDDEN_VALUE_MASK;
                } else {
                    bitmask &= ~HIDDEN_VALUE_MASK;
                }
                bitmask |= DIRECTORY_SET_MASK | HIDDEN_SET_MASK | EXISTS_SET_MASK;
            } catch(Exception e) {
                LOGGER.info("Could not retrieve file attributes for {}", file, e);
            }
        }
    }


    ////////////////////////////////////////////////////
    // Overridden methods to cache their return value //
    ////////////////////////////////////////////////////

    @Override
    public long getSize() {
        if ((bitmask & SIZE_SET_MASK) == 0) {
            getSize = file.getSize();
            bitmask |= SIZE_SET_MASK;
        }
        return getSize;
    }

    @Override
    public long getDate() {
        if ((bitmask & DATE_SET_MASK) == 0) {
            getDate = file.getDate();
            bitmask |= DATE_SET_MASK;
        }
        return getDate;
    }

    @Override
    public boolean isSymlink() {
        if ((bitmask & SYMLINK_SET_MASK) == 0) {
            if (file.isSymlink()) {
                bitmask |= SYMLINK_VALUE_MASK;
            } else {
                bitmask &= ~SYMLINK_VALUE_MASK;
            }
            bitmask |= SYMLINK_SET_MASK;
        }
        return (bitmask & SYMLINK_VALUE_MASK) != 0;
    }

    @Override
    public boolean isDirectory() {
        if ((bitmask & DIRECTORY_SET_MASK) == 0) {
            if (GET_FILE_ATTRIBUTES_AVAILABLE && FileProtocols.FILE.equals(file.getURL().getScheme())) {
                getFileAttributes(file);
            }
            // Note: getFileAttributes() might fail to retrieve file attributes, so we need to test isDirectorySet again
            if ((bitmask & DIRECTORY_SET_MASK) == 0) {
                if (file.isDirectory()) {
                    bitmask |= DIRECTORY_VALUE_MASK;
                } else {
                    bitmask &= ~DIRECTORY_VALUE_MASK;
                }
                bitmask |= DIRECTORY_SET_MASK;
            }
        }
        return (bitmask & DIRECTORY_VALUE_MASK) != 0;
    }

    @Override
    public boolean isArchive() {
        if ((bitmask & ARCHIVE_SET_MASK) == 0) {
            if (file.isArchive()) {
                bitmask |= ARCHIVE_VALUE_MASK;
            } else {
                bitmask &= ~ARCHIVE_VALUE_MASK;
            }
            bitmask |= ARCHIVE_SET_MASK;
        }
        return (bitmask & ARCHIVE_VALUE_MASK) != 0;
    }

    @Override
    public boolean isHidden() {
        if ((bitmask & HIDDEN_SET_MASK) == 0) {
            if (GET_FILE_ATTRIBUTES_AVAILABLE && FileProtocols.FILE.equals(file.getURL().getScheme())) {
                getFileAttributes(file);
            }
            // Note: getFileAttributes() might fail to retrieve file attributes, so we need to test isDirectorySet again
            if ((bitmask & HIDDEN_SET_MASK) == 0) {
                if (file.isHidden()) {
                    bitmask |= HIDDEN_VALUE_MASK;
                } else {
                    bitmask &= ~HIDDEN_VALUE_MASK;
                }
                bitmask |= HIDDEN_SET_MASK;
            }
        }
        return (bitmask & HIDDEN_VALUE_MASK) != 0;
    }

    @Override
    public boolean isExecutable() {
        if ((bitmask & EXECUTABLE_SET_MASK) == 0) {
            if (file.isExecutable()) {
                bitmask |= EXECUTABLE_VALUE_MASK;
            } else {
                bitmask &= ~EXECUTABLE_VALUE_MASK;
            }
            bitmask |= EXECUTABLE_SET_MASK;
        }
        return (bitmask & EXECUTABLE_VALUE_MASK) != 0;
    }

    @Override
    public String getAbsolutePath() {
        if ((bitmask & ABSOLUTE_PATH_SET_MASK) == 0) {
            getAbsolutePath = file.getAbsolutePath();
            bitmask |= ABSOLUTE_PATH_SET_MASK;
        }
        return getAbsolutePath;
    }

    @Override
    public String getCanonicalPath() {
        if ((bitmask & CANONICAL_PATH_SET_MASK) == 0) {
            getCanonicalPath = file.getCanonicalPath();
            bitmask |= CANONICAL_PATH_SET_MASK;
        }
        return getCanonicalPath;
    }

    @Override
    public String getExtension() {
        if ((bitmask & EXTENSION_SET_MASK) == 0) {
            getExtension = file.getExtension();
            bitmask |= EXTENSION_SET_MASK;
        }
        return getExtension;
    }

    @Override
    public String getName() {
        if ((bitmask & NAME_SET_MASK) == 0) {
            getName = file.getName();
            bitmask |= NAME_SET_MASK;
        }
        return getName;
    }

    @Override
    public long getFreeSpace() throws IOException {
        if ((bitmask & FREE_SPACE_SET_MASK) == 0) {
            getFreeSpace = file.getFreeSpace();
            bitmask |= FREE_SPACE_SET_MASK;
        }
        return getFreeSpace;
    }

    @Override
    public long getTotalSpace() throws IOException {
        if ((bitmask & TOTAL_SPACE_SET_MASK) == 0) {
            getTotalSpace = file.getTotalSpace();
            bitmask |= TOTAL_SPACE_SET_MASK;
        }
        return getTotalSpace;
    }

    @Override
    public boolean exists() {
        if ((bitmask & EXISTS_SET_MASK) == 0) {
            if (GET_FILE_ATTRIBUTES_AVAILABLE && FileProtocols.FILE.equals(file.getURL().getScheme())) {
                getFileAttributes(file);
            }
            // Note: getFileAttributes() might fail to retrieve file attributes, so we need to test isDirectorySet again
            if ((bitmask & EXISTS_SET_MASK) == 0) {
                if (file.exists()) {
                    bitmask |= EXISTS_VALUE_MASK;
                } else {
                    bitmask &= ~EXISTS_VALUE_MASK;
                }
                bitmask |= EXISTS_SET_MASK;
            }
        }
        return (bitmask & EXISTS_VALUE_MASK) != 0;
    }

    @Override
    public FilePermissions getPermissions() {
        if ((bitmask & PERMISSIONS_SET_MASK) == 0) {
            getPermissions = file.getPermissions();
            bitmask |= PERMISSIONS_SET_MASK;
        }
        return getPermissions;
    }

    @Override
    public String getPermissionsString() {
        if ((bitmask & PERMISSIONS_STRING_SET_MASK) == 0) {
            getPermissionsString = file.getPermissionsString();
            bitmask |= PERMISSIONS_STRING_SET_MASK;
        }
        return getPermissionsString;
    }

    @Override
    public String getOwner() {
        if ((bitmask & OWNER_SET_MASK) == 0) {
            getOwner = file.getOwner();
            bitmask |= OWNER_SET_MASK;
        }
        return getOwner;
    }

    @Override
    public String getGroup() {
        if ((bitmask & GROUP_SET_MASK) == 0) {
            getGroup = file.getGroup();
            bitmask |= GROUP_SET_MASK;
        }
        return getGroup;
    }

    @Override
    public boolean isRoot() {
        if ((bitmask & IS_ROOT_SET_MASK) == 0) {
            if (file.isRoot()) {
                bitmask |= IS_ROOT_VALUE_MASK;
            } else {
                bitmask &= ~IS_ROOT_VALUE_MASK;
            }
            bitmask |= IS_ROOT_SET_MASK;
        }
        return (bitmask & IS_ROOT_VALUE_MASK) != 0;
    }


    @Override
    public AbstractFile getParent() {
        if ((bitmask & PARENT_SET_MASK) == 0) {
            getParent = file.getParent();
            // create a CachedFile instance around the file if recursion is enabled
            if ((bitmask & RECURSE_INSTANCES_MASK) != 0 && getParent != null) {
                getParent = new CachedFile(getParent, true);
            }
            bitmask |= PARENT_SET_MASK;
        }
        return getParent;
    }

    @Override
    public AbstractFile getRoot() {
        if ((bitmask & GET_ROOT_SET_MASK) == 0) {
            getRoot = file.getRoot();
            // create a CachedFile instance around the file if recursion is enabled
            if ((bitmask & RECURSE_INSTANCES_MASK) != 0) {
                getRoot = new CachedFile(getRoot, true);
            }
            bitmask |= GET_ROOT_SET_MASK;
        }
        return getRoot;
    }

    @Override
    public AbstractFile getCanonicalFile() {
        if ((bitmask & CANONICAL_FILE_SET_MASK) == 0) {
            getCanonicalFile = file.getCanonicalFile();
            // create a CachedFile instance around the file if recursion is enabled
            if ((bitmask & RECURSE_INSTANCES_MASK) != 0) {
                // AbstractFile#getCanonicalFile() may return 'this' if the file is not a symlink. In that case,
                // no need to create a new CachedFile, simply use this one. 
                if (getCanonicalFile == file) {
                    getCanonicalFile = this;
                } else {
                    getCanonicalFile = new CachedFile(getCanonicalFile, true);
                }
            }
            bitmask |= CANONICAL_FILE_SET_MASK;
        }
        return getCanonicalFile;
    }


    ////////////////////////////////////////////////
    // Overridden for recursion only (no caching) //
    ////////////////////////////////////////////////

    @Override
    public AbstractFile[] ls() throws IOException {
        // Don't cache ls() result but create a CachedFile instance around each of the files if recursion is enabled
        AbstractFile files[] = file.ls();

        if ((bitmask & RECURSE_INSTANCES_MASK) != 0) {
            return createCachedFiles(files);
        }

        return files;
    }

    @Override
    public AbstractFile[] ls(FileFilter filter) throws IOException {
        // Don't cache ls() result but create a CachedFile instance around each of the files if recursion is enabled
        AbstractFile files[] = file.ls(filter);

        if ((bitmask & RECURSE_INSTANCES_MASK) != 0) {
            return createCachedFiles(files);
        }

        return files;
    }

    @Override
    public AbstractFile[] ls(FilenameFilter filter) throws IOException {
        // Don't cache ls() result but create a CachedFile instance around each of the files if recursion is enabled
        AbstractFile files[] = file.ls(filter);

        if ((bitmask & RECURSE_INSTANCES_MASK) != 0) {
            return createCachedFiles(files);
        }

        return files;
    }

}
