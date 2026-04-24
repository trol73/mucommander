/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/trolcommander
 * Copyright (C) 2014-2026 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License* along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.ui.main

import com.mucommander.commons.file.AbstractFile
import com.mucommander.ui.theme.*
import java.awt.Cursor
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JLabel
import javax.swing.JTextField

/**
 * Renders the current directory as a horizontal row of hyperlink-style labels separated by `›` glyphs.
 * Clicking a label navigates the owning [FolderPanel] to the corresponding ancestor directory.
 *
 *
 * Extends [JTextField] (rather than `JPanel`) so that the look-and-feel paints the correct native border automatically.
 * On macOS Aqua the border renderer checks `instanceof JTextComponent`; a plain `JPanel` would not receive the beveled
 * round-rect treatment.
 * * [.paintComponent] is overridden to suppress text rendering and just fill the interior with the background color.
 *
 * Uses [AbstractFile.getParent] to walk the hierarchy, so it works uniformly for local paths (Windows, Unix) and remote file systems (SFTP, FTP…).
 */
internal class BreadcrumbBar(private val folderPanel: FolderPanel) : JTextField(), ThemeListener {
    init {
        isEditable = false
        setFocusable(false)
        setLayout(FlowLayout(FlowLayout.LEFT, 0, 0))
        setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR))
        ThemeManager.addCurrentThemeListener(this)
    }

    /**
     * Rebuilds the breadcrumb labels for the given [AbstractFile].
     * Walks up via [AbstractFile.getParent] to collect all ancestors, then renders them root-first.
     */
    fun setFile(file: AbstractFile?) {
        removeAll()

        if (file == null) {
            revalidate()
            repaint()
            return
        }

        // Collect ancestors from current directory up to the root
        val stack: Deque<AbstractFile> = ArrayDeque<AbstractFile>()
        var f: AbstractFile? = file
        while (f != null) {
            stack.push(f) // push → top of deque is the root after the loop
            f = f.getParent()
        }

        var first = true
        for (ancestor in stack) {
            if (!first) {
                add(makeSeparatorLabel())
            }
            first = false

            val isLast = stack.peekLast() === ancestor
            if (isLast) {
                add(makePlainLabel(ancestor.getName()))
            } else {
                add(makeLinkLabel(ancestor.getName(), ancestor.absolutePath))
            }
        }

        revalidate()
        repaint()
    }

    /** A label that looks and behaves like a hyperlink.  */
    private fun makeLinkLabel(text: String, targetPath: String?): JLabel {
        val escapedText = escapeHtml(text)
        val normalContent = "<html>$escapedText</html>"
        val hoverContent = "<html><u>$escapedText</u></html>"

        return JLabel(normalContent).apply {
            setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR))
            setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT))
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
            setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR))
            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent?) {
                    folderPanel.tryChangeCurrentFolder(targetPath)
                }

                override fun mouseEntered(e: MouseEvent?) =
                    setText(hoverContent)

                override fun mouseExited(e: MouseEvent?) =
                    setText(normalContent)
            })
        }
    }

    /** A plain (non-clickable) label for the current directory segment.  */
    private fun makePlainLabel(text: String): JLabel =
        JLabel("<html>" + escapeHtml(text) + "</html>").apply {
            setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR))
            setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT))
            setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR))
        }

    private fun makeSeparatorLabel(): JLabel =
        JLabel(SEPARATOR_GLYPH).apply {
            setForeground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_FOREGROUND_COLOR))
            setFont(ThemeManager.getCurrentFont(Theme.LOCATION_BAR_FONT))
            setBackground(ThemeManager.getCurrentColor(Theme.LOCATION_BAR_BACKGROUND_COLOR))
        }

    /** Escapes the minimal HTML special characters that can appear in file names.  */
    private fun escapeHtml(text: String): String {
        return text.replace("&", HTML_AMP)
            .replace("<", HTML_LT)
            .replace(">", HTML_GT)
    }

    override fun colorChanged(event: ColorChangedEvent) {
        if (event.colorId == Theme.LOCATION_BAR_BACKGROUND_COLOR) {
            setBackground(event.color)
        }
    }

    override fun fontChanged(event: FontChangedEvent?) {
    }

    companion object {
        /** The `›` glyph rendered between path segments.  */
        private const val SEPARATOR_GLYPH = " \u203A "

        private const val HTML_AMP = "&amp;"
        private const val HTML_LT = "&lt;"
        private const val HTML_GT = "&gt;"
    }
}