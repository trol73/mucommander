/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2013-2014 Oleg Trifonov
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
package ru.trolsoft.hexeditor.data;

import java.io.*;


public class FileByteBuffer extends AbstractByteBuffer {

    private final String filePath;
    private final String fileMode;
    private RandomAccessFile file;

    public FileByteBuffer(String filePath, String fileMode, int capacity) {
        super(capacity);
        this.filePath = filePath;
        this.fileMode = fileMode;
    }


    public FileByteBuffer(String filePath, String fileMode) {
        this(filePath, fileMode, DEFAULT_CAPACITY);
    }

    private RandomAccessFile getFile() throws FileNotFoundException {
        if (file == null) {
            file = new RandomAccessFile(filePath, fileMode);
        }
        return file;
    }


    @Override
    protected void closeStream() throws IOException {
        if (file != null) {
            file.close();
        }
    }

    @Override
    protected long getStreamSize() throws IOException {
        return getFile().length();
    }

    @Override
    protected void loadBuffer() throws IOException {
        getFile().seek(offset);
        size = getFile().read(buffer);
    }

    @Override
    protected boolean supportRandomAccess() {
        return true;
    }


}
