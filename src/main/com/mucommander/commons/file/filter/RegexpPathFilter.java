package com.mucommander.commons.file.filter;

import java.util.regex.PatternSyntaxException;

/**
 * This {@link PathFilter} that accepts or rejects files whose path match a specific regular expression.
 *
 * @author Maxence Bernard
 */
public class RegexpPathFilter extends AbstractRegexpFilter implements PathFilter {

    /**
     * Creates a new <code>RegexpPathFilter</code> matching the specified regexp and operating in non-inverted
     * mode.
     *
     * @param regexp regular expression that matches string values.
     * @param caseSensitive whether the regular expression is case sensitive or not.
     * @throws PatternSyntaxException if the syntax of the regular expression is not correct.
     */
    public RegexpPathFilter(String regexp, boolean caseSensitive) throws PatternSyntaxException {
        super(new PathGenerator(), regexp, caseSensitive, false);
    }

    /**
     * Creates a new <code>RegexpPathFilter</code> matching the specified regexp and operating in the specified
     * modes.
     *
     * @param regexp regular expression that matches string values.
     * @param caseSensitive whether the regular expression is case sensitive or not.
     * @param inverted if true, this filter will operate in inverted mode.
     * @throws PatternSyntaxException if the syntax of the regular expression is not correct.
     */
    public RegexpPathFilter(String regexp, boolean caseSensitive, boolean inverted) throws PatternSyntaxException {
        super(new PathGenerator(), regexp, caseSensitive, inverted);
    }
}
