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


package com.mucommander.ui.helper;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


/**
 * MnemonicHelper provides a way to easily set mnemonics to UI components, without having to bother
 * with remembering which ones have already been assigned to another component.
 *
 * <p>To use it: simply create a new instance and keep calling {@link #getMnemonic(String)}
 * to get mnemonics from the giving pieces of text.
 * 
 * @author Maxence Bernard
 */
public class MnemonicHelper {

    /** Current list of previously assigned mnemonics */
    private final List<Character> takenMnemonics;
	
	
    /**
     * Creates a new blank MnemonicHelper.
     */
    public MnemonicHelper() {
        takenMnemonics = new ArrayList<>();
    }
	
	
    /**
     * Finds and returns first character in the given string that's not already as a mnemonic.
     *
     * <p>Returned mnemonic will be added to current internal list of taken mnemonics
     * and won't ever be used again by this instance.
     *
     * @return the character to be used as a mnemonic, always in lower case, 0 if no
     * mnemonic was available for this piece of text. 0 is returned if a <code>null</code> string is passed.
     * @param text text to get a mnemonic from.
     */
    public char getMnemonic(String text) {
        // Returns 0 in case of null string
        if (text == null || text.isEmpty()) {
            return 0;
        }
		
        // Find first letter available for mnemonic (keyboard shortcut)
        int mnemonicPos = 0;
        text = text.toLowerCase();
        int textLength = text.length();
        do {
            char mnemonic = text.charAt(mnemonicPos++);
            if (!isMnemonicUsed(mnemonic)) {
                takenMnemonics.add(mnemonic);
                return mnemonic;
            }
        } while (mnemonicPos < textLength);

        return 0;
    }


    /**
     * Convenience method that returns a mnemonic for the specified button.
     * Yields to the same result as if {@link #getMnemonic(String)} were called with JButton.getText().
     *
     * @param button the button to get a mnemonic for
     * @return the character to be used as a mnemonic, always in lower case, 0 if no
     * mnemonic was available for this piece of text. 0 is returned if a <code>null</code> string is passed.
     */
    public char getMnemonic(JButton button) {
        return getMnemonic(button.getText());
    }


    /**
     * Returns <code>true</code> if the specified character has already been previously
     * used as a mnemonic, returned by {@link #getMnemonic(String)}.
     *
     * @param ch the character which will be tested for an existing mnemonic.
     * @return whether or not the character is already used in the mnemonics array.
     */
    private boolean isMnemonicUsed(char ch) {
        return takenMnemonics.contains(ch);
    }


    /**
     * Clears any previously registered mnemonics by {@link #getMnemonic(String)}.
     */
    public void clear() {
        takenMnemonics.clear();
    }
}
