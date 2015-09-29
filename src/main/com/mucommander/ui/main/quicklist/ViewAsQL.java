/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2015 Oleg Trifonov
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
package com.mucommander.ui.main.quicklist;

import com.mucommander.command.Command;
import com.mucommander.command.CommandManager;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.process.ProcessRunner;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ViewAction;
import com.mucommander.ui.action.impl.ViewAsAction;
import com.mucommander.ui.dialog.InformationDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.quicklist.QuickListWithDataList;
import com.mucommander.ui.quicklist.item.QuickListDataList;
import com.mucommander.ui.viewer.FileViewer;
import com.mucommander.ui.viewer.ViewerFactory;
import com.mucommander.ui.viewer.ViewerRegistrar;
import com.mucommander.ui.viewer.WarnUserException;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oleg Trifonov
 * Created on 02/07/14.
 */
public class ViewAsQL extends QuickListWithDataList<ViewerFactory> {

    private class CommandViewFactory implements ViewerFactory {

        private Command cmd;

        CommandViewFactory(Command cmd) {
            this.cmd = cmd;
        }

        @Override
        public boolean canViewFile(AbstractFile file) throws WarnUserException {
            return CommandManager.checkFileMask(cmd, file);
        }

        @Override
        public FileViewer createFileViewer() {
            return null;
        }

        @Override
        public String getName() {
            return cmd.getDisplayName() + " (" + cmd.getCommand() + ")";
        }

        @Override
        public String toString() {
            return getName();
        }

        private void viewFile(AbstractFile file) {
            try {
                ProcessRunner.execute(cmd.getTokens(file), file);
            } catch(Exception e) {
                InformationDialog.showErrorDialog(mainFrame);
            }
        }
    }

    private final AbstractFile file;
    private final MainFrame mainFrame;

    public ViewAsQL(MainFrame mainFame, AbstractFile file) {
        super(mainFame.getActivePanel(), ActionProperties.getActionLabel(ViewAsAction.Descriptor.ACTION_ID), "");
        this.file = file;
        this.mainFrame = mainFame;
    }


    @Override
    protected ViewerFactory[] getData() {
        if (file == null) {
            return new ViewerFactory[0];
        }
        // Builtin viewers
        List<ViewerFactory> factories = ViewerRegistrar.getAllViewers(file);
        List<ViewerFactory> result = new ArrayList<>(factories.size());
        for (final ViewerFactory factory : factories) {
            result.add(new ViewerFactory() {

                @Override
                public boolean canViewFile(AbstractFile file) throws WarnUserException {
                    return factory.canViewFile(file);
                }

                @Override
                public FileViewer createFileViewer() {
                    return factory.createFileViewer();
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
        for (Command cmd : CommandManager.getCommands(CommandManager.VIEWER_ALIAS)) {
            if (CommandManager.checkFileMask(cmd, file)) {
                result.add(new CommandViewFactory(cmd));
            }
        }
        ViewerFactory[] resultArray = new ViewerFactory[result.size()];
        resultArray = result.toArray(resultArray);
        return resultArray;
    }

    @Override
    protected void acceptListItem(ViewerFactory item) {
        if (item instanceof CommandViewFactory) {
            ((CommandViewFactory) item).viewFile(file);
            return;
        }
        Image icon = ActionProperties.getActionIcon(ViewAction.Descriptor.ACTION_ID).getImage();
        ViewerRegistrar.createViewerFrame(mainFrame, file, icon, item);
    }

    @Override
    protected QuickListDataList<ViewerFactory> getList() {
        return new QuickListDataList<>(getData());
    }

}
