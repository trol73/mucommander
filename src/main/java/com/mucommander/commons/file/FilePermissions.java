package com.mucommander.commons.file;

/**
 * FilePermissions is an interface that represents the permissions of an {@link com.mucommander.commons.file.AbstractFile}.
 * The actual permission values can be retrieved by the methods inherited from the
 * {@link com.mucommander.commons.file.PermissionBits} interface. The permissions mask returned by {@link #getMask()} allows
 * to determine which permission bits are significant, i.e. should be taken into account. That way, certain
 * {@link AbstractFile} implementations that have limited permissions support can set those supported permission bits
 * while making it clear that other bits should be ignored, and not simply be considered as being disabled.
 * For instance, a file implementation with support for the sole 'user' permissions (read/write/execute) will return a
 * mask whose int value is 448 (700 octal).
 *
 * <p>This interface also defines constants for commonly used file permissions.
 *
 * @see com.mucommander.commons.file.AbstractFile#getPermissions()
 * @author Maxence Bernard
 */
public interface FilePermissions extends PermissionBits {

    /** Empty file permissions: read/write/execute permissions cleared for user/group/other (0), none of the permission
     * bits are supported (mask is 0) */
    FilePermissions EMPTY_FILE_PERMISSIONS = new SimpleFilePermissions(0, 0);

    /** Default file permissions used by {@link AbstractFile#importPermissions(AbstractFile)} for permission bits that
     * are not available in the source: rw-r--r-- (644 octal). All the permission bits are marked as supported. */
    FilePermissions DEFAULT_FILE_PERMISSIONS = new SimpleFilePermissions(420, FULL_PERMISSION_BITS);

    /** Default directory permissions used by {@link AbstractFile#importPermissions(AbstractFile)} for permission bits that
     * are not available in the source: rwxr-xr-x (755 octal). All the permission bits are marked as supported. */
    FilePermissions DEFAULT_DIRECTORY_PERMISSIONS = new SimpleFilePermissions(493, FULL_PERMISSION_BITS);

    FilePermissions DEFAULT_EXECUTABLE_PERMISSIONS = new SimpleFilePermissions(493, FULL_PERMISSION_BITS);


    /**
     * Returns the mask that indicates which permission bits are significant and should be taken into account.
     * Permission bits that are unsupported have no meaning and their value should simply be ignored.
     *
     * @return the mask that indicates which permission bits are significant and should be taken into account.
     */
    PermissionBits getMask();
}
