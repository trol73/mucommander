/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.ui.action.impl;

import com.mucommander.ui.action.*;
import com.mucommander.ui.main.MainFrame;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * @author Oleg Trifonov
 *
 * Created on 24/10/14.
 */
public class TerminalPanelAction extends TcAction {

    private TerminalPanelAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }


    @Override
    public void performAction() {
        mainFrame.toggleTerminalPanel();
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }



    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "TerminalPanel";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.ALL;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        }

        public TcAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new TerminalPanelAction(mainFrame, properties);
        }
    }
}
