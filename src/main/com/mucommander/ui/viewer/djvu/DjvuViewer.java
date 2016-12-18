/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
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
package com.mucommander.ui.viewer.djvu;

import com.lizardtech.djvubean.DjVuBean;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.viewer.FileViewer;
import org.fife.ui.StatusBar;

import javax.swing.JScrollPane;
import java.io.IOException;

/**
 * Created on 04/08/14.
 */
public class DjvuViewer extends FileViewer {

    private DjVuBean djvuBean;

    public DjvuViewer() {
        super();
        djvuBean = new DjVuBean();
        setComponentToPresent(new JScrollPane(djvuBean));
    }
    @Override
    protected void show(AbstractFile file) throws IOException {
        djvuBean.setURL(file.getURL().getJavaNetURL());
    }

    @Override
    protected StatusBar getStatusBar() {
        return null;
    }

    @Override
    protected void saveStateOnClose() {

    }

    @Override
    protected void restoreStateOnStartup() {

    }
}
