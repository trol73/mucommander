package com.mucommander.commons.file.util;

import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides access to a static instance of the {@link CLibrary} interface, allowing to invoke selected
 * functions of the C standard library.
 *
 * <p>The C standard library and the JNA library (which is used to access native libraries) may not be available on
 * all OS/CPU architectures: {@link #isAvailable()} can be used to determine that at runtime.
 *
 * @see CLibrary
 * @author Maxence Bernard
 */
public class C {
    private static final Logger LOGGER = LoggerFactory.getLogger(C.class);

    /** Singleton instance */
    private static CLibrary INSTANCE;

    static {
        try {
            INSTANCE = (CLibrary)Native.loadLibrary("c", CLibrary.class);
        }
        catch(Throwable e) {
            LOGGER.info("Unable to load C library", e);

            // java.lang.UnsatisfiedLinkError is thrown if the CPU architecture is not supported by JNA.
            INSTANCE = null;
        }
    }

    /**
     * Returns <code>true</code> if the C standard library can be accessed on the current OS/CPU architecture.
     *
     * @return <code>true</code> if the C standard library can be accessed on the current OS/CPU architecture
     */
    public static boolean isAvailable() {
        return INSTANCE!=null;
    }

    /**
     * Returns a static instance of the {@link CLibrary} interface, allowing to invoke selected functions of the C
     * standard library. <code>null</code> will be returned if {@link #isAvailable()} returned <code>false</code>.
     *
     * @return a static instance of the {@link CLibrary} interface, <code>null</code> if it is not available on the
     * current OS/CPU architecture
     */
    public static CLibrary getInstance() {
        return INSTANCE;
    }
}
