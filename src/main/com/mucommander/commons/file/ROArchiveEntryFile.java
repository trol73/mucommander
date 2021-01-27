package com.mucommander.commons.file;

import java.io.OutputStream;

/**
 * Represents a file entry inside a read-only archive. Read-only archives are characterized by
 * {@link AbstractArchiveFile#isWritable()} returning <code>false</code>.
 *
 * @see AbstractArchiveFile
 * @see RWArchiveEntryFile
 * @author Maxence Bernard
 */
public class ROArchiveEntryFile extends AbstractArchiveEntryFile {

    protected ROArchiveEntryFile(FileURL url, AbstractArchiveFile archiveFile, ArchiveEntry entry) {
        super(url, archiveFile, entry);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     *
     * @throws UnsupportedFileOperationException always
     */
    @Override
    @UnsupportedFileOperation
    public void setLastModifiedDate(long lastModified) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
    }

    /**
     * Always return {@link PermissionBits#EMPTY_PERMISSION_BITS}.
     */
    @Override
    public PermissionBits getChangeablePermissions() {
        return PermissionBits.EMPTY_PERMISSION_BITS;
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     */
    @Override
    @UnsupportedFileOperation
    public void delete() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.DELETE);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     */
    @Override
    @UnsupportedFileOperation
    public void mkdir() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     */
    @Override
    @UnsupportedFileOperation
    public OutputStream getOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     */
    @Override
    @UnsupportedFileOperation
    public void changePermissions(int permissions) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
    }

    /**
     * Always throws {@link UnsupportedFileOperationException} when called.
     */
    @Override
    @UnsupportedFileOperation
    public void changePermission(int access, int permission, boolean enabled) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
    }

    /**
     * Always returns <code>0</code>.
     */
    @Override
    public long getFreeSpace() {
        return 0;
    }
}
