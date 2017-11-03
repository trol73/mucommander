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

import com.mucommander.utils.xml.XmlAttributes;
import com.mucommander.utils.xml.XmlWriter;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class used to save themes in XML format.
 * @author Nicolas Rinaudo
 */
class ThemeWriter implements ThemeXmlConstants, ThemeId {
    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Prevents instanciation of the class.
     */
    private ThemeWriter() {}



    // - XML output ----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Saves the specified theme to the specified output stream.
     * @param  theme       theme to save.
     * @param  stream      where to write the theme to.
     * @throws IOException thrown if any IO related error occurs.
     */
    public static void write(ThemeData theme, OutputStream stream) throws IOException {
        XmlWriter out = new XmlWriter(stream);
        out.startElement(ELEMENT_ROOT);
        out.println();

        // - File table description ------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_TABLE);
        out.println();

        // Global values.
        writeColor(theme, out, FILE_TABLE_BORDER_COLOR, ELEMENT_BORDER);
        writeColor(theme, out, FILE_TABLE_INACTIVE_BORDER_COLOR, ELEMENT_INACTIVE_BORDER);
        writeColor(theme, out, FILE_TABLE_SELECTED_OUTLINE_COLOR, ELEMENT_OUTLINE);
        writeColor(theme, out, FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR, ELEMENT_INACTIVE_OUTLINE);
        writeFont(theme, out, FILE_TABLE_FONT, ELEMENT_FONT);

        // Normal background colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, FILE_TABLE_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, FILE_TABLE_INACTIVE_BACKGROUND_COLOR, ELEMENT_INACTIVE_BACKGROUND);
        out.endElement(ELEMENT_NORMAL);

        // Selected background colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, FILE_TABLE_SELECTED_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR, ELEMENT_INACTIVE_BACKGROUND);
        writeColor(theme, out, FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR, ELEMENT_INACTIVE_SECONDARY_BACKGROUND);
        writeColor(theme, out, FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR, ELEMENT_SECONDARY_BACKGROUND);

        out.endElement(ELEMENT_SELECTED);

        // Alternate background colors.
        out.startElement(ELEMENT_ALTERNATE);
        out.println();
        writeColor(theme, out, FILE_TABLE_ALTERNATE_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR, ELEMENT_INACTIVE_BACKGROUND);
        out.endElement(ELEMENT_ALTERNATE);

        // Unmatched colors.
        out.startElement(ELEMENT_UNMATCHED);
        out.println();
        writeColor(theme, out, FILE_TABLE_UNMATCHED_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, FILE_TABLE_UNMATCHED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_UNMATCHED);

        // Hidden files.
        out.startElement(ELEMENT_HIDDEN);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, HIDDEN_FILE_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        writeColor(theme, out, HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, HIDDEN_FILE_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        writeColor(theme, out, HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_HIDDEN);

        // Folders.
        out.startElement(ELEMENT_FOLDER);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, FOLDER_INACTIVE_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, FOLDER_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, FOLDER_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_FOLDER);

        // Archives.
        out.startElement(ELEMENT_ARCHIVE);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, ARCHIVE_INACTIVE_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, ARCHIVE_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, ARCHIVE_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_ARCHIVE);

