package com.mucommander.commons.file.impl.sftp;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURLTestCase;

/**
 * A {@link FileURLTestCase} implementation for SFTP URLs.
 *
 * @author Maxence Bernard
 */
public class SFTPFileURLTest extends FileURLTestCase {

    ////////////////////////////////////
    // FileURLTestCase implementation //
    ////////////////////////////////////

    @Override
    protected String getScheme() {
        return "sftp";
    }

    @Override
    protected int getDefaultPort() {
        return 22;
    }

    @Override
    protected AuthenticationType getAuthenticationType() {
        return AuthenticationType.AUTHENTICATION_REQUIRED;
    }

    @Override
    protected Credentials getGuestCredentials() {
        return null;
    }

    @Override
    protected String getPathSeparator() {
        return "/";
    }

    @Override
    protected boolean isQueryParsed() {
        return false;
    }
}
