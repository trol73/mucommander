package com.mucommander.commons.file;


import com.mucommander.commons.file.util.PathUtils;
import com.mucommander.commons.runtime.OsFamily;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Top-level Mac OS X system folders hidden by Finder. For more info about those files:
 * http://www.westwind.com/reference/OS-X/invisibles.html
 * 
 * @author Arik Hadas. Maxence Bernard 
 */
public enum MacOsSystemFolder {
    TRASHES("/.Trashes"),
    VOL("/.vol"),
    DEV("/dev"),
    AUTOMOUNT("/automount"),
    BIN("/bin"),
    CORES("/cores"),
    ETC("/etc"),
    LOST_FOUND("/lost+found"),    
    NETWORK("/Network"),
    PRIVATE("/private"),
    SBIN("/sbin"),
    TMP("/tmp"),
    USR("/usr"),
    VAR("/var"),
//    "/Volumes",
    MACH_SYM("/mach.sym"),
    MACH_KERNEL("/mach_kernel"),
    MACH("/mach"),
    DESKTOP_DB("/Desktop DB"),
    DESKTOP_DF("/Desktop DF"),
    FILE_TRANSFER_FOLDER("/File Transfer Folder"),
    HOTFILES_BTREE("/.hotfiles.btree"),
    SPOTLIGHT_V100("/.Spotlight-V100"),
    HIDDEN("/.hidden"),     // Used by Mac OS X up to 10.3, not in 10.4
    USER_TRASH(System.getProperty("user.home")+"/.Trash"),  // User trash folder
    // Mac OS 9 system folders 
    APPLESHARE_PDS("/AppleShare PDS"),
    CLEANUP_AT_STARTUP("/Cleanup At Startup"),
    DESKTOP_FOLDER("/Desktop Folder"),
    NETWORK_TRASH_FOLDER("/Network Trash Folder"),
    SHUTDOWN_CHECK("/Shutdown Check"),
    TEMPORARY_ITEMS("/Temporary Items"),
    USER_TEMPORARY_ITEMS(System.getProperty("user.home")+"/Temporary Items"),  // User trash folder
    THEFINDBYCONTENTFOLDER("/TheFindByContentFolder"),
    THEVOLUMESETTINGSFOLDER("/TheVolumeSettingsFolder"),
    TRASH("/Trash"),
    VM_STORAGE("/VM Storage");

    /** file path */
    final String path;

    static Set<String> paths;

    static {
        if (OsFamily.MAC_OS_X.isCurrent()) {
            paths = Stream.of(values()).map(f -> f.path).collect(Collectors.toSet());
        }
    }

	MacOsSystemFolder(String path) {
		this.path = path;
	}

	public static boolean isSystemFile(AbstractFile file) {
        String path = file.getAbsolutePath();
        path = PathUtils.removeTrailingSeparator(path);
        return paths.contains(path);
	}
}
