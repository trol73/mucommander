package com.mucommander.commons.file;

/**
 * This interface provides methods to access file permissions, for every combination of types and accesses defined
 * in {@link com.mucommander.commons.file.PermissionTypes} and {@link com.mucommander.commons.file.PermissionAccesses} respectively.
 * This interface also defines constants for commonly used permission values.
 *
 * <p>Permission bits can be queried individually using {@link #getBitValue(int, int)} or be represented altogether
 * as a UNIX-style permission int, using {@link #getIntValue()}.
 *
 * <p>Two implementations of this interface are provided:
 *   <ul>
 *     <li>{@link com.mucommander.commons.file.GroupedPermissionBits} (full implementation): implements this interface using a
 * given permission int.</li>
 *     <li>{@link com.mucommander.commons.file.IndividualPermissionBits} (partial implementation): implements the
 * <code>getIntValue()</code> method by relying on <code>getBitValue()</code> and querying it sequentially for every
 * permission bit.</li>
 *   </ul>
 *
 * @see com.mucommander.commons.file.GroupedPermissionBits
 * @see com.mucommander.commons.file.IndividualPermissionBits
 * @author Maxence Bernard
 */
public interface PermissionBits {

    /** read/write/execute permissions set for user/group/other (777 octal) */
    int FULL_PERMISSION_INT = 511;

    /** read/write/execute permissions set for user/group/other (777 octal) */
    PermissionBits FULL_PERMISSION_BITS = new GroupedPermissionBits(FULL_PERMISSION_INT);

    /** read/write/execute permissions cleared for user/group/other (0) */
    int EMPTY_PERMISSION_INT = 0;

    /** read/write/execute permissions cleared for user/group/other (0) */
    PermissionBits EMPTY_PERMISSION_BITS = new GroupedPermissionBits(EMPTY_PERMISSION_INT);

    
    //////////////////////
    // Abstract methods //
    //////////////////////

    /**
     * Returns the value of all the permission bits (9 in total) in a UNIX-style permission int. Each of the permission
     * bits can be isolated by comparing them against the values defined in {@link com.mucommander.commons.file.PermissionTypes}
     * and {@link com.mucommander.commons.file.PermissionAccesses}.
     *
     * @return the value of all the permission bits (9 in total) in a UNIX-style permission int
     */
    int getIntValue();

    /**
     * Returns the value of a specific permission bit: <code>true</code> if the permission is set, <code>false</code>
     * if it isn't.
     *
     * @param access one of the values defined in {@link com.mucommander.commons.file.PermissionAccesses}
     * @param type one of the values defined in {@link com.mucommander.commons.file.PermissionTypes}
     * @return <code>true</code> if the permission is set, <code>false</code> if it isn't
     */
    boolean getBitValue(int access, int type);
}
