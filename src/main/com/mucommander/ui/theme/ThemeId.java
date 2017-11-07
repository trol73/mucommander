/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2017 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.theme;

/**
 * Created on 09/02/17.
 * @author Oleg Trifonov
 */
public interface ThemeId {
    // - Dirty hack ----------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    // This is an effort to make the ThemeData class a bit easier to maintain, but I'm the first
    // to admit it's rather dirty.
    //
    // For optimization reasons, we're storing the fonts and colors in arrays, using their
    // identifiers as indexes in the array. This, however, means that lots of bits of code
    // must be updated whenever a font or color is added or removed. The probability of
    // someone forgetting this is, well, 100%.
    //
    // For this reason, we've declared the number of font and colors as constants.
    // People are still going to forget to update these constants, but at least it'll be
    // a lot easier to fix.



    // - Font definitions ----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Font used in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> font.
     */
    int FILE_TABLE_FONT = 0;

    /**
     * Font used to display shell output.
     * <p>
     * This defaults to the current <code>JTextArea</code> font.
     */
    int SHELL_FONT = 1;

    /**
     * Font used in the file editor and viewer.
     * <p>
     * This defaults to the current <code>JTable</code> font.
     */
    int EDITOR_FONT = 2;

    /**
     * Font used in the location bar.
     * <p>
     * This defaults to the current <code>JTextField</code> font.
     */
    int LOCATION_BAR_FONT = 3;

    /**
     * Font used in the shell history widget.
     * <p>
     * This defaults to the current <code>JTextField</code> font.
     */
    int SHELL_HISTORY_FONT = 4;

    /**
     * Font used in the status bar.
     * <p>
     * This defaults to the current <code>JLabel</code> font.
     */
    int STATUS_BAR_FONT = 5;

    /**
     * Font used in the quick list header.
     * <p>
     * This defaults to a similar font of the current <code>JTable</code> font, but a little bigger.
     */
    int QUICK_LIST_HEADER_FONT = 6;

    /**
     * Font used in the quick list item.
     * <p>
     * This defaults to the current <code>JTable</code> font.
     */
    int QUICK_LIST_ITEM_FONT = 7;

    int TERMINAL_FONT = 8;

    /**
     * Font used in the file editor and viewer.
     * <p>
     * This defaults to the current <code>JTable</code> font.
     */
    int HEX_VIEWER_FONT = 9;

    /**
     * Number of known fonts.
     * <p>
     * Since font identifiers are contiguous, it is possible to explore all fonts contained
     * by an instance of theme data by looping from 0 to this value.
     */
    int FONT_COUNT  = 10;



    // - Color definitions ---------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Color used to paint the folder panels' borders.
     * <p>
     * This defaults to <code>Color.GRAY</code>.
     */
    int FILE_TABLE_BORDER_COLOR = 0;

    /**
     * Color used to paint the folder panels' borders when it doesn't have the focus.
     * <p>
     * This defaults to <code>Color.GRAY</code>.
     */
    int FILE_TABLE_INACTIVE_BORDER_COLOR = 56;

    /**
     * Color used to paint the folder panel's background color.
     * <p>
     * This defaults to the current <code>JTable</code> background color.
     */
    int FILE_TABLE_BACKGROUND_COLOR = 1;

    /**
     * Color used to paint the folder panel's alternate background color.
     * <p>
     * This defaults to the current <code>JTable</code> background color.
     */
    int FILE_TABLE_ALTERNATE_BACKGROUND_COLOR = 2;

    /**
     * Color used to paint the folder panel's background color when it doesn't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FILE_TABLE_BACKGROUND_COLOR}, and defaults
     * to the same value.
     */
    int FILE_TABLE_INACTIVE_BACKGROUND_COLOR = 3;

    /**
     * Color used to paint the folder panel's alternate background color when inactive.
     * <p>
     * This defaults to the current <code>JTable</code> background color.
     */
    int FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR = 4;

    /**
     * Color used to paint the file table's background color when it's part of an unmatched file.
     */
    int FILE_TABLE_UNMATCHED_BACKGROUND_COLOR = 5;

    /**
     * Color used to paint the file table's foreground color when it's part of an unmatched file.
     */
    int FILE_TABLE_UNMATCHED_FOREGROUND_COLOR = 6;

    /**
     * Color used to paint the file table's background color when in a selected row.
     */
    int FILE_TABLE_SELECTED_BACKGROUND_COLOR = 7;

