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
package com.mucommander.ui.main.quicklist;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.action.ActionProperties;
import com.mucommander.ui.action.impl.ViewAction;
import com.mucommander.ui.action.impl.ViewAsAction;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.quicklist.QuickListWithDataList;
import com.mucommander.ui.quicklist.item.QuickListDataList;
import com.mucommander.ui.viewer.FileViewer;
import com.mucommander.ui.viewer.ViewerFactory;
import com.mucommander.ui.viewer.ViewerRegistrar;
import com.mucommander.ui.viewer.WarnUserException;

import java.awt.Image;
import java.util.List;

/**
 * Created on 02/07/14.
 */
public class ViewAsQL extends QuickListWithDataList<ViewerFactory> {

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
        List<ViewerFactory> factories = ViewerRegistrar.getAllViewers(file);
        ViewerFactory[] result = new ViewerFactory[factories.size()];
        for (int i = 0; i < factories.size(); i++) {
            final ViewerFactory factory = factories.get(i);
            result[i] = new ViewerFactory() {

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
            };
        }
        return result;
    }

    @Override
    protected void acceptListItem(ViewerFactory item) {
        Image icon = ActionProperties.getActionIcon(ViewAction.Descriptor.ACTION_ID).getImage();
        ViewerRegistrar.createViewerFrame(mainFrame, file, icon, item);
    }

    @Override
    protected QuickListDataList<ViewerFactory> getList() {
        return new QuickListDataList<>(getData());
    }

}
