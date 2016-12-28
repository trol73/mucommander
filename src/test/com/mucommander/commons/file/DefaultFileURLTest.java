package com.mucommander.commons.file;

/**
 * A {@link FileURLTestCase} implementation for URLs with no specific handler, i.e. using the
 * {@link com.mucommander.commons.file.FileURL#getDefaultHandler() default URL handler}.
 *
 * @author Maxence Bernard
 */
public class DefaultFileURLTest extends FileURLTestCase {

    ////////////////////////////////////
    // FileURLTestCase implementation //
    ////////////////////////////////////

    @Override
    protected String getScheme() {
        return "unknown";
    }

    @Override
    protected int getDefaultPort() {
        return -1;
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
