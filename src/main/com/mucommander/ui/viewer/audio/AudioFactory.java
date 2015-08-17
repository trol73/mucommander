/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2014 Oleg Trifonov
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
package com.mucommander.ui.viewer.audio;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.text.Translator;
import com.mucommander.ui.viewer.FileViewer;
import com.mucommander.ui.viewer.ViewerFactory;
import com.mucommander.ui.viewer.WarnUserException;


public class AudioFactory implements ViewerFactory {

    public final static ExtensionFilenameFilter AUDIO_FILTER = new ExtensionFilenameFilter(new String[] {".wav", ".mp3", ".ogg", ".mid"});

    static {
        AUDIO_FILTER.setCaseSensitive(false);
    }


    @Override
    public boolean canViewFile(AbstractFile file) throws WarnUserException {
        return false;
//        if (file.isDirectory()) {
//            return false;
//        }
//        return AUDIO_FILTER.accept(file);
    }

    @Override
    public FileViewer createFileViewer() {
        return new AudioViewer();
    }

    @Override
    public String getName() {
        return Translator.get("viewer_type.audio");
    }
}
