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
 * Created by trol on 02/07/14.
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
