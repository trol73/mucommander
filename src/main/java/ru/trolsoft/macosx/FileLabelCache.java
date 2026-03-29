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
package ru.trolsoft.macosx;

import ch.randelshofer.quaqua.osx.OSXFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.impl.CachedFile;
import com.mucommander.commons.file.impl.local.LocalFile;
import com.mucommander.commons.runtime.OsFamily;
import com.mucommander.utils.FileIconsCache;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created on 27/12/16.
 * @author Oleg Trifonov
 */
public class FileLabelCache {

    private static final int CACHE_SIZE = 1000;
    private static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

    private static FileLabelCache instance;
    private final Map<String, Color> colors = new HashMap<>();
    private final LinkedList<String> files = new LinkedList<>();

    public static FileLabelCache getInstance() {
        if (instance == null) {
            synchronized (FileIconsCache.class) {
                if (instance == null) {
                    if (OsFamily.MAC_OS_X.isCurrent()) {
                        instance = new FileLabelCache();
                    } else {
                        instance = new FileLabelCache() {
                            @Override
                            public Color getLabelColor(AbstractFile file) {
                                return null;
                            }
                        };
                    }
                }
            }
        }
        return instance;
    }

    public Color getLabelColor(AbstractFile file) {
        String path = file.getAbsolutePath();
        Color result = colors.get(path);
        if (result != null) {
            // move record to top
            files.remove(path);
            files.addFirst(path);
            return result == TRANSPARENT_COLOR ? null : result;
        }
        return addColor(file);
    }

    private Color addColor(AbstractFile file) {
        Color color = getFileLabel(file);
        if (color == null) {
            color = TRANSPARENT_COLOR;
        }
        String path = file.getAbsolutePath();
        colors.put(path, color);
        files.addFirst(path);

        // remove oldest record if the cache is full
        if (files.size() > CACHE_SIZE) {
            colors.remove(files.removeLast());
        }
        return color == TRANSPARENT_COLOR ? null : color;
    }

    private Color getFileLabel(AbstractFile file) {
        if (file instanceof CachedFile) {
            file = ((CachedFile) file).getProxiedFile();
        }
        if (file instanceof LocalFile) {
            File f = (File) file.getUnderlyingFileObject();
            return OSXFile.getLabelColor(OSXFile.getLabel(f), 0);
        }
        return null;
    }

}
