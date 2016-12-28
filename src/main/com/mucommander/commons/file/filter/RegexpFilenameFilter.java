package com.mucommander.commons.file.filter;

import java.util.regex.PatternSyntaxException;

/**
 * This {@link FilenameFilter} that accepts or rejects files whose filename match a specific regular expression.
 *
 * @author Maxence Bernard
 */
public class RegexpFilenameFilter extends AbstractRegexpFilter implements FilenameFilter {

    /**
     * Creates a new <code>RegexpFilenameFilter</code> matching the specified regexp and operating in non-inverted
     * mode.
     *
     * @param regexp regular expression that matches string values.
     * @param caseSensitive whether the regular expression is case sensitive or not.
     * @throws PatternSyntaxException if the syntax of the regular expression is not correct.
     */
    public RegexpFilenameFilter(String regexp, boolean caseSensitive) throws PatternSyntaxException {
        super(new FilenameGenerator(), regexp, caseSensitive, false);
    }

    /**
     * Creates a new <code>RegexpFilenameFilter</code> matching the specified regexp and operating in the specified
     * modes.
     *
     * @param regexp regular expression that matches string values.
     * @param caseSensitive whether the regular expression is case sensitive or not.
     * @param inverted if true, this filter will operate in inverted mode.
     * @throws PatternSyntaxException if the syntax of the regular expression is not correct.
     */
    public RegexpFilenameFilter(String regexp, boolean caseSensitive, boolean inverted) throws PatternSyntaxException {
        super(new FilenameGenerator(), regexp, caseSensitive, inverted);
    }
}
