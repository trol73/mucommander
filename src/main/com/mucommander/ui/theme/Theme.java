/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2020 Oleg Trifonov
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

import com.mucommander.utils.text.Translator;

import java.awt.*;
import java.util.WeakHashMap;

/**
 * @author Nicolas Rinaudo
 */
public class Theme extends ThemeData {
    // - Theme types ---------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public enum Type {
        /** Describes the user defined theme. */
        USER,
        /** Describes predefined muCommander themes. */
        PREDEFINED,
        /** Describes custom muCommander themes. */
        CUSTOM
    }



    // - Theme listeners -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    private static final WeakHashMap<ThemeListener, ?> listeners = new WeakHashMap<>();


    
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Theme name. */
    private String name;
    /** Theme type. */
    private Type type;

    // While this field might look useless, it's actually critical for proper event notification:
    // ThemeData uses a weak hashmap to store its listeners, meaning that each listener must be 'linked'
    // somewhere or be garbage collected. Simply put, if we do not store the instance here, we might
    // as well not bother registering it.
    /** Default values listener. */
    private DefaultValuesListener defaultValuesListener;


    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Creates a new empty user theme.
     */
    Theme(ThemeListener listener) {
        super();
        init(listener, Type.USER, null);
    }

    Theme(ThemeListener listener, Type type, String name) {
        super();
        init(listener, type, name);
    }

    Theme(ThemeListener listener, ThemeData template) {
        super(template);
        init(listener, Type.USER, null);
    }

    Theme(ThemeListener listener, ThemeData template, Type type, String name) {
        super(template);
        init(listener, type, name);
    }

    private void init(ThemeListener listener, Type type, String name) {
        // This might seem like a roundabout way of doing things, but it's actually necessary.
        // If we didn't explicitly call a defaultValuesListener method, proGuard would 'optimise'
        // the instance out with catastrophic results (the listener would become a weak reference,
        // be removed by the garbage collector, and all our carefully crafted event system would
        // crumble).
        // While Theme.addDefaultValuesListener(defaultValuesListener = new DefaultValuesListener(this));
        // might seem like a more compact way of doing things, it wouldn't actually work.
        defaultValuesListener = new DefaultValuesListener();
        defaultValuesListener.setTheme(this);
        ThemeData.addDefaultValuesListener(defaultValuesListener);

        addThemeListener(listener);
        setType(type);
        if (name != null) {
            setName(name);
        }
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
            return true;
        }
		if (obj == null) {
            return false;
        }
		if (getClass() != obj.getClass()) {
            return false;
        }
		Theme other = (Theme) obj;
		if (name == null) {
			if (other.name != null) {
                return false;
            }
		} else if (!name.equals(other.name)) {
            return false;
        }
        return type == other.type;
    }

	// - Data retrieval ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
	 * Checks whether this theme is modifiable.
	 * <p>
	 * A theme is modifiable if and only if it's not a predefined theme.
	 *
	 * @return <code>true</code> if the theme is modifiable, <code>false</code> otherwise.
	 */
	public boolean canModify() {
		return type != Type.PREDEFINED;
	}

    /**
     * Returns the theme's type.
     * @return the theme's type.
     */
    public Type getType() {return type;}

    /**
     * Returns the theme's name.
     * @return the theme's name.
     */
    public String getName() {
        // Lazy loading for Launcher speedup
        if (name == null && type == Type.USER) {
            name = Translator.get("theme.custom_theme");
        }
        return name;
    }



    // - Data modification ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
	 * Sets one of the theme's fonts.
	 * <p>
	 * Note that this method will only work if the theme is not a predifined one. Any other theme type will throw an
	 * exception.
	 * 
	 * @see ThemeManager#setCurrentFont(int,Font)
	 * @param id
	 *            identifier of the font to set.
	 * @param font
	 *            value for the specified font.
	 * @throws IllegalStateException
	 *             thrown if the theme is a predifined one.
	 */
    @Override
    public boolean setFont(int id, Font font) {
        // Makes sure we're not trying to modify a non-user theme.
		if (type == Type.PREDEFINED) {
			throw new IllegalStateException("Trying to modify a predefined theme.");
        }

        if (super.setFont(id, font)) {
            // We're using getFont here to make sure that no event is propagated with a null value.
            triggerFontEvent(new FontChangedEvent(this, id, getFont(id)));
            return true;
        }
        return false;
    }

    /**
	 * Sets one of the theme's colors.
	 * <p>
	 * Note that this method will not work if the theme is a predefined one. Any other theme type will throw an
	 * exception.
	 *
	 * @see ThemeManager#setCurrentColor(int,Color)
	 * @param id
	 *            identifier of the color to set.
	 * @param color
	 *            value for the specified color.
	 * @throws IllegalStateException
	 *             thrown if the theme is a predefined one.
	 */
    @Override
    public boolean setColor(int id, Color color) {
        // Makes sure we're not trying to modify a non-user theme.
		if (type == Type.PREDEFINED) {
			throw new IllegalStateException("Trying to modify a predefined theme.");
        }

        if (super.setColor(id, color)) {
            // We're using getColor here to make sure that no event is propagated with a null value.
            triggerColorEvent(new ColorChangedEvent(this, id, getColor(id)));
            return true;
        }
        return false;
    }

    /**
     * Sets this theme's type.
     * <p>
     * If <code>type</code> is set to {@link Type#USER}, this method will also set the
     * theme's name to the proper value taken from the dictionary.
     *
     * @param type theme's type.
     */
    void setType(Type type) {
        checkType(type);

        this.type = type;
        if (type == Type.USER) {
            setName(null);      // the name will be lazy loaded later after dictionaly loading
        }
    }

    /**
     * Sets this theme's name.
     * @param name theme's name.
     */
    void setName(String name) {
        this.name = name;
    }



    static void checkType(Type type) {
        if (type != Type.USER && type != Type.PREDEFINED && type != Type.CUSTOM) {
            throw new IllegalArgumentException("Illegal theme type: " + type);
        }
    }

    /**
     * Returns the theme's name.
     * @return the theme's name.
     */
    public String toString() {
        return getName();
    }

    private static void addThemeListener(ThemeListener listener) {
        listeners.put(listener, null);
    }

    private static void removeThemeListener(ThemeListener listener) {
        listeners.remove(listener);
    }

    private static void triggerFontEvent(FontChangedEvent event) {
        for (ThemeListener listener : listeners.keySet()) {
            listener.fontChanged(event);
        }
    }

    private static void triggerColorEvent(ColorChangedEvent event) {
        for (ThemeListener listener : listeners.keySet()) {
            listener.colorChanged(event);
        }
    }

    private class DefaultValuesListener implements ThemeListener {
        private Theme theme;

        DefaultValuesListener() {}

        public void setTheme(Theme theme) {this.theme = theme;}

        public void colorChanged(ColorChangedEvent event) {
            if (!theme.isColorSet(event.getColorId())) {
                int colorId = event.getColorId();
                Theme.triggerColorEvent(new ColorChangedEvent(theme, colorId, getColor(colorId)));
            }
        }

        public void fontChanged(FontChangedEvent event) {
            if (!theme.isFontSet(event.getFontId())) {
                int fontId = event.getFontId();
                Theme.triggerFontEvent(new FontChangedEvent(theme, fontId, getFont(fontId)));
            }
        }
    }
}
