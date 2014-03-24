package ru.trolsoft.hexeditor.data;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.io.RandomAccessInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by trol on 24/01/14.
 */
public class MuCommanderByteBuffer extends AbstractByteBuffer {

    private AbstractFile file;
    private InputStream is;
    private long lastOffset;


    public MuCommanderByteBuffer(AbstractFile file) {
        super(DEFAULT_CAPACITY);
        this.file = file;
    }

    @Override
    protected void closeStream() throws IOException {
        if (is != null) {
            is.close();
        }
    }

    @Override
    protected long getStreamSize() throws IOException {
        return file.getSize();
    }

    @Override
    protected void loadBuffer() throws IOException {
        getInputStream();
        if (is instanceof RandomAccessInputStream) {
            RandomAccessInputStream rndIs = ((RandomAccessInputStream) is);
            // Seek and reuse the stream
            rndIs.seek(offset);
//System.out.println("RANDOM ACCESS " + offset);
        } else {
            // TODO: it would be more efficient to use some sort of PushBackInputStream, though we can't use PushBackInputStream because we don't want to keep pushing back for the whole InputStream lifetime
            // Close the InputStream and open a new one
            // Note: we could use mark/reset if the InputStream supports it, but it is almost never implemented by
            // InputStream subclasses and a broken by design anyway.
            if (lastOffset > offset) {
//System.out.println("GENERAL ACCESS WITH RECREATE " + offset + "   " + lastOffset + "    " + (lastOffset - offset));
                is.close();
                is = file.getInputStream();
                is.skip(offset);
            } else if (lastOffset != offset) {
//System.out.println("GENERAL ACCESS WITH SKIP " + offset + "   " + (offset - lastOffset));
                is.skip(offset - lastOffset);
            }
        }
        int bufPos = 0;
        size = 0;
        while (size < capacity) {
            int read = is.read(buffer, bufPos, capacity-size);
            if (read < 0) {
                break;
            }
            bufPos += read;
            size += read;
        }
        lastOffset = offset + size;
    }

    @Override
    protected boolean supportRandomAccess() {
        return file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE);
    }


    protected InputStream getInputStream() throws IOException {
        if (is == null) {
            if (file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE)) {
                try {
                    is = file.getRandomAccessInputStream();
                } catch(IOException e) {
                    // In that case we simply get an InputStream
                }
            }
            if (is == null) {
                is = file.getInputStream();
            }
            lastOffset = 0;
        }
        return is;
    }
}
