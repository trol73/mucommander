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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Oleg Trifonov
 * Created on 24/03/16.
 */
public class AvrDeviceDir extends AvrdudeFile {


    private AvrdudeFile parent;

    public AvrDeviceDir(FileURL url) throws IOException {
        super(url);
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
        List<AvrdudeFile> childs = new ArrayList<>();
        AvrdudeDevice device = getDevice();
        Set<String> blocks = device.blockSizes.keySet();

        childs.add(new AvrConfigFile(FileURL.getFileURL(getURL() + AvrConfigFile.FILENAME)));

        if (blocks.contains("flash")) {
            childs.add(new AvrMemoryDir(FileURL.getFileURL(getURL() + AvrMemoryFile.Type.FLASH.name)));
        }
        if (blocks.contains("eeprom")) {
            childs.add(new AvrMemoryDir(FileURL.getFileURL(getURL() + AvrMemoryFile.Type.EEPROM.name)));
        }
        if (blocks.contains("fuse") || blocks.contains("lfuse")  || blocks.contains("hfuse") || blocks.contains("efuse")) {
            childs.add(new AvrMemoryFile(FileURL.getFileURL(getURL() + AvrMemoryFile.Type.FUSES.name)));
        }
        if (blocks.contains("signature")) {
            childs.add(new AvrMemoryFile(FileURL.getFileURL(getURL() + getDevice().name + SIGNATURE_FILE_EXT)));
        }
        if (blocks.contains("calibration")) {
            childs.add(new AvrMemoryFile(FileURL.getFileURL(getURL() + AvrMemoryFile.Type.CALIBRATION.name)));
        }
        if (blocks.contains("lock")) {
            childs.add(new AvrMemoryFile(FileURL.getFileURL(getURL() + AvrMemoryFile.Type.LOCK.name)));
        }

        AbstractFile[] result = new AbstractFile[childs.size()];
        return childs.toArray(result);
    }

    @Override
    public void mkdir() throws IOException {
        AbstractFile file = getBaseFolder().getChild(getURL().getFilename() + CONFIG_FILE_EXT);
        if (file.exists()) {
            throw new IOException("already exist");
        }
        file.mkfile();
        AvrConfigFileUtils.save(new AvrdudeConfiguration(), file.getAbsolutePath());
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
    public void delete() throws IOException {
        getLocalConfigFile().delete();
    }

    @Override
    public void renameTo(AbstractFile destFile) throws IOException {
        if (destFile.getParent().getURL().getHost() == null) {
            AbstractFile newConfig = getLocalConfigFile().getParent().getChild(destFile.getName() + CONFIG_FILE_EXT);
            getLocalConfigFile().renameTo(newConfig);
        }
    }
}
