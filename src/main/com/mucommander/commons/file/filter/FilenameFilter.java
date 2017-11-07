package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

/**
 * <code>FilenameFilter</code> is a {@link FileFilter} that operates on filenames.
 *
 * <p>A <code>FilenameFilter</code> can be passed to {@link AbstractFile#ls(FilenameFilter)} to filter out some of the
 * files contained by a folder without creating the associated <code>AbstractFile</code> instances.
 *
 * @see AbstractFilenameFilter
 * @author Maxence Bernard
 */
public interface FilenameFilter extends StringCriterionFilter {
}
