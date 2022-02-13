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

/**
 * Default base class for the analysis of the logical structure of a {@link Configuration}.
 * <p>
 * This class is available as a convenience for applications that need to explore the content
 * of a configuration tree. It provides <i>no-op</i> implementations for the methods defined in
 * {@link ConfigurationBuilder}, and application writers can override the ones that are of use
 * to them and ignore the others.
 *
 * @author Nicolas Rinaudo
 */
public class DefaultConfigurationBuilder implements ConfigurationBuilder {
    /**
     * Receive notification at the beginning of the configuration.
     * <p>
     * By default, do nothing. Application writers may override this method in a subclass to take
     * specific actions at the beginning of a document (such as allocating the root node of a tree
     * or creating an output file).
     *
     */
    public void startConfiguration() {}

    /**
     * Receive notification at the end of the configuration.
     * <p>
     * By default, do nothing. Application writers may override this method in a subclass to take
     * specific actions at the end of a document (such as finalising a tree or closing an output file).
     *
     */
    public void endConfiguration() {}

    /**
     * Receive notification at the beginning of a section.
     * <p>
     * By default, do nothing. Application writers may override this method in a subclass to take
     * specific actions at the start of each element (such as allocating a new tree node or writing
     * output to a file).
     *
     * @param  name                   name of the new section.
     */
    public void startSection(String name) {}

    /**
     * Receive notification at the end of a section.
     * <p>
     * By default, do nothing. Application writers may override this method in a subclass to take
     * specific actions at the end of each element (such as finalising a tree node or writing output
     * to a file).
     *
     * @param  name                   name of the finished section.
     */
    public void endSection(String name) {}

    /**
     * Receive notification of variable definition.
     * <p>
     * By default, do nothing. Application writers may override this method to take specific actions for
     * each variable definition (such as adding a leaf to a tree node, or printing it to a file).
     *
     * @param  name                   name of the new variable.
     * @param  value                  value of the new variable.
     */
    public void addVariable(String name, String value) {}
}
