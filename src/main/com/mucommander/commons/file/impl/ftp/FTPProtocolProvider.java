package com.mucommander.commons.file.impl.ftp;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;

import java.io.IOException;

/**
 * This class is the provider for the FTP filesystem implemented by {@link com.mucommander.commons.file.impl.ftp.FTPFile}.
 *
 * @author Nicolas Rinaudo
 * @see com.mucommander.commons.file.impl.ftp.FTPFile
 */
public class FTPProtocolProvider implements ProtocolProvider {

    /** Controls whether to force the listing of hidden files */
    private static boolean forceHiddenFilesListing = false;

    /**
     * Controls whether to force the listing of hidden files. Enabling this option will cause 'LIST -al' commands
     * to be issued when listing files, instead of 'LIST -l'.
     * When this option is disabled, the decision to list hidden files is left to the FTP server: some servers will
     * choose to show them, some will not. This behavior is usually a configuration setting of the FTP server.
     * <p>
     * This option is disabled by default. The reason for this is that the commons-net library will fail to properly
     * parse directory listings on some servers when 'LIST -al' is used (bug).
     * </p>
     *
     * @param value <code>true</code> to force the listing of hidden files, <code>false</code> to leave it for the
     * server to decide whether to show hidden files or not.
     */
    // Todo: check if this is still needed after upgrading to commons-net 2.0
    // Todo: this should not be a configuration variable but rather a FileURL property
    public static void setForceHiddenFilesListing(boolean value) {
        forceHiddenFilesListing = value;
    }

    /**
     * Returns <code>true</code> if the listing of hidden files is forced, <code>false</code> if the decision to show
     * them is left to the server.
     *
     * @return <code>true</code> if the listing of hidden files is forced, <code>false</code> if the decision to show
     * them is left to the server.
     * @see #setForceHiddenFilesListing(boolean)
     */
    public static boolean getForceHiddenFilesListing() {
        return forceHiddenFilesListing;
    }


    /////////////////////////////////////
    // ProtocolProvider Implementation //
    /////////////////////////////////////

    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        return instantiationParams.length==0
            ?new FTPFile(url)
            :new FTPFile(url, (org.apache.commons.net.ftp.FTPFile)instantiationParams[0]);
    }
}
