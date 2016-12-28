package com.mucommander.commons.file;

import java.net.MalformedURLException;

/**
 * SchemeParser is an interface that provides a single {@link #parse(String, FileURL)} method used by
 * {@link FileURL#getFileURL(String)} to turn a URL string into a corresponding <code>FileURL</code> instance.
 *
 * @see FileURL#getFileURL(String)
 * @see com.mucommander.commons.file.SchemeHandler
 * @author Maxence Bernard
 */
public interface SchemeParser {

    /**
     * Extracts the different parts from the given URL string and sets them in the specified FileURL instance.
     * The FileURL is empty when it is passed, with just the handler set. The scheme, host, port, login, password, path,
     * ... parts must all be set, using the corresponding setter methods.
     *
     * <p>Some parts such as the query and fragment have a meaning only for certain schemes such as HTTP, other schemes
     * may simply ignore the corresponding query/fragment delimiters ('?' and '#' resp.) and include them in the
     * path part.</p>
     *
     * @param url the URL to parse
     * @param fileURL the FileURL instance in which to set the different parsed parts
     * @throws MalformedURLException if the specified string is not a valid URL and cannot be parsed
     */
    void parse(String url, FileURL fileURL) throws MalformedURLException;
}
