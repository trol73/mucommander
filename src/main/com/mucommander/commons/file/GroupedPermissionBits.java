package com.mucommander.commons.file;

/**
 * GroupedPermissionBits is an implementation of {@link com.mucommander.commons.file.PermissionBits} using a given UNIX-style
 * permission int: {@link #getIntValue()} returns the specified int, and {@link #getBitValue(int, int)} isolates a
 * specified value.
 *
 * @see com.mucommander.commons.file.IndividualPermissionBits
 * @author Maxence Bernard
 */
public class GroupedPermissionBits implements PermissionBits {

    /** UNIX-style permission int */
    protected int permissions;

    /**
     * Creates a new GroupedPermissionBits using the specified UNIX-style permission int. The int can be created
     * by combining (binary OR and shift) values defined in {@link com.mucommander.commons.file.PermissionTypes} and
     * {@link com.mucommander.commons.file.PermissionAccesses}.
     *
     * @param permissions a UNIX-style permission int.
     */
    public GroupedPermissionBits(int permissions) {
        this.permissions = permissions;
    }

    @Override
    public int getIntValue() {
        return permissions;
    }

    @Override
    public boolean getBitValue(int access, int type) {
        return (permissions & (type << (access*3))) != 0;
    }
}
