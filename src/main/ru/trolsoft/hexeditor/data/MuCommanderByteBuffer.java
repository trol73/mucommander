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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileOperation;
import com.mucommander.commons.io.RandomAccessInputStream;

import java.io.IOException;
import java.io.InputStream;


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
                is = file.getPushBackInputStream(1024);
            }
            lastOffset = 0;
        }
        return is;
    }
}
