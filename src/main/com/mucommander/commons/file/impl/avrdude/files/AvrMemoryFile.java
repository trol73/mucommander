/*
 * This file is part of trolCommander, http://www.trolsoft.ru/soft/trolcommander
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.impl.avrdude.files;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;

import java.io.IOException;

/**
 * @author Oleg Trifonov
 * Created on 24/03/16.
 */
public class AvrMemoryFile extends AvrdudeFile {

    public enum Type {
        FLASH("flash"),
        EEPROM("eeprom"),
        FUSES("fuses");

        Type(String name) {
            this.name = name;
        }

        static Type fromFileName(String fileName) {
            for (Type type : values()) {
                if (fileName.startsWith(type.name + ".")) {
                    return type;
                }
            }
            return null;
        }

        public final String name;
    }

    private final Type type;

    public AvrMemoryFile(FileURL url) throws IOException {
        super(url, url.getPath(), url.getFilename());
        this.type = Type.fromFileName(name);

    }

    @Override
    public FilePermissions getPermissions() {
        return FilePermissions.DEFAULT_FILE_PERMISSIONS;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        return new AbstractFile[0];
    }

    @Override
    public void mkdir() throws IOException {

    }

    @Override
    public boolean exists() {
        return true;
    }
}
