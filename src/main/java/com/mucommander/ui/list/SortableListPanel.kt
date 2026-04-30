/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2026 Oleg Trifonov
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
package com.mucommander.ui.list

import com.mucommander.commons.collections.AlteredVector
import com.mucommander.ui.button.ArrowButton
import com.mucommander.utils.text.Translator
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.JScrollPane

/**
 * SortableListPanel is a JPanel which contains a scrollable [DynamicList] in the center and two buttons
 * 'Move up' and 'Move down' buttons on the right side of the list which allow to move the items up and down and
 * easily reorder them within the list.
 * 
 * @author Maxence Bernard
 */
class SortableListPanel<E>(
    items: AlteredVector<E>
) : JPanel(BorderLayout()) {
    /**
     * Returns the [DynamicList] used by this SortableListPanel.
     */
    @JvmField
    val dynamicList: DynamicList<E> = DynamicList<E>(items)


    /**
     * Creates a new SortableListPanel with a [DynamicList] that uses the provided items [AlteredVector].
     */
    init {
        add(
            JScrollPane(dynamicList), BorderLayout.CENTER
        )

        val buttonPanel = JPanel(GridLayout(2, 1)).apply {
            add(
                ArrowButton(dynamicList.getMoveUpAction(), ArrowButton.Direction.UP).apply {
                    preferredSize = Dimension(19, 0) // Constrain the button's size which by default is huge under Windows/Java 1.5
                    setFocusable(false) // Make the button non-focusable so that it doesn't steal focus from the list
                    setToolTipText(Translator.get("sortable_list.move_up"))
                }
            )
            add(
                ArrowButton(dynamicList.getMoveDownAction(), ArrowButton.Direction.DOWN).apply {
                    preferredSize = Dimension(19, 0) // Constrain the button's size which by default is huge under Windows/Java 1.5
                    setFocusable(false) // Make the button non-focusable so that it doesn't steal focus from the list
                    setToolTipText(Translator.get("sortable_list.move_down"))
                }
            )
        }

        add(buttonPanel, BorderLayout.EAST)
    }
}
