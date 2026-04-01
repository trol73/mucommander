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

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * Buffered file reader
 */
public abstract class AbstractByteBuffer {

    /**
     * Стратегия кеширования при чтении
     */
    public enum CacheStrategy {
        FORWARD,
        BACKWARD,
        CENTER
    }

    /**
     *
     */
    static final int DEFAULT_CAPACITY = 1024*256;


    /**
     * Size of buffer
     */
    @Getter
    protected final int capacity;

    /**
     * Number of bytes in buffer
     */
    protected int bufferSize;
    @Getter
    protected long offset;
    protected byte[] buffer;

    /**
     * Size of file
     */
    protected long streamSize;


    @Getter
    @Setter
    private CacheStrategy cacheStrategy = CacheStrategy.CENTER;

    public AbstractByteBuffer(int capacity) {
        this.capacity = capacity;
        buffer = new byte[capacity];
        this.offset = 0;
        this.bufferSize = 0;
        this.streamSize = -1;
    }

    public byte getByte(long fileOffset) throws IOException {
        long index = fileOffset - offset;
        if (index < 0 || index >= bufferSize) {
            if (fileOffset < 0 || fileOffset >= getFileSize()) {
                throw new IndexOutOfBoundsException("Position: " + fileOffset + ", file size = " + getFileSize());
            }
            offset = calcOffset(fileOffset, supportRandomAccess());
            // FIXME if offset > size
            if (offset < 0) {
                offset = 0;
            }
            loadBuffer();
            index = fileOffset - offset;
        }
        try {
            return buffer[(int)index];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("\nERROR\nfile offset: " + fileOffset + ", file size: " + getFileSize());
            System.err.println("offset: " + offset + ", buffer size: " + buffer.length);
            e.printStackTrace();
            return 0;   // TODO !!!!
        }
    }


    private long calcOffset(long fileOffset, boolean randomAccessStream) {
        if (randomAccessStream) {
            return switch (cacheStrategy) {
                case FORWARD -> fileOffset;
                case BACKWARD -> fileOffset - buffer.length + 1;
                case CENTER -> fileOffset - buffer.length / 2;
            };
        } else {
            // TODO что-то странное !
            switch (cacheStrategy) {
                case FORWARD:
                    return fileOffset;
                case BACKWARD:
                    return fileOffset - buffer.length + 1;
                case CENTER:
                    return fileOffset;
            }
        }
        return fileOffset;
    }

    public long getFileSize() throws IOException {
        if (streamSize < 0) {
            streamSize = getStreamSize();
        }
        return streamSize;
    }

    public void close() throws IOException {
        bufferSize = 0;
        buffer = null;
        closeStream();
    }

    abstract protected void closeStream() throws IOException;

    abstract protected long getStreamSize() throws IOException;


    /**
     * Load file data from #offset and fills #buffer
     */
    abstract protected void loadBuffer() throws IOException;

    abstract protected boolean supportRandomAccess();

}
