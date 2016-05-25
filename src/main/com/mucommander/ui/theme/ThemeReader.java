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

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Loads theme instances from properly formatted XML files.
 * @author Nicolas Rinaudo
 */
class ThemeReader extends DefaultHandler implements ThemeXmlConstants {
	private static Logger logger;
	
    private enum State {
    /** Parsing hasn't started yet. */
        UNKNOWN,
    /** Parsing the root element. */
        ROOT,
    /** Parsing the table element.*/
        TABLE,
    /** Parsing the shell element. */
        SHELL,
    /** Parsing the editor element. */
        EDITOR,
    /** Parsing the location bar element. */
        LOCATION_BAR,
    /** Parsing the shell.normal element. */
        SHELL_NORMAL,
    /** Parsing the shell.selected element. */
        SHELL_SELECTED,
    /** Parsing the editor.normal element. */
        EDITOR_NORMAL,
    /** Parsing the location bar.normal element. */
        LOCATION_BAR_NORMAL,
    /** Parsing the editor.selected element. */
        EDITOR_SELECTED,
    /** Parsing the location bar.selected element. */
        LOCATION_BAR_SELECTED,
    /** Parsing the shell_history element. */
        SHELL_HISTORY,
    /** Parsing the shell_history.normal element. */
        SHELL_HISTORY_NORMAL,
    /** Parsing the shell_history.selected element. */
        SHELL_HISTORY_SELECTED,
    /** Parsing the volume_label element. */
        STATUS_BAR,
        HIDDEN,
        HIDDEN_NORMAL,
        HIDDEN_SELECTED,
        FOLDER,
        FOLDER_NORMAL,
        FOLDER_SELECTED,
        ARCHIVE,
        ARCHIVE_NORMAL,
        ARCHIVE_SELECTED,
        SYMLINK,
        SYMLINK_NORMAL,
        SYMLINK_SELECTED,
        MARKED,
        MARKED_NORMAL,
        MARKED_SELECTED,
        EXECUTABLE,
        EXECUTABLE_NORMAL,
        EXECUTABLE_SELECTED,
        FILE,
        FILE_NORMAL,
        FILE_SELECTED,
        TABLE_NORMAL,
        TABLE_SELECTED,
        TABLE_ALTERNATE,
        TABLE_UNMATCHED,
    /** Parsing the quick list element. */
        QUICK_LIST,
    /** Parsing the quick list header element. */
        QUICK_LIST_HEADER,
    /** Parsing the quick list item element. */
        QUICK_LIST_ITEM,
        QUICK_LIST_ITEM_NORMAL,
        QUICK_LIST_ITEM_SELECTED,
    /** Parsing the editor.selected element. */
        EDITOR_CURRENT,
        FILE_GROUP,
        GROUP_1,
        GROUP_2,
        GROUP_3,
        GROUP_4,
        GROUP_5,
        GROUP_6,
        GROUP_7,
        GROUP_8,
        GROUP_9,
        GROUP_10,

        TERMINAL,
        /** Parsing the terminal.normal element. */
        TERMINAL_NORMAL,
        /** Parsing the terminal.selected element. */
        TERMINAL_SELECTED

    }

    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Theme template that is currently being built. */
    private ThemeData     template;
    /** Current state of the XML parser. */
    private State           state;
    /** Used to ignore the content of an unknown tag. */
    private String        unknownElement;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new theme reader.
     */
    private ThemeReader(ThemeData t) {
        template = t;
        state = State.UNKNOWN;
    }

    /**
     * Attempts to read a theme from the specified input stream.
     * @param     in        where to read the theme from.
     * @param     template  template in which to store the data.
     * @exception Exception thrown if an error occured while reading the template.
     */
    public static void read(InputStream in, ThemeData template) throws Exception {
        SAXParserFactory.newInstance().newSAXParser().parse(in, new ThemeReader(template));
    }


    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Notifies the reader that a new XML element is starting.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        // Ignores the content of unknown elements.
        if (unknownElement != null) {
            getLogger().debug("Ignoring element " + qName);
            return;
        }

