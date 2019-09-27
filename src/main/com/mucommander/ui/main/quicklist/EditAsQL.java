/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2019 Oleg Trifonov
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
package com.mucommander.ui.main.quicklist;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.process.ProcessRunner;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.EditAction;
import com.mucommander.ui.action.impl.EditAsAction;
import com.mucommander.ui.action.impl.ViewAction;
import com.mucommander.ui.action.impl.ViewAsAction;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.quicklist.QuickListWithDataList;
import com.mucommander.ui.quicklist.item.QuickListDataList;
import com.mucommander.ui.viewer.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EditAsQL extends QuickListWithDataList<EditorFactory> {

    private class CommandEditFactory implements EditorFactory {

        private final Command cmd;

        CommandEditFactory(Command cmd) {
            this.cmd = cmd;
        }

        @Override
        public boolean canEditFile(AbstractFile file) {
            return CommandManager.checkFileMask(cmd, file);
        }

        @Override
        public FileEditor createFileEditor() {
            return null;
        }

        @Override
        public String getName() {
            return cmd.getDisplayName();// + " (" + cmd.getCommand() + ")";
        }

        @Override
        public String toString() {
            return getName();
        }
        private void editFile(AbstractFile file) {
            try {
                ProcessRunner.execute(cmd.getTokens(file), file);
            } catch(Exception e) {
                InformationDialog.showErrorDialog(mainFrame);
            }
        }
    }

    private final AbstractFile file;
    private final MainFrame mainFrame;

    public EditAsQL(MainFrame mainFame, AbstractFile file) {
        super(mainFame.getActivePanel(), ActionProperties.getActionLabel(EditAsAction.Descriptor.ACTION_ID), "");
        this.file = file;
        this.mainFrame = mainFame;
    }


    @Override
    protected EditorFactory[] getData() {
        if (file == null) {
            return new EditorFactory[0];
        }
        // Builtin viewers
        List<EditorFactory> factories = EditorRegistrar.getAllEditors(file);
        List<EditorFactory> result = new ArrayList<>(factories.size());
        for (final EditorFactory factory : factories) {
            result.add(new EditorFactory() {

                @Override
                public boolean canEditFile(AbstractFile file) throws WarnUserException {
                    return factory.canEditFile(file);
                }

                @Override
                public FileEditor createFileEditor() {
                    return factory.createFileEditor();
                }

                @Override
                public String getName() {
                    return factory.getName();
                }

                @Override
                public String toString() {
                    return getName();
                }
            });
        }
        // View commands
        for (Command cmd : CommandManager.getCommands(CommandManager.EDITOR_ALIAS)) {
            if (CommandManager.checkFileMask(cmd, file)) {
                result.add(new EditAsQL.CommandEditFactory(cmd));
            }
        }
        EditorFactory[] resultArray = new EditorFactory[result.size()];
        resultArray = result.toArray(resultArray);
        return resultArray;
    }

    @Override
    protected void acceptListItem(EditorFactory item) {
        if (item instanceof EditAsQL.CommandEditFactory) {
            ((EditAsQL.CommandEditFactory) item).editFile(file);
            return;
        }
        Image icon = ActionProperties.getActionIcon(EditAction.Descriptor.ACTION_ID).getImage();
        EditorRegistrar.createEditorFrame(mainFrame, file, icon);//, item, null);
    }

    @Override
    protected QuickListDataList<EditorFactory> getList() {
        return new QuickListDataList<>(getData());
    }

}
