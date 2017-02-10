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
class ThemeReader extends DefaultHandler implements ThemeXmlConstants, ThemeId {
	private static Logger logger;

	// TODO !!! add previous state to enum instead off terrible if-else list
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
        TERMINAL_SELECTED,

        HEX_VIEWER,
        HEX_VIEWER_NORMAL,
        HEX_VIEWER_SELECTED,

    }

    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Theme template that is currently being built. */
    private ThemeData template;
    /** Current state of the XML parser. */
    private State state;
    /** Used to ignore the content of an unknown tag. */
    private String unknownElement;



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
        if (ELEMENT_NORMAL.equals(qName)) {
            if (state == State.SHELL) {
                state = State.SHELL_NORMAL;
            } else if (state == State.EDITOR) {
                state = State.EDITOR_NORMAL;
            } else if (state == State.LOCATION_BAR) {
                state = State.LOCATION_BAR_NORMAL;
            } else if (state == State.SHELL_HISTORY) {
                state = State.SHELL_HISTORY_NORMAL;
            } else if (state == State.TERMINAL) {
                state = State.TERMINAL_NORMAL;
            } else if (state == State.HIDDEN) {
                state = State.HIDDEN_NORMAL;
            } else if (state == State.FOLDER) {
                state = State.FOLDER_NORMAL;
            } else if (state == State.ARCHIVE) {
                state = State.ARCHIVE_NORMAL;
            } else if (state == State.SYMLINK) {
                state = State.SYMLINK_NORMAL;
            } else if (state == State.MARKED) {
                state = State.MARKED_NORMAL;
            } else if (state == State.EXECUTABLE) {
                state = State.EXECUTABLE_NORMAL;
            } else if (state == State.FILE) {
                state = State.FILE_NORMAL;
            } else if (state == State.TABLE) {
                state = State.TABLE_NORMAL;
            } else if (state == State.QUICK_LIST_ITEM) {
                state = State.QUICK_LIST_ITEM_NORMAL;
            } else if (state.ordinal() >= State.GROUP_1.ordinal() && state.ordinal() <= State.GROUP_10.ordinal()) {
                int group = state.ordinal() - State.GROUP_1.ordinal();
                setColor(FILE_GROUP_1_FOREGROUND_COLOR + group, attributes);
            } else if (state == State.HEX_VIEWER) {
                state = State.HEX_VIEWER_NORMAL;
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // Background color.
        else if (ELEMENT_BACKGROUND.equals(qName)) {
            if (state == State.TABLE_NORMAL) {
                setColor(FILE_TABLE_BACKGROUND_COLOR, attributes);
            } else if (state == State.TABLE_SELECTED) {
                setColor(FILE_TABLE_SELECTED_BACKGROUND_COLOR, attributes);
            } else if (state == State.TABLE_ALTERNATE) {
                setColor(FILE_TABLE_ALTERNATE_BACKGROUND_COLOR, attributes);
            } else if (state == State.TABLE_UNMATCHED) {
                setColor(FILE_TABLE_UNMATCHED_BACKGROUND_COLOR, attributes);
            } else if (state == State.SHELL_NORMAL) {
                setColor(SHELL_BACKGROUND_COLOR, attributes);
            } else if (state == State.SHELL_SELECTED) {
                setColor(SHELL_SELECTED_BACKGROUND_COLOR, attributes);
            } else if (state == State.EDITOR_NORMAL) {
                setColor(EDITOR_BACKGROUND_COLOR, attributes);
            } else if (state == State.EDITOR_SELECTED) {
                setColor(EDITOR_SELECTED_BACKGROUND_COLOR, attributes);
            } else if (state == State.EDITOR_CURRENT) {
                setColor(EDITOR_CURRENT_BACKGROUND_COLOR, attributes);
            } else if (state == State.LOCATION_BAR_NORMAL) {
                setColor(LOCATION_BAR_BACKGROUND_COLOR, attributes);
            } else if (state == State.LOCATION_BAR_SELECTED) {
                setColor(LOCATION_BAR_SELECTED_BACKGROUND_COLOR, attributes);
            } else if (state == State.SHELL_HISTORY_NORMAL) {
                setColor(SHELL_HISTORY_BACKGROUND_COLOR, attributes);
            } else if (state == State.SHELL_HISTORY_SELECTED) {
                setColor(SHELL_HISTORY_SELECTED_BACKGROUND_COLOR, attributes);
            } else if (state == State.TERMINAL_NORMAL) {
                setColor(TERMINAL_BACKGROUND_COLOR, attributes);
            } else if (state == State.TERMINAL_SELECTED) {
                setColor(TERMINAL_SELECTED_BACKGROUND_COLOR, attributes);
            } else if (state == State.STATUS_BAR) {
                setColor(STATUS_BAR_BACKGROUND_COLOR, attributes);
            } else if (state == State.QUICK_LIST_HEADER) {
                setColor(QUICK_LIST_HEADER_BACKGROUND_COLOR, attributes);
            } else if (state == State.QUICK_LIST_ITEM_NORMAL) {
                setColor(QUICK_LIST_ITEM_BACKGROUND_COLOR, attributes);
            } else if (state == State.QUICK_LIST_ITEM_SELECTED) {
                setColor(QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR, attributes);
            } else if (state == State.HEX_VIEWER_NORMAL) {
                setColor(HEX_VIEWER_BACKGROUND_COLOR, attributes);
            } else if (state == State.HEX_VIEWER_SELECTED) {
                setColor(HEX_VIEWER_SELECTED_BACKGROUND_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // Selected element declaration.
        else if (ELEMENT_SELECTED.equals(qName)) {
            if (state == State.SHELL) {
                state = State.SHELL_SELECTED;
            } else if (state == State.EDITOR) {
                state = State.EDITOR_SELECTED;
            } else if (state == State.LOCATION_BAR) {
                state = State.LOCATION_BAR_SELECTED;
            } else if (state == State.SHELL_HISTORY) {
                state = State.SHELL_HISTORY_SELECTED;
            } else if (state == State.TERMINAL) {
                state = State.TERMINAL_SELECTED;
            } else if (state == State.HIDDEN) {
                state = State.HIDDEN_SELECTED;
            } else if (state == State.FOLDER) {
                state = State.FOLDER_SELECTED;
            } else if (state == State.ARCHIVE) {
                state = State.ARCHIVE_SELECTED;
            } else if (state == State.SYMLINK) {
                state = State.SYMLINK_SELECTED;
            } else if (state == State.MARKED) {
                state = State.MARKED_SELECTED;
            } else if (state == State.EXECUTABLE) {
                state = State.EXECUTABLE_SELECTED;
            } else if (state == State.FILE) {
                state = State.FILE_SELECTED;
            } else if (state == State.TABLE) {
                state = State.TABLE_SELECTED;
            } else if (state == State.QUICK_LIST_ITEM) {
                state = State.QUICK_LIST_ITEM_SELECTED;
            } else if (state == State.HEX_VIEWER) {
                state = State.HEX_VIEWER_SELECTED;
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // Unfocused foreground color.
        else if (ELEMENT_INACTIVE_FOREGROUND.equals(qName)) {
            if (state == State.FILE_NORMAL) {
                setColor(FILE_INACTIVE_FOREGROUND_COLOR, attributes);
            } else if(state == State.FOLDER_NORMAL) {
                setColor(FOLDER_INACTIVE_FOREGROUND_COLOR, attributes);
            } else if(state == State.ARCHIVE_NORMAL) {
                setColor(ARCHIVE_INACTIVE_FOREGROUND_COLOR, attributes);
            } else if(state == State.SYMLINK_NORMAL) {
                setColor(SYMLINK_INACTIVE_FOREGROUND_COLOR, attributes);
            } else if(state == State.HIDDEN_NORMAL) {
                setColor(HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR, attributes);
            } else if(state == State.MARKED_NORMAL) {
                setColor(MARKED_INACTIVE_FOREGROUND_COLOR, attributes);
            } else if(state == State.EXECUTABLE_NORMAL) {
                setColor(EXECUTABLE_INACTIVE_FOREGROUND_COLOR, attributes);
            } else if(state == State.FILE_SELECTED) {
                setColor(FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, attributes);
            } else if(state == State.FOLDER_SELECTED) {
                setColor(FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR, attributes);
            } else if(state == State.ARCHIVE_SELECTED) {
                setColor(ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR, attributes);
            } else if(state == State.SYMLINK_SELECTED) {
                setColor(SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR, attributes);
            } else if(state == State.HIDDEN_SELECTED) {
                setColor(HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, attributes);
            } else if(state == State.MARKED_SELECTED) {
                setColor(MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR, attributes);
            } else if(state == State.EXECUTABLE_SELECTED) {
                setColor(EXECUTABLE_INACTIVE_SELECTED_FOREGROUND_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // Font creation.
        else if (ELEMENT_FONT.equals(qName)) {
            if (state == State.SHELL) {
                setFont(SHELL_FONT, attributes);
            } else if (state == State.EDITOR) {
                setFont(EDITOR_FONT, attributes);
            } else if (state == State.LOCATION_BAR) {
                setFont(LOCATION_BAR_FONT, attributes);
            } else if (state == State.SHELL_HISTORY) {
                setFont(SHELL_HISTORY_FONT, attributes);
            } else if (state == State.TERMINAL) {
                setFont(TERMINAL_FONT, attributes);
            } else if (state == State.STATUS_BAR) {
                setFont(STATUS_BAR_FONT, attributes);
            } else if (state == State.TABLE) {
                setFont(FILE_TABLE_FONT, attributes);
            } else if (state == State.QUICK_LIST_HEADER) {
                setFont(QUICK_LIST_HEADER_FONT, attributes);
            } else if (state == State.QUICK_LIST_ITEM) {
                setFont(QUICK_LIST_ITEM_FONT, attributes);
            } else if (state == State.HEX_VIEWER) {
                setFont(ThemeId.HEX_VIEWER_FONT, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // XML root element.
        else if (ELEMENT_ROOT.equals(qName)) {
            if (state != State.UNKNOWN) {
                traceIllegalDeclaration(ELEMENT_ROOT);
            }
            state = State.ROOT;
        }

        // File table declaration.
        else if (ELEMENT_TABLE.equals(qName)) {
            if (state != State.ROOT) {
                traceIllegalDeclaration(qName);
            }
            state = State.TABLE;
        }

        else if (ELEMENT_FILE_GROUPS.equals(qName)) {
            if (state != State.ROOT) {
                traceIllegalDeclaration(qName);
            }
            state = State.FILE_GROUP;
        }

        // Shell declaration.
        else if (ELEMENT_SHELL.equals(qName)) {
            if (state != State.ROOT) {
                traceIllegalDeclaration(qName);
            }
            state = State.SHELL;
        }

        // Editor declaration.
        else if (ELEMENT_EDITOR.equals(qName)) {
            if (state != State.ROOT) {
                traceIllegalDeclaration(qName);
            }
            state = State.EDITOR;
        }

        // Location bar declaration.
        else if (ELEMENT_LOCATION_BAR.equals(qName)) {
            if (state != State.ROOT) {
                traceIllegalDeclaration(qName);
            }
            state = State.LOCATION_BAR;
        }
        
        // Quick list declaration.
        else if (ELEMENT_QUICK_LIST.equals(qName)) {
            if (state != State.ROOT) {
                traceIllegalDeclaration(qName);
            }
            state = State.QUICK_LIST;
        }

        // Shell history declaration.
        else if (ELEMENT_SHELL_HISTORY.equals(qName)) {
            if (state != State.ROOT) {
                traceIllegalDeclaration(qName);
            }
            state = State.SHELL_HISTORY;
        }

        else if (ELEMENT_TERMINAL.equals(qName)) {
            if (state != State.ROOT) {
                traceIllegalDeclaration(qName);
            }
            state = State.TERMINAL;
        }

        // Volume label declaration.
        else if (ELEMENT_STATUS_BAR.equals(qName)) {
            if (state != State.ROOT) {
                traceIllegalDeclaration(qName);
            }
            state = State.STATUS_BAR;
        }

        else if (ELEMENT_HIDDEN.equals(qName)) {
            if (state != State.TABLE) {
                traceIllegalDeclaration(qName);
            }
            state = State.HIDDEN;
        }

        else if (ELEMENT_FOLDER.equals(qName)) {
            if (state != State.TABLE) {
                traceIllegalDeclaration(qName);
            }
            state = State.FOLDER;
        }

        else if (ELEMENT_ARCHIVE.equals(qName)) {
            if (state != State.TABLE) {
                traceIllegalDeclaration(qName);
            }
            state = State.ARCHIVE;
        }

        else if (ELEMENT_SYMLINK.equals(qName)) {
            if (state != State.TABLE) {
                traceIllegalDeclaration(qName);
            }
            state = State.SYMLINK;
        }

        else if (ELEMENT_MARKED.equals(qName)) {
            if (state != State.TABLE) {
                traceIllegalDeclaration(qName);
            }
            state = State.MARKED;
        }

        else if (ELEMENT_EXECUTABLE.equals(qName)) {
            if (state != State.TABLE) {
                traceIllegalDeclaration(qName);
            }
            state = State.EXECUTABLE;
        }

        else if (ELEMENT_FILE.equals(qName)) {
            if (state != State.TABLE) {
                traceIllegalDeclaration(qName);
            }
            state = State.FILE;
        }

        else if (ELEMENT_ALTERNATE.equals(qName)) {
            if (state != State.TABLE) {
                traceIllegalDeclaration(qName);
            }
            state = State.TABLE_ALTERNATE;
        }
        
        // Header declaration.
        else if (ELEMENT_HEADER.equals(qName)) {
        	if (state == State.QUICK_LIST) {
                state = State.QUICK_LIST_HEADER;
            } else {
                traceIllegalDeclaration(qName);
            }
        }
        
        // Item declaration.
        else if (ELEMENT_ITEM.equals(qName)) {
        	if (state == State.QUICK_LIST) {
                state = State.QUICK_LIST_ITEM;
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        else if(ELEMENT_TERMINAL.equals(qName)) {
            if (state != State.ROOT) {
                traceIllegalDeclaration(qName);
            }
            state = State.TERMINAL;
        }


        // Current element declaration.
        else if (ELEMENT_CURRENT.equals(qName)) {
            if (state == State.EDITOR) {
                state = State.EDITOR_CURRENT;
            } else {
                traceIllegalDeclaration(qName);
            }
        }


        // Unfocused background color.
        else if (ELEMENT_INACTIVE_BACKGROUND.equals(qName)) {
            if (state == State.TABLE_NORMAL) {
                setColor(FILE_TABLE_INACTIVE_BACKGROUND_COLOR, attributes);
            } else {
                if (state == State.TABLE_SELECTED) {
                    setColor(FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR, attributes);
                } else if (state == State.TABLE_ALTERNATE) {
                    setColor(FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR, attributes);
                } else {
                    traceIllegalDeclaration(qName);
                }
            }
        }

        // Secondary background.
        else if (ELEMENT_SECONDARY_BACKGROUND.equals(qName)) {
            if (state == State.TABLE_SELECTED) {
                setColor(FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR, attributes);
            } else if (state == State.HEX_VIEWER_NORMAL) {
                setColor(HEX_VIEWER_ALTERNATE_BACKGROUND_COLOR, attributes);
            } else {
                if (state == State.QUICK_LIST_HEADER) {
                    setColor(QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR, attributes);
                } else {
                    traceIllegalDeclaration(qName);
                }
            }
        }

        // Inactive secondary background.
        else if (ELEMENT_INACTIVE_SECONDARY_BACKGROUND.equals(qName)) {
            if (state == State.TABLE_SELECTED) {
                setColor(FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // File table border color.
        else if (ELEMENT_BORDER.equals(qName)) {
            if (state == State.TABLE) {
                setColor(FILE_TABLE_BORDER_COLOR, attributes);
            } else {
                if (state == State.STATUS_BAR) {
                    setColor(STATUS_BAR_BORDER_COLOR, attributes);
                } else {
                    traceIllegalDeclaration(qName);
                }
            }
        }

        // File table inactive border color.
        else if (ELEMENT_INACTIVE_BORDER.equals(qName)) {
            if (state == State.TABLE) {
                setColor(FILE_TABLE_INACTIVE_BORDER_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // File table outline color.
        else if (ELEMENT_OUTLINE.equals(qName)) {
            if (state == State.TABLE) {
                setColor(FILE_TABLE_SELECTED_OUTLINE_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // File table inactive outline color.
        else if (ELEMENT_INACTIVE_OUTLINE.equals(qName)) {
            if (state == State.TABLE) {
                setColor(FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // Unmatched file table.
        else if (ELEMENT_UNMATCHED.equals(qName)) {
            if (state != State.TABLE) {
                traceIllegalDeclaration(qName);
            }
            state = State.TABLE_UNMATCHED;
        }

        // Progress bar color.
        else if (ELEMENT_PROGRESS.equals(qName)) {
            if (state == State.LOCATION_BAR) {
                setColor(LOCATION_BAR_PROGRESS_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // 'OK' color.
        else if (ELEMENT_OK.equals(qName)) {
            if (state == State.STATUS_BAR) {
                setColor(STATUS_BAR_OK_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // 'WARNING' color.
        else if (ELEMENT_WARNING.equals(qName)) {
            if (state == State.STATUS_BAR) {
                setColor(STATUS_BAR_WARNING_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // 'CRITICAL' color.
        else if (ELEMENT_CRITICAL.equals(qName)) {
            if (state == State.STATUS_BAR) {
                setColor(STATUS_BAR_CRITICAL_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        // Text color.
        else if (ELEMENT_FOREGROUND.equals(qName)) {
            if (state == State.HIDDEN_NORMAL) {
                setColor(HIDDEN_FILE_FOREGROUND_COLOR, attributes);
            } else if (state == State.HIDDEN_SELECTED) {
                setColor(HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.TABLE_UNMATCHED) {
                setColor(FILE_TABLE_UNMATCHED_FOREGROUND_COLOR, attributes);
            } else if (state == State.FOLDER_NORMAL) {
                setColor(FOLDER_FOREGROUND_COLOR, attributes);
            } else if (state == State.FOLDER_SELECTED) {
                setColor(FOLDER_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.ARCHIVE_NORMAL) {
                setColor(ARCHIVE_FOREGROUND_COLOR, attributes);
            } else if (state == State.ARCHIVE_SELECTED) {
                setColor(ARCHIVE_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.SYMLINK_NORMAL) {
                setColor(SYMLINK_FOREGROUND_COLOR, attributes);
            } else if (state == State.SYMLINK_SELECTED) {
                setColor(SYMLINK_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.MARKED_NORMAL) {
                setColor(MARKED_FOREGROUND_COLOR, attributes);
            } else if (state == State.MARKED_SELECTED) {
                setColor(MARKED_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.EXECUTABLE_NORMAL) {
                setColor(EXECUTABLE_FOREGROUND_COLOR, attributes);
            } else if (state == State.EXECUTABLE_SELECTED) {
                setColor(EXECUTABLE_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.FILE_NORMAL) {
                setColor(FILE_FOREGROUND_COLOR, attributes);
            } else if (state == State.FILE_SELECTED) {
                setColor(FILE_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.SHELL_NORMAL) {
                setColor(SHELL_FOREGROUND_COLOR, attributes);
            } else if (state == State.SHELL_SELECTED) {
                setColor(SHELL_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.SHELL_HISTORY_NORMAL) {
                setColor(SHELL_HISTORY_FOREGROUND_COLOR, attributes);
            } else if (state == State.SHELL_HISTORY_SELECTED) {
                setColor(SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.TERMINAL_NORMAL) {
                setColor(TERMINAL_FOREGROUND_COLOR, attributes);
            } else if (state == State.TERMINAL_SELECTED) {
                setColor(TERMINAL_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.EDITOR_NORMAL) {
                setColor(EDITOR_FOREGROUND_COLOR, attributes);
            } else if (state == State.EDITOR_SELECTED) {
                setColor(EDITOR_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.LOCATION_BAR_NORMAL) {
                setColor(LOCATION_BAR_FOREGROUND_COLOR, attributes);
            } else if (state == State.LOCATION_BAR_SELECTED) {
                setColor(LOCATION_BAR_SELECTED_FOREGROUND_COLOR, attributes);
            } else if (state == State.STATUS_BAR) {
                setColor(STATUS_BAR_FOREGROUND_COLOR, attributes);
            } else if (state == State.QUICK_LIST_HEADER) {
                setColor(QUICK_LIST_HEADER_FOREGROUND_COLOR, attributes);
            } else if (state == State.QUICK_LIST_ITEM_NORMAL) {
                setColor(QUICK_LIST_ITEM_FOREGROUND_COLOR, attributes);
            } else if (state == State.QUICK_LIST_ITEM_SELECTED) {
                setColor(QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR, attributes);
            } else if (state == State.HEX_VIEWER_NORMAL) {
                setColor(HEX_VIEWER_HEX_FOREGROUND_COLOR, attributes);
            } else if (state == State.HEX_VIEWER_SELECTED) {
                setColor(HEX_VIEWER_SELECTED_DUMP_FOREGROUND_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        else if (qName.startsWith(ELEMENT_GROUP)) {
            if (state != State.FILE_GROUP) {
                traceIllegalDeclaration(qName);
            }
            String groupStr = qName.substring(ELEMENT_GROUP.length());

            state = State.values()[State.GROUP_1.ordinal() + Integer.parseInt(groupStr) - 1];

        } else if (qName.equals(ELEMENT_HEX_VIEWER)) {
            state = State.HEX_VIEWER;

        } else if (ELEMENT_ASCII_FOREGROUND.equals(qName)) {
            if (state == State.HEX_VIEWER_NORMAL) {
                setColor(HEX_VIEWER_ASCII_FOREGROUND_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }

        } else if (ELEMENT_ASCII_BACKGROUND.equals(qName)) {
            if (state == State.HEX_VIEWER_SELECTED) {
                setColor(HEX_VIEWER_SELECTED_ASCII_BACKGROUND_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        else if (qName.equals(ELEMENT_OFFSET_FOREGROUND)) {
            if (state == State.HEX_VIEWER_NORMAL) {
                setColor(HEX_VIEWER_OFFSET_FOREGROUND_COLOR, attributes);
            } else {
                traceIllegalDeclaration(qName);
            }
        }

        else {
            traceIllegalDeclaration(qName);
        }
    }

    /**
     * Notifies the reader that the current element declaration is over.
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // If we're in an unknown element....
        if (unknownElement != null) {
            // If it just closed, resume normal parsing.
            if (qName.equals(unknownElement)) {
                unknownElement = null;
            } else {
                return; // Ignores all other tags.
            }
        }

        // XML root element.
        if (ELEMENT_ROOT.equals(qName)) {
            state = State.UNKNOWN;
        }// File table declaration.
        else if (ELEMENT_TABLE.equals(qName)) {
            state = State.ROOT;
        } else if (ELEMENT_ALTERNATE.equals(qName)) {
            state = State.TABLE;
        } else if (ELEMENT_UNMATCHED.equals(qName)) {
            state = State.TABLE;
        } else if (qName.equals(ELEMENT_HIDDEN)) {
            state = State.TABLE;
        } else if (ELEMENT_FOLDER.equals(qName)) {
            state = State.TABLE;
        } else if (ELEMENT_ARCHIVE.equals(qName)) {
            state = State.TABLE;
        } else if (ELEMENT_SYMLINK.equals(qName)) {
            state = State.TABLE;
        } else if (ELEMENT_MARKED.equals(qName)) {
            state = State.TABLE;
        } else if (ELEMENT_EXECUTABLE.equals(qName)) {
            state = State.TABLE;
        } else if (ELEMENT_FILE.equals(qName)) {
            state = State.TABLE;
        } else if (ELEMENT_SHELL.equals(qName)) {
            state = State.ROOT;
        } else if (ELEMENT_SHELL_HISTORY.equals(qName)) {
            state = State.ROOT;
        } else if (ELEMENT_TERMINAL.equals(qName)) {
            state = State.ROOT;
        } else if (ELEMENT_EDITOR.equals(qName)) {
            state = State.ROOT;
        } else if (ELEMENT_LOCATION_BAR.equals(qName)) {
            state = State.ROOT;
        } else if (ELEMENT_QUICK_LIST.equals(qName)) {
            state = State.ROOT;
        } else if (ELEMENT_STATUS_BAR.equals(qName)) {
            state = State.ROOT;
        } else if (ELEMENT_HEADER.equals(qName)) {
        	if (state == State.QUICK_LIST_HEADER) {
                state = State.QUICK_LIST;
            }
        } else if (ELEMENT_ITEM.equals(qName)) {
        	if (state == State.QUICK_LIST_ITEM) {
                state = State.QUICK_LIST;
            }
        } else if (ELEMENT_NORMAL.equals(qName)) {
            if (state == State.SHELL_NORMAL) {
                state = State.SHELL;
            } else if (state == State.SHELL_HISTORY_NORMAL) {
                state = State.SHELL_HISTORY;
            } else if (state == State.TERMINAL_NORMAL) {
                state = State.TERMINAL;
            } else if (state == State.HIDDEN_NORMAL) {
                state = State.HIDDEN;
            } else if (state == State.FOLDER_NORMAL) {
                state = State.FOLDER;
            } else if (state == State.ARCHIVE_NORMAL) {
                state = State.ARCHIVE;
            } else if (state == State.SYMLINK_NORMAL) {
                state = State.SYMLINK;
            } else if (state == State.MARKED_NORMAL) {
                state = State.MARKED;
            } else if (state == State.EXECUTABLE_NORMAL) {
                state = State.EXECUTABLE;
            } else if (state == State.FILE_NORMAL) {
                state = State.FILE;
            } else if (state == State.EDITOR_NORMAL) {
                state = State.EDITOR;
            } else if (state == State.LOCATION_BAR_NORMAL) {
                state = State.LOCATION_BAR;
            } else if (state == State.TABLE_NORMAL) {
                state = State.TABLE;
            } else if (state == State.QUICK_LIST_ITEM_NORMAL) {
                state = State.QUICK_LIST_ITEM;
            } else if (state == State.HEX_VIEWER_NORMAL) {
                state = State.HEX_VIEWER;
            }
        }

        // Selected element declaration.
        else if (ELEMENT_SELECTED.equals(qName)) {
            if (state == State.SHELL_SELECTED) {
                state = State.SHELL;
            } else if (state == State.SHELL_HISTORY_SELECTED) {
                state = State.SHELL_HISTORY;
            } else if (state == State.TERMINAL_SELECTED) {
                state = State.TERMINAL;
            } else if (state == State.HIDDEN_SELECTED) {
                state = State.HIDDEN;
            } else if (state == State.FOLDER_SELECTED) {
                state = State.FOLDER;
            } else if (state == State.ARCHIVE_SELECTED) {
                state = State.ARCHIVE;
            } else if (state == State.SYMLINK_SELECTED) {
                state = State.SYMLINK;
            } else if (state == State.MARKED_SELECTED) {
                state = State.MARKED;
            } else if (state == State.EXECUTABLE_SELECTED) {
                state = State.EXECUTABLE;
            } else if (state == State.FILE_SELECTED) {
                state = State.FILE;
            } else if (state == State.EDITOR_SELECTED) {
                state = State.EDITOR;
            } else if (state == State.LOCATION_BAR_SELECTED) {
                state = State.LOCATION_BAR;
            } else if (state == State.TABLE_SELECTED) {
                state = State.TABLE;
            } else if (state == State.QUICK_LIST_ITEM_SELECTED) {
                state = State.QUICK_LIST_ITEM;
            } else if (state == State.HEX_VIEWER_SELECTED) {
                state = State.HEX_VIEWER;
            }
        } else if (qName.startsWith(ELEMENT_GROUP)) {
            state = State.FILE_GROUP;
        } else if (ELEMENT_HEX_VIEWER.equals(qName)) {
            state = State.ROOT;
        } else if (ELEMENT_FILE_GROUPS.equals(qName)) {
            state = State.ROOT;
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
        // Looks for the specified font.
        // TODO very slow operation (for first execution) !!!!
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        for (String availableFont : availableFonts) {
            if (availableFont.equalsIgnoreCase(font)) {
                return true;
            }
        }
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
        String buffer; // Buffer for attribute values.

        // Computes the font style.
        int style = 0;
        if (((buffer = attributes.getValue(ATTRIBUTE_BOLD)) != null) && buffer.equals(VALUE_TRUE)) {
            style |= Font.BOLD;
        }
        if (((buffer = attributes.getValue(ATTRIBUTE_ITALIC)) != null) && buffer.equals(VALUE_TRUE)) {
            style |= Font.ITALIC;
        }

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
        StringTokenizer parser = new StringTokenizer(buffer, ",");
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
        if (buffer == null) {
            return new Color(color);
        }
        return new Color(color | (Integer.parseInt(buffer, 16) << 24), true);
    }

    /**
     * Set color helper method
     * @param id color id
     * @param attributes xml attributes
     */
    private void setColor(int id, Attributes attributes) {
        template.setColorFast(id, createColor(attributes));
    }

    /**
     * Set font helper method
     * @param id font id
     * @param attributes xml attributes
     */
    private void setFont(int id, Attributes attributes) {
        template.setFontFast(id, createFont(attributes));
    }


    // - Error generation methods --------------------------------------------
    // -----------------------------------------------------------------------
    private void traceIllegalDeclaration(String element) {
        System.out.println("Unexpected start of element " + element + " for state " + state + ", ignoring.");
        new Exception().printStackTrace();
        unknownElement = element;
        getLogger().debug("Unexpected start of element " + element + " for state " + state + ", ignoring.");
    }


    private static Logger getLogger() {
        if (logger == null) {
            logger = LoggerFactory.getLogger(ThemeReader.class);
        }
        return logger;
    }
}
