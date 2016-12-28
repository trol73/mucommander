package com.mucommander.commons.file.filter;

/**
 * This {@link PathFilter} matches paths that end with a specified string.
 *
 * @author Maxence Bernard
 */
public class EndsWithPathFilter extends AbstractEndsWithFilter implements PathFilter {

    /**
     * Creates a new case-insensitive <code>EndsWithPathFilter</code> operating in non-inverted mode.
     *
     * @param s the string to compare paths against
     */
    public EndsWithPathFilter(String s) {
        this(s, false, false);
    }

    /**
     * Creates a new <code>EndsWithPathFilter</code> operating in non-inverted mode.
     *
     * @param s the string to compare paths against
     * @param caseSensitive if true, this PathFilter will be case-sensitive
     */
    public EndsWithPathFilter(String s, boolean caseSensitive) {
        this(s, caseSensitive, false);
    }

    /**
     * Creates a new <code>EndsWithPathFilter</code> operating in the specified mode.
     *
     * @param s the string to compare paths against
     * @param caseSensitive if true, this PathFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public EndsWithPathFilter(String s, boolean caseSensitive, boolean inverted) {
        super(new PathGenerator(), s, caseSensitive, inverted);
    }
}
