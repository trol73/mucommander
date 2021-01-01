/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.main.table;

import com.mucommander.conf.TcConfigurations;
import com.mucommander.conf.TcPreference;
import com.mucommander.conf.TcPreferences;

/**
 * This class holds information describes how a {@link FileTable} is currently sorted: sort criterion,
 * ascending/descending order, whether directories are displayed first or mixed with regular files.
 *
 * <p>The values are not meant to be changed outside this package: all setters are package-protected.
 * Use {@link FileTable} methods to change how the table is sorted.
 *
 * @author Maxence Bernard
 */
public class SortInfo implements Cloneable {

    /** Current sort criterion */
    private Column criterion = Column.NAME;

    /** Ascending/descending order */
    private boolean ascendingOrder = true;

    /** Should folders be displayed first, or mixed with regular files */
    private boolean showFoldersFirst = TcConfigurations.getPreferences().getVariable(TcPreference.SHOW_FOLDERS_FIRST, TcPreferences.DEFAULT_SHOW_FOLDERS_FIRST);

    /** Should Folders also get sorted or alway alphabetical ... only possible if Folders First enabled */
    private boolean foldersAlwaysAlphabetical = TcConfigurations.getPreferences().getVariable(TcPreference.FOLDERS_ALWAYS_ALPHABETICAL, TcPreferences.DEFAULT_FOLDERS_ALWAYS_ALPHABETICAL);

    private boolean showQuickSearchMatchesFirst = TcConfigurations.getPreferences().getVariable(TcPreference.SHOW_QUICK_SEARCH_MATCHES_FIRST, TcPreferences.DEFAULT_SHOW_QUICK_SEARCH_MATCHES_FIRST);

    SortInfo() {
    }


    /**
     * Returns the column used as a criterion to sort the table.
     *
     * @return the column used as a criterion to sort the table.
     */
    public Column getCriterion() {
        return criterion;
    }

    /**
     * Sets the column to be used as a criterion to sort the table.
     *
     * @param criterion the column to be used as a criterion to sort the table, see {@link Column} for possible values
     */
    public void setCriterion(Column criterion) {
        this.criterion = criterion;
    }

    /**
     * Returns <code>true</code> if the current sort order of is ascending, <code>false</code> if it is descending.
     *
     * @return true if the current sort order is ascending, false if it is descending
     */
    public boolean getAscendingOrder() {
        return ascendingOrder;
    }

    /**
     * Sets the sort order of the column corresponding to the current criterion.
     *
     * @param ascending true if the current sort order is ascending, false if it is descending
     */
    public void setAscendingOrder(boolean ascending) {
        this.ascendingOrder = ascending;
    }

    /**
     * Sets whether folders are currently sorted and displayed before regular files or mixed with them.
     *
     * @param showFoldersFirst true if folders are sorted and displayed before regular files, false if they are mixed with regular files and sorted altogether
     */
    public void setFoldersFirst(boolean showFoldersFirst) {
        this.showFoldersFirst = showFoldersFirst;
    }

    /**
     * Sets whether folders are currently sorted always alphabetical.
     *
     * @param foldersAlwaysAlphabetical true if folders are sorted always alphabetical
     */
    public void setFoldersAlwaysAlphabetical(boolean foldersAlwaysAlphabetical) {
        this.foldersAlwaysAlphabetical = foldersAlwaysAlphabetical;
    }

    /**
     * Returns <code>true</code> if folders are sorted and displayed before regular files, <code>false</code> if they
     * are mixed with regular files and sorted altogether.
     *
     * @return true if folders are sorted and displayed before regular files, false if they are mixed with regular files and sorted altogether
     */
    public boolean getFoldersFirst() {
        return showFoldersFirst;
    }

    /**
     * Returns <code>true</code> if folders are sorted always alphabetical
     *
     * @return true if folders are sorted always alphabetical
     */
    public boolean getFoldersAlwaysAlphabetical() {
        return foldersAlwaysAlphabetical;
    }

    /**
     * Sets whether matched files are currently sorted and displayed before other files or mixed with them on quick search.
     *
     * @param quickSearchMatchesFirst true if matched are sorted and displayed before other files, false if they are mixed with regular files and sorted altogether
     */
    void setQuickSearchMatchesFirst(boolean quickSearchMatchesFirst) {
        this.showQuickSearchMatchesFirst = quickSearchMatchesFirst;
    }

    /**
     * Returns <code>true</code> if quick search matched are sorted and displayed before other files, <code>false</code> if they
     * are mixed with other files and sorted altogether.
     *
     * @return true if matched are sorted and displayed before other files, false if they are mixed with other files and sorted altogether
     */
    public boolean getQuickSearchMatchesFirst() {
        return showQuickSearchMatchesFirst;
    }



    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public SortInfo clone() {
        try {
            return (SortInfo)super.clone();
        } catch(CloneNotSupportedException e) {
            // Should never happen
            return null;
        }
    }
}