    /**
     * Color used to paint the gradient of the file table's selection.
     */
    int FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR = 59;

    /**
     * Color used to paint the gradient of the file table's selection when inactive.
     */
    int FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR = 60;

    /**
     * Colors used to pain the file table's background color when in an inactive selected row.
     */
    int FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR = 8;

    /**
     * Color used to paint hidden files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     */
    int HIDDEN_FILE_FOREGROUND_COLOR = 9;

    /**
     * Color used to paint hidden files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #HIDDEN_FILE_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR = 10;

    /**
     * Color used to paint selected hidden files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     */
    int HIDDEN_FILE_SELECTED_FOREGROUND_COLOR = 11;

    /**
     * Color used to paint selected hidden files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #HIDDEN_FILE_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR = 12;

    /**
     * Color used to paint folders text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     */
    int FOLDER_FOREGROUND_COLOR = 13;

    /**
     * Color used to paint folders text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FOLDER_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int FOLDER_INACTIVE_FOREGROUND_COLOR = 14;

    /**
     * Color used to paint selected folders text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     */
    int FOLDER_SELECTED_FOREGROUND_COLOR = 15;

    /**
     * Color used to paint selected folders text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FOLDER_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR = 16;

    /**
     * Color used to paint archives text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     */
    int ARCHIVE_FOREGROUND_COLOR = 17;

    /**
     * Color used to paint archives text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #ARCHIVE_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int ARCHIVE_INACTIVE_FOREGROUND_COLOR = 18;

    /**
     * Color used to paint selected archives text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     */
    int ARCHIVE_SELECTED_FOREGROUND_COLOR = 19;

    /**
     * Color used to paint selected archives text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #ARCHIVE_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR = 20;

    /**
     * Color used to paint symlinks text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     */
    int SYMLINK_FOREGROUND_COLOR = 21;

    /**
     * Color used to paint symlinks text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #SYMLINK_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int SYMLINK_INACTIVE_FOREGROUND_COLOR = 22;

    /**
     * Color used to paint selected symlinks text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     */
    int SYMLINK_SELECTED_FOREGROUND_COLOR = 23;

    /**
     * Color used to paint selected symlinks text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #SYMLINK_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR = 24;

    /**
     * Color used to paint marked files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     */
    int MARKED_FOREGROUND_COLOR = 25;

    /**
     * Color used to paint marked files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #MARKED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int MARKED_INACTIVE_FOREGROUND_COLOR = 26;

    /**
     * Color used to paint selected marked files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     */
    int MARKED_SELECTED_FOREGROUND_COLOR = 27;

    /**
     * Color used to paint selected marked files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #MARKED_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR = 28;

    /**
     * Color used to paint plain files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     */
    int FILE_FOREGROUND_COLOR = 29;

    /**
     * Color used to paint plain files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FILE_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int FILE_INACTIVE_FOREGROUND_COLOR = 30;

    /**
     * Color used to paint selected plain files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     */
    int FILE_SELECTED_FOREGROUND_COLOR = 31;

    /**
     * Color used to paint selected plain files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FILE_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int FILE_INACTIVE_SELECTED_FOREGROUND_COLOR = 32;

    /**
     * Color used to paint shell commands output.
     * <p>
     * This defaults to the current <code>JTextArea</code> foreground color.
     */
    int SHELL_FOREGROUND_COLOR = 33;

    /**
     * Color used to paint the background of shell commands output.
     * <p>
     * This defaults to the current <code>JTextArea</code> background color.
     */
    int SHELL_BACKGROUND_COLOR = 34;

    /**
     * Color used to paint shell commands output when selected.
     * <p>
     * This defaults to the current <code>JTextArea</code> selection foreground color.
     */
    int SHELL_SELECTED_FOREGROUND_COLOR = 35;

    /**
     * Color used to paint the background of shell commands output when selected.
     * <p>
     * This defaults to the current <code>JTextArea</code> selection background color.
     */
    int SHELL_SELECTED_BACKGROUND_COLOR = 36;

    /**
     * Color used to paint the shell history's text.
     * <p>
     * This defaults to the current <code>JTextField</code> foreground color.
     */
    int SHELL_HISTORY_FOREGROUND_COLOR = 37;

    /**
     * Color used to paint the shell history's background.
     * <p>
     * This defaults to the current <code>JTextField</code> background color.
     */
    int SHELL_HISTORY_BACKGROUND_COLOR = 38;

