/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
 * <p>
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.main.menu;

import com.mucommander.commons.file.impl.ar.ArFormatProvider;
import com.mucommander.commons.file.impl.bzip2.Bzip2FormatProvider;
import com.mucommander.commons.file.impl.gzip.GzipFormatProvider;
import com.mucommander.commons.file.impl.iso.IsoFormatProvider;
import com.mucommander.commons.file.impl.lst.LstFormatProvider;
import com.mucommander.commons.file.impl.rar.RarFormatProvider;
import com.mucommander.commons.file.impl.sevenzip.SevenZipFormatProvider;
import com.mucommander.commons.file.impl.tar.TarFormatProvider;
import com.mucommander.commons.file.impl.zip.ZipFormatProvider;
import com.mucommander.ui.action.ActionManager;
import com.mucommander.ui.action.ActionParameters;
import com.mucommander.ui.action.impl.OpenAsAction;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.utils.text.Translator;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Open as menu.
 *
 * @author Arik Hadas
 */
public class OpenAsMenu extends JMenu {

    private final static List<String> EXTENSIONS = new ArrayList<>();

    static {
        EXTENSIONS.addAll(Arrays.asList(ArFormatProvider.EXTENSIONS));
        EXTENSIONS.addAll(Arrays.asList(Bzip2FormatProvider.EXTENSIONS));
        EXTENSIONS.addAll(Arrays.asList(GzipFormatProvider.EXTENSIONS));
        EXTENSIONS.addAll(Arrays.asList(IsoFormatProvider.EXTENSIONS));
        EXTENSIONS.addAll(Arrays.asList(LstFormatProvider.EXTENSIONS));
        EXTENSIONS.addAll(Arrays.asList(RarFormatProvider.EXTENSIONS));
        EXTENSIONS.addAll(Arrays.asList(SevenZipFormatProvider.EXTENSIONS));
        EXTENSIONS.addAll(Arrays.asList(TarFormatProvider.EXTENSIONS));
        EXTENSIONS.addAll(Arrays.asList(ZipFormatProvider.EXTENSIONS));
        Collections.sort(EXTENSIONS);
    }

    private MainFrame mainFrame;

    /**
     * Creates a new Open As menu.
     */
    OpenAsMenu(MainFrame mainFrame) {
        super(Translator.get("file_menu.open_as"));
        this.mainFrame = mainFrame;
        populate();
    }

    /**
     * Refreshes the content of the menu.
     */
    private synchronized void populate() {
        for (String extension : EXTENSIONS) {
            Action action = ActionManager.getActionInstance(new ActionParameters(OpenAsAction.Descriptor.ACTION_ID, Collections.singletonMap("extension", extension)), mainFrame);
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
