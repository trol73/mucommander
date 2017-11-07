package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

/**
 * <code>CriterionFilter</code> is a {@link FileFilter} that operates on a file criterion. It can be used to match
 * paths without having to deal with {@link AbstractFile} instances. By extending {@link FileFilter}, this class can be
 * used everywhere a <code>FileFilter</code> is accepted.
 *
 * <p>Several convenience methods are provided to operate this filter on a set of criteria values, and filter out
 * values that are rejected by this filter.
 *
 * @see AbstractPathFilter
 * @author Maxence Bernard
 */
public interface CriterionFilter<C> extends FileFilter {

    /**
     * Returns <code>true</code> if this filter matched the given value, according to the current {@link #isInverted()}
     * mode:
     * <ul>
     *  <li>if this filter currently operates in normal (non-inverted) mode, this method will return the value of {@link #accept(Object)}</li>
     *  <li>if this filter currently operates in inverted mode, this method will return the value of {@link #reject(Object)}</li>
     * </ul>
     *
     * @param value the value to test
     * @return true if this filter matched the given value, according to the current inverted mode
     */
    boolean match(C value);

    /**
     * Returns <code>true</code> if the given value was rejected by this filter, <code>false</code> if it was accepted.
     *
     * <p>The {@link #isInverted() inverted} mode has no effect on the values returned by this method.
     *
     * @param value the value to be tested
     * @return true if the given value was rejected by this filter
     */
    boolean reject(C value);

    /**
     * Convenience method that filters out files that do not {@link #match(AbstractFile) match} this filter and
     * returns a file array of matched <code>AbstractFile</code> instances.
     *
     * @param value values to be tested
     * @return an array of accepted AbstractFile instances
     */
    C[] filter(C value[]);

    /**
     * Convenience method that returns <code>true</code> if all the values in the specified array were matched by
     * {@link #match(Object)}, <code>false</code> if one of the values wasn't.
     *
     * @param value the values to be tested
     * @return true if all the values in the specified array were accepted
     */
    boolean match(C value[]);

    /**
     * Convenience method that returns <code>true</code> if all the values in the specified array were accepted by
     * {@link #accept(Object)}, <code>false</code> if one of the values wasn't.
     *
     * @param value the values to be tested
     * @return true if all the values in the specified array were accepted
     */
    boolean accept(C value[]);

    /**
     * Convenience method that returns <code>true</code> if all the values in the specified array were rejected by
     * {@link #reject(Object)}, <code>false</code> if one of the values wasn't.
     *
     * @param value the values to be tested
     * @return true if all the values in the specified array were rejected
     */
    boolean reject(C value[]);

    /**
     * Returns <code>true</code> if the given value was accepted by this filter, <code>false</code> if it was rejected.
     *
     * @param value the value to test
     * @return true if the given value was accepted by this filter, false if it was rejected
     */
    boolean accept(C value);
}
