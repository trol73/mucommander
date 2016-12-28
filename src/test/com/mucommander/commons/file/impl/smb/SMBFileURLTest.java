package com.mucommander.commons.file.impl.smb;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURLTestCase;

import java.net.MalformedURLException;

/**
 * A {@link FileURLTestCase} implementation for SMB URLs.
 *
 * @author Maxence Bernard
 */
public class SMBFileURLTest extends FileURLTestCase {

    ////////////////////////////////////
    // FileURLTestCase implementation //
    ////////////////////////////////////

    @Override
    protected String getScheme() {
        return "smb";
    }

    @Override
    protected int getDefaultPort() {
        return -1;
    }

    @Override
    protected AuthenticationType getAuthenticationType() {
        return AuthenticationType.AUTHENTICATION_REQUIRED;
    }

    @Override
    protected Credentials getGuestCredentials() {
        return new Credentials("GUEST", "");
    }

    @Override
    protected String getPathSeparator() {
        return "/";
    }

    @Override
    protected boolean isQueryParsed() {
        return false;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * This method is overridden to test SMB's specific notion of realm. 
     */
    @Override
    public void testRealm() throws MalformedURLException {
        assertEquals(getURL("host", "/share"), getURL("host", "/share/path/to/file").getRealm());
    }
}
