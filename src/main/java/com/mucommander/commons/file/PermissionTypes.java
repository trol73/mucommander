package com.mucommander.commons.file;

/**
 * This interface defines constants fields used for designating the three different permission types:
 * {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION} and {@link #EXECUTE_PERMISSION}. Their actual value represent
 * the bit to be set and left-shifted with the desired {@link com.mucommander.commons.file.PermissionAccesses permission access}
 * in a UNIX-style permission int.
 *
 * @see PermissionAccesses
 * @author Maxence Bernard
 */
public interface PermissionTypes {

    /** Designates the 'execute' permission. */
    int EXECUTE_PERMISSION = 1;

    /** Designates the 'write' permission. */
    int WRITE_PERMISSION = 2;

    /** Designates the 'read' permission. */
    int READ_PERMISSION = 4;
}
