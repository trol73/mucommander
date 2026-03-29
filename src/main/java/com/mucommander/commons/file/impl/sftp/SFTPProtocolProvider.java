package com.mucommander.commons.file.impl.sftp;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;

import java.io.IOException;

/**
 * This class is the provider for the FTP filesystem implemented by {@link com.mucommander.commons.file.impl.ftp.FTPFile}.
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 * @see com.mucommander.commons.file.impl.sftp.SFTPFile
 */
public class SFTPProtocolProvider implements ProtocolProvider {

    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        return instantiationParams.length == 0 ?
                new SFTPFile(url) : new SFTPFile(url, (SFTPFile.SFTPFileAttributes)instantiationParams[0]);
    }
}
