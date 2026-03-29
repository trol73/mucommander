package com.mucommander.commons.file.impl.nfs;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;

import java.io.IOException;

/**
 * This class is the provider for the NFS filesystem implemented by {@link com.mucommander.commons.file.impl.nfs.NFSFile}.
 *
 * @author Nicolas Rinaudo
 * @see com.mucommander.commons.file.impl.nfs.NFSFile
 */
public class NFSProtocolProvider implements ProtocolProvider {

    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        return new NFSFile(url);
    }
}
