package com.mucommander.commons.file.impl.nfs;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURLTestCase;

/**
 * A {@link FileURLTestCase} implementation for NFS URLs.
 *
 * @author Maxence Bernard
 */
public class NFSFileURLTest extends FileURLTestCase {

    ////////////////////////////////////
    // FileURLTestCase implementation //
    ////////////////////////////////////

    @Override
    protected String getScheme() {
        return "nfs";
    }

    @Override
    protected int getDefaultPort() {
        return 2049;
    }

    @Override
    protected AuthenticationType getAuthenticationType() {
        return AuthenticationType.NO_AUTHENTICATION;
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
