/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.bookmark;

import com.mucommander.RuntimeConstants;
import com.mucommander.utils.xml.XmlAttributes;
import com.mucommander.utils.xml.XmlWriter;

import java.io.IOException;
import java.io.OutputStream;


/**
 * This class provides a method to write bookmarks to an XML file.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
class BookmarkWriter implements BookmarkConstants, BookmarkBuilder {
    private XmlWriter out;

    BookmarkWriter(OutputStream stream) throws IOException {
        out = new XmlWriter(stream);
    }

    public void startBookmarks() throws BookmarkException {
        // Root element
        try {
            // Version the file.
            // Note: the version attribute was introduced in muCommander 0.8.4.
            XmlAttributes attributes = new XmlAttributes();
            attributes.add("version", RuntimeConstants.VERSION);

            out.startElement(ELEMENT_ROOT, attributes);
            out.println();
        } catch(IOException e) {
            throw new BookmarkException(e);
        }
    }

    public void endBookmarks() throws BookmarkException {
        try {
            out.endElement(ELEMENT_ROOT);
        } catch(IOException e) {
            throw new BookmarkException(e);
        }
    }

    public void addBookmark(String name, String location, String parent) throws BookmarkException {
        try {
            out.startElement(ELEMENT_BOOKMARK);
            out.println();

            writeElement(ELEMENT_NAME, name);
            writeElement(ELEMENT_LOCATION, location);
            writeElement(ELEMENT_PARENT, parent);

            out.endElement(ELEMENT_BOOKMARK);
        } catch(IOException e) {
            throw new BookmarkException(e);
        }
    }


    private void writeElement(String name, String value) throws IOException {
        if (value != null) {
            out.startElement(name);
            out.writeCData(value);
            out.endElement(name);
        }
    }
}
