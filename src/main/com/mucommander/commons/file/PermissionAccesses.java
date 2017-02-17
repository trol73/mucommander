package com.mucommander.commons.file;

/**
 * This interface defines constants fields used for designating the three different permission accesses:
 * {@link #USER_ACCESS}, {@link #GROUP_ACCESS} and {@link #OTHER_ACCESS}. Their actual int values represent the number
 * of 3-bit left shifts (&lt;&lt; operator) needed to represent a particular
 * {@link com.mucommander.commons.file.PermissionTypes permission type} in a UNIX-style permission int. To illustrate,
 * the 'read' permission (value = 4) for the 'user' access (value = 2) is represented in a UNIX-style permission int as:
 * <code>4 &lt;&lt; 3*2 = 256 (400 octal)</code>.
 *
 * @see com.mucommander.commons.file.PermissionTypes
 * @author Maxence Bernard
 */
public interface PermissionAccesses {

    /** Designates the 'other' permission access. */
    int OTHER_ACCESS = 0;

    /** Designates the 'group' permission access. */
    int GROUP_ACCESS = 1;

    /** Designates the 'user' permission access. */
    int USER_ACCESS = 2;
}
