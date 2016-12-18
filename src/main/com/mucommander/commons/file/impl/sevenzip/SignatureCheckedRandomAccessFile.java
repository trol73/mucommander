/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2013-2016 Oleg Trifonov
 *
 * trolCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * trolCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.impl.sevenzip;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.file.UnsupportedFileOperationException;
import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.StreamUtils;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZipException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trolsoft.utils.StrUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * @author Oleg Trifonov
 */
public class SignatureCheckedRandomAccessFile implements IInStream, ISequentialInStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureCheckedRandomAccessFile.class);

    private AbstractFile file;

    private InputStream stream;

    private long position;

    private byte[] signature;

    public SignatureCheckedRandomAccessFile(AbstractFile file, byte[] signature)
            throws UnsupportedFileOperationException {
        super();
        this.signature = signature;
        this.position = 0;
        this.file = file;
        try {
            this.stream = openStreamAndCheckSignature(file);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.trace("Error", e);
            throw new UnsupportedFileOperationException(FileOperation.READ_FILE);
        }
    }

    @Override
    public synchronized long seek(long offset, int seekOrigin) throws SevenZipException {
        try {
            if (file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE)) {
                seekOnRandomAccessFile(offset, seekOrigin);
            } else {
                seekOnSequentialFile(offset, seekOrigin);
            }
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
        return position;
    }

    private void seekOnRandomAccessFile(long offset, int seekOrigin) throws IOException {
        RandomAccessInputStream randomAccessInputStream = (RandomAccessInputStream) stream;
        switch (seekOrigin) {
        case SEEK_SET:
            position = offset;
            break;
        case SEEK_CUR:
            position += offset;
            break;
        case SEEK_END:
            position = randomAccessInputStream.getLength() + offset;
            break;
        }
        randomAccessInputStream.seek(position);
    }

    /**
     * @param offset
     * @param seekOrigin
     * @throws IOException
     */
    private void seekOnSequentialFile(long offset, int seekOrigin) throws IOException {
        switch (seekOrigin) {
        case SEEK_SET:
            if (position != offset) {
                stream.close();
                stream = file.getInputStream();
                skip(offset);
                position = offset;
            }
            break;
        case SEEK_CUR:
            skip(offset);
            position += offset;
            break;
        case SEEK_END:
            long size = file.getSize();
            if (size == -1) {
                throw new IOException("can't seek from file end without knowing it's size");
            }
            long newPosition = size + (offset > 0 ? offset : 0);
            if (position != newPosition) {
                position = newPosition;
                stream.close();
                stream = file.getInputStream();
                stream.skip(position);
            }
            break;
        }
    }

    /**
     * @param skip
     * @throws IOException
     */
    private void skip(long skip) throws IOException {
        if (skip <= 0) {
            return;
        }
        long skipped = stream.skip(skip);
        if (skipped < 0) {
            throw new IOException("non reasonable number of bytes skipped");
        }
        position += skipped;
        while (skipped < skip) {
            int skipNow = (int) Long.min(skip - skipped, 1024);
            byte[] skipBuffer = new byte[skipNow];
            int read = stream.read(skipBuffer, 0, skipBuffer.length);
            if (read == -1) {
                break;
            } else {
                position += read;
                skipped += read;
            }
        }
    }

    @Override
    public synchronized int read(byte[] bytes) throws SevenZipException {
        if (bytes.length == 0) {
            return 0;
        }
        try {
            int read = stream.read(bytes);
            if (read != -1) {
                position += read;
                return read;
            }
            return 0;
        } catch (IOException e) {
            throw new SevenZipException(e);
        }
    }

    private InputStream openStreamAndCheckSignature(AbstractFile file) throws IOException {
        byte[] buf = new byte[signature.length];

        InputStream iStream = null;

        int read = 0;
        try {
            if (file.isFileOperationSupported(FileOperation.RANDOM_READ_FILE)) {
                RandomAccessInputStream raiStream = file.getRandomAccessInputStream();
                iStream = raiStream;
                if (buf.length > 0) {
                    raiStream.seek(0);
                    read = StreamUtils.readUpTo(raiStream, buf);
                    raiStream.seek(0);
                }
            } else {
                PushbackInputStream pushbackInputStream = null;
                if (buf.length > 0) {
                    pushbackInputStream = file.getPushBackInputStream(buf.length);
                    iStream = pushbackInputStream;
                    read = StreamUtils.readUpTo(pushbackInputStream, buf);
                } else {
                    iStream = file.getInputStream();
                }
                // TODO sometimes reading from pushbackInputStream returns 0
                if (read <= 0 && file.getSize() > 0) {
                    return file.getInputStream();
                }
                pushbackInputStream.unread(buf, 0, read);
            }
            if (!checkSignature(buf)) {
                throw new IOException("Wrong file signature was " + StrUtils.bytesToHexStr(buf, 0, read)
                + " but should be " + StrUtils.bytesToHexStr(signature, 0, signature.length));
            }
        } catch (IOException e) {
            IOUtils.closeQuietly(iStream);
            throw e;
        }

        return iStream;
    }

    private boolean checkSignature(byte[] data) {
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }
}
