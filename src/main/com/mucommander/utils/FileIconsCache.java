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
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created on 07.01.15.
 *
 * Cache of system file icons
 */
public class FileIconsCache {

    private final Map<AbstractFile, Icon> icons = new HashMap<>();
    private final Set<Icon> iconsSet = new HashSet<>();

    /**
     * Get icon from cache or get it from system and add to cache
     * @param file
     * @return
     */
    public Icon getIcon(AbstractFile file) {
        Icon result = icons.get(file);
        if (result != null) {
            return result;
        }
        return addIcon(file);
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
     * @param file
     * @return loaded icon
     */
    private Icon addIcon(AbstractFile file) {
        Icon icon = loadIcon(file);


        return icon;
    }


    public static void main(String args[]) {
        JFileChooser fileChooser = new JFileChooser();
        FileIconsCache cache = new FileIconsCache();


        cache.iconsSet.add(fileChooser.getIcon(new File("/Applications/")));
        cache.iconsSet.add(fileChooser.getIcon(new File("/bin/bash")));
        cache.iconsSet.add(fileChooser.getIcon(new File("/bin/cat")));
        cache.iconsSet.add(fileChooser.getIcon(new File("/bin/cp")));

        System.out.println(cache.iconsSet);

    }

}
