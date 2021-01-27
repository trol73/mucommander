package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

/**
 * This interface specializes {@link CriterionValueGenerator} to have {@link #getCriterionValue(AbstractFile)} return
 * the filename of the specified file.
 *
 * @author Maxence Bernard
 */
public class FilenameGenerator implements CriterionValueGenerator<String> {


    public String getCriterionValue(AbstractFile file) {
        return file.getName();
    }
}