    /**
     * Color used to paint the shell history's text when selected.
     * <p>
     * This defaults to the current <code>JTextField</code> selection foreground color.
     */
    int SHELL_HISTORY_SELECTED_FOREGROUND_COLOR = 39;

    /**
     * Color used to paint the shell history's background when selected.
     * <p>
     * This defaults to the current <code>JTextField</code> selection background color.
     */
    int SHELL_HISTORY_SELECTED_BACKGROUND_COLOR = 40;

    /**
     * Color used to paint the file editor / viewer's text.
     * <p>
     * This defaults to the current <code>JTextArea</code> foreground color.
     */
    int EDITOR_FOREGROUND_COLOR = 41;

    /**
     * Color used to paint the file editor / viewer's background.
     * <p>
     * This defaults to the current <code>JTextArea</code> background color.
     */
    int EDITOR_BACKGROUND_COLOR = 42;

    /**
     * Color used to paint the file editor / viewer's foreground when selected.
     * <p>
     * This defaults to the current <code>JTextArea</code> selection foreground color.
     */
    int EDITOR_SELECTED_FOREGROUND_COLOR = 43;

    /**
     * Color used to paint the file editor / viewer's background when selected.
     * <p>
     * This defaults to the current <code>JTextArea</code> selection background color.
     */
    int EDITOR_SELECTED_BACKGROUND_COLOR = 44;

    /**
     * Color used to paint the location's bar text.
     * <p>
     * This defaults to the current <code>JTextField</code> foreground color.
     */
    int LOCATION_BAR_FOREGROUND_COLOR = 45;

    /**
     * Color used to paint the location's bar background.
     * <p>
     * This defaults to the current <code>JTextField</code> background color.
     */
    int LOCATION_BAR_BACKGROUND_COLOR = 46;

    /**
     * Color used to paint the location's bar text when selected.
     * <p>
     * This defaults to the current <code>JTextField</code> selection foreground color.
     */
    int LOCATION_BAR_SELECTED_FOREGROUND_COLOR = 47;

    /**
     * Color used to paint the location's bar background when selected.
     * <p>
     * This defaults to the current <code>JTextField</code> selection background color.
     */
    int LOCATION_BAR_SELECTED_BACKGROUND_COLOR = 48;

    /**
     * Color used to paint the location's bar background when used as a progress bar.
     * <p>
     * Note that this color is painted over the location's bar background and foreground. In order
     * for anything to be visible under it, it needs to have an alpha transparency component.
     * <p>
     * This defaults to the current <code>JTextField</code> selection background color, with an
     * alpha transparency value of 64.
     */
    int LOCATION_BAR_PROGRESS_COLOR = 49;

    /**
     * Color used to paint the status bar's text.
     * <p>
     * This defaults to the current <code>JLabel</code> foreground color.
     */
    int STATUS_BAR_FOREGROUND_COLOR = 50;

    /**
     * Color used to paint the status bar's background
     * <p>
     * This defaults to the current <code>JLabel</code> background color.
     */
    int STATUS_BAR_BACKGROUND_COLOR = 51;

    /**
     * Color used to paint the status bar's border.
     * <p>
     * This defaults to <code>Color.GRAY</code>.
     */
    int STATUS_BAR_BORDER_COLOR = 52;

    /**
     * Color used to paint the status bar's drive usage color when there's plenty of space left.
     * <p>
     * This defaults to <code>0x70EC2B</code>.
     */
    int STATUS_BAR_OK_COLOR = 53;

    /**
     * Color used to paint the status bar's drive usage color when there's an average amount of space left.
     * <p>
     * This defaults to <code>0xFF7F00</code>.
     */
    int STATUS_BAR_WARNING_COLOR = 54;

    /**
     * Color used to paint the status bar's drive usage color when there's dangerously little space left.
     * <p>
     * This defaults to <code>Color.RED</code>.
     */
    int STATUS_BAR_CRITICAL_COLOR = 55;

    /**
     * Color used to paint the outline of selected files.
     */
    int FILE_TABLE_SELECTED_OUTLINE_COLOR = 57;

    /**
     * Color used to paint the outline of selected files in an inactive table.
     */
    int FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR = 58;

    /**
     * Color used to paint the main background of a quick list header.
     */
    int QUICK_LIST_HEADER_BACKGROUND_COLOR = 61;

