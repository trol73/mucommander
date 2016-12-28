package com.mucommander.commons.file.filter;

/**
 * This {@link PathFilter} matches paths that start with a specified string.
 *
 * @author Maxence Bernard
 */
public class StartsWithPathFilter extends AbstractStartsWithFilter implements PathFilter {

    /**
     * Creates a new case-insensitive <code>StartsPathFilter</code> matching paths starting with the specified
     * string and operating in the specified mode.
     *
     * @param s the string to compare paths against
     */
    public StartsWithPathFilter(String s) {
        this(s, false, false);
    }

    /**
     * Creates a new <code>StartsPathFilter</code> matching paths starting with the specified string and
     * operating in non-inverted mode.
     *
     * @param s the string to compare paths against
     * @param caseSensitive if true, this PathFilter will be case-sensitive
     */
    public StartsWithPathFilter(String s, boolean caseSensitive) {
        this(s, caseSensitive, false);
    }

    /**
     * Creates a new <code>StartsPathFilter</code> matching paths starting with the specified string and
     * operating in the specified modes.
     *
     * @param s the string to compare paths against
     * @param caseSensitive if true, this PathFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public StartsWithPathFilter(String s, boolean caseSensitive, boolean inverted) {
        super(new PathGenerator(), s, caseSensitive, inverted);
    }
}
