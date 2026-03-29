package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

/**
 * This interface specializes {@link CriterionValueGenerator} to have {@link #getCriterionValue(AbstractFile)} return
 * the absolute path of the specified file.
 *
 * @author Maxence Bernard
 */
public class PathGenerator implements CriterionValueGenerator<String> {

    @Override
    public String getCriterionValue(AbstractFile file) {
        return file.getPath();
    }
}
