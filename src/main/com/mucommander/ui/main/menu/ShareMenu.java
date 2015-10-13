package com.mucommander.ui.main.menu;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.share.ShareManager;
import com.mucommander.share.ShareProvider;
import com.mucommander.text.Translator;
import com.mucommander.ui.action.impl.ShareAction;
import com.mucommander.ui.main.MainFrame;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import javax.swing.JMenu;

/**
 *
 * @author Mathias
 */
public class ShareMenu extends JMenu {

    private MainFrame mainFrame;
    private FileSet markedFiles;
    private AbstractFile clickedFile;

    /**
     * Creates a new Share menu.
     */
    ShareMenu(MainFrame frame, FileSet markedFiles, AbstractFile clickedFile) {
        super(Translator.get("file_menu.share"));
        this.mainFrame = frame;
        this.markedFiles = markedFiles;
        this.clickedFile = clickedFile;
        populate();
    }

    /**
     * Refreshes the content of the menu.
     */
    private synchronized void populate() {

        Set<String> extensions = new HashSet();
        
        if(clickedFile != null){
            markedFiles.add(clickedFile);
        }

        for (AbstractFile file : markedFiles) {
            extensions.add(file.getExtension());
        }
        boolean noAvailableProviders = true;
        
        for (ShareProvider provider : ShareManager.getProviders()) {

            if (provider.supportsFiletypes(extensions)) {
                add(new ShareAction(mainFrame, new Hashtable<String, Object>(), provider));
                noAvailableProviders = false;
            }
        }

        if (noAvailableProviders || markedFiles.isEmpty() || ShareManager.getProviders().isEmpty()) {
            setEnabled(false);
        }
    }

}
