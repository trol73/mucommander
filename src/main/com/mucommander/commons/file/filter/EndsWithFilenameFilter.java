package com.mucommander.commons.file.filter;

/**
 * This {@link FilenameFilter} matches filenames that end with a specified string.
 *
 * @author Maxence Bernard
 */
public class EndsWithFilenameFilter extends AbstractEndsWithFilter implements FilenameFilter {

    /**
     * Creates a new case-insensitive <code>EndsWithFilenameFilter</code> operating in non-inverted mode.
     *
     * @param s the string to compare filenames against
     */
    public EndsWithFilenameFilter(String s) {
        this(s, false, false);
    }

    /**
     * Creates a new <code>EndsWithFilenameFilter</code> operating in non-inverted mode.
     *
     * @param s the string to compare filenames against
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     */
    public EndsWithFilenameFilter(String s, boolean caseSensitive) {
        this(s, caseSensitive, false);
    }

    /**
     * Creates a new <code>EndsWithFilenameFilter</code> operating in the specified mode.
     *
     * @param s the string to compare filenames against
     * @param caseSensitive if true, this FilenameFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public EndsWithFilenameFilter(String s, boolean caseSensitive, boolean inverted) {
        super(new FilenameGenerator(), s, caseSensitive, inverted);
    }
}
