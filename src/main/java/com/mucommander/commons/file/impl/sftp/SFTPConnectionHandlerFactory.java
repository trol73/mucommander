package com.mucommander.commons.file.impl.sftp;

import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.connection.ConnectionHandler;
import com.mucommander.commons.file.connection.ConnectionHandlerFactory;

/**
 * <code>ConnectionHandlerFactory</code> that creates {@link SFTPConnectionHandler} instances.
 *
 * @author Maxence Bernard
 */
public class SFTPConnectionHandlerFactory implements ConnectionHandlerFactory {

    public ConnectionHandler createConnectionHandler(FileURL location) {
        return new SFTPConnectionHandler(location);
    }
}