        // Normal element declaration.
        if (qName.equals(ELEMENT_NORMAL)) {
            if (state == State.SHELL)
                state = State.SHELL_NORMAL;
            else if (state == State.EDITOR)
                state = State.EDITOR_NORMAL;
            else if (state == State.LOCATION_BAR)
                state = State.LOCATION_BAR_NORMAL;
            else if (state == State.SHELL_HISTORY)
                state = State.SHELL_HISTORY_NORMAL;
            else if (state == State.TERMINAL)
                state = State.TERMINAL_NORMAL;
            else if (state == State.HIDDEN)
                state = State.HIDDEN_NORMAL;
            else if (state == State.FOLDER)
                state = State.FOLDER_NORMAL;
            else if (state == State.ARCHIVE)
                state = State.ARCHIVE_NORMAL;
            else if (state == State.SYMLINK)
                state = State.SYMLINK_NORMAL;
            else if (state == State.MARKED)
                state = State.MARKED_NORMAL;
            else if (state == State.EXECUTABLE)
                state = State.EXECUTABLE_NORMAL;
            else if (state == State.FILE)
                state = State.FILE_NORMAL;
            else if (state == State.TABLE)
                state = State.TABLE_NORMAL;
            else if (state == State.QUICK_LIST_ITEM)
                state = State.QUICK_LIST_ITEM_NORMAL;
            else if (state.ordinal() >= State.GROUP_1.ordinal() && state.ordinal() <= State.GROUP_10.ordinal()) {
                int group = state.ordinal() - State.GROUP_1.ordinal();
                template.setColorFast(ThemeData.FILE_GROUP_1_FOREGROUND_COLOR + group, createColor(attributes));
            } else
                traceIllegalDeclaration(qName);
        }

