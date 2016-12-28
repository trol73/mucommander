package com.mucommander.commons.file.impl.http;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURLTestCase;

/**
 * A {@link FileURLTestCase} implementation for HTTP URLs.
 *
 * @author Maxence Bernard
 */
public class HTTPFileURLTest extends FileURLTestCase {

    ////////////////////////////////////
    // FileURLTestCase implementation //
    ////////////////////////////////////

    @Override
    protected String getScheme() {
        return "http";
    }

    @Override
    protected int getDefaultPort() {
        return 80;
    }

    @Override
    protected AuthenticationType getAuthenticationType() {
        return AuthenticationType.AUTHENTICATION_OPTIONAL;
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
        return true;
    }
}
