package com.mucommander.commons.file.util;

import com.mucommander.commons.runtime.OsFamily;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

/**
 * This class provides access to a static instance of the {@link com.mucommander.commons.file.util.Shell32API} interface,
 * allowing to invoke selected Shell32 Windows DLL functions.
 *
 * <p>The Kernel32 DLL and the JNA library (which is used to access native libraries) may not be available on
 * all OS/CPU architectures: {@link #isAvailable()} can be used to determine that at runtime.
 *
 * @see Shell32API
 * @author Maxence Bernard
 */
public class Shell32 {

    /** An instance of the Shell32 DLL */
    private static Shell32API INSTANCE;

    static {
        if(OsFamily.WINDOWS.isCurrent()) {        // Don't even bother if we're not running Windows
            try {
                INSTANCE = (Shell32API)Native.loadLibrary("shell32", Shell32API.class, W32APIOptions.UNICODE_OPTIONS);
            }
            catch(Throwable e) {
                // java.lang.UnsatisfiedLinkError is thrown if the CPU architecture is not supported by JNA.
                INSTANCE = null;
            }
        }
    }

    /**
     * Returns <code>true</code> if the Shell32 API can be accessed on the current OS/CPU architecture.
     *
     * @return <code>true</code> if the Shell32 API can be accessed on the current OS/CPU architecture
     */
    public static boolean isAvailable() {
        return INSTANCE!=null;
    }

    /**
     * Returns a static instance of the {@link com.mucommander.commons.file.util.Shell32API} interface, allowing to invoke
     * some Shell32 Windows DLL functions. <code>null</code> will be returned if {@link #isAvailable()} returned
     * <code>false</code>.
     *
     * @return a static instance of the {@link com.mucommander.commons.file.util.Shell32API} interface, <code>null</code> if it
     * is not available on the current OS/CPU architecture
     */
    public static Shell32API getInstance() {
        return INSTANCE;
    }
}
