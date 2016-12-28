package com.mucommander.commons.file.filter;

/**
 * This {@link FilenameFilter} matches filenames that contain a specified string that can be located anywhere in the
 * filename.
 *
 * @author Maxence Bernard
 */
public class ContainsFilenameFilter extends AbstractContainsFilter implements FilenameFilter {

    /**
     * Creates a new case-insensitive <code>ContainsFilenameFilter</code> operating in non-inverted mode.
     *
     * @param s the string to compare filenames against
     */
    public ContainsFilenameFilter(String s) {
        this(s, false, false);
    }

    /**
     * Creates a new <code>ContainsFilenameFilter</code> operating in non-inverted mode.
     *
     * @param s the string to compare filenames against
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     */
    public ContainsFilenameFilter(String s, boolean caseSensitive) {
        this(s, caseSensitive, false);
    }

    /**
     * Creates a new <code>ContainsFilenameFilter</code> operating in the specified mode.
     *
     * @param s the string to compare filenames against
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public ContainsFilenameFilter(String s, boolean caseSensitive, boolean inverted) {
        super(new FilenameGenerator(), s, caseSensitive, inverted);
    }
}
