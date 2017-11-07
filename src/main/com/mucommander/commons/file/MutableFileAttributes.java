package com.mucommander.commons.file;

/**
 * This interface extends <code>FileAttributes</code> to add attribute getters. Refer to {@link FileAttributes}'s
 * documentation for more information about attributes.
 *
 * <p>See the {@link SimpleFileAttributes} class for an implementation of this interface.
 *
 * @author Maxence Bernard
 * @see SimpleFileAttributes
 */
public interface MutableFileAttributes extends FileAttributes {

    /**
     * Sets the file's path.
     *
     * <p>The format and separator character of the path are filesystem-dependent.
     *
     * @param path the file's path
     */
    void setPath(String path);

    /**
     * Sets whether the file exists physically on the underlying filesystem.
     *
     * @param exists <code>true</code> if the file exists physically on the underlying filesystem
     */
    void setExists(boolean exists);

    /**
     * Sets the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     *
     * @param date the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     */
    void setDate(long date);

    /**
     * Sets the file's size in bytes.
     *
     * @param size the file's size in bytes
     */
    void setSize(long size);

    /**
     * Specifies whether the file is a directory or a regular file.
     *
     * @param directory <code>true</code> for directory, <code>false</code> for regular file
     */
    void setDirectory(boolean directory);

    /**
     * Sets the file's permissions.
     *
     * @param permissions the file's permissions
     */
    void setPermissions(FilePermissions permissions);

    /**
     * Sets the file's owner.
     *
     * @param owner the file's owner
     */
    void setOwner(String owner);

    /**
     * Sets the file's group.
     *
     * @param group the file's owner
     */
    void setGroup(String group);
}
