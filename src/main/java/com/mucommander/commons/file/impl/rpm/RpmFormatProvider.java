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
package com.mucommander.commons.file.impl.rpm;

import com.mucommander.commons.file.AbstractArchiveFile;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.ArchiveFormatProvider;
import com.mucommander.commons.file.filter.ExtensionFilenameFilter;
import com.mucommander.commons.file.filter.FilenameFilter;
import com.mucommander.commons.file.impl.SevenZipJBindingROArchiveFile;
import net.sf.sevenzipjbinding.ArchiveFormat;

import java.io.IOException;

public class RpmFormatProvider implements ArchiveFormatProvider {

        private static final String[] EXTENSIONS = { ".rpm" };

        private final static ExtensionFilenameFilter FILENAME_FILTER = new ExtensionFilenameFilter(EXTENSIONS);

        private final static byte[] SIGNATURE = {(byte)0xED, (byte)0xAB, (byte)0xEE, (byte)0xDB, 0x03};

        @Override
        public AbstractArchiveFile getFile(AbstractFile file) throws IOException {
            return new SevenZipJBindingROArchiveFile(file, ArchiveFormat.RPM, SIGNATURE);
        }

        @Override
        public FilenameFilter getFilenameFilter() {
            return FILENAME_FILTER;
        }

        @Override
        public String[] getFileExtensions() {
            return EXTENSIONS;
        }


}
