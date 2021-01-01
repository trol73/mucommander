/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2016 Oleg Trifonov
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

import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.TcAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.views.TableViewMode;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * @author Oleg Trifonov
 * Created on 15/04/15.
 */
public class ToggleTableViewModeCompactAction extends TcAction {

    /**
     * Creates a new <code>ToggleTableViewModeCompactAction</code>
     *
     * @param mainFrame  the MainFrame to associate with this new MuAction
     * @param properties the initial properties to use in this action. The Hashtable may simply be empty if no initial
     */
    private ToggleTableViewModeCompactAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        getMainFrame().getActiveTable().setViewMode(TableViewMode.COMPACT);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }



    public static final class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "ToggleTableViewModeCompact";

        public String getId() {
            return ACTION_ID;
        }

        public ActionCategory getCategory() {
            return ActionCategory.VIEW;
        }

        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        public KeyStroke getDefaultKeyStroke() {
            return KeyStroke.getKeyStroke(KeyEvent.VK_2, KeyEvent.CTRL_DOWN_MASK);
        }

        public TcAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new ToggleTableViewModeCompactAction(mainFrame, properties);
        }
    }
}
