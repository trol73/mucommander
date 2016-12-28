package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

/**
 * <code>PassThroughFileFilter</code> is a filter that {@link #accept(com.mucommander.commons.file.AbstractFile) accepts} all
 * files. Depending on the {@link #isInverted() inverted} mode, this filter will match all files or no file at all.
 *
 * @author Maxence Bernard
 */
public class PassThroughFileFilter extends AbstractFileFilter {

    /**
     * Creates a new <code>PassThroughFileFilter</code> operating in non-inverted mode.
     */
    public PassThroughFileFilter() {
        this(false);
    }

    /**
     * Creates a new <code>PassThroughFileFilter</code> operating in the specified mode.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public PassThroughFileFilter(boolean inverted) {
        super(inverted);
    }


    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean accept(AbstractFile file) {
        return true;
    }
}
