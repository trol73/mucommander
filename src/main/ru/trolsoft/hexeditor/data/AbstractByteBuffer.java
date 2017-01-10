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
    protected int capacity;

    /**
     * Number of bytes in buffer
     */
    protected int size;
    protected long offset;
    protected byte[] buffer;

    /**
     * Size of file
     */
    protected long streamSize;


    private CacheStrategy cacheStrategy = CacheStrategy.CENTER;

    public AbstractByteBuffer(int capacity) {
        this.capacity = capacity;
        buffer = new byte[capacity];
        this.offset = 0;
        this.size = 0;
        this.streamSize = -1;
    }


    public long getOffset() {
        return offset;
    }


    /**
     *
     * @param fileOffset
     * @return
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    public byte getByte(long fileOffset) throws IOException {
        long index = fileOffset - offset;
        if (index < 0 || index >= size) {
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
        return buffer[(int)index];
    }


    private long calcOffset(long fileOffset, boolean randomAccessStream) {
        if (randomAccessStream) {
            switch (cacheStrategy) {
                case FORWARD:
                    return fileOffset;
                case BACKWARD:
                    return fileOffset - buffer.length;
                case CENTER:
                    return fileOffset - buffer.length / 2;
            }
        } else {
            switch (cacheStrategy) {
                case FORWARD:
                    return fileOffset;
                case BACKWARD:
                    return fileOffset - buffer.length;
                case CENTER:
                    return fileOffset;
            }
        }
        return fileOffset;
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public long getFileSize() throws IOException {
        if (streamSize < 0) {
            streamSize = getStreamSize();
        }
        return streamSize;
    }


    /**
     *
     * @throws IOException
     */
    public void close() throws IOException {
        size = 0;
        buffer = null;
        closeStream();
    }


    /**
     *
     * @return
     */
    public int getCapacity() {
        return capacity;
    }


    abstract protected void closeStream() throws IOException;

    abstract protected long getStreamSize() throws IOException;


    /**
     * Load file data from #offset and fills #buffer
     *
     * @throws IOException
     *
     * @see #offset
     * @see #buffer
     * @see #size
     */
    abstract protected void loadBuffer() throws IOException;

    /**
     *
     * @return
     */
    abstract protected boolean supportRandomAccess();

    public CacheStrategy getCacheStrategy() {
        return cacheStrategy;
    }

    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }


}
