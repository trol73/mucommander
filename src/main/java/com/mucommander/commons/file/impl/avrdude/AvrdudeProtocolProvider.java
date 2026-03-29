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
package com.mucommander.commons.file.impl.avrdude;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.ProtocolProvider;
import com.mucommander.commons.file.impl.avrdude.files.*;

import java.io.IOException;

/**
 * @author Oleg Trifonov
 * Created on 09/02/16.
 */
public class AvrdudeProtocolProvider implements ProtocolProvider {

    private static final String STORAGE_DIR = "avr";

    @Override
    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        if (isRootUrl(url)) {
            return new AvrRootDir(url, getUrlPath(url));
        } else if (isRootUrl(url.getParent())) {
            return new AvrDeviceDir(url);
        } else if (isRootUrl(url.getParent().getParent())) {
            if (url.getFilename().equalsIgnoreCase(AvrConfigFile.FILENAME)) {
                return new AvrConfigFile(url);
            } else {
                return new AvrMemoryDir(url);
            }
        }
        return new AvrMemoryFile(url);
    }

    private static String getUrlPath(FileURL url) {
        if (url == null) {
            return null;
        }
        String location = url.toString();
        int schemeDelimPos = location.indexOf("://");
        if (schemeDelimPos > 0) {
            return location.substring(schemeDelimPos + 3);
        }
        return "";
    }

    private static boolean isRootUrl(FileURL url) {
        if (url == null) {
            return true;
        }
        final String path = getUrlPath(url);
        return path == null || path.isEmpty() || path.equals("/") || path.equals("\\");
    }
}
