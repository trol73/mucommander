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
package com.mucommander.commons.file.impl.avrdude.files;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.impl.avrdude.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Oleg Trifonov
 * Created on 24/03/16.
 */
public class AvrMemoryFile extends AvrdudeFile {

    public enum Type {
        FLASH("flash"),
        EEPROM("eeprom"),
        FUSES("fuses"),
        SIGNATURE("signature"),
        CALIBRATION("calibration"),
        LOCK("lock");

        Type(String name) {
            this.name = name;
        }

        static Type fromFileName(String fileName) {
            for (Type type : values()) {
                if (fileName.startsWith(type.name)) {
                    return type;
                }
            }
            return null;
        }

        public final String name;
    }

    private final Type type;

    public AvrMemoryFile(FileURL url) throws IOException {
        super(url);
        this.type = Type.fromFileName(getURL().getFilename());
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


    @Override
    public long getSize() {
        String fullName = getURL().getFilename();
        int dotPos = fullName.indexOf('.');
        String fileName = dotPos > 0 ? fullName.substring(0, dotPos) : fullName;
        if (fileName.contains("fuse")) {
            int size = 0;
            for (String blockName : getDevice().blockSizes.keySet()) {
                if (blockName.toLowerCase().contains("fuse")) {
                    size += getDevice().blockSizes.get(blockName);
                }
            }
            return size;
        } else if (fullName.endsWith(SIGNATURE_FILE_EXT)) {
            return getDevice().blockSizes.get("signature");
        } else {
            return getDevice().blockSizes.get(fileName);
        }
    }


    @Override
    public InputStream getInputStream() throws IOException {
System.out.println("?-> " + type);
        Avrdude.Operation operation;
        switch (type) {
            case FLASH:
                operation = Avrdude.Operation.READ_FLASH;
                break;
            case EEPROM:
                operation = Avrdude.Operation.READ_EEPROM;
                break;
            case SIGNATURE:
                operation = Avrdude.Operation.READ_SIGNATURE;
                break;
            case CALIBRATION:
                operation = Avrdude.Operation.READ_CALIBRATION;
                break;
            default:
                throw new RuntimeException("unsupported operation for " + type);
        }
        return new AvrDudeInputStream(StreamType.HEX, configuration, operation);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        Avrdude.Operation operation;
        switch (type) {
            case FLASH:
                operation = Avrdude.Operation.WRITE_FLASH;
                break;
            case EEPROM:
                operation = Avrdude.Operation.WRITE_EEPROM;
                break;
            case CALIBRATION:
                operation = Avrdude.Operation.WRITE_CALIBRATION;
                break;
            default:
                throw new RuntimeException("unsupported operation for " + type);
        }
        return new AvrdudeOutputStream(StreamType.HEX, configuration, operation);
    }
}
