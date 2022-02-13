package com.mucommander.commons.file;

/**
 * IndividualPermissionBits is a partial implementation of {@link com.mucommander.commons.file.PermissionBits} that relies
 * on {@link #getBitValue(int, int)}: the implementation of {@link #getIntValue()} calls <code>getBitValue()</code>
 * sequentially for each permission bit, 9 times in total.
 *
 * @see com.mucommander.commons.file.GroupedPermissionBits
 * @author Maxence Bernard
 */
public abstract class IndividualPermissionBits implements PermissionBits {

    public IndividualPermissionBits() {
    }


    @Override
    public int getIntValue() {
        int bitShift = 0;
        int perms = 0;

        for(int a=PermissionAccesses.OTHER_ACCESS; a<=PermissionAccesses.USER_ACCESS; a++) {
            for(int p=PermissionTypes.EXECUTE_PERMISSION; p<=PermissionTypes.READ_PERMISSION; p=p<<1) {
                if(getBitValue(a, p))
                    perms |= (1<<bitShift);

                bitShift++;
            }
        }

        return perms;
    }
}
