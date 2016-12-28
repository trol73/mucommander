package com.mucommander.commons.file.impl.s3;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURLTestCase;

/**
 * A {@link FileURLTestCase} implementation for S3 URLs.
 *
 * @author Maxence Bernard
 */
public class S3FileURLTest extends FileURLTestCase {

    ////////////////////////////////////
    // FileURLTestCase implementation //
    ////////////////////////////////////

    @Override
    protected String getScheme() {
        return "s3";
    }

    @Override
    protected int getDefaultPort() {
        return 443;
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
        return true;
    }
}
