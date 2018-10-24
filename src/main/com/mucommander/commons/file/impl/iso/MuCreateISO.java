/*
 * Copyright (c) 2010. Stephen Connolly.
 * Copyright (C) 2007. Jens Hatlak <hatlak@rbg.informatik.tu-darmstadt.de>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

/*
 * This file is copied from https://github.com/stephenc/java-iso-tools/blob/master/iso9660-writer/src/main/java/com/github/stephenc/javaisotools/iso9660/impl/CreateISO.java
 * and has been modified to be able to retrieve data from the arhiving process
 */

package com.mucommander.commons.file.impl.iso;

import com.github.stephenc.javaisotools.eltorito.impl.ElToritoConfig;
import com.github.stephenc.javaisotools.eltorito.impl.ElToritoHandler;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Config;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Element;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Handler;
import com.github.stephenc.javaisotools.iso9660.impl.LogicalSectorPaddingHandler;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.joliet.impl.JolietHandler;
import com.github.stephenc.javaisotools.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import java.io.FileNotFoundException;

public class MuCreateISO {

    private ISO9660RootDirectory root;
    private StreamHandler streamHandler;
    private MuFileHandler fileHandler;

    public MuCreateISO(StreamHandler streamHandler, ISO9660RootDirectory root) {
        this.streamHandler = new LogicalSectorPaddingHandler(streamHandler, streamHandler);
        this.root = root;
    }

    public void process(ISO9660Config iso9660Config, RockRidgeConfig rrConfig, JolietConfig jolietConfig,
                        ElToritoConfig elToritoConfig) throws HandlerException {
        if (iso9660Config == null) {
            throw new NullPointerException("Cannot create ISO without ISO9660Config.");
        }
        ((LogicalSectorPaddingHandler) streamHandler).setPadEnd(iso9660Config.getPadEnd());

        // Last handler added processes data first
        if (jolietConfig != null) {
            streamHandler = new JolietHandler(streamHandler, root, jolietConfig);
        }
        if (elToritoConfig != null) {
            streamHandler = new ElToritoHandler(streamHandler, elToritoConfig);
        }
        streamHandler = new ISO9660Handler(streamHandler, root, iso9660Config, rrConfig);
        fileHandler = new MuFileHandler(streamHandler, root);
        streamHandler = fileHandler;

        streamHandler.startDocument();

        // System Area
        streamHandler.startElement(new ISO9660Element("SA"));
        streamHandler.endElement();

        // Volume Descriptor Set
        streamHandler.startElement(new ISO9660Element("VDS"));
        streamHandler.endElement();

        // Boot Info Area
        streamHandler.startElement(new ISO9660Element("BIA"));
        streamHandler.endElement();

        // Path Table Area
        streamHandler.startElement(new ISO9660Element("PTA"));
        streamHandler.endElement();

        // Directory Records Area
        streamHandler.startElement(new ISO9660Element("DRA"));
        streamHandler.endElement();

        // Boot Data Area
        streamHandler.startElement(new ISO9660Element("BDA"));
        streamHandler.endElement();

        // File Contents Area
        streamHandler.startElement(new ISO9660Element("FCA"));
        streamHandler.endElement();

        streamHandler.endDocument();
    }
    
    /**
     * @return Name of current file being processed
     */
    public String getProcessingFile(){
        return fileHandler != null ? fileHandler.getProcessingFile() : null;
    }
    
    /**
     * Written bytes in total without the current file progress
     * @return number of bytes written as a long
     */
    public long totalWrittenBytes(){
        return fileHandler != null ? fileHandler.totalWrittenBytes(): 0;
    }

    /**
     * Written bytes to the current file being processed, will be the same size as the
     * file if complete.
     * @return number of bytes written as a long
     */
    public long writtenBytesCurrentFile() {
        return fileHandler != null ? fileHandler.writtenBytesCurrentFile(): 0;
    }
    
    /**
     * @return Size of the current file being processed in bytes
     */
    public long currentFileLength(){
        return fileHandler != null ? fileHandler.currentFileLength(): 0;
    }
    
}