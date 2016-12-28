package com.mucommander.commons.file.filter;

/**
 * This {@link PathFilter} matches paths that are equal to a specified string.
 *
 * @author Maxence Bernard
 */
public class EqualsPathFilter extends AbstractEqualsFilter implements PathFilter {

    /**
     * Creates a new case-insensitive <code>EqualsPathFilter</code> operating in non-inverted mode.
     *
     * @param s the string to compare paths against
     */
    public EqualsPathFilter(String s) {
        this(s, false, false);
    }

    /**
     * Creates a new <code>EqualsPathFilter</code> operating in non-inverted mode.
     *
     * @param s the string to compare paths against
     * @param caseSensitive if true, this PathFilter will be case-sensitive
     */
    public EqualsPathFilter(String s, boolean caseSensitive) {
        this(s, caseSensitive, false);
    }

    /**
     * Creates a new <code>EqualsPathFilter</code> operating in the specified mode.
     *
     * @param s the string to compare paths against
     * @param caseSensitive if true, this PathFilter will be case-sensitive
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public EqualsPathFilter(String s, boolean caseSensitive, boolean inverted) {
        super(new PathGenerator(), s, caseSensitive, inverted);
    }
}
