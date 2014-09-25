/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
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
 * @autor Oleg Trifonov
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
