package com.mucommander.commons.file.filter;

/**
 * This {@link PathFilter} matches paths that contain a specified string that can be located anywhere in the
 * path.
 *
 * @author Maxence Bernard
 */
public class ContainsPathFilter extends AbstractContainsFilter implements PathFilter {

    /**
     * Creates a new case-insensitive <code>ContainsPathFilter</code> operating in non-inverted mode.
     *
     * @param s the string to compare paths against
     */
    public ContainsPathFilter(String s) {
        this(s, false, false);
    }

    /**
     * Creates a new <code>ContainsPathFilter</code> operating in non-inverted mode.
     *
     * @param s the string to compare paths against
     * @param caseSensitive if true, this PathFilter will be case-sensitive
     */
    public ContainsPathFilter(String s, boolean caseSensitive) {
        this(s, caseSensitive, false);
    }

    /**
     * Creates a new <code>ContainsPathFilter</code> operating in the specified mode.
     *
     * @param s the string to compare paths against
     * @param caseSensitive if true, this PathFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public ContainsPathFilter(String s, boolean caseSensitive, boolean inverted) {
        super(new PathGenerator(), s, caseSensitive, inverted);
    }
}
