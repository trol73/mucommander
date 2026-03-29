/*
 * This file is part of trolCommander, http://www.trolsoft.ru/en/soft/trolcommander
 * Copyright (C) 2014-2020 Oleg Trifonov
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
package com.mucommander.commons.file;

import com.mucommander.commons.io.BufferPool;
import com.mucommander.commons.io.StreamUtils;
import net.sf.sevenzipjbinding.ArchiveFormat;

import java.io.IOException;
import java.io.PushbackInputStream;

public abstract class SevenZipArchiveFormatDetector {
    private final int maxLen;

    public SevenZipArchiveFormatDetector(int maxLen) {
        this.maxLen = maxLen;
    }

    protected abstract ArchiveFormat detect(byte[] bytes);

    public ArchiveFormat detect(AbstractFile file) {
        byte[] bytes = readFirst(file);
        if (bytes == null) {
            return null;
        }
        ArchiveFormat format = detect(bytes);
        BufferPool.releaseByteArray(bytes);
        return format;
    }

    private byte[] readFirst(AbstractFile file) {
        byte[] bytes = BufferPool.getByteArray(maxLen);
        int readBytes;
        try {
            PushbackInputStream is = file.getPushBackInputStream(maxLen);
            readBytes = StreamUtils.readUpTo(is, bytes);
            is.unread(bytes, 0, readBytes);
        } catch (IOException e) {
            e.printStackTrace();
            BufferPool.releaseByteArray(bytes);
            try {
                file.closePushbackInputStream();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
        return bytes;
    }


    protected static boolean checkSignature(byte[] data, byte[] signature) {
        if (data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    protected static boolean checkSignature(byte[] data, int[] signature) {
        if (data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((data[i] & 0xff) != signature[i] && signature[i] != -1) {
                return false;
            }
        }
        return true;
    }
}
