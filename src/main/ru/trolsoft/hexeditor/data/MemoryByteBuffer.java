/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2017 Oleg Trifonov
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

import java.io.IOException;

/**
 * @author Oleg Trifonov
 * Created on 08/02/17.
 */
public class MemoryByteBuffer extends AbstractByteBuffer {
    public MemoryByteBuffer(int capacity) {
        super(capacity);
        size = capacity;
        streamSize = capacity;
    }

    @Override
    protected void closeStream() throws IOException {

    }

    @Override
    protected long getStreamSize() throws IOException {
        return capacity;
    }

    @Override
    protected void loadBuffer() throws IOException {
    }

    @Override
    protected boolean supportRandomAccess() {
        return true;
    }

    @Override
    public long getFileSize() throws IOException {
        return capacity;
    }

    @Override
    public byte getByte(long offset) {
        return buffer[(int)offset];
    }

    public void setByte(long offset, int val) {
        buffer[(int)offset] = (byte)val;
    }
}
