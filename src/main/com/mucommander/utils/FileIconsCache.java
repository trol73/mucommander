/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
 * Copyright (C) 2013-2015 Oleg Trifonov
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
package com.mucommander.utils;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.ui.icon.FileIcons;

import javax.swing.*;
import java.util.*;

/**
 * Created on 07.01.15.
 * @author Oleg trifonov
 *
 * Cache of system file icons
 */
public class FileIconsCache {

    /**
     * Default cache size
     */
    public static final int DEFAULT_SIZE = 1000;

    private final Map<String, Icon> icons = new HashMap<>();
    private final LinkedList<String> files = new LinkedList<>();

    private int size = DEFAULT_SIZE;


    private static FileIconsCache instance;



    public static FileIconsCache getInstance() {
        if (instance == null) {
            synchronized (FileIconsCache.class) {
                if (instance == null) {
                    instance = new FileIconsCache();
                }
            }
        }
        return instance;
    }

    /**
     * Get icon from cache or get it from system and add to cache
     * @param file
     * @return
     */
    public Icon getIcon(AbstractFile file) {
        String path = file.getAbsolutePath();
        Icon result = icons.get(path);
        if (result != null) {
            // move record to top
            files.remove(path);
            files.addFirst(path);
            return result;
        }
        return addIcon(file, path);
    }


    /**
     * Request file icon from OS
     * @param file
     * @return
     */
    private Icon loadIcon(AbstractFile file) {
        return FileIcons.getFileIcon(file);
    }


    /**
     * Loads icon and adds it to cache
     * @param path absolute path of file
     * @return loaded icon
     */
    private Icon addIcon(AbstractFile file, String path) {
        Icon icon = loadIcon(file);
        icons.put(path, icon);
        files.addFirst(path);

        // remove oldest record if the cache is full
        if (files.size() > size) {
            icons.remove(files.removeLast());
        }
        return icon;
    }


    public void clear() {
        icons.clear();
        files.clear();
    }

}
