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

package com.mucommander.ui.theme;

/**
 * Defines the format of the XML theme files.
 * @author Nicolas Rinaudo
 */
interface ThemeXmlConstants {
    // - Main elements -------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** XML theme file root element. */
    String ELEMENT_ROOT                 = "theme";
    /** File table description element. */
    String ELEMENT_TABLE                = "file_table";
    /** Shell description element. */
    String ELEMENT_SHELL                = "shell";
    /** File editor description element. */
    String ELEMENT_EDITOR               = "editor";
    /** Location bar description element. */
    String ELEMENT_LOCATION_BAR         = "location_bar";
    /** Shell history description element. */
    String ELEMENT_SHELL_HISTORY        = "shell_history";
    /** Volume label description element. */
    String ELEMENT_STATUS_BAR           = "status_bar";
    /** Quick list label description element. */
    String ELEMENT_QUICK_LIST           = "quick_list";

    String ELEMENT_FILE_GROUPS          = "file_groups";
    String ELEMENT_GROUP                = "group";
    String ELEMENT_TERMINAL             = "terminal";
    String ELEMENT_HEX_VIEWER           = "hex_viewer";




    // - Status element ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Item normal state description element. */
    String ELEMENT_NORMAL               = "normal";
    /** Item selected state description element. */
    String ELEMENT_SELECTED             = "selected";
    /** Item alternate state description element. */
    String ELEMENT_ALTERNATE            = "alternate";
    /** Item unmatched state description element. */
    String ELEMENT_UNMATCHED            = "unmatched";
    /** Item current line state description element. */
    String ELEMENT_CURRENT               = "current";
    
    
    
    // - Quick list element ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Quick list header state description element. */
    String ELEMENT_HEADER               = "header";
    /** Quick list item state description element. */
    String ELEMENT_ITEM		            = "item";



    // - Font element --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Font description element. */
    String ELEMENT_FONT                 = "font";
    /** Font family attribute. */
    String ATTRIBUTE_FAMILY             = "family";
    /** Font size attribute. */
    String ATTRIBUTE_SIZE               = "size";
    /** Font bold attribute. */
    String ATTRIBUTE_BOLD               = "bold";
    /** Font italic attribute. */
    String ATTRIBUTE_ITALIC             = "italic";
    /** <i>true</i> value. */
    String VALUE_TRUE                   = "true";
    /** <i>false</i> value. */
    String VALUE_FALSE                  = "false";




    // - Color elements ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    String ELEMENT_INACTIVE_BACKGROUND           = "inactive_background";
    String ELEMENT_INACTIVE_SECONDARY_BACKGROUND = "inactive_secondary_background";
    String ELEMENT_INACTIVE_FOREGROUND           = "inactive_foreground";
    String ELEMENT_BACKGROUND                    = "background";
    String ELEMENT_SECONDARY_BACKGROUND          = "secondary_background";
    String ELEMENT_FOREGROUND                    = "foreground";
    String ELEMENT_HIDDEN_FILE                   = "hidden_file";
    String ELEMENT_HIDDEN_FOLDER                 = "hidden_folder";
    String ELEMENT_FOLDER                        = "folder";
    String ELEMENT_ARCHIVE                       = "archive";
    String ELEMENT_SYMLINK                       = "symlink";
    String ELEMENT_MARKED                        = "marked";
    String ELEMENT_EXECUTABLE                    = "executable";
    String ELEMENT_FILE                          = "file";
    String ELEMENT_PROGRESS                      = "progress";
    String ELEMENT_BORDER                        = "border";
    String ELEMENT_INACTIVE_BORDER               = "inactive_border";
    String ELEMENT_OUTLINE                       = "outline";
    String ELEMENT_INACTIVE_OUTLINE              = "inactive_outline";
    String ELEMENT_OK                            = "ok";
    String ELEMENT_WARNING                       = "warning";
    String ELEMENT_CRITICAL                      = "critical";
    String ELEMENT_ASCII_FOREGROUND              = "ascii_foreground";
    String ELEMENT_ASCII_BACKGROUND              = "ascii_background";
    String ELEMENT_OFFSET_FOREGROUND             = "offset_foreground";
    String ATTRIBUTE_COLOR                       = "color";
    String ATTRIBUTE_ALPHA                       = "alpha";

}
