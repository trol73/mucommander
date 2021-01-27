/*
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

package com.mucommander.commons.conf;

import java.util.Stack;

/**
 * Helper class meant for instances of {@link Configuration} to explore their own configuration tree.
 * <p>
 * This behaves exactly as a {@link ConfigurationExplorer}, but keeps track of its own path. This is meant
 * for instances of {@link Configuration} to prune empty branches.
 *
 * @author Nicolas Rinaudo
 */
class BufferedConfigurationExplorer extends ConfigurationExplorer {
    /** Sections that have been passed through. */
    private final Stack<ConfigurationSection> sections = new Stack<>();


    /**
     * Creates a new explorer on the specified section.
     * @param root section from which to start exploring.
     */
    BufferedConfigurationExplorer(ConfigurationSection root) {
        super(root);
    }



    /**
     * Returns <code>true</code> if there are more sections in the history.
     * @return <code>true</code> if there are more sections in the history.
     */
    boolean hasSections() {
        return !sections.empty();
    }

    /**
     * Returns the next section in history.
     * @return the next section in history.
     */
    ConfigurationSection popSection() {
        return sections.pop();
    }


    /**
     * Move to the specified section.
     * @param  name   name of the current section's subsection in which to move.
     * @param  create if <code>true</code> and <code>name</code> doesn't exist, it will be created.
     * @return        <code>true</code> if we could move to <code>name</code>, <code>false</code> otherwise.
     */
    @Override
    public boolean moveTo(String name, boolean create) {
        if (super.moveTo(name, create)) {
            sections.push(getSection());
            return true;
        }
        return false;
    }
}