        // Symlink.
        out.startElement(ELEMENT_SYMLINK);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, SYMLINK_INACTIVE_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, SYMLINK_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, SYMLINK_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_SYMLINK);

        // Marked files.
        out.startElement(ELEMENT_MARKED);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, MARKED_INACTIVE_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, MARKED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, MARKED_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_MARKED);

        // Executable files.
        out.startElement(ELEMENT_EXECUTABLE);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, EXECUTABLE_INACTIVE_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, EXECUTABLE_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, EXECUTABLE_INACTIVE_SELECTED_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, EXECUTABLE_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_EXECUTABLE);

        // Plain files.
        out.startElement(ELEMENT_FILE);
        out.println();
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, FILE_INACTIVE_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, FILE_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, ELEMENT_INACTIVE_FOREGROUND);
        writeColor(theme, out, FILE_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_FILE);
        out.endElement(ELEMENT_TABLE);

        // File groups
        out.startElement(ELEMENT_FILE_GROUPS);
        out.println();
        for (int i = 0; i < 10; i++) {
            out.startElement(ELEMENT_GROUP + (i+1));
            out.println();
            writeColor(theme, out, FILE_GROUP_1_FOREGROUND_COLOR+i, ELEMENT_NORMAL);
            out.endElement(ELEMENT_GROUP + (i+1));
        }
        out.endElement(ELEMENT_FILE_GROUPS);


        // - Shell description ----------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_SHELL);
        out.println();
        writeFont(theme, out, SHELL_FONT, ELEMENT_FONT);

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, SHELL_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, SHELL_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, SHELL_SELECTED_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, SHELL_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_SHELL);


        // - Shell history description ---------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_SHELL_HISTORY);
        out.println();
        writeFont(theme, out, SHELL_HISTORY_FONT, ELEMENT_FONT);

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, SHELL_HISTORY_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, SHELL_HISTORY_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, SHELL_HISTORY_SELECTED_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_SHELL_HISTORY);

        // - Terminal description ---------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_TERMINAL);
        out.println();
        writeFont(theme, out, TERMINAL_FONT, ELEMENT_FONT);

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, TERMINAL_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, TERMINAL_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, TERMINAL_SELECTED_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, TERMINAL_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_TERMINAL);


        // - Editor description ----------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_EDITOR);
        out.println();
        writeFont(theme, out, EDITOR_FONT, ELEMENT_FONT);

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, EDITOR_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, EDITOR_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, EDITOR_SELECTED_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, EDITOR_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);

        // Current line
        out.startElement(ELEMENT_CURRENT);
        out.println();
        writeColor(theme, out, EDITOR_CURRENT_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        out.endElement(ELEMENT_CURRENT);
        out.endElement(ELEMENT_EDITOR);

        // - Hex viewer description ------------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_HEX_VIEWER);
        out.println();
        writeFont(theme, out, HEX_VIEWER_FONT, ELEMENT_FONT);

        // Normal colors
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, HEX_VIEWER_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, HEX_VIEWER_HEX_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        writeColor(theme, out, HEX_VIEWER_ALTERNATE_BACKGROUND_COLOR, ELEMENT_SECONDARY_BACKGROUND);
        writeColor(theme, out, HEX_VIEWER_ASCII_FOREGROUND_COLOR, ELEMENT_ASCII_FOREGROUND);
        writeColor(theme, out, HEX_VIEWER_OFFSET_FOREGROUND_COLOR, ELEMENT_OFFSET_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);

        // Selected colors
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, HEX_VIEWER_SELECTED_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, HEX_VIEWER_SELECTED_DUMP_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        writeColor(theme, out, HEX_VIEWER_SELECTED_ASCII_BACKGROUND_COLOR, ELEMENT_ASCII_BACKGROUND);
        out.endElement(ELEMENT_SELECTED);

        out.endElement(ELEMENT_HEX_VIEWER);

        // - Location bar description ----------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_LOCATION_BAR);
        out.println();
        writeFont(theme, out, LOCATION_BAR_FONT, ELEMENT_FONT);
        writeColor(theme, out, LOCATION_BAR_PROGRESS_COLOR, ELEMENT_PROGRESS);

        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, LOCATION_BAR_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, LOCATION_BAR_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_NORMAL);

        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, LOCATION_BAR_SELECTED_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, LOCATION_BAR_SELECTED_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_LOCATION_BAR);


        // - Volume label description ----------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_STATUS_BAR);
        out.println();
        // Font.
        writeFont(theme, out, STATUS_BAR_FONT, ELEMENT_FONT);

        // Colors.
        writeColor(theme, out, STATUS_BAR_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, STATUS_BAR_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        writeColor(theme, out, STATUS_BAR_BORDER_COLOR, ELEMENT_BORDER);
        writeColor(theme, out, STATUS_BAR_OK_COLOR, ELEMENT_OK);
        writeColor(theme, out, STATUS_BAR_WARNING_COLOR, ELEMENT_WARNING);
        writeColor(theme, out, STATUS_BAR_CRITICAL_COLOR, ELEMENT_CRITICAL);
        out.endElement(ELEMENT_STATUS_BAR);


        
        // - Quick list label description ----------------------------------------------------
        // -------------------------------------------------------------------------------
        out.startElement(ELEMENT_QUICK_LIST);
        out.println();
        
        // Quick list header
        out.startElement(ELEMENT_HEADER);
        out.println();
        // Font.
        writeFont(theme, out, QUICK_LIST_HEADER_FONT, ELEMENT_FONT);
        // Colors.
        writeColor(theme, out, QUICK_LIST_HEADER_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        writeColor(theme, out, QUICK_LIST_HEADER_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        writeColor(theme, out, QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR, ELEMENT_SECONDARY_BACKGROUND);
        out.endElement(ELEMENT_HEADER);
        
        // Quick list item
        out.startElement(ELEMENT_ITEM);
        out.println();
        // Font.
        writeFont(theme, out, QUICK_LIST_ITEM_FONT, ELEMENT_FONT);
        // Colors.
        // Normal colors.
        out.startElement(ELEMENT_NORMAL);
        out.println();
        writeColor(theme, out, QUICK_LIST_ITEM_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        writeColor(theme, out, QUICK_LIST_ITEM_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        out.endElement(ELEMENT_NORMAL);
        // Selected colors.
        out.startElement(ELEMENT_SELECTED);
        out.println();
        writeColor(theme, out, QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR, ELEMENT_FOREGROUND);
        writeColor(theme, out, QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR, ELEMENT_BACKGROUND);
        out.endElement(ELEMENT_SELECTED);
        out.endElement(ELEMENT_ITEM);
        out.endElement(ELEMENT_QUICK_LIST);

        out.endElement(ELEMENT_ROOT);
    }



    // - Helper methods ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the XML attributes describing the specified font.
     * @param  font font to described as XML attributes.
     * @return      the XML attributes describing the specified font.
     */
    private static XmlAttributes getFontAttributes(Font font) {
        XmlAttributes attributes = new XmlAttributes(); // Stores the font's description.

        // Font family and size.
        attributes.add(ATTRIBUTE_FAMILY, font.getFamily());
        attributes.add(ATTRIBUTE_SIZE, Integer.toString(font.getSize()));

        // Font style.
        if (font.isBold()) {
            attributes.add(ATTRIBUTE_BOLD, VALUE_TRUE);
        }
        if (font.isItalic()) {
            attributes.add(ATTRIBUTE_ITALIC, VALUE_TRUE);
        }

        return attributes;
    }

    /**
     * Returns the XML attributes describing the specified color.
     * @param  color color to described as XML attributes.
     * @return       the XML attributes describing the specified color.
     */
    private static XmlAttributes getColorAttributes(Color color) {
        StringBuilder buffer = new StringBuilder(); // Used to build the color's string representation.

        // Red component.
        if (color.getRed() < 16) {
            buffer.append('0');
        }
        buffer.append(Integer.toString(color.getRed(), 16));

        // Green component.
        if (color.getGreen() < 16) {
            buffer.append('0');
        }
        buffer.append(Integer.toString(color.getGreen(), 16));

        // Blue component.
        if (color.getBlue() < 16) {
            buffer.append('0');
        }
        buffer.append(Integer.toString(color.getBlue(), 16));

        // Builds the XML attributes.
        XmlAttributes attributes = new XmlAttributes(); // Stores the color's description.
        attributes.add(ATTRIBUTE_COLOR, buffer.toString());

        if (color.getAlpha() != 255) {
            buffer.setLength(0);
            if (color.getAlpha() < 16) {
                buffer.append('0');
            }
            buffer.append(Integer.toString(color.getAlpha(), 16));
            attributes.add(ATTRIBUTE_ALPHA, buffer.toString());
        }

        return attributes;
    }

    private static void writeColor(ThemeData theme, XmlWriter out, int colorId, String name) throws IOException {
        if (theme.isColorSet(colorId)) {
            out.writeStandAloneElement(name, getColorAttributes(theme.getColor(colorId)));
        }
    }

    private static void writeFont(ThemeData theme, XmlWriter out, int fontId, String name) throws IOException {
        if (theme.isFontSet(fontId)) {
            out.writeStandAloneElement(name, getFontAttributes(theme.getFont(fontId)));
        }
    }

}
