/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2018 Oleg Trifonov
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
package com.mucommander.ui.dialog.bookmark;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.ui.combobox.MuComboBox;
import com.mucommander.utils.text.Translator;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.List;

public class BookmarkParentComboBox extends MuComboBox<String> implements PopupMenuListener {

    private String childName;

    BookmarkParentComboBox() {
        super();
        addPopupMenuListener(this);
    }


    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        removeAllItems();
        List<Bookmark> parents = BookmarkManager.getParentBookmarks();
        addItem("<" + Translator.get("root") + ">");
        for (Bookmark bookmark : parents) {
            if (bookmark.getName().equals(BookmarkManager.BOOKMARKS_SEPARATOR)) {
                continue;
            }
            if (childName == null || !childName.equals(bookmark.getName())) {
                addItem(bookmark.getName());
            }
        }
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {

    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getSelectedParent() {
        if (getSelectedIndex() == 0) {
            return null;
        }
        return getSelectedItem() == null ? null : getSelectedItem().toString();
    }

    public void setSelectedParent(String parent) {
        getModel().setSelectedItem(parent);
    }
}
