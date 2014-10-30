/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2014 Oleg Trifonov
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
package com.mucommander.ui.terminal;

import com.jediterm.terminal.model.StyleState;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.mucommander.ui.action.ActionKeymap;
import com.mucommander.ui.action.impl.TerminalPanelAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.commandbar.CommandBarAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;

/**
 * @author Oleg Trifonov
 * Created on 27/10/14.
 */
public class JediTerminalPanelEx extends com.jediterm.terminal.ui.TerminalPanel {

    private final MainFrame mainFrame;

    public JediTerminalPanelEx(@NotNull SettingsProvider settingsProvider, @NotNull TerminalTextBuffer terminalTextBuffer,
                               @NotNull StyleState styleState, MainFrame mainFrame) {
        super(settingsProvider, terminalTextBuffer, styleState);
        this.mainFrame = mainFrame;
    }


    @Override
    public void processKeyEvent(KeyEvent e) {
        final int id = e.getID();

        if (id == KeyEvent.KEY_PRESSED) {
            String actionId = ActionKeymap.getRegisteredActionIdForKeystroke(KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiers(), false));
            if (TerminalPanelAction.Descriptor.ACTION_ID.equals(actionId)) {
                mainFrame.showTerminalPanel(false);
                return;
            }
            myKeyListener.keyPressed(e);
        } else if (id == KeyEvent.KEY_TYPED) {
            myKeyListener.keyTyped(e);
        }

        if (e.getKeyCode() == CommandBarAttributes.getModifier().getKeyCode()) {
            if (id == KeyEvent.KEY_PRESSED) {
                mainFrame.getCommandBar().keyPressed(e);
            } else if (id == KeyEvent.KEY_RELEASED) {
                mainFrame.getCommandBar().keyReleased(e);
            }

        }

        e.consume();
        //mainFrame.dispatchEvent(e);
        //super.processKeyEvent(e);
    }


}
