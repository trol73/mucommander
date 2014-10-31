/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;

import java.util.ArrayList;
import java.util.List;


/**
 * <code>AbstractFileFilter</code> implements the bulk of the {@link FileFilter} interface. The only method left for
 * subclasses to implement is {@link #accept(AbstractFile)}.
 *
 * @see AbstractFilenameFilter
 * @author Maxence Bernard
 */
public abstract class AbstractFileFilter implements FileFilter {

    /** True if this filter should operate in inverted mode and invert matches */
    protected boolean inverted;

    /**
     * Creates a new <code>AbstractFileFilter</code> operating in non-inverted mode.
     */
    public AbstractFileFilter() {
        this(false);
    }

    /**
     * Creates a new <code>AbstractFileFilter</code> operating in the specified mode.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public AbstractFileFilter(boolean inverted) {
        setInverted(inverted);
    }


    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public boolean match(AbstractFile file) {
        if(inverted)
            return reject(file);

        return accept(file);
    }

    public boolean reject(AbstractFile file) {
        return !accept(file);
    }

    public AbstractFile[] filter(AbstractFile files[]) {
        List<AbstractFile> filteredFilesV = new ArrayList<>();
        for (AbstractFile file : files) {
            if (match(file))
                filteredFilesV.add(file);
        }

        AbstractFile filteredFiles[] = new AbstractFile[filteredFilesV.size()];
        filteredFilesV.toArray(filteredFiles);
        return filteredFiles;
    }

    public void filter(FileSet files) {
        for(int i=0; i<files.size();) {
            if(reject(files.elementAt(i)))
                files.removeElementAt(i);
            else
                i++;
        }
    }

    public boolean match(AbstractFile files[]) {
        for (AbstractFile file : files)
            if (!match(file))
                return false;

        return true;
    }

    public boolean match(FileSet files) {
        int nbFiles = files.size();
        for(int i=0; i<nbFiles; i++)
            if(!match(files.elementAt(i)))
                return false;

        return true;
    }

    public boolean accept(AbstractFile files[]) {
        for (AbstractFile file : files)
            if (!accept(file))
                return false;

        return true;
    }

    public boolean accept(FileSet files) {
        int nbFiles = files.size();
        for(int i=0; i<nbFiles; i++)
            if(!accept(files.elementAt(i)))
                return false;

        return true;
    }

    public boolean reject(AbstractFile files[]) {
        for (AbstractFile file : files)
            if (!reject(file))
                return false;

        return true;
    }

    public boolean reject(FileSet files) {
        int nbFiles = files.size();
        for(int i=0; i<nbFiles; i++)
            if(!reject(files.elementAt(i)))
                return false;

        return true;
    }
}