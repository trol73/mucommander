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
    private long lastReadedByteIndex;

    public FileByteBuffer(String filePath, String fileMode, int capacity) {
        this.filePath = filePath;
        this.fileMode = fileMode;
        this.capacity = capacity;
        buffer = new byte[capacity];
        this.offset = 0;
        this.size = 0;
        this.lastReadedByteIndex = 0;
    }


    public FileByteBuffer(String filePath, String fileMode) {
        this(filePath, fileMode, DEFAULT_CAPACITY);
    }

    public void load(long offset) throws IOException {
        this.offset = offset;
        load();
    }

    public void load() throws IOException {
        getFile().seek(offset);
        size = getFile().read(buffer);
        lastReadedByteIndex = offset + size - 1;
buffer[0] = 0;buffer[1] = 1;buffer[2] = 2;buffer[3] = 3;buffer[4] = 4;buffer[5] = 5;buffer[6] = 'M';
    }

    private RandomAccessFile getFile() throws FileNotFoundException {
        if (file == null) {
            file = new RandomAccessFile(filePath, fileMode);
        }
        return file;
    }

    public byte getFileByte(long fileOffset) {
        if (fileOffset >= offset && fileOffset <= lastReadedByteIndex) {
            return buffer[(int)(fileOffset - offset)];
        }
        throw new IndexOutOfBoundsException("Invalid position for buffer " + fileOffset + ". Buffer = [" + offset + ", " + size + "]");
    }


    public byte getBufferByte(int index) {
        return buffer[index];
    }


    public void close() throws IOException {
        file.close();
        size = 0;
        buffer = null;
    }

    public long getFileSize() throws IOException {
        return file.length();
    }

    public byte[] getData() {
        return buffer;
    }

    public long getOffset() {
        return offset;
    }
}
