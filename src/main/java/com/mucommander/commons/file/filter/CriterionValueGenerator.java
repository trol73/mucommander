package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

/**
 * This interface defines a {@link #getCriterionValue(AbstractFile)} method that generates a criterion value for
 * a specified {@link AbstractFile}. It is used by {@link CriterionFilter} to match files based on their criteria
 * values.
 *
 * @see CriterionFilter
 * @author Maxence Bernard
 */
public interface CriterionValueGenerator<C> {

    C getCriterionValue(AbstractFile file);
}