    /**
     * Color used to paint the secondary background of a quick list header.
     */
    int QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR = 62;

    /**
     * Color used to paint the text of a quick list header.
     */
    int QUICK_LIST_HEADER_FOREGROUND_COLOR = 63;

    /**
     * Color used to paint the background of a quick list item.
     */
    int QUICK_LIST_ITEM_BACKGROUND_COLOR = 64;

    /**
     * Color used to paint the text of a quick list item.
     */
    int QUICK_LIST_ITEM_FOREGROUND_COLOR = 65;

    /**
     * Color used to paint the background of a selected quick list item.
     */
    int QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR = 66;

    /**
     * Color used to paint the text of a selected quick list item.
     */
    int QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR = 67;

    /**
     * Color used to paint current line in the file editor / viewer's background when selected.
     * <p>
     * This defaults to the current <code>RTextArea</code> selection background color
     */
    int EDITOR_CURRENT_BACKGROUND_COLOR = 68;

    int FILE_GROUP_1_FOREGROUND_COLOR = 69;
    //    static final int FILE_GROUP_2_FOREGROUND_COLOR = 70;
//    static final int FILE_GROUP_3_FOREGROUND_COLOR = 71;
//    static final int FILE_GROUP_4_FOREGROUND_COLOR = 72;
//    static final int FILE_GROUP_5_FOREGROUND_COLOR = 73;
//    static final int FILE_GROUP_6_FOREGROUND_COLOR = 74;
//    static final int FILE_GROUP_7_FOREGROUND_COLOR = 75;
//    static final int FILE_GROUP_8_FOREGROUND_COLOR = 76;
//    static final int FILE_GROUP_9_FOREGROUND_COLOR = 77;
    int FILE_GROUP_10_FOREGROUND_COLOR = 78;

    int TERMINAL_BACKGROUND_COLOR = 79;
    int TERMINAL_FOREGROUND_COLOR = 80;
    int TERMINAL_SELECTED_FOREGROUND_COLOR = 81;
    int TERMINAL_SELECTED_BACKGROUND_COLOR = 82;

    /**
     * Color used to paint plain files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> foreground color.
     */
    int EXECUTABLE_FOREGROUND_COLOR = 83;

    /**
     * Color used to paint plain files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FILE_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int EXECUTABLE_INACTIVE_FOREGROUND_COLOR = 84;

    /**
     * Color used to paint selected plain files text in the folder panels.
     * <p>
     * This defaults to the current <code>JTable</code> selection foreground color.
     */
    int EXECUTABLE_SELECTED_FOREGROUND_COLOR = 85;

    /**
     * Color used to paint selected plain files text in the folder panels when they don't have the focus.
     * <p>
     * This behaves in exactly the same fashion as {@link #FILE_SELECTED_FOREGROUND_COLOR}, and defaults
     * to the same value.
     */
    int EXECUTABLE_INACTIVE_SELECTED_FOREGROUND_COLOR = 86;

    /**
     * Color used to paint the hex viewer dump's.
     */
    int HEX_VIEWER_HEX_FOREGROUND_COLOR = 87;
    /**
     * Color used to paint the hex viewer background.
     */
    int HEX_VIEWER_BACKGROUND_COLOR = 88;
    /**
     * Color used to paint the hex viewer alternate background.
     */
    int HEX_VIEWER_ALTERNATE_BACKGROUND_COLOR = 89;
    /**
     * Color used to paint the hex viewer ascii's text.
     */
    int HEX_VIEWER_ASCII_FOREGROUND_COLOR = 90;
    /**
     * Color used to paint the hex viewer offset's text.
     */
    int HEX_VIEWER_OFFSET_FOREGROUND_COLOR = 91;
    /**
     * Color used to paint the hex viewer's dump foreground when selected.
     */
    int HEX_VIEWER_SELECTED_DUMP_FOREGROUND_COLOR = 92;
    /**
     * Color used to paint the hex viewer's background when selected.
     */
    int HEX_VIEWER_SELECTED_BACKGROUND_COLOR = 93;
    /**
     * Color used to paint the hex viewer's ascii foreground when selected.
     */
    int HEX_VIEWER_SELECTED_ASCII_BACKGROUND_COLOR = 94;

    /**
     * Number of known colors.
     * <p>
     * Since color identifiers are contiguous, it is possible to explore all colors contained
     * by an instance of theme data by looping from 0 to this color.
     */
    int COLOR_COUNT = 95;

}
