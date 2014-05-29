package com.mucommander.commons.file.impl.sevenzip;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by trol on 23/05/14.
 */
public class SevenZipRandomAccessFile implements IInStream, ISequentialInStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(SevenZipRandomAccessFile.class);

    private AbstractFile file;

    private InputStream stream;

    private long position;

    public SevenZipRandomAccessFile(AbstractFile file) throws UnsupportedFileOperationException {
        super();
        position = 0;
        this.file = file;
        try {
            stream = file.getInputStream();
        } catch (IOException e) {
            LOGGER.trace("Error", e);
        }
    }


    @Override
    public long seek(long offset, int seekOrigin) throws SevenZipException {
        try {
            if (seekOrigin == SEEK_SET) {
                stream.close();
                stream = file.getInputStream();
                stream.skip(offset);
                position = offset;
            } else if (seekOrigin == SEEK_CUR) {
                stream.skip(offset);
                position += offset;
            }
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
        return position;
    }

    @Override
    public int read(byte[] bytes) throws SevenZipException {
        int read;
        try {
            read = stream.read(bytes);
            position += read;
            return read;
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
    }
}
