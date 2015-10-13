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
 * This file is copied from https://github.com/stephenc/java-iso-tools/blob/master/iso9660-writer/src/main/java/com/github/stephenc/javaisotools/iso9660/impl/FileHandler.java
 * and has been modified to be able to retrieve data from the arhiving process
 */

package com.mucommander.commons.file.impl.iso;

import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import com.github.stephenc.javaisotools.iso9660.ISO9660File;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.impl.FileElement;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Element;
import com.github.stephenc.javaisotools.sabre.DataReference;
import com.github.stephenc.javaisotools.sabre.Element;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.github.stephenc.javaisotools.sabre.impl.ChainingStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class MuFileHandler extends ChainingStreamHandler {

    private ISO9660RootDirectory root;
    private String processingFile = null;
    private DataReferenceProgress drp = null;
    private long totalWritenBytes = 0;

    public MuFileHandler(StreamHandler streamHandler, ISO9660RootDirectory root) {
        super(streamHandler, streamHandler);
        this.root = root;
    }

    public void startElement(Element element) throws HandlerException {
        if (element instanceof ISO9660Element) {
            String id = (String) element.getId();
            process(id);
        }
        super.startElement(element);
    }

    private void process(String id) throws HandlerException {
        if (id.equals("FCA")) {
            doFCA();
        }
    }

    private void doFCA() throws HandlerException {
        doFCADirs(root);

        Iterator<ISO9660Directory> it = root.sortedIterator();
        while (it.hasNext()) {
            ISO9660Directory dir = it.next();
            doFCADirs(dir);
        }
    }

    private void doFCADirs(ISO9660Directory dir) throws HandlerException {
        for (ISO9660File file : dir.getFiles()) {
            doFile(file);
        }
    }

    private void doFile(ISO9660File file) throws HandlerException {
        processingFile = file.getName();
        DataReferenceProgress dataReferenceProgress = new DataReferenceProgress(file.getDataReference());
        drp = dataReferenceProgress;
        
        super.startElement(new FileElement(file));

        data(dataReferenceProgress);
        super.endElement();
        totalWritenBytes += currentFileLength();
    }

    /**
     * @return Name of current file being processed
     */
    public String getProcessingFile() {
        return processingFile;
    }
    
    /**
     * Written bytes in total without the current file progress
     * @return number of bytes written as a long
     */
    public long totalWrittenBytes(){
        return drp != null ? totalWritenBytes : 0;
    }
    
    /**
     * @return Size of the current file being processed in bytes
     */
    public long currentFileLength(){
        return drp != null ? drp.getLength() : 0;
    }

    /**
     * Written bytes to the current file being processed, will be the same size as the
     * file if complete.
     * @return number of bytes written as a long
     */
    public long writtenBytesCurrentFile() {
        return drp != null ? drp.getWrittenBytes(): 0;
    }
    
    /*
     * DataReference wrapper that will allow the progress to be retrieved
     */
    private class DataReferenceProgress implements DataReference{
        private DataReference dr = null;
        private InputStream is = null;
        
        DataReferenceProgress(DataReference dr){
            this.dr = dr;
        }

        @Override
        public long getLength() {
            return dr.getLength();
        }
        
        public int available(){
            try {
                return is != null ? is.available() : 0;
            } catch (IOException ex) {}
            return 0;
        }
        
        public long getWrittenBytes(){
            return is != null ? getLength() - available(): 0;
        }

        @Override
        public InputStream createInputStream() throws IOException {
            is = dr.createInputStream();
            return is; 
        }

    }
}