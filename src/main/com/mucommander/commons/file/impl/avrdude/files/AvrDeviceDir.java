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
import com.mucommander.commons.file.impl.avrdude.*;

import java.io.IOException;

/**
 * @author Oleg Trifonov
 * Created on 24/03/16.
 */
public class AvrDeviceDir extends AvrdudeFile {


    private AvrdudeFile parent;

    public AvrDeviceDir(FileURL url, String path) throws IOException {
        super(url, path, path.replace("/", "").replace("\\", ""));
    }

    @Override
    public FilePermissions getPermissions() {
        return FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        AbstractFile[] result = new AbstractFile[4];
        result[0] = new AvrConfigFile(FileURL.getFileURL(getURL() + AvrConfigFile.FILENAME));
        result[1] = new AvrMemoryDir(FileURL.getFileURL(getURL() + AvrMemoryFile.Type.FLASH.name), path, AvrMemoryFile.Type.FLASH.name);
        result[2] = new AvrMemoryDir(FileURL.getFileURL(getURL() + AvrMemoryFile.Type.EEPROM.name), path, AvrMemoryFile.Type.EEPROM.name);
        result[3] = new AvrMemoryDir(FileURL.getFileURL(getURL() + AvrMemoryFile.Type.FUSES.name), path, AvrMemoryFile.Type.FUSES.name);
        return result;
    }

    @Override
    public void mkdir() throws IOException {
        AbstractFile file = getBaseFolder().getChild(getURL().getFilename() + CONFIG_FILE_EXT);
        if (file.exists()) {
            throw new IOException("already exist");
        }
        file.mkfile();
        AvrConfigFileUtils.save(new AvrdudeConfiguration(), file.getAbsolutePath());
//        System.out.println("mkdir " + getURL() + " / " + getURL().getParent());
    }

    @Override
    public boolean exists() {
        try {
            AbstractFile[] devices = getConfigFiles();
            final String name = getName() + CONFIG_FILE_EXT;
            for (AbstractFile configFile : devices) {
                if (configFile.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public AbstractFile getParent() {
        if (parent == null) {
            try {
                parent = new AvrRootDir(FileURL.getFileURL(getURL().getScheme() + "://"), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return parent;
    }

    @Override
    public String getDeviceName() {
        return name;
    }

    @Override
    public void delete() throws IOException {
        getLocalConfigFile().delete();
    }

}
