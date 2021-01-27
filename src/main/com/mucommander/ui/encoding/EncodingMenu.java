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

package com.mucommander.ui.encoding;

import com.mucommander.utils.text.Translator;
import com.mucommander.ui.dialog.DialogOwner;
import ru.trolsoft.ui.TCheckBoxMenuItem;
import ru.trolsoft.ui.TMenuSeparator;
import ru.trolsoft.ui.TRadioButtonMenuItem;

import javax.swing.*;
import java.awt.*;
import java.util.WeakHashMap;

/**
 * This menu lets the user choose a character encoding among a list of {@link EncodingPreferences#getPreferredEncodings()
 * preferred encodings}.
 * The menu contains a checkbox menu item for each of the preferred encodings, and a special item that invokes a dialog
 * that allows the list of preferred encodings to be customized.
 *
 * @see EncodingPreferences
 * @author Maxence Bernard
 */
public class EncodingMenu extends JMenu {

    /** Contains all registered encoding listeners, stored as weak references */
    protected final WeakHashMap<EncodingListener, ?> listeners = new WeakHashMap<>();

    /** the dialog/frame that owns this component */
    private final DialogOwner dialogOwner;

    /** The encoding that is currently selected, may be null */
    private String selectedEncoding;


    /**
     * Creates a new <code>EncodingMenu</code> with no encoding selected.
     *
     * @param dialogOwner the frame that owns this menu
     */
    public EncodingMenu(DialogOwner dialogOwner) {
        this(dialogOwner, null);
    }

    /**
     * Creates a new <code>EncodingMenu</code> with the specified encoding initially selected (may be <code>null</code>).
     * If the encoding is not one of the preferred encodings, it is added as the first encoding in the menu.
     *
     * @param dialogOwner the frame that owns this menu
     * @param selectedEncoding the encoding initially selected, <code>null</code> for none
     */
    public EncodingMenu(final DialogOwner dialogOwner, String selectedEncoding) {
        super(Translator.get("encoding"));

        this.dialogOwner = dialogOwner;
        this.selectedEncoding = selectedEncoding;

        populateMenu();
    }


    /**
     * Adds a checkbox menu item for each of the preferred encodings, and a special item that invokes a dialog
     * that allows the list of preferred encodings to be customized.
     */
    private void populateMenu() {
        java.util.List<String> encodings = EncodingPreferences.getPreferredEncodings();

        // Add the current encoding if it is not in the list of preferred encodings
        if (selectedEncoding != null && !encodings.contains(selectedEncoding)) {
            encodings.add(0, selectedEncoding);
        }

        addPreferredEncodings(encodings);
        add(new TMenuSeparator());
        addCustomizeMenu();

    }


    private void addPreferredEncodings(java.util.List<String> encodings) {
        ButtonGroup group = new ButtonGroup();
        for (String enc: encodings) {
            JMenuItem item = new TRadioButtonMenuItem(enc);

            // Select the current encoding, if there is one
            if (selectedEncoding != null && selectedEncoding.equals(enc)) {
                item.setSelected(true);
            }

            // Listen to checkbox actions
            item.addActionListener(e -> {
                String oldEncoding = selectedEncoding;
                selectedEncoding = ((JMenuItem)e.getSource()).getText();
                if (!oldEncoding.equals(selectedEncoding)) {
                    // Notify listeners of the new encoding
                    fireEncodingListener(oldEncoding, EncodingMenu.this.selectedEncoding);
                }
            });

            group.add(item);
            add(item);
        }
    }

    private void addCustomizeMenu() {
        // 'Customize' menu item
        JMenuItem customizeItem = new JMenuItem(Translator.get("customize") + "...");
        customizeItem.addActionListener(e -> {
            Window owner = dialogOwner.getOwner();
            if (owner instanceof Frame) {
                new PreferredEncodingsDialog((Frame) owner).showDialog();
            } else {
                new PreferredEncodingsDialog((Dialog) owner).showDialog();
            }

            removeAll();
            populateMenu();
        });
        add(customizeItem);
    }

    /**
     * Returns the encoding that is currently selected, <code>null</code> if none is selected.
     *
     * @return the encoding that is currently selected, <code>null</code> if none is selected.
     */
    public String getSelectedEncoding() {
        return selectedEncoding;
    }


    public void addEncodingListener(EncodingListener listener) {
        synchronized (listeners) {
            listeners.put(listener, null);
        }
    }

    public void removeEncodingListener(EncodingListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    private void fireEncodingListener(String oldEncoding, String newEncoding) {
        synchronized(listeners) {
            for (EncodingListener listener : listeners.keySet()) {
                listener.encodingChanged(this, oldEncoding, newEncoding);
            }
        }

    }
}