package com.mucommander.commons.file.filter;

/**
 * @author Maxence Bernard
 */
public interface StringCriterionFilter extends CriterionFilter<String> {

    /**
     * Returns <code>true</code> if this <code>CriterionFilter</code> is case-sensitive.
     *
     * @return true if this <code>CriterionFilter</code> is case-sensitive.
     */
    boolean isCaseSensitive();

    /**
     * Specifies whether this <code>CriterionFilter</code> should be case-sensitive or not when comparing paths.
     *
     * @param caseSensitive if true, this CriterionFilter will be case-sensitive
     */
    void setCaseSensitive(boolean caseSensitive);
}
