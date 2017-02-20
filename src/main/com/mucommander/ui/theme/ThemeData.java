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

import com.mucommander.commons.runtime.OsFamily;
import org.fife.ui.rtextarea.RTextArea;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Base class for all things Theme.
 * <p>
 * The role of <code>ThemeData</code> is twofold:<br>
 * - theme data storage.<br/>
 * - default values retrievals and notification.<br>
 *
 * <p>
 * In the current version, theme data is solely composed of assorted colors and fonts. <code>ThemeData</code>
 * offers methods to {@link #setColor(int,Color) set}, {@link #getColor(int) retrieve}, {@link #isIdentical(ThemeData,boolean) compare}
 * and {@link #cloneData() clone} these values.
 *
 * <p>
 * One of its major constraints is that it can <b>never</b> return <code>null</code> values for the items it contains. Whenever a specific
 * value hasn't been set, <code>ThemeData</code> will seemlessly provide the rest of the world with default values retrieved from the current
 * look&amp;feel.
 *
 * <p>
 * This default values system means that theme items can change outside of anybody's control: Swing UI properties can be updated, the current
 * look&amp;feel can be modified... <code>ThemeData</code> will track this changes and make sure that the proper event are dispatched
 * to listeners.
 *
 * <p>
 * In theory, classes that use the theme API should not need to worry about default value modifications. This is already managed internally, and
 * if the change affects any of the themes being listened on, the event will be propagated to them. There might special cases where it's necessary,
 * however, for which <code>ThemeData</code> provides a {@link #addDefaultValuesListener(ThemeListener) listening} mechanism.
 *
 * @see Theme
 * @see ThemeManager
 * @see javax.swing.UIManager
 * @author Nicolas Rinaudo
 */
public class ThemeData implements ThemeId {
    // - Default fonts -------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    // The following fields are look&feel dependant values for the fonts that are used by
    // themes. We need to monitor them, as they are prone to change through UIManager.



    // - Default identifiers -------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    private static final String DEFAULT_TEXT_AREA_FOREGROUND            = "TextArea.foreground";
    private static final String DEFAULT_TEXT_AREA_BACKGROUND            = "TextArea.background";
    private static final String DEFAULT_TEXT_AREA_SELECTION_FOREGROUND  = "TextArea.selectionForeground";
    private static final String DEFAULT_TEXT_AREA_SELECTION_BACKGROUND  = "TextArea.selectionBackground";
    private static final String DEFAULT_TEXT_AREA_CURRENT_BACKGROUND    = "TextArea.currentBackground";
    private static final String DEFAULT_TEXT_FIELD_FOREGROUND           = "TextField.foreground";
    private static final String DEFAULT_TEXT_FIELD_BACKGROUND           = "TextField.background";
    private static final String DEFAULT_TEXT_FIELD_SELECTION_FOREGROUND = "TextField.selectionForeground";
    private static final String DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND = "TextField.selectionBackground";
    private static final String DEFAULT_TEXT_FIELD_PROGRESS_BACKGROUND  = "TextField.progress";
    private static final String DEFAULT_TABLE_FOREGROUND                = "Table.foreground";
    private static final String DEFAULT_TABLE_BACKGROUND                = "Table.background";
    private static final String DEFAULT_TABLE_SELECTION_FOREGROUND      = "Table.selectionForeground";
    private static final String DEFAULT_TABLE_SELECTION_BACKGROUND      = "Table.selectionBackground";
    private static final String DEFAULT_TABLE_UNMATCHED_FOREGROUND      = "Table.unmatchedForeground";
    private static final String DEFAULT_TABLE_UNMATCHED_BACKGROUND      = "Table.unmatchedBackground";
    private static final String DEFAULT_MENU_HEADER_FOREGROUND          = "MenuHeader.foreground";
    private static final String DEFAULT_MENU_HEADER_BACKGROUND          = "MenuHeader.background";
    private static final String DEFAULT_TEXT_AREA_FONT                  = "TextArea.font";
    private static final String DEFAULT_TEXT_FIELD_FONT                 = "TextField.font";
    private static final String DEFAULT_LABEL_FONT                      = "Label.font";
    private static final String DEFAULT_TABLE_FONT                      = "Table.font";
    private static final String DEFAULT_MENU_HEADER_FONT                = "MenuHeader.font";
    private static final String DEFAULT_HEX_VIEWER_FONT                 = "HexViewer.font";


    // - Listeners -----------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Listeners on the default font and colors. */
    private static WeakHashMap<ThemeListener, ?> listeners = new WeakHashMap<>();



    // - Registered colors & fonts -------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** All registered colors. */
    private static final Map<Integer, DefaultColor> COLORS = new Hashtable<>();
    /** All registered default colors. */
    private static final Map<String, DefaultColor>  DEFAULT_COLORS = new Hashtable<>();
    /** All registered fonts. */
    private static final Map<Integer, DefaultFont>  FONTS = new Hashtable<>();
    /** All registered default fonts. */
    private static final Map<String, DefaultFont>   DEFAULT_FONTS = new Hashtable<>();



    // - Instance variables --------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** All the colors contained by the theme. */
    private Color[] colors;
    /** All the fonts contained by the theme. */
    private Font[]  fonts;


    private static void registerDefaultColor(String name, DefaultColor color) {
        DEFAULT_COLORS.put(name, color);
    }

    private static void registerDefaultFont(String name, DefaultFont font) {
        DEFAULT_FONTS.put(name, font);
    }

    private static void registerColor(int id, String defaultColor) {
        DefaultColor color = DEFAULT_COLORS.get(defaultColor);
        if (color == null) {
            throw new IllegalArgumentException("Not a registered default color: " + defaultColor);
        }
        registerColor(id, color);
    }

    private static void registerFont(int id, String defaultFont) {
        DefaultFont font = DEFAULT_FONTS.get(defaultFont);

        if (font == null) {
            throw new IllegalArgumentException("Not a registered default font: " + defaultFont);
        }
        registerFont(id, font);
    }

    private static void registerColor(int id, Color color) {
        registerColor(id, new FixedDefaultColor(color));
    }

    private static void registerFont(int id, Font font) {
        registerFont(id, new FixedDefaultFont(font));
    }

    private static void registerColor(int id, int defaultId) {
        registerColor(id, new LinkedDefaultColor(defaultId));
    }

    public static void registerFont(int id, int defaultId) {
        registerFont(id, new LinkedDefaultFont(defaultId));
    }

    private static void registerColor(int id, DefaultColor color) {
        Integer colorId = id;
        COLORS.put(colorId, color);
        color.link(colorId);
    }

    private static void registerFont(int id, DefaultFont font) {
        Integer fontId = id;
        FONTS.put(fontId, font);
        font.link(fontId);
    }


    static {
        // - Default values registering --------------------------------------------------------------------------------
        // -------------------------------------------------------------------------------------------------------------
        ComponentMapper mapper = new ComponentMapper() {
            @Override
            public JComponent getComponent() {
                return new RTextArea();
            }
        };

        registerDefaultFont(DEFAULT_TEXT_AREA_FONT, new SystemDefaultFont("TextArea.font", mapper) {
            @Override
            public Font getFont(ThemeData data) {
                Font font = super.getFont(data);
                if (OsFamily.getCurrent() == OsFamily.MAC_OS_X) {
                    return new Font("Menlo", font.getStyle(), font.getSize());
                }
                return font;
            }
        });
        registerDefaultFont(DEFAULT_HEX_VIEWER_FONT, new SystemDefaultFont("Table.font", mapper) {
            @Override
            public Font getFont(ThemeData data) {
                return new Font("Monospaced", Font.PLAIN, 14);
            }
        });
        registerDefaultColor(DEFAULT_TEXT_AREA_FOREGROUND, new SystemDefaultColor(SystemDefaultColor.FOREGROUND, "TextArea.foreground", mapper));
        registerDefaultColor(DEFAULT_TEXT_AREA_BACKGROUND, new SystemDefaultColor(SystemDefaultColor.BACKGROUND, "TextArea.background", mapper));
        registerDefaultColor(DEFAULT_TEXT_AREA_SELECTION_FOREGROUND, new SystemDefaultColor(SystemDefaultColor.SELECTION_FOREGROUND, "TextArea.selectionForeground", mapper));
        registerDefaultColor(DEFAULT_TEXT_AREA_SELECTION_BACKGROUND, new SystemDefaultColor(SystemDefaultColor.SELECTION_BACKGROUND, "TextArea.selectionBackground", mapper));
        registerDefaultColor(DEFAULT_TEXT_AREA_CURRENT_BACKGROUND, new SystemDefaultColor(SystemDefaultColor.CURRENT_LINE_BACKGROUND, "TextArea.currentBackground", mapper));


        // Register TextField related default values.
        mapper = new ComponentMapper() {
            @Override
            public JComponent getComponent() {
                return new JTextField();
            }
        };

        registerDefaultFont(DEFAULT_TEXT_FIELD_FONT, new SystemDefaultFont("TextField.font", mapper));
        registerDefaultColor(DEFAULT_TEXT_FIELD_FOREGROUND, new SystemDefaultColor(SystemDefaultColor.FOREGROUND, "TextField.foreground", mapper));
        registerDefaultColor(DEFAULT_TEXT_FIELD_BACKGROUND, new SystemDefaultColor(SystemDefaultColor.BACKGROUND, "TextField.background", mapper));
        registerDefaultColor(DEFAULT_TEXT_FIELD_SELECTION_FOREGROUND, new SystemDefaultColor(SystemDefaultColor.SELECTION_FOREGROUND, "TextField.selectionForeground", mapper));
        registerDefaultColor(DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND, new SystemDefaultColor(SystemDefaultColor.SELECTION_BACKGROUND, "TextField.selectionBackground", mapper));
        registerDefaultColor(DEFAULT_TEXT_FIELD_PROGRESS_BACKGROUND, new SystemDefaultColor(SystemDefaultColor.SELECTION_BACKGROUND, "TextField.selectionBackground", mapper) {
                                 @Override
                                 public Color getColor(ThemeData data) {
                Color color = super.getColor(data);
                                     return new Color(color.getRed(), color.getGreen(), color.getBlue(), 64);
                                 }
                             });

        // Register Table related default values.
        mapper = new ComponentMapper() {
            @Override
            public JComponent getComponent() {
                return new JTable();
            }
        };

        registerDefaultFont(DEFAULT_TABLE_FONT, new SystemDefaultFont("Table.font", mapper));
        registerDefaultColor(DEFAULT_TABLE_FOREGROUND, new SystemDefaultColor(SystemDefaultColor.FOREGROUND, "Table.foreground", mapper));
        registerDefaultColor(DEFAULT_TABLE_BACKGROUND, new SystemDefaultColor(SystemDefaultColor.BACKGROUND, "Table.background", mapper));
        registerDefaultColor(DEFAULT_TABLE_SELECTION_FOREGROUND, new SystemDefaultColor(SystemDefaultColor.SELECTION_FOREGROUND, "Table.selectionForeground", mapper));
        registerDefaultColor(DEFAULT_TABLE_SELECTION_BACKGROUND, new SystemDefaultColor(SystemDefaultColor.SELECTION_BACKGROUND, "Table.selectionBackground", mapper));
        registerDefaultColor(DEFAULT_TABLE_UNMATCHED_FOREGROUND, new SystemDefaultColor(SystemDefaultColor.FOREGROUND, "Table.foreground", mapper) {
                                 @Override
                                 public Color getColor(ThemeData data) {
                                     return super.getColor(data).darker();
                                 }
                             });
        registerDefaultColor(DEFAULT_TABLE_UNMATCHED_BACKGROUND, new SystemDefaultColor(SystemDefaultColor.BACKGROUND, "Table.background", mapper) {
                                 @Override
                                 public Color getColor(ThemeData data) {
                                     return super.getColor(data).darker();
                                 }
                             });

        // Menu header related default values.
        mapper = new ComponentMapper() {
            @Override
            public JComponent getComponent() {
                return new JInternalFrame();
            }
        };
        registerDefaultFont(DEFAULT_MENU_HEADER_FONT, new SystemDefaultFont("InternalFrame.font", mapper));
        registerDefaultColor(DEFAULT_MENU_HEADER_BACKGROUND, new SystemDefaultColor(SystemDefaultColor.BACKGROUND, "InternalFrame.activeTitleBackground", mapper));
        registerDefaultColor(DEFAULT_MENU_HEADER_FOREGROUND, new SystemDefaultColor(SystemDefaultColor.FOREGROUND, "InternalFrame.activeTitleForeground", mapper));

        // Label related default values.
        mapper = new ComponentMapper() {
            @Override
            public JComponent getComponent() {
                return new JLabel();
            }
        };
        registerDefaultFont(DEFAULT_LABEL_FONT, new SystemDefaultFont("Label.font", mapper));

        

        // - Default values linking ------------------------------------------------------------------------------------
        // -------------------------------------------------------------------------------------------------------------
        // QuickList default values.
        registerFont(QUICK_LIST_ITEM_FONT,                          DEFAULT_TABLE_FONT);
        registerFont(QUICK_LIST_HEADER_FONT,                        DEFAULT_MENU_HEADER_FONT);
        registerColor(QUICK_LIST_HEADER_SECONDARY_BACKGROUND_COLOR, DEFAULT_MENU_HEADER_BACKGROUND);
        registerColor(QUICK_LIST_HEADER_BACKGROUND_COLOR,           DEFAULT_MENU_HEADER_BACKGROUND);
        registerColor(QUICK_LIST_HEADER_FOREGROUND_COLOR,           DEFAULT_MENU_HEADER_FOREGROUND);
        registerColor(QUICK_LIST_ITEM_BACKGROUND_COLOR,             FILE_TABLE_BACKGROUND_COLOR);
        registerColor(QUICK_LIST_ITEM_FOREGROUND_COLOR,             FILE_FOREGROUND_COLOR);
        registerColor(QUICK_LIST_SELECTED_ITEM_BACKGROUND_COLOR,    FILE_TABLE_SELECTED_BACKGROUND_COLOR);
        registerColor(QUICK_LIST_SELECTED_ITEM_FOREGROUND_COLOR,    FILE_SELECTED_FOREGROUND_COLOR);

        // File default values.
        registerColor(HIDDEN_FILE_FOREGROUND_COLOR,                   Color.GRAY);
        registerColor(FOLDER_FOREGROUND_COLOR,                        DEFAULT_TABLE_FOREGROUND);
        registerColor(ARCHIVE_FOREGROUND_COLOR,                       DEFAULT_TABLE_FOREGROUND);
        registerColor(SYMLINK_FOREGROUND_COLOR,                       DEFAULT_TABLE_FOREGROUND);
        registerColor(FILE_INACTIVE_FOREGROUND_COLOR,                 DEFAULT_TABLE_FOREGROUND);
        registerColor(HIDDEN_FILE_INACTIVE_FOREGROUND_COLOR,          Color.GRAY);
        registerColor(FOLDER_INACTIVE_FOREGROUND_COLOR,               DEFAULT_TABLE_FOREGROUND);
        registerColor(ARCHIVE_INACTIVE_FOREGROUND_COLOR,              DEFAULT_TABLE_FOREGROUND);
        registerColor(SYMLINK_INACTIVE_FOREGROUND_COLOR,              DEFAULT_TABLE_FOREGROUND);
        registerColor(FILE_FOREGROUND_COLOR,                          DEFAULT_TABLE_FOREGROUND);
        registerColor(HIDDEN_FILE_SELECTED_FOREGROUND_COLOR,          DEFAULT_TABLE_SELECTION_FOREGROUND);
        registerColor(FOLDER_SELECTED_FOREGROUND_COLOR,               DEFAULT_TABLE_SELECTION_FOREGROUND);
        registerColor(ARCHIVE_SELECTED_FOREGROUND_COLOR,              DEFAULT_TABLE_SELECTION_FOREGROUND);
        registerColor(SYMLINK_SELECTED_FOREGROUND_COLOR,              DEFAULT_TABLE_SELECTION_FOREGROUND);
        registerColor(HIDDEN_FILE_INACTIVE_SELECTED_FOREGROUND_COLOR, DEFAULT_TABLE_SELECTION_FOREGROUND);
        registerColor(FOLDER_INACTIVE_SELECTED_FOREGROUND_COLOR,      DEFAULT_TABLE_SELECTION_FOREGROUND);
        registerColor(ARCHIVE_INACTIVE_SELECTED_FOREGROUND_COLOR,     DEFAULT_TABLE_SELECTION_FOREGROUND);
        registerColor(SYMLINK_INACTIVE_SELECTED_FOREGROUND_COLOR,     DEFAULT_TABLE_SELECTION_FOREGROUND);
        registerColor(FILE_INACTIVE_SELECTED_FOREGROUND_COLOR,        DEFAULT_TABLE_SELECTION_FOREGROUND);
        registerColor(FILE_SELECTED_FOREGROUND_COLOR,                 DEFAULT_TABLE_SELECTION_FOREGROUND);

        // FileTable default values.
        registerFont(FILE_TABLE_FONT,                                          DEFAULT_TABLE_FONT);
        registerColor(FILE_TABLE_BACKGROUND_COLOR,                             DEFAULT_TABLE_BACKGROUND);
        registerColor(FILE_TABLE_INACTIVE_BACKGROUND_COLOR,                    DEFAULT_TABLE_BACKGROUND);
        registerColor(FILE_TABLE_ALTERNATE_BACKGROUND_COLOR,                   DEFAULT_TABLE_BACKGROUND);
        registerColor(FILE_TABLE_INACTIVE_ALTERNATE_BACKGROUND_COLOR,          DEFAULT_TABLE_BACKGROUND);
        registerColor(FILE_TABLE_SELECTED_BACKGROUND_COLOR,                    DEFAULT_TABLE_SELECTION_BACKGROUND);
        registerColor(FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR,           DEFAULT_TABLE_SELECTION_BACKGROUND);
        registerColor(FILE_TABLE_UNMATCHED_FOREGROUND_COLOR,                   DEFAULT_TABLE_UNMATCHED_FOREGROUND);
        registerColor(FILE_TABLE_UNMATCHED_BACKGROUND_COLOR,                   DEFAULT_TABLE_UNMATCHED_BACKGROUND);
        registerColor(STATUS_BAR_BACKGROUND_COLOR,                             new Color(0xD5D5D5));
        registerColor(MARKED_FOREGROUND_COLOR,                                 Color.RED);
        registerColor(MARKED_INACTIVE_FOREGROUND_COLOR,                        Color.RED);
        registerColor(MARKED_SELECTED_FOREGROUND_COLOR,                        Color.RED);
        registerColor(MARKED_INACTIVE_SELECTED_FOREGROUND_COLOR,               Color.RED);

        registerColor(EXECUTABLE_FOREGROUND_COLOR,                             Color.GREEN);
        registerColor(EXECUTABLE_INACTIVE_FOREGROUND_COLOR,                    Color.GREEN);
        registerColor(EXECUTABLE_SELECTED_FOREGROUND_COLOR,                    Color.GREEN);
        registerColor(EXECUTABLE_INACTIVE_SELECTED_FOREGROUND_COLOR,           Color.GREEN);

        registerColor(FILE_TABLE_BORDER_COLOR,                                 Color.GRAY);
        registerColor(FILE_TABLE_INACTIVE_BORDER_COLOR,                        Color.GRAY);
        registerColor(FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR,          FILE_TABLE_SELECTED_BACKGROUND_COLOR);
        registerColor(FILE_TABLE_SELECTED_OUTLINE_COLOR,                       FILE_TABLE_SELECTED_BACKGROUND_COLOR);
        registerColor(FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR, FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR);
        registerColor(FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR,              FILE_TABLE_INACTIVE_SELECTED_BACKGROUND_COLOR);

        // File groups colors
        for (int i = FILE_GROUP_1_FOREGROUND_COLOR; i<= FILE_GROUP_10_FOREGROUND_COLOR; i++) {
            registerColor(i, DEFAULT_TABLE_FOREGROUND);
        }
        // Shell default values.
        registerFont(SHELL_FONT,                               DEFAULT_TEXT_AREA_FONT);
        registerFont(SHELL_HISTORY_FONT,                       DEFAULT_TEXT_FIELD_FONT);
        registerColor(SHELL_FOREGROUND_COLOR,                  DEFAULT_TEXT_AREA_FOREGROUND);
        registerColor(SHELL_BACKGROUND_COLOR,                  DEFAULT_TEXT_AREA_BACKGROUND);
        registerColor(SHELL_SELECTED_FOREGROUND_COLOR,         DEFAULT_TEXT_AREA_SELECTION_FOREGROUND);
        registerColor(SHELL_SELECTED_BACKGROUND_COLOR,         DEFAULT_TEXT_AREA_SELECTION_BACKGROUND);
        registerColor(SHELL_HISTORY_FOREGROUND_COLOR,          DEFAULT_TEXT_FIELD_FOREGROUND);
        registerColor(SHELL_HISTORY_BACKGROUND_COLOR,          DEFAULT_TEXT_FIELD_BACKGROUND);
        registerColor(SHELL_HISTORY_SELECTED_FOREGROUND_COLOR, DEFAULT_TEXT_FIELD_SELECTION_FOREGROUND);
        registerColor(SHELL_HISTORY_SELECTED_BACKGROUND_COLOR, DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND);

        // Terminal
        registerFont(TERMINAL_FONT,                           createDefaultTerminalFont());
        registerColor(TERMINAL_BACKGROUND_COLOR,              Color.BLACK);
        registerColor(TERMINAL_FOREGROUND_COLOR,              Color.GREEN);
        registerColor(TERMINAL_SELECTED_BACKGROUND_COLOR,     new Color(0x6666ff));
        registerColor(TERMINAL_SELECTED_FOREGROUND_COLOR,     Color.WHITE);

        // Editor default values.
        registerFont(EDITOR_FONT,                       DEFAULT_TEXT_AREA_FONT);
        registerColor(EDITOR_FOREGROUND_COLOR,          DEFAULT_TEXT_AREA_FOREGROUND);
        registerColor(EDITOR_BACKGROUND_COLOR,          DEFAULT_TEXT_AREA_BACKGROUND);
        registerColor(EDITOR_SELECTED_FOREGROUND_COLOR, DEFAULT_TEXT_AREA_SELECTION_FOREGROUND);
        registerColor(EDITOR_SELECTED_BACKGROUND_COLOR, DEFAULT_TEXT_AREA_SELECTION_BACKGROUND);
        registerColor(EDITOR_CURRENT_BACKGROUND_COLOR,  DEFAULT_TEXT_AREA_CURRENT_BACKGROUND);

        // Location default values.
        registerFont(LOCATION_BAR_FONT,                       DEFAULT_TEXT_FIELD_FONT);
        registerColor(LOCATION_BAR_FOREGROUND_COLOR,          DEFAULT_TEXT_FIELD_FOREGROUND);
        registerColor(LOCATION_BAR_BACKGROUND_COLOR,          DEFAULT_TEXT_FIELD_BACKGROUND);
        registerColor(LOCATION_BAR_SELECTED_FOREGROUND_COLOR, DEFAULT_TEXT_FIELD_SELECTION_FOREGROUND);
        registerColor(LOCATION_BAR_SELECTED_BACKGROUND_COLOR, DEFAULT_TEXT_FIELD_SELECTION_BACKGROUND);
        registerColor(LOCATION_BAR_PROGRESS_COLOR,            DEFAULT_TEXT_FIELD_PROGRESS_BACKGROUND);

        // Status bar default values.
        registerFont(STATUS_BAR_FONT,              DEFAULT_LABEL_FONT);
        registerColor(STATUS_BAR_FOREGROUND_COLOR, DEFAULT_TEXT_FIELD_FOREGROUND);
        registerColor(STATUS_BAR_CRITICAL_COLOR,   Color.RED);
        registerColor(STATUS_BAR_BORDER_COLOR,     Color.GRAY);
        registerColor(STATUS_BAR_BACKGROUND_COLOR, new Color(0xD5D5D5));
        registerColor(STATUS_BAR_OK_COLOR,         new Color(0x70EC2B));
        registerColor(STATUS_BAR_WARNING_COLOR,    new Color(0xFF7F00));


        // Hex viewer default values
        registerFont(HEX_VIEWER_FONT, DEFAULT_HEX_VIEWER_FONT);
        registerColor(HEX_VIEWER_HEX_FOREGROUND_COLOR, DEFAULT_TEXT_FIELD_FOREGROUND);
        registerColor(HEX_VIEWER_BACKGROUND_COLOR, DEFAULT_TEXT_FIELD_BACKGROUND);
        registerColor(HEX_VIEWER_ALTERNATE_BACKGROUND_COLOR, DEFAULT_TEXT_FIELD_BACKGROUND);
        registerColor(HEX_VIEWER_ASCII_FOREGROUND_COLOR, DEFAULT_TEXT_FIELD_FOREGROUND);
        registerColor(HEX_VIEWER_OFFSET_FOREGROUND_COLOR, DEFAULT_TEXT_FIELD_FOREGROUND);
        registerColor(HEX_VIEWER_SELECTED_DUMP_FOREGROUND_COLOR, DEFAULT_TEXT_FIELD_FOREGROUND);
        registerColor(HEX_VIEWER_SELECTED_BACKGROUND_COLOR, new Color(0x0000ff));
        registerColor(HEX_VIEWER_SELECTED_ASCII_BACKGROUND_COLOR, DEFAULT_TEXT_FIELD_FOREGROUND);
    }


    /**
     * Creates an empty set of theme data.
     * <p>
     * <code>ThemeData</code> instances created that way will return default values for every
     * single one of their items.
     *
     * @see #cloneData()
     */
    public ThemeData() {
        colors = new Color[COLOR_COUNT];
        fonts  = new Font[FONT_COUNT];
    }

    /**
     * Creates a new set of theme data.
     * <p>
     * The content of <code>from</code> will be copied in the new theme data. Note that
     * since we're copying the arrays themselves, rather than creating new ones and copying
     * each color and font individually, <code>from</code> will be unreliable at the end of this
     * call.
     *
     * <p>
     * This constructor is only meant for optimisation purposes. When transforming
     * theme data in a proper theme, using this constructor allows us to not duplicate
     * all the fonts and color. It's a risky constructor to use, however, and should not be exposed
     * outside of the scope of the package.
     *
     * @param from theme data from which to import values.
     */
    ThemeData(ThemeData from) {
        this();
        fonts  = from.fonts;
        colors = from.colors;
    }



    // - Data import / export ------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Clones the current theme data.
     * <p>
     * This method allows callers to decide whether they want to <i>freeze</i> default values or
     * not. Freezing a value means that it will be considered to have been set to the default value,
     * and will not be updated when this default value changes.
     *
     * @param  freezeDefaults whether or not to freeze the data's default values.
     * @return                a clone of the current theme data.
     * @see                   #cloneData()
     */
    private ThemeData cloneData(boolean freezeDefaults) {
        ThemeData data = new ThemeData();

        // Clones the theme's colors.
        for (int i = 0; i < COLOR_COUNT; i++) {
            data.colors[i] = freezeDefaults ? getColor(i) : colors[i];
        }

        // Clones the theme's fonts.
        for (int i = 0; i < FONT_COUNT; i++) {
            data.fonts[i] = freezeDefaults ? getFont(i) : fonts[i];
        }

        return data;
    }

    /**
     * Clones the theme data without freezing default values.
     * <p>
     * This is a convenience method, and is exactly equivalent to calling <code>{@link #cloneData(boolean) cloneData(false)}</code>.
     *
     * @return a clone of the current theme data.
     */
    public ThemeData cloneData() {return cloneData(false);}

    /**
     * Imports the specified data in the current one.
     * <p>
     * This method can be dangerous in that it overwrites every single value
     * of the current data without hope of retrieval. Moreoever, if something were to
     * go wrong during the operation and an exception was raised, the current data would
     * find itself in an invalid state, where some of its values would have been updated but
     * not all of them. It is up to callers to deal with these issues.
     *
     * <p>
     * Values overwriting is done through the use of the current instance's {@link #setColor(int,Color)}
     * and {@link #setFont(int,Font)} methods. This allows subclasses to plug their own code here. A good
     * example of that is {@link Theme}, which will automatically trigger font and color events when
     * importing data.
     *
     * @param data data to import.
     */
    public void importData(ThemeData data) {
        // Imports the theme's colors.
        for (int i = 0; i < COLOR_COUNT; i++) {
            setColor(i, data.colors[i]);
        }

        // Imports the theme's fonts.
        for (int i = 0; i < FONT_COUNT; i++) {
            setFont(i, data.fonts[i]);
        }
    }



    // - Items setting -------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Sets the specified color to the specified value.
     * <p>
     * Use a value of <code>null</code> to restore the color to it's default value.
     *
     * <p>
     * This method will return <code>false</code> if it didn't actually change the theme data.
     * This is checked through the use of <code>{@link #isColorDifferent(int,Color) isColorDifferent(}id,color)</code>.
     *
     * <p>
     * Note that even if the color is found to be identical, the previous value will be overwritten -
     * this is a design choice, meant for these cases where developers need to work with home-made
     * subclasses of <code>Color</code>.
     *
     * @param  id    identifier of the color to set.
     * @param  color value to which the color should be set.
     * @return       <code>true</code> if the call actually changed the data, <code>false</code> otherwise.
     */
    public synchronized boolean setColor(int id, Color color) {
        boolean buffer = isColorDifferent(id, color);   // Used to store the result of isColorDifferent.
        colors[id] = color;
        if (id >= FILE_GROUP_1_FOREGROUND_COLOR && id <= FILE_GROUP_10_FOREGROUND_COLOR) {
            triggerColorEvent(id, color);
        } else {
            switch (id) {
                case FILE_TABLE_SELECTED_SECONDARY_BACKGROUND_COLOR:
                case FILE_TABLE_SELECTED_OUTLINE_COLOR:
                case FILE_TABLE_INACTIVE_SELECTED_SECONDARY_BACKGROUND_COLOR:
                case FILE_TABLE_INACTIVE_SELECTED_OUTLINE_COLOR:
                    triggerColorEvent(id, color);
                }
        }
        return buffer;
    }

    void setColorFast(int id, Color color) {
        colors[id] = color;
    }

    /**
     * Sets the specified font to the specified value.
     * <p>
     * Use a value of <code>null</code> to restore the font to it's default value.
     *
     * <p>
     * This method will return <code>false</code> if it didn't actually change the theme data.
     * This is checked through the use of <code>{@link #isFontDifferent(int,Font) isFontDifferent(}id, font)</code>.
     *
     * <p>
     * Note that even if the font is found to be identical, the previous value will be overwritten -
     * this is a design choice, meant for these cases where developers need to work with home-made
     * subclasses of <code>Font</code>.
     *
     * @param  id   identifier of the font to set.
     * @param  font value to which the font should be set.
     * @return      <code>true</code> if the call actually changed the data, <code>false</code> otherwise.
     */
    public synchronized boolean setFont(int id, Font font) {
        boolean buffer = isFontDifferent(id, font); // Used to store the result of isFontDifferent.
        fonts[id] = font;
        return buffer;
    }

    void setFontFast(int id, Font font) {
        fonts[id] = font;
    }


    // - Items retrieval -----------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns the requested color.
     * <p>
     * If the requested color wasn't set, its default value will be returned.
     *
     * @param  id identifier of the color to retrieve.
     * @return    the requested color, or its default value if not set.
     * @see       #getDefaultColor(int,ThemeData)
     * @see       #isColorSet(int)
     */
    public synchronized Color getColor(int id) {
        checkColorIdentifier(id);
        return (colors[id] == null) ? getDefaultColor(id, this) : colors[id];
    }

    /**
     * Returns the requested font.
     * <p>
     * If the requested font wasn't set, its default value will be returned.
     *
     * @param  id identifier of the font to retrieve.
     * @return    the requested font, or its default value if not set.
     * @see       #getDefaultFont(int, ThemeData)
     * @see       #isFontSet(int)
     */
    public synchronized Font getFont(int id) {
        checkFontIdentifier(id);
        return fonts[id] == null ? getDefaultFont(id, this) : fonts[id];
    }


    /**
     * Returns <code>true</code> if the specified color is set.
     * @param  id identifier of the color to check for.
     * @return    <code>true</code> if the specified color is set, <code>false</code> otherwise.
     * @see       #getDefaultColor(int,ThemeData)
     */
    boolean isColorSet(int id) {
        return colors[id] != null;
    }

    /**
     * Returns <code>true</code> if the specified font is set.
     * @param  id identifier of the font to check for.
     * @return    <code>true</code> if the specified font is set, <code>false</code> otherwise.
     * @see       #getDefaultFont(int, ThemeData)
     */
    boolean isFontSet(int id) {
        return fonts[id] != null;
    }

    /**
     * Returns the default value for the specified color.
     * <p>
     * Default values are look&amp;feel dependant, and are subject to change during the application's
     * life time.<br/>
     * Classes that need to monitor such changes can register themselves using {@link #addDefaultValuesListener(ThemeListener)}.
     *
     * @param  id   identifier of the color whose default value should be retrieved.
     * @param  data theme data from which to retrieve default values.
     * @return      the default value for the specified color.
     * @see         #addDefaultValuesListener(ThemeListener)
     */
    private static Color getDefaultColor(int id, ThemeData data) {
        // Makes sure id is a legal color identifier.
        checkColorIdentifier(id);
        return COLORS.get(id).getColor(data);
    }

    /**
     * Returns the default value for the specified font.
     * <p>
     * Default values are look&amp;feel dependant, and are subject to change during the application's
     * life time.<br/>
     * Classes that need to monitor such changes can register themselves using {@link #addDefaultValuesListener(ThemeListener)}.
     *
     * @param  id identifier of the font whose default value should be retrieved.
     * @return    the default value for the specified font.
     * @see       #addDefaultValuesListener(ThemeListener)
     */
    private static Font getDefaultFont(int id, ThemeData data) {
        checkFontIdentifier(id);

        return FONTS.get(id).getFont(data);
    }



    // - Comparison ----------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns <code>true</code> if the specified data and the current one are identical.
     * <p>
     * Comparisons is done by calling {@link #isFontDifferent(int,Font,boolean)} and {@link #isColorDifferent(int,Color,boolean)}
     * on every font and color. Refer to the documentation of these methods for more information on using the <code>ignoreDefaults</code>
     * parameter.
     *
     * @param  data           data against which to compare.
     * @param  ignoreDefaults whether or not to compare default values.
     * @return                <code>true</code> if the specified data and the current one are identical, <code>false</code> otherwise.
     * @see                   #isFontDifferent(int,Font,boolean)
     * @see                   #isColorDifferent(int,Color,boolean)
     */
    private boolean isIdentical(ThemeData data, boolean ignoreDefaults) {
        // Compares the colors.
        for (int i = 0; i < COLOR_COUNT; i++) {
            if (isColorDifferent(i, data.colors[i], ignoreDefaults)) {
                return false;
            }
        }
        // Compares the fonts.
        for (int i = 0; i < FONT_COUNT; i++) {
            if (isFontDifferent(i, data.fonts[i], ignoreDefaults)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the current data is identical to the specified one, using default values when items haven't been set.
     * <p>
     * This is a convenience method, and is strictly equivalent to calling {@link #isIdentical(ThemeData,boolean) isIdentical(data, false)}.
     *
     * @param  data data against which to compare.
     * @return      <code>true</code> if the specified data and the current one are identical, <code>false</code> otherwise.
     */
    public boolean isIdentical(ThemeData data) {return isIdentical(data, false);}

    /**
     * Checks whether the current font and the specified one are different from one another.
     * <p>
     * This is a convenience method, and is stricly equivalent to calling
     * <code>{@link #isFontDifferent(int,Font,boolean) isFontDifferent(}id, font, false)</code>.
     *
     * @param  id   identifier of the font to check.
     * @param  font font to check.
     * @return      <code>true</code> if <code>font</code> is different from the one defined in the data.
     * @see         #isFontDifferent(int,Font,boolean)
     * @see         #isColorDifferent(int,Color)
     */
    boolean isFontDifferent(int id, Font font) {return isFontDifferent(id, font, false);}

    /**
     * Checks whether the current font and the specified one are different from one another.
     * <p>
     * Setting <code>ignoreDefaults</code> to <code>false</code> will compare both fonts from a 'user' point of view: comparison
     * will be done on the values that are used by the rest of the application. It might however be necessary to consider
     * fonts to be different if one is set but not the other. This can be achieved by setting <code>ignoreDefaults</code> to <code>true</code>.
     *
     * @param  id             identifier of the font to check.
     * @param  font           font to check.
     * @param  ignoreDefaults whether or not to ignore defaults if the requested item doesn't have a value.
     * @return                <code>true</code> if <code>font</code> is different from the one defined in the data.
     * @see                   #isFontDifferent(int,Font)
     * @see                   #isColorDifferent(int,Color)
     */
    private synchronized boolean isFontDifferent(int id, Font font, boolean ignoreDefaults) {
        checkFontIdentifier(id);

        // If the specified font is null, the only way for both fonts to be equal is for fonts[id]
        // to be null as well.
        if (font == null) {
            return fonts[id] != null;
        }

        // If fonts[id] is null and we're set to ignore defaults, both fonts are different.
        // If we're set to use defaults, we must compare font and the default value for id.
        if (fonts[id] == null) {
            return ignoreDefaults || !getDefaultFont(id, this).equals(font);
        }

        // 'Standard' case: both fonts are set, compare them normally.
        return !font.equals(fonts[id]);
    }

    /**
     * Checks whether the current color and the specified one are different from one another.
     * <p>
     * This is a convenience method, and is strictly equivalent to calling
     * <code>{@link #isColorDifferent(int,Color,boolean) isColorDifferent(}id, color, false)</code>.
     *
     * @param  id   identifier of the color to check.
     * @param  color color to check.
     * @return      <code>true</code> if <code>color</code> is different from the one defined in the data.
     * @see         #isColorDifferent(int,Color,boolean)
     * @see         #isFontDifferent(int,Font)
     */
    public boolean isColorDifferent(int id, Color color) {return isColorDifferent(id, color, false);}

    /**
     * Checks whether the current color and the specified one are different from one another.
     * <p>
     * Setting <code>ignoreDefaults</code> to <code>false</code> will compare both colors from a 'user' point of view: comparison
     * will be done on the values that are used by the rest of the application. It might however be necessary to consider
     * colors to be different if one is set but not the other. This can be achieved by setting <code>ignoreDefaults</code> to <code>true</code>.
     *
     * @param  id             identifier of the color to check.
     * @param  color           color to check.
     * @param  ignoreDefaults whether or not to ignore defaults if the requested item doesn't have a value.
     * @return                <code>true</code> if <code>color</code> is different from the one defined in the data.
     * @see                   #isColorDifferent(int,Color)
     * @see                   #isFontDifferent(int,Font)
     */
    private synchronized boolean isColorDifferent(int id, Color color, boolean ignoreDefaults) {
        checkColorIdentifier(id);

        // If the specified color is null, the only way for both colors to be equal is for colors[id]
        // to be null as well.
        if (color == null)
            return colors[id] != null;

        // If colors[id] is null and we're set to ignore defaults, both colors are different.
        // If we're set to use defaults, we must compare color and the default value for id.
        if (colors[id] == null)
            return ignoreDefaults || !getDefaultColor(id, this).equals(color);

        // 'Standard' case: both colors are set, compare them normally.
        return !color.equals(colors[id]);
    }



    // - Theme events --------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Registers the specified theme listener.
     * <p>
     * The listener will receive {@link FontChangedEvent font} and {@link ColorChangedEvent color} events whenever
     * one of the default values has been changed, by a modification to the current look&amp;feel for example.
     *
     * <p>
     * It is not necessary for 'themable' components to listen to default values, as they are automatically propagated
     * through {@link Theme} and {@link ThemeManager}.
     *
     * <p>
     * Note that listeners are stored as weak references, to make sure that the API doesn't keep ghost copies of objects
     * whose usefulness is long since past. This forces callers to make sure they keep a copy of the listener's instance: if
     * they do not, the instance will be weakly linked and garbage collected out of existence.
     *
     * @param listener theme listener to register.
     * @see            #removeDefaultValuesListener(ThemeListener)
     */
    static void addDefaultValuesListener(ThemeListener listener) {
        listeners.put(listener, null);
    }

    /**
     * Removes the specified instance from the list of registered theme listeners.
     * <p>
     * Note that since listeners are stored as weak references, calling this method is not strictly necessary. As soon
     * as a listener instance is not referenced anymore, it will automatically be caught and destroyed by the garbage
     * collector.
     *
     * @param listener instance to remove from the list of registered theme listeners.
     * @see            #addDefaultValuesListener(ThemeListener)
     */
    private static void removeDefaultValuesListener(ThemeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Dispatches a {@link FontChangedEvent} to all registered listeners.
     * @param id   identifier of the font that changed.
     * @param font new value for the font that changed.
     */
    static void triggerFontEvent(int id, Font font) {
        // Creates the event.
        FontChangedEvent event = new FontChangedEvent(null, id, font);  // Event that will be dispatched.

        // Dispatches it.
        for (ThemeListener listener : listeners.keySet()) {
            listener.fontChanged(event);
        }
    }

    /**
     * Dispatches a {@link ColorChangedEvent} to all registered listeners.
     * @param id    identifier of the color that changed.
     * @param color new value for the color that changed.
     */
    static void triggerColorEvent(int id, Color color) {
        ColorChangedEvent event = new ColorChangedEvent(null, id, color);

        // Dispatches it.
        for (ThemeListener listener : listeners.keySet()) {
            listener.colorChanged(event);
    }
    }



    // - Helper methods ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Checks whether the specified color identifier is legal.
     * @param  id                       identifier to check against.
     * @throws IllegalArgumentException if <code>id</code> is not a legal color identifier.
     */
    private static void checkColorIdentifier(int id) {
        if (id < 0 || id >= COLOR_COUNT) {
            throw new IllegalArgumentException("Illegal color identifier: " + id);
        }
    }

    /**
     * Checks whether the specified font identifier is legal.
     * @param  id                       identifier to check against.
     * @throws IllegalArgumentException if <code>id</code> is not a legal font identifier.
     */
    private static void checkFontIdentifier(int id) {
        if (id < 0 || id >= FONT_COUNT) {
            throw new IllegalArgumentException("Illegal font identifier: " + id);
        }
    }

    private static Font createDefaultTerminalFont() {
        String name;
        if (OsFamily.getCurrent() == OsFamily.WINDOWS) {
            name = "Consolas";
        } else if (OsFamily.getCurrent() == OsFamily.MAC_OS_X) {
            name = "Menlo";
        } else {
            name = "Monospaced";
        }
        return new Font(name, Font.PLAIN, 14);
    }
}
