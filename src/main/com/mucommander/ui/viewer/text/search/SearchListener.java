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


import java.util.EventListener;

/**
 * Listens for events fired from a Find or Replace dialog/tool bar.
 *
 * Created on 21/06/16.
 *
 */
public interface SearchListener extends EventListener {


    /**
     * Callback called whenever a search event occurs.
     *
     * @param e The event.
     */
    void searchEvent(SearchEvent e);


    String getSelectedText();


}