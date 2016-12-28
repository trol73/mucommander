/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.ui.viewer.text.search;

import org.fife.ui.rtextarea.SearchContext;

import java.util.EventObject;

/**
 * The event fired whenever a user wants to search for or replace text in a
 * Find or Replace dialog/tool bar.
 *
 * Created on 21/06/16.
 */
public class SearchEvent extends EventObject {

    private SearchContext context;
    private Type type;

    public SearchEvent(Object source, Type type, SearchContext context) {
        super(source);
        this.type = type;
        this.context = context;
    }


    public Type getType() {
        return type;
    }


    public SearchContext getSearchContext() {
        return context;
    }


    /**
     * Types of search events.
     */
    public enum Type {

        /**
         * The event fired when the text to "mark all" has changed.
         */
        MARK_ALL,

        /**
         * The event fired when the user wants to find text in the editor.
         */
        FIND,

        /**
         * The event fired when the user wants to replace text in the editor.
         */
        REPLACE,

        /**
         * The event fired when the user wants to replace all instances of
         * specific text with new text in the editor.
         */
        REPLACE_ALL

    }
}