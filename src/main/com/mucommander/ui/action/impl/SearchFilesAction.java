package com.mucommander.ui.action.impl;

import com.mucommander.ui.action.*;
import com.mucommander.ui.dialog.file.SearchDialog;
import com.mucommander.ui.main.MainFrame;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * This action brings up the 'Search' dialog which allows to search in target directory for file pattern.
 *
 * @author sstolpovskiy
 */
public class SearchFilesAction extends MuAction {

    /**
     * Creates a new <code>MuAction</code> associated with the specified {@link com.mucommander.ui.main.MainFrame}. The properties contained by
     * the given {@link java.util.Hashtable} are used to initialize this action's property map.
     *
     * @param mainFrame  the MainFrame to associate with this new MuAction
     * @param properties the initial properties to use in this action. The Hashtable may simply be empty if no initial
     *                   properties are specified.
     */
    public SearchFilesAction(MainFrame mainFrame, Map<String, Object> properties) {
        super(mainFrame, properties);
    }

    @Override
    public void performAction() {
        new SearchDialog(mainFrame).showDialog();
    }

    public static class Factory implements ActionFactory {

        public MuAction createAction(MainFrame mainFrame, Map<String,Object> properties) {
            return new SearchFilesAction(mainFrame, properties);
        }
    }

    public static class Descriptor extends AbstractActionDescriptor {
        public static final String ACTION_ID = "SearchFilesCommand";

        public String getId() { return ACTION_ID; }

        public ActionCategory getCategory() { return ActionCategory.FILES; }

        public KeyStroke getDefaultAltKeyStroke() { return null; }

        public KeyStroke getDefaultKeyStroke() { return KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK); }
    }
    @Override
    public ActionDescriptor getDescriptor() {
        return new Descriptor();
    }
}
