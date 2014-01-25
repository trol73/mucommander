package ru.trolsoft.hexeditor.data;

import java.io.*;

/**
 * Created by trol on 15/01/14.
 */
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
