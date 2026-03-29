package com.mucommander.commons.file.filter;

/**
 * This {@link FilenameFilter} matches filenames that start with a specified string.
 *
 * @author Maxence Bernard
 */
public class StartsWithFilenameFilter extends AbstractStartsWithFilter implements FilenameFilter {

    /**
     * Creates a new case-insensitive <code>StartsFilenameFilter</code> matching filenames starting with the specified
     * string and operating in the specified mode.
     *
     * @param s the string to compare filenames against
     */
    public StartsWithFilenameFilter(String s) {
        this(s, false, false);
    }

    /**
     * Creates a new <code>StartsFilenameFilter</code> matching filenames starting with the specified string and
     * operating in non-inverted mode.
     *
     * @param s the string to compare filenames against
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     */
    public StartsWithFilenameFilter(String s, boolean caseSensitive) {
        this(s, caseSensitive, false);
    }

    /**
     * Creates a new <code>StartsFilenameFilter</code> matching filenames starting with the specified string and
     * operating in the specified modes.
     *
     * @param s the string to compare filenames against
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public StartsWithFilenameFilter(String s, boolean caseSensitive, boolean inverted) {
        super(new FilenameGenerator(), s, caseSensitive, inverted);
    }
}
