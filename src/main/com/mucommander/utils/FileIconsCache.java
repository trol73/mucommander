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
package com.mucommander.utils;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.ui.icon.FileIcons;
import ru.trolsoft.macosx.RetinaImageIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
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
    private static final int CACHE_SIZE = 1000;

    private final Map<String, Icon> icons = new HashMap<>();
    private final LinkedList<String> files = new LinkedList<>();


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

    public Icon getIcon(String path) {
        Icon result = icons.get(path);
        if (result != null) {
            // move record to top
            files.remove(path);
            files.addFirst(path);
            return result;
        }
        AbstractFile file = null;
        try {
            file = FileFactory.getFile(FileURL.getFileURL(path));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return addIcon(file, path);
    }


    public Image getImageIcon(AbstractFile file) {
        Icon icon = getIcon(file);
        if (icon instanceof RetinaImageIcon) {
            return ((RetinaImageIcon) icon).getImage();
        } else if (icon instanceof ImageIcon) {
            return ((ImageIcon)icon).getImage();
        } else {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            BufferedImage image = gc.createCompatibleImage(w, h);
            Graphics2D g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            return image;
        }
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
        if (files.size() > CACHE_SIZE) {
            icons.remove(files.removeLast());
        }
        return icon;
    }


    public void clear() {
        icons.clear();
        files.clear();
    }

}
