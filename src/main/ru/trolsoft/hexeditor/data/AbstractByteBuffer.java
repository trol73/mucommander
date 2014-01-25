package ru.trolsoft.hexeditor.data;

import java.io.IOException;

/**
 * Created by trol on 24/01/14.
 */
public abstract class AbstractByteBuffer {

    /**
     *
     */
    protected static final int DEFAULT_CAPACITY = 1024*256;


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
//System.out.println("GET BYTE " + fileOffset + "   " + offset + "  " + size);
            if (fileOffset < 0 || fileOffset >= getFileSize()) {
                throw new IndexOutOfBoundsException("Position: " + fileOffset + ", file size = " + getFileSize());
            }
            // TODO detect if we scroll up or down and try forecast next offset
            if (supportRandomAccess()) {
                offset = fileOffset - buffer.length/2;
            } else {
                offset = fileOffset;
            }
            if (offset < 0) {
                offset = 0;
            }
            loadBuffer();
            index = fileOffset - offset;
        }
        return buffer[(int)index];
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

}
