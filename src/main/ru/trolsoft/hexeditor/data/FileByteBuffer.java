package ru.trolsoft.hexeditor.data;

import java.io.*;

/**
 * Created by trol on 15/01/14.
 */
public class FileByteBuffer {

    private static final int DEFAULT_CAPACITY = 1024*256;
    private final String filePath;
    private final String fileMode;
    private RandomAccessFile file;
    private int capacity;
    private int size;
    private long offset;
    private byte[] buffer;

    public FileByteBuffer(String filePath, String fileMode, int capacity) {
        this.filePath = filePath;
        this.fileMode = fileMode;
        this.capacity = capacity;
        buffer = new byte[capacity];
        this.offset = 0;
        this.size = 0;
    }


    public FileByteBuffer(String filePath, String fileMode) {
        this(filePath, fileMode, DEFAULT_CAPACITY);
    }

    protected void load(long offset) throws IOException {
        this.offset = offset;
        load();
    }

    public void load() throws IOException {
        getFile().seek(offset);
        size = getFile().read(buffer);
    }

    private RandomAccessFile getFile() throws FileNotFoundException {
        if (file == null) {
            file = new RandomAccessFile(filePath, fileMode);
        }
        return file;
    }

    /**
     *
     * @param fileOffset
     * @return
     * @throws IOException
     */
    public byte getByte(long fileOffset) throws IOException {
        long index = fileOffset - offset;
        if (index < 0 || index >= size) {
            if (fileOffset < 0 || fileOffset >= file.length()) {
                throw new IndexOutOfBoundsException("Position: " + fileOffset + ", file size = " + file.length());
            }
            offset = fileOffset - buffer.length/2;
            if (offset < 0) {
                offset = 0;
            }
            load();
            index = fileOffset - offset;
        }
        return buffer[(int)index];
    }


    public void close() throws IOException {
        file.close();
        size = 0;
        buffer = null;
    }

    public long getFileSize() throws IOException {
        return file.length();
    }

    public long getOffset() {
        return offset;
    }
}
