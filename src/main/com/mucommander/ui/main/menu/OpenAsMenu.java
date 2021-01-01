package com.mucommander.ui.main.menu;

import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionParameters;
import com.mucommander.ui.action.impl.OpenAsAction;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.utils.text.Translator;

import javax.swing.*;
import java.util.*;

public class OpenAsMenu extends JMenu {

    private final static List<String> EXTENSIONS = new ArrayList<>();
    private final MainFrame mainFrame;

    static {
        for (Iterator<ArchiveFormatProvider> it = FileFactory.archiveFormats(); it.hasNext(); ) {
            ArchiveFormatProvider provider = it.next();
            EXTENSIONS.addAll(Arrays.asList(provider.getFileExtensions()));
        }
        Collections.sort(EXTENSIONS);
    }


    OpenAsMenu(MainFrame mainFrame) {
        super(Translator.get("file_menu.open_as") + "...");
        this.mainFrame = mainFrame;
        populate();
    }

    /**
     * Refreshes the content of the menu.
     */
    private void populate() {
        for (String extension : EXTENSIONS) {
            Map<String, Object> params = Collections.singletonMap("extension", extension);
            Action action = ActionManager.getActionInstance(new ActionParameters(OpenAsAction.Descriptor.ACTION_ID, params), mainFrame);
            action.putValue(Action.NAME, extension.substring(1));
            add(action);
        }
    }

    @Override
    public final JMenuItem add(Action a) {
        JMenuItem item = super.add(a);
        MenuToolkit.configureActionMenuItem(item);
        return item;
    }

}