        // Background color.
        else if (qName.equals(ELEMENT_BACKGROUND)) {
            if (state == State.TABLE_NORMAL)
                template.setColorFast(ThemeData.FILE_TABLE_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.TABLE_SELECTED)
                template.setColorFast(ThemeData.FILE_TABLE_SELECTED_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.TABLE_ALTERNATE)
                template.setColorFast(ThemeData.FILE_TABLE_ALTERNATE_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.TABLE_UNMATCHED)
                template.setColorFast(ThemeData.FILE_TABLE_UNMATCHED_BACKGROUND_COLOR, createColor(attributes));

            else if (state == State.SHELL_NORMAL)
                template.setColorFast(ThemeData.SHELL_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.SHELL_SELECTED)
                template.setColorFast(ThemeData.SHELL_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if (state == State.EDITOR_NORMAL)
                template.setColorFast(ThemeData.EDITOR_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.EDITOR_SELECTED)
                template.setColorFast(ThemeData.EDITOR_SELECTED_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.EDITOR_CURRENT)
                template.setColorFast(ThemeData.EDITOR_CURRENT_BACKGROUND_COLOR, createColor(attributes));

            else if (state == State.LOCATION_BAR_NORMAL)
                template.setColorFast(ThemeData.LOCATION_BAR_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.LOCATION_BAR_SELECTED)
                template.setColorFast(ThemeData.LOCATION_BAR_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if (state == State.SHELL_HISTORY_NORMAL)
                template.setColorFast(ThemeData.SHELL_HISTORY_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.SHELL_HISTORY_SELECTED)
                template.setColorFast(ThemeData.SHELL_HISTORY_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if (state == State.TERMINAL_NORMAL)
                template.setColorFast(ThemeData.TERMINAL_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.TERMINAL_SELECTED)
                template.setColorFast(ThemeData.TERMINAL_SELECTED_BACKGROUND_COLOR, createColor(attributes));

            else if (state == State.STATUS_BAR)
                template.setColorFast(ThemeData.STATUS_BAR_BACKGROUND_COLOR, createColor(attributes));

            else if (state == State.QUICK_LIST_HEADER)
                template.setColorFast(ThemeData.QUICK_LIST_HEADER_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.QUICK_LIST_ITEM_NORMAL)
                template.setColorFast(ThemeData.QUICK_LIST_ITEM_BACKGROUND_COLOR, createColor(attributes));
            else if (state == State.QUICK_LIST_ITEM_SELECTED)
                template.setColorFast(ThemeData.QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR, createColor(attributes));

            else
                traceIllegalDeclaration(qName);
        }

        // Selected element declaration.
        else if (qName.equals(ELEMENT_SELECTED)) {
            if (state == State.SHELL)
                state = State.SHELL_SELECTED;
            else if (state == State.EDITOR)
                state = State.EDITOR_SELECTED;
            else if (state == State.LOCATION_BAR)
                state = State.LOCATION_BAR_SELECTED;
            else if (state == State.SHELL_HISTORY)
                state = State.SHELL_HISTORY_SELECTED;
            else if (state == State.TERMINAL)
                state = State.TERMINAL_SELECTED;
            else if (state == State.HIDDEN)
                state = State.HIDDEN_SELECTED;
            else if (state == State.FOLDER)
                state = State.FOLDER_SELECTED;
            else if (state == State.ARCHIVE)
                state = State.ARCHIVE_SELECTED;
            else if (state == State.SYMLINK)
                state = State.SYMLINK_SELECTED;
            else if (state == State.MARKED)
                state = State.MARKED_SELECTED;
            else if (state == State.EXECUTABLE)
                state = State.EXECUTABLE_SELECTED;
            else if (state == State.FILE)
                state = State.FILE_SELECTED;
            else if (state == State.TABLE)
                state = State.TABLE_SELECTED;
            else if (state == State.QUICK_LIST_ITEM)
                state = State.QUICK_LIST_ITEM_SELECTED;

            else
                traceIllegalDeclaration(qName);
        }

        // Unfocused foreground color.
        else if(qName.equals(ELEMENT_INACTIVE_FOREGROUND)) {
            if(state == State.FILE_NORMAL)
                template.setColorFast(ThemeData.FILE_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.FOLDER_NORMAL)
                template.setColorFast(ThemeData.FOLDER_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.ARCHIVE_NORMAL)
                template.setColorFast(ThemeData.ARCHIVE_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.SYMLINK_NORMAL)
                template.setColorFast(ThemeData.SYMLINK_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.HIDDEN_NORMAL)
                template.setColorFast(ThemeData.HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.MARKED_NORMAL)
                template.setColorFast(ThemeData.MARKED_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.EXECUTABLE_NORMAL)
                template.setColorFast(ThemeData.EXECUTABLE_INACTIVE_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.FILE_SELECTED)
                template.setColorFast(ThemeData.FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.FOLDER_SELECTED)
                template.setColorFast(ThemeData.FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.ARCHIVE_SELECTED)
                template.setColorFast(ThemeData.ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.SYMLINK_SELECTED)
                template.setColorFast(ThemeData.SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.HIDDEN_SELECTED)
                template.setColorFast(ThemeData.HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.MARKED_SELECTED)
                template.setColorFast(ThemeData.MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else if(state == State.EXECUTABLE_SELECTED)
                template.setColorFast(ThemeData.EXECUTABLE_INACTIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Font creation.
        else if (qName.equals(ELEMENT_FONT)) {
            if (state == State.SHELL)
                template.setFontFast(ThemeData.SHELL_FONT, createFont(attributes));
            else if (state == State.EDITOR)
                template.setFontFast(ThemeData.EDITOR_FONT, createFont(attributes));
            else if (state == State.LOCATION_BAR)
                template.setFontFast(ThemeData.LOCATION_BAR_FONT, createFont(attributes));
            else if (state == State.SHELL_HISTORY)
                template.setFontFast(ThemeData.SHELL_HISTORY_FONT, createFont(attributes));
            else if (state == State.TERMINAL)
                template.setFontFast(ThemeData.TERMINAL_FONT, createFont(attributes));
            else if (state == State.STATUS_BAR)
                template.setFontFast(ThemeData.STATUS_BAR_FONT, createFont(attributes));
            else if (state == State.TABLE)
                template.setFontFast(ThemeData.FILE_TABLE_FONT, createFont(attributes));
            else if (state == State.QUICK_LIST_HEADER)
                template.setFontFast(ThemeData.QUICK_LIST_HEADER_FONT, createFont(attributes));
            else if (state == State.QUICK_LIST_ITEM)
                template.setFontFast(ThemeData.QUICK_LIST_ITEM_FONT, createFont(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // XML root element.
        else if (qName.equals(ELEMENT_ROOT)) {
            if (state != State.UNKNOWN)
                traceIllegalDeclaration(ELEMENT_ROOT);
            state = State.ROOT;
        }

        // File table declaration.
        else if (qName.equals(ELEMENT_TABLE)) {
            if (state != State.ROOT)
                traceIllegalDeclaration(qName);
            state = State.TABLE;
        }

        else if (qName.equals(ELEMENT_FILE_GROUPS)) {
            if (state != State.ROOT)
                traceIllegalDeclaration(qName);
            state = State.FILE_GROUP;
        }

        // Shell declaration.
        else if (qName.equals(ELEMENT_SHELL)) {
            if (state != State.ROOT)
                traceIllegalDeclaration(qName);
            state = State.SHELL;
        }

        // Editor declaration.
        else if (qName.equals(ELEMENT_EDITOR)) {
            if (state != State.ROOT)
                traceIllegalDeclaration(qName);
            state = State.EDITOR;
        }

        // Location bar declaration.
        else if (qName.equals(ELEMENT_LOCATION_BAR)) {
            if (state != State.ROOT)
                traceIllegalDeclaration(qName);
            state = State.LOCATION_BAR;
        }
        
        // Quick list declaration.
        else if (qName.equals(ELEMENT_QUICK_LIST)) {
            if (state != State.ROOT)
                traceIllegalDeclaration(qName);
            state = State.QUICK_LIST;
        }

        // Shell history declaration.
        else if (qName.equals(ELEMENT_SHELL_HISTORY)) {
            if (state != State.ROOT)
                traceIllegalDeclaration(qName);
            state = State.SHELL_HISTORY;
        }

        else if (qName.equals(ELEMENT_TERMINAL)) {
            if (state != State.ROOT)
                traceIllegalDeclaration(qName);
            state = State.TERMINAL;
        }

        // Volume label declaration.
        else if (qName.equals(ELEMENT_STATUS_BAR)) {
            if (state != State.ROOT)
                traceIllegalDeclaration(qName);
            state = State.STATUS_BAR;
        }

        else if (qName.equals(ELEMENT_HIDDEN)) {
            if (state != State.TABLE)
                traceIllegalDeclaration(qName);
            state = State.HIDDEN;
        }

        else if (qName.equals(ELEMENT_FOLDER)) {
            if (state != State.TABLE)
                traceIllegalDeclaration(qName);
            state = State.FOLDER;
        }

        else if (qName.equals(ELEMENT_ARCHIVE)) {
            if(state != State.TABLE)
                traceIllegalDeclaration(qName);
            state = State.ARCHIVE;
        }

        else if (qName.equals(ELEMENT_SYMLINK)) {
            if (state != State.TABLE)
                traceIllegalDeclaration(qName);
            state = State.SYMLINK;
        }

        else if (qName.equals(ELEMENT_MARKED)) {
            if (state != State.TABLE)
                traceIllegalDeclaration(qName);
            state = State.MARKED;
        }

        else if (qName.equals(ELEMENT_EXECUTABLE)) {
            if (state != State.TABLE)
                traceIllegalDeclaration(qName);
            state = State.EXECUTABLE;
        }

        else if (qName.equals(ELEMENT_FILE)) {
            if (state != State.TABLE)
                traceIllegalDeclaration(qName);
            state = State.FILE;
        }

        else if (qName.equals(ELEMENT_ALTERNATE)) {
            if (state != State.TABLE)
                traceIllegalDeclaration(qName);
            state = State.TABLE_ALTERNATE;
        }
        
        // Header declaration.
        else if (qName.equals(ELEMENT_HEADER)) {
        	if (state == State.QUICK_LIST)
                state = State.QUICK_LIST_HEADER;
            else
                traceIllegalDeclaration(qName);
        }
        
        // Item declaration.
        else if (qName.equals(ELEMENT_ITEM)) {
        	if (state == State.QUICK_LIST)
                state = State.QUICK_LIST_ITEM;
            else
                traceIllegalDeclaration(qName);
        }

        else if(qName.equals(ELEMENT_TERMINAL)) {
            if (state != State.ROOT)
                traceIllegalDeclaration(qName);
            state = State.TERMINAL;
        }


        // Current element declaration.
        else if (qName.equals(ELEMENT_CURRENT)) {
            if (state == State.EDITOR) {
                state = State.EDITOR_CURRENT;
            } else {
                traceIllegalDeclaration(qName);
            }
        }


        // Unfocused background color.
        else if(qName.equals(ELEMENT_INACTIVE_BACKGROUND)) {
            if(state == State.TABLE_NORMAL)
                template.setColorFast(ThemeData.FILE_TABLE_INACTIVE_BACKGROUND_COLOR, createColor(attributes));
            else if(state == State.TABLE_SELECTED)
                template.setColorFast(ThemeData.FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR, createColor(attributes));
            else if(state == State.TABLE_ALTERNATE)
                template.setColorFast(ThemeData.FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Secondary background.
        else if(qName.equals(ELEMENT_SECONDARY_BACKGROUND)) {
            if(state == State.TABLE_SELECTED)
                template.setColorFast(ThemeData.FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR, createColor(attributes));
            else if(state == State.QUICK_LIST_HEADER)
            	template.setColorFast(ThemeData.QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Inactive secondary background.
        else if(qName.equals(ELEMENT_INACTIVE_SECONDARY_BACKGROUND)) {
            if(state == State.TABLE_SELECTED)
                template.setColorFast(ThemeData.FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // File table border color.
        else if(qName.equals(ELEMENT_BORDER)) {
            if(state == State.TABLE)
                template.setColorFast(ThemeData.FILE_TABLE_BORDER_COLOR, createColor(attributes));

            else if(state == State.STATUS_BAR)
                template.setColorFast(ThemeData.STATUS_BAR_BORDER_COLOR, createColor(attributes));

            else
                traceIllegalDeclaration(qName);
        }

        // File table inactive border color.
        else if(qName.equals(ELEMENT_INACTIVE_BORDER)) {
            if(state == State.TABLE)
                template.setColorFast(ThemeData.FILE_TABLE_INACTIVE_BORDER_COLOR, createColor(attributes));

            else
                traceIllegalDeclaration(qName);
        }

        // File table outline color.
        else if(qName.equals(ELEMENT_OUTLINE)) {
            if(state == State.TABLE)
                template.setColorFast(ThemeData.FILE_TABLE_SELECTED_OUTLINE_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // File table inactive outline color.
        else if (qName.equals(ELEMENT_INACTIVE_OUTLINE)) {
            if (state == State.TABLE)
                template.setColorFast(ThemeData.FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Unmatched file table.
        else if(qName.equals(ELEMENT_UNMATCHED)) {
            if (state != State.TABLE)
                traceIllegalDeclaration(qName);
            state = State.TABLE_UNMATCHED;
        }

        // Progress bar color.
        else if (qName.equals(ELEMENT_PROGRESS)) {
            if (state == State.LOCATION_BAR)
                template.setColorFast(ThemeData.LOCATION_BAR_PROGRESS_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // 'OK' color.
        else if (qName.equals(ELEMENT_OK)) {
            if (state == State.STATUS_BAR)
                template.setColorFast(ThemeData.STATUS_BAR_OK_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // 'WARNING' color.
        else if (qName.equals(ELEMENT_WARNING)) {
            if (state == State.STATUS_BAR)
                template.setColorFast(ThemeData.STATUS_BAR_WARNING_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // 'CRITICAL' color.
        else if (qName.equals(ELEMENT_CRITICAL)) {
            if (state == State.STATUS_BAR)
                template.setColorFast(ThemeData.STATUS_BAR_CRITICAL_COLOR, createColor(attributes));
            else
                traceIllegalDeclaration(qName);
        }

        // Text color.
        else if (qName.equals(ELEMENT_FOREGROUND)) {
            if (state == State.HIDDEN_NORMAL)
                template.setColorFast(ThemeData.HIDDEN_FILE_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.HIDDEN_SELECTED)
                template.setColorFast(ThemeData.HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.TABLE_UNMATCHED)
                template.setColorFast(ThemeData.FILE_TABLE_UNMATCHED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.FOLDER_NORMAL)
                template.setColorFast(ThemeData.FOLDER_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.FOLDER_SELECTED)
                template.setColorFast(ThemeData.FOLDER_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.ARCHIVE_NORMAL)
                template.setColorFast(ThemeData.ARCHIVE_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.ARCHIVE_SELECTED)
                template.setColorFast(ThemeData.ARCHIVE_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.SYMLINK_NORMAL)
                template.setColorFast(ThemeData.SYMLINK_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.SYMLINK_SELECTED)
                template.setColorFast(ThemeData.SYMLINK_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.MARKED_NORMAL)
                template.setColorFast(ThemeData.MARKED_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.MARKED_SELECTED)
                template.setColorFast(ThemeData.MARKED_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.EXECUTABLE_NORMAL)
                template.setColorFast(ThemeData.EXECUTABLE_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.EXECUTABLE_SELECTED)
                template.setColorFast(ThemeData.EXECUTABLE_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.FILE_NORMAL)
                template.setColorFast(ThemeData.FILE_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.FILE_SELECTED)
                template.setColorFast(ThemeData.FILE_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.SHELL_NORMAL)
                template.setColorFast(ThemeData.SHELL_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.SHELL_SELECTED)
                template.setColorFast(ThemeData.SHELL_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.SHELL_HISTORY_NORMAL)
                template.setColorFast(ThemeData.SHELL_HISTORY_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.SHELL_HISTORY_SELECTED)
                template.setColorFast(ThemeData.SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.TERMINAL_NORMAL)
                template.setColorFast(ThemeData.TERMINAL_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.TERMINAL_SELECTED)
                template.setColorFast(ThemeData.TERMINAL_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.EDITOR_NORMAL)
                template.setColorFast(ThemeData.EDITOR_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.EDITOR_SELECTED)
                template.setColorFast(ThemeData.EDITOR_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.LOCATION_BAR_NORMAL)
                template.setColorFast(ThemeData.LOCATION_BAR_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.LOCATION_BAR_SELECTED)
                template.setColorFast(ThemeData.LOCATION_BAR_SELECTED_FOREGROUND_COLOR, createColor(attributes));

            else if (state == State.STATUS_BAR)
                template.setColorFast(ThemeData.STATUS_BAR_FOREGROUND_COLOR, createColor(attributes));
            
            else if (state == State.QUICK_LIST_HEADER)
            	template.setColorFast(ThemeData.QUICK_LIST_HEADER_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.QUICK_LIST_ITEM_NORMAL)
            	template.setColorFast(ThemeData.QUICK_LIST_ITEM_FOREGROUND_COLOR, createColor(attributes));
            else if (state == State.QUICK_LIST_ITEM_SELECTED)
            	template.setColorFast(ThemeData.QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR, createColor(attributes));

            else
                traceIllegalDeclaration(qName);
        }

        else if (qName.startsWith(ELEMENT_GROUP)) {
            if (state != State.FILE_GROUP) {
                traceIllegalDeclaration(qName);
            }
            String groupStr = qName.substring(ELEMENT_GROUP.length());

            state = State.values()[State.GROUP_1.ordinal() + Integer.parseInt(groupStr) - 1];
        }


        else
            traceIllegalDeclaration(qName);
    }

    /**
     * Notifies the reader that the current element declaration is over.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // If we're in an unknown element....
        if (unknownElement != null) {
            // If it just closed, resume normal parsing.
            if (qName.equals(unknownElement))
                unknownElement = null;
            // Ignores all other tags.
            else
                return;
        }

        // XML root element.
        if (qName.equals(ELEMENT_ROOT))
            state = State.UNKNOWN;

        // File table declaration.
        else if (qName.equals(ELEMENT_TABLE))
            state = State.ROOT;

        else if (qName.equals(ELEMENT_ALTERNATE))
            state = State.TABLE;

        else if (qName.equals(ELEMENT_UNMATCHED))
            state = State.TABLE;

        else if (qName.equals(ELEMENT_HIDDEN))
            state = State.TABLE;

        else if (qName.equals(ELEMENT_FOLDER))
            state = State.TABLE;

        else if (qName.equals(ELEMENT_ARCHIVE))
            state = State.TABLE;

        else if (qName.equals(ELEMENT_SYMLINK))
            state = State.TABLE;

        else if (qName.equals(ELEMENT_MARKED))
            state = State.TABLE;

        else if (qName.equals(ELEMENT_EXECUTABLE))
            state = State.TABLE;

        else if (qName.equals(ELEMENT_FILE))
            state = State.TABLE;

        // Shell declaration.
        else if (qName.equals(ELEMENT_SHELL))
            state = State.ROOT;

        // Shell history declaration.
        else if (qName.equals(ELEMENT_SHELL_HISTORY))
            state = State.ROOT;

        else if (qName.equals(ELEMENT_TERMINAL))
            state = State.ROOT;


        // Editor declaration.
        else if (qName.equals(ELEMENT_EDITOR))
            state = State.ROOT;

        // Location bar declaration.
        else if (qName.equals(ELEMENT_LOCATION_BAR))
            state = State.ROOT;
        
        // Quick list declaration.
        else if (qName.equals(ELEMENT_QUICK_LIST))
            state = State.ROOT;

        // Volume label declaration
        else if (qName.equals(ELEMENT_STATUS_BAR))
            state = State.ROOT;
        
        // Header declaration.
        else if (qName.equals(ELEMENT_HEADER)) {
        	if (state == State.QUICK_LIST_HEADER)
                state = State.QUICK_LIST;
        }
        
        // Item declaration.
        else if (qName.equals(ELEMENT_ITEM)) {
        	if (state == State.QUICK_LIST_ITEM)
                state = State.QUICK_LIST;
        }

        // Normal element declaration.
        else if (qName.equals(ELEMENT_NORMAL)) {
            if (state == State.SHELL_NORMAL)
                state = State.SHELL;
            else if (state == State.SHELL_HISTORY_NORMAL)
                state = State.SHELL_HISTORY;
            else if (state == State.TERMINAL_NORMAL)
                state = State.TERMINAL;
            else if (state == State.HIDDEN_NORMAL)
                state = State.HIDDEN;
            else if (state == State.FOLDER_NORMAL)
                state = State.FOLDER;
            else if (state == State.ARCHIVE_NORMAL)
                state = State.ARCHIVE;
            else if (state == State.SYMLINK_NORMAL)
                state = State.SYMLINK;
            else if (state == State.MARKED_NORMAL)
                state = State.MARKED;
            else if (state == State.EXECUTABLE_NORMAL)
                state = State.EXECUTABLE;
            else if (state == State.FILE_NORMAL)
                state = State.FILE;
            else if (state == State.EDITOR_NORMAL)
                state = State.EDITOR;
            else if (state == State.LOCATION_BAR_NORMAL)
                state = State.LOCATION_BAR;
            else if (state == State.TABLE_NORMAL)
                state = State.TABLE;
            else if (state == State.QUICK_LIST_ITEM_NORMAL)
            	state = State.QUICK_LIST_ITEM;
        }

        // Selected element declaration.
        else if (qName.equals(ELEMENT_SELECTED)) {
            if (state == State.SHELL_SELECTED)
                state = State.SHELL;
            else if (state == State.SHELL_HISTORY_SELECTED)
                state = State.SHELL_HISTORY;
            else if (state == State.TERMINAL_SELECTED)
                state = State.TERMINAL;
            else if (state == State.HIDDEN_SELECTED)
                state = State.HIDDEN;
            else if (state == State.FOLDER_SELECTED)
                state = State.FOLDER;
            else if (state == State.ARCHIVE_SELECTED)
                state = State.ARCHIVE;
            else if (state == State.SYMLINK_SELECTED)
                state = State.SYMLINK;
            else if (state == State.MARKED_SELECTED)
                state = State.MARKED;
            else if (state == State.EXECUTABLE_SELECTED)
                state = State.EXECUTABLE;
            else if (state == State.FILE_SELECTED)
                state = State.FILE;
            else if (state == State.EDITOR_SELECTED)
                state = State.EDITOR;
            else if (state == State.LOCATION_BAR_SELECTED)
                state = State.LOCATION_BAR;
            else if (state == State.TABLE_SELECTED)
                state = State.TABLE;
            else if (state == State.QUICK_LIST_ITEM_SELECTED)
            	state = State.QUICK_LIST_ITEM;
        }

        else if (qName.startsWith(ELEMENT_GROUP)) {
            state = State.FILE_GROUP;
        }
    }



    // - Helper methods ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Checks whether the specified font is available on the system.
     * @param  font name of the font to check for.
     * @return <code>true</code> if the font is available, <code>false</code> otherwise.
     */
    private static boolean isFontAvailable(String font)  {
	    String[] availableFonts; // All available fonts.

        // Looks for the specified font.
        // TODO very slow operation (for first execution) !!!!
        availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        for (String availableFont : availableFonts)
            if (availableFont.equalsIgnoreCase(font))
                return true;

        // Font doesn't exist on the system.
        return false;
    }

    /**
     * Creates a font from the specified XML attributes.
     * <p>
     * Ignored attributes will be set to their default values.
     * </p>
     * @param  attributes XML attributes describing the font to use.
     * @return            the resulting Font instance.
     */
    private static Font createFont(Attributes attributes) {
        String          buffer; // Buffer for attribute values.
        StringTokenizer parser; // Used to parse the font family.

        // Computes the font style.
        int style = 0;
        if (((buffer = attributes.getValue(ATTRIBUTE_BOLD)) != null) && buffer.equals(VALUE_TRUE))
            style |= Font.BOLD;
        if (((buffer = attributes.getValue(ATTRIBUTE_ITALIC)) != null) && buffer.equals(VALUE_TRUE))
            style |= Font.ITALIC;

        // Computes the font size.
        if ((buffer = attributes.getValue(ATTRIBUTE_SIZE)) == null) {
            getLogger().debug("Missing font size attribute in theme, ignoring.");
            return null;
	    }
        int size = Integer.parseInt(buffer);

            // Computes the font family.
            if ((buffer = attributes.getValue(ATTRIBUTE_FAMILY)) == null) {
                getLogger().debug("Missing font family attribute in theme, ignoring.");
                return null;
        }

        // Looks through the list of declared fonts to find one that is installed on the system.
        parser = new StringTokenizer(buffer, ",");
        while (parser.hasMoreTokens()) {
            buffer = parser.nextToken().trim();

            // Font was found, use it.
            if (isFontAvailable(buffer)) {
                return new Font(buffer, style, size);
            }
        }

        // No font was found, instructs the ThemeManager to use the system default.
        getLogger().debug("Requested font families are not installed on the system, using default.");
        return null;
    }

    /**
     * Creates a color from the specified XML attributes.
     * @param  attributes XML attributes describing the font to use.
     * @return            the resulting Color instance.
     */
    private static Color createColor(Attributes attributes) {
        String buffer = attributes.getValue(ATTRIBUTE_COLOR);

        // Retrieves the color attribute's value.
        if (buffer == null) {
            getLogger().debug("Missing color attribute in theme, ignoring.");
            return null;
        }
        int color = Integer.parseInt(buffer, 16);

        // Retrieves the transparency attribute's value..
        buffer = attributes.getValue(ATTRIBUTE_ALPHA);
        if (buffer == null)
            return new Color(color);
        return new Color(color | (Integer.parseInt(buffer, 16) << 24), true);
    }


    // - Error generation methods --------------------------------------------
    // -----------------------------------------------------------------------
    private void traceIllegalDeclaration(String element) {
        unknownElement = element;
        getLogger().debug("Unexpected start of element " + element + ", ignoring.");
    }


    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(ThemeReader.class);
        }
        return logger;
    }
}
