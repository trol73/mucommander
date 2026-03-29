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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.commons.file.impl.avrdude.files;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FilePermissions;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.file.impl.avrdude.AvrConfigFileUtils;
import com.mucommander.commons.file.impl.avrdude.AvrdudeConfiguration;
import com.mucommander.commons.file.impl.avrdude.AvrdudeDevice;

import java.io.*;

/**
 * @author Oleg Trifonov
 * Created on 24/03/16.
 */
public class AvrConfigFile extends AvrdudeFile {

    public static final String FILENAME = "config.properties";

    private static class ConfigInputStream extends ByteArrayInputStream {

        public ConfigInputStream(String s) {
            super(s.getBytes());
        }
    }

    private class ConfigOutputStream extends ByteArrayOutputStream {
        @Override
        public void close() throws IOException {
            AvrdudeConfiguration configuration = AvrConfigFileUtils.load(new ByteArrayInputStream(buf));
            if (!configuration.isValid()) {
                throw new IOException("wrong configuration");
            }
            if (AvrdudeDevice.getDevice(configuration.deviceName) == null) {
                throw new IOException("unknown device");
            }
            AvrConfigFileUtils.save(configuration, getLocalConfigFile().getAbsolutePath());
            AvrConfigFile.this.device = null;
            AvrConfigFile.this.configuration = configuration;
            super.close();
        }
    }

    public AvrConfigFile(FileURL url) throws IOException {
        super(url);
    }

    private static String extractPathFromUrl(FileURL url) {
        return url.getParent().toString().substring(6) + FILENAME;  // 6 - length of "avr://"
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
    public InputStream getInputStream() throws IOException {
        AbstractFile configFile = getLocalConfigFile();
        return configFile.getInputStream();
    }

    @Override
    public long getSize() {
        try {
            return getLocalConfigFile().getSize();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new ConfigOutputStream();
    }


    @Override
    public void copyRemotelyTo(AbstractFile destFile) throws IOException {
        getLocalConfigFile().copyRemotelyTo(destFile);
    }
}
