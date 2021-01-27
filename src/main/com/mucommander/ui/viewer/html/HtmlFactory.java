/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2016 Oleg Trifonov
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
package com.mucommander.ui.viewer.html;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.utils.text.Translator;
import com.mucommander.ui.viewer.FileViewer;
import com.mucommander.ui.viewer.ViewerFactory;
import com.mucommander.ui.viewer.WarnUserException;

/**
 * <code>ViewerFactory</code> implementation for creating html viewers.
 *
 * @author Oleg Trifonov
 */
public class HtmlFactory implements ViewerFactory {

    public final static ExtensionFilenameFilter HTML_FILTER = new ExtensionFilenameFilter(new String[] {
            ".htm", ".html"
    });

    public static Boolean webViewIsAvailable;

    static {
        HTML_FILTER.setCaseSensitive(false);
    }


    @Override
    public boolean canViewFile(AbstractFile file) {
        if (webViewIsAvailable == null) {
            webViewIsAvailable = isWebViewIsAvailable();
        }
        return webViewIsAvailable && !file.isDirectory() && HTML_FILTER.accept(file);
    }

    @Override
    public FileViewer createFileViewer() {
        return new HtmlViewer();
    }

    @Override
    public String getName() {
        return Translator.get("viewer_type.html");
    }


    private static boolean isWebViewIsAvailable() {
        try {
            Class.forName("javafx.application.Platform");
            Class.forName("javafx.embed.swing.JFXPanel");
            Class.forName("javafx.scene.Group");
            Class.forName("javafx.scene.Scene");
            Class.forName("javafx.scene.web.WebView");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}