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
package com.mucommander.utils

import com.mucommander.commons.file.AbstractFile
import com.mucommander.commons.file.FileFactory
import com.mucommander.commons.file.FileURL
import com.mucommander.ui.icon.FileIcons
import ru.trolsoft.macosx.RetinaImageIcon
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.net.MalformedURLException
import java.util.*
import javax.swing.Icon
import javax.swing.ImageIcon
import kotlin.concurrent.Volatile

/**
 * Created on 07.01.15.
 * @author Oleg Trifonov
 * 
 * Cache of system file icons
 */
class FileIconsCache {
    private val icons: MutableMap<String?, Icon?> = HashMap<String?, Icon?>()
    private val files = LinkedList<String?>()

    /**
     * Get icon from cache or get it from system and add to cache
     */
    fun getIcon(file: AbstractFile): Icon {
        val path = file.absolutePath
        val result = icons[path]
        if (result != null) {
            // move record to top
            files.remove(path)
            files.addFirst(path)
            return result
        }
        return addIcon(file, path)
    }

    fun getIcon(path: String?): Icon {
        val result = icons[path]
        if (result != null) {
            // move record to top
            files.remove(path)
            files.addFirst(path)
            return result
        }
        var file: AbstractFile? = null
        try {
            file = FileFactory.getFile(FileURL.getFileURL(path))
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return addIcon(file, path)
    }


    fun getImageIcon(file: AbstractFile): Image? =
        when (val icon = getIcon(file)) {
            is RetinaImageIcon -> icon.getImage()
            is ImageIcon -> icon.getImage()
            else -> iconToImage(icon)
        }

    /**
     * Request file icon from OS
     */
    private fun loadIcon(file: AbstractFile?): Icon =
        FileIcons.getFileIcon(file)


    /**
     * Loads icon and adds it to cache
     * @param path absolute path of file
     * @return loaded icon
     */
    private fun addIcon(file: AbstractFile?, path: String?): Icon {
        val icon = loadIcon(file)
        icons[path] = icon
        files.addFirst(path)

        // remove oldest record if the cache is full
        if (files.size > CACHE_SIZE) {
            icons.remove(files.removeLast())
        }
        return icon
    }


    fun clear() {
        icons.clear()
        files.clear()
    }

    companion object {
        /**
         * Default cache size
         */
        private const val CACHE_SIZE = 1000

        @JvmStatic
        @Volatile
        var instance: FileIconsCache? = null
            get() {
                if (field == null) {
                    synchronized(FileIconsCache::class.java) {
                        if (field == null) {
                            field = FileIconsCache()
                        }
                    }
                }
                return field
            }
            private set


        private fun iconToImage(icon: Icon): Image {
            val w = icon.iconWidth
            val h = icon.iconHeight
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val gd = ge.defaultScreenDevice
            val gc = gd.defaultConfiguration
            val image = gc.createCompatibleImage(w, h)
            val g = image.createGraphics()
            icon.paintIcon(null, g, 0, 0)
            g.dispose()
            return image
        }
    }
}
