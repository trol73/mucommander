package com.mucommander.commons.file.impl.webdav;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mathias
 */
public class WebDAVProvider implements ProtocolProvider {

    @Override
    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {

        try {
            return new WebDAVFile(url);
        } catch (URISyntaxException ex) {
            Logger.getLogger(WebDAVProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;

    }

}
