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

package com.mucommander.ui.action.impl;

import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.conf.MuConfigurations;
import com.mucommander.conf.MuPreference;
import com.mucommander.conf.MuPreferences;
import com.mucommander.ui.action.AbstractActionDescriptor;
import com.mucommander.ui.action.ActionCategory;
import com.mucommander.ui.action.ActionDescriptor;
import com.mucommander.ui.action.MuAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.table.FileTable;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * Marks/unmarks the previous block's rows in the current {@link FileTable}, starting with the
 * current row, and moves the selected row right before the last marked/unmarked row.
 *
 * @author Maxence Bernard
 */
public class MarkPreviousBlockAction extends MarkBackwardAction {

    MarkPreviousBlockAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    protected int getRowDecrement() {
        return MuConfigurations.getPreferences().getVariable(MuPreference.BLOCK_MARK_STEP_SIZE, MuPreferences.DEFAULT_BLOCK_MARK_STEP_SIZE);
    }

    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }

    public static final class Descriptor extends AbstractActionDescriptor {

        public static final String ACTION_ID = "MarkPreviousBlock";

        @Override
        public String getId() {
            return ACTION_ID;
        }

        @Override
        public ActionCategory getCategory() {
            return ActionCategory.SELECTION;
        }

        @Override
        public KeyStroke getDefaultAltKeyStroke() {
            return null;
        }

        @Override
        public KeyStroke getDefaultKeyStroke() {
            if (!OsFamily.MAC_OS_X.isCurrent()) {
                return KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
            } else {
                return KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.META_DOWN_MASK);
            }
        }

        @Override
        public MuAction createAction(MainFrame mainFrame, Map<String, Object> properties) {
            return new MarkPreviousBlockAction(mainFrame, properties);
        }

    }

}
